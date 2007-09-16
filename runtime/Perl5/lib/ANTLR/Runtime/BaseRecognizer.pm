package ANTLR::Runtime::BaseRecognizer;

use Readonly;
use Carp;
use ANTLR::Runtime::Class;
use ANTLR::Runtime::Token;

use strict;
use warnings;

Readonly my $MEMO_RULE_FAILED => -2;
sub MEMO_RULE_FAILED { return $MEMO_RULE_FAILED; }
sub MEMO_RULE_FAILED_I { return $MEMO_RULE_FAILED; }

Readonly my $MEMO_RULE_UNKNOWN => -1;
sub MEMO_RULE_UNKNOWN { return $MEMO_RULE_UNKNOWN; }

Readonly my $INITIAL_FOLLOW_STACK_SIZE => 100;
sub INITIAL_FOLLOW_STACK_SIZE { return $INITIAL_FOLLOW_STACK_SIZE; }

# copies from Token object for convenience in actions
Readonly my $DEFAULT_TOKEN_CHANNEL => ANTLR::Runtime::Token->DEFAULT_CHANNEL;
sub DEFAULT_TOKEN_CHANNEL { return $DEFAULT_TOKEN_CHANNEL; }

Readonly my $HIDDEN => ANTLR::Runtime::Token->HIDDEN_CHANNEL;
sub HIDDEN { return $HIDDEN; }

Readonly my $NEXT_TOKEN_RULE_NAME => 'next_token';
sub NEXT_TOKEN_RULE_NAME { return $NEXT_TOKEN_RULE_NAME; }

ANTLR::Runtime::Class::create_attributes(__PACKAGE__, [
  qw( following _fsp error_recovery last_error_index failed backtracking rule_memo )
]);

sub new {
    my ($class) = @_;

    my $self = bless {}, $class;

    $self->following([]);
    $self->_fsp(-1);
    $self->error_recovery(0);
    $self->last_error_index(-1);
    $self->failed(0);
    $self->backtracking(0);
    $self->rule_memo([]);

    return $self;
}

sub reset {
    Readonly my $usage => 'void reset()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    $self->_fsp(-1);
    $self->error_recovery(0);
    $self->last_error_index(-1);
    $self->failed(0);

    # wack everything related to backtracking and memoization
    $self->backtracking(0);
    # wipe cache
    foreach my $rule (@{$self->rule_memo}) {
        $rule = undef;
    }
}

sub match {
    Readonly my $usage => 'void match(IntStream input, int ttype, BitSet follow)';
    croak $usage if @_ != 4;
    my ($self, $input, $ttype, $follow) = @_;

    if ($input->LA(1) eq $ttype) {
        $input->consume();
        $self->error_recovery(0);
        $self->failed(0);
        return;
    }

    if ($self->backtracking > 0) {
        $self->failed(1);
        return;
    }

    $self->mismatch($input, $ttype, $follow);
}

sub match_any {
    Readonly my $usage => 'void match_any(IntStream input)';
    croak $usage if @_ != 2;
    my ($self, $input) = @_;

    $self->error_recovery(0);
    $self->failed(0);
    $input->consume();
}

sub mismatch {
    Readonly my $usage => 'void mismatch(IntStream input, int ttype, BitSet follow)';
    croak $usage if @_ != 4;
    my ($self, $input, $ttype, $follow) = @_;

    my $mte = ANTLR::Runtime::MismatchedTokenException->new($ttype, $input);

    $self->recover_from_mismatched_token($input, $mte, $ttype, $follow);
}

sub report_error {
    Readonly my $usage => 'void report_error(RecognitionException e)';
    croak $usage if @_ != 2;
    my ($self, $e) = @_;

    if ($self->error_recovery) {
        return;
    }
    $self->error_recovery(1);

    $self->display_recognition_error($self->get_token_names(), $e);
}

sub display_recognition_error {
    Readonly my $usage => 'void display_recognition_error(String[] token_names, RecognitionException e)';
    croak $usage if @_ != 3;
    my ($self, $token_names, $e) = @_;

    my $hdr = $self->get_error_header($e);
    my $msg = $self->get_error_message($e, $token_names);
    $self->emit_error_message("$hdr $msg");
}

sub get_error_message {
    Readonly my $usage => 'String get_error_message(RecognitionException e, String[] token_names)';
    croak $usage if @_ != 3;
    my ($self, $e, $token_names) = @_;

    if ($e->isa('ANTLR::Runtime::MismatchedTokenException')) {
        my $token_name;
        if ($e->expecting == ANTLR::Runtime::Token->EOF) {
            $token_name = 'EOF';
        } else {
            $token_name = $token_names->[$e->expecting];
        }

        return 'mismatched input ' . $self->get_token_error_display($e->token)
            . ' expecting ' . $token_name;
    } elsif ($e->isa('ANTLR::Runtime::MismatchedTreeNodeException')) {
        my $token_name;
        if ($e->expecting == ANTLR::Runtime::Token->EOF) {
            $token_name = 'EOF';
        } else {
            $token_name = $token_names->[$e->expecting];
        }

        return 'mismatched tree node: ' . $e->node
            . ' expecting ' . $token_name;
    } elsif ($e->isa('ANTLR::Runtime::NoViableAltException')) {
        return 'no viable alternative at input ' . $self->get_token_error_display($e->token);
    } elsif ($e->isa('ANTLR::Runtime::EarlyExitException')) {
        return 'required (...)+ loop did not match anything at input '
            . get_token_error_display($e->token);
    } elsif ($e->isa('ANTLR::Runtime::MismatchedSetException')) {
        return 'mismatched input ' . $self->get_token_error_display($e->token)
            . ' expecting set ' . $e->expecting;
    } elsif ($e->isa('ANTLR::Runtime::MismatchedNotSetException')) {
        return 'mismatched input ' . $self->get_token_error_display($e->token)
            . ' expecting set ' . $e->expecting;
    } elsif ($e->isa('ANTLR::Runtime::FailedPredicateException')) {
        return 'rule ' . $e->rule_name . ' failed predicate: {'
            . $e->predicate_text . '}?';
    } else {
        return undef;
    }
}

sub get_error_header {
    Readonly my $usage => 'String get_error_header(RecognitionException e)';
    croak $usage if @_ != 2;
    my ($self, $e) = @_;

    my $line = $e->line;
    my $col = $e->char_position_in_line;

    return "line $line:$col";
}

sub get_token_error_display {
    Readonly my $usage => 'String get_token_error_display(Token t)';
    croak $usage if @_ != 2;
    my ($self, $t) = @_;

    my $s = $t->get_text();
    if (!defined $s) {
        if ($t->get_type() == ANTLR::Runtime::Token->EOF) {
            $s = '<EOF>';
        } else {
            my $ttype = $t->get_type();
            $s = "<$ttype>";
        }
    }

    $s =~ s/\n/\\\\n/g;
    $s =~ s/\r/\\\\r/g;
    $s =~ s/\t/\\\\t/g;

    return "'$s'";
}

sub emit_error_message {
    Readonly my $usage => 'void emit_error_message(String msg)';
    croak $usage if @_ != 2;
    my ($self, $msg) = @_;

    print STDERR $msg, "\n";
}

sub recover {
    Readonly my $usage => 'void recover(IntStream input, RecognitionException re)';
    croak $usage if @_ != 3;
    my ($self, $input, $re) = @_;

    if ($self->last_error_index == $input->index()) {
	# uh oh, another error at same token index; must be a case
	# where LT(1) is in the recovery token set so nothing is
	# consumed; consume a single token so at least to prevent
	# an infinite loop; this is a failsafe.
        $input->consume();
    }

    my $last_error_index = $input->index();
    my $follow_set = $self->compute_error_recovery_set();
    $self->begin_resync();
    $self->consume_until($input, $follow_set);
    $self->end_resync();
}

sub begin_resync {
}

sub end_resync {
}

sub compute_error_recovery_set {
    Readonly my $usage => 'void compute_error_recovery_set()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    $self->combine_follows(0);
}

sub compute_context_sensitive_rule_FOLLOW {
    Readonly my $usage => 'void compute_context_sensitive_rule_FOLLOW()';
    croak $usage if @_ != 2;
    my ($self) = @_;

    $self->combine_follows(1);
}

sub combine_follows {
    Readonly my $usage => 'BitSet combine_follows(boolean exact)';
    croak $usage if @_ != 2;
    my ($self, $exact) = @_;

    my $top = $self->_fsp;
    my $follow_set = ANTLR::Runtime::BitSet->new();

    foreach my $local_follow_set (reverse @{$self->following}) {
        $follow_set |= $local_follow_set;
        if ($exact && $local_follow_set->member(ANTLR::Runtime::Token->EOR_TOKEN_TYPE)) {
            last;
        }
    }
    $follow_set->remove(ANTLR::Runtime::Token->EOR_TOKEN_TYPE);
    return $follow_set;
}

sub recover_from_mismatched_token {
    Readonly my $usage => 'void recover_from_mismatched_token(IntStream input, RecognitionException e, int ttype, BitSet follow)';
    croak $usage if @_ != 5;
    my ($self, $input, $e, $ttype, $follow) = @_;

    if ($input->LA(2) eq $ttype) {
        $self->report_error($e);

        $self->begin_resync();
        $input->consume();
        $self->end_resync();
        $input->consume();
        return;
    }

    if (!$self->recover_from_mismatched_element($input, $e, $follow)) {
        croak $e;
    }
}

sub recover_from_mismatched_set {
    Readonly my $usage => 'void recover_from_mismatched_set(IntStream input, RecognitionException e, BitSet follow)';
    croak $usage if @_ != 4;
    my ($self, $input, $e, $follow) = @_;

    if (!$self->recover_from_mismatched_element($input, $e, $follow)) {
        croak $e;
    }
}

sub recover_from_mismatched_element {
    Readonly my $usage => 'boolean recover_from_mismatched_element(IntStream input, RecognitionException e, BitSet follow)';
    croak $usage if @_ != 4;
    my ($self, $input, $e, $follow) = @_;

    return 0 if (!defined $follow);

    if ($follow->member(ANTLR::Runtime::Token->EOR_TOKEN_TYPE)) {
        my $viable_tokens_following_this_rule = $self->compute_context_sensitive_rule_FOLLOW();
        $follow |= $viable_tokens_following_this_rule;
        $follow->remove(ANTLR::Runtime::Token->EOR_TOKEN_TYPE);
    }

    if ($follow->member($input->LA(1))) {
        $self->report_error($e);
        return 1;
    }

    return 0;
}

sub consume_until {
    Readonly my $usage => 'void consume_until(IntStream input, (int token_type | BitSet set))';
    croak $usage if @_ != 3;

    if ($_[2]->isa('ANTLR::Runtime::BitSet')) {
        my ($self, $input, $set) = @_;

        my $ttype = $input->LA(1);
        while ($ttype != ANTLR::Runtime::Token->EOF && !$set->member($ttype)) {
            $input->consume();
            $ttype = $input->LA(1);
        }
    } else {
        my ($self, $input, $token_type) = @_;

        my $ttype = $input->LA(1);
        while ($ttype != ANTLR::Runtime::Token->EOF && $ttype != $token_type) {
            $input->consume();
            $ttype = $input->LA(1);
        }
    }
}

sub push_follow {
    Readonly my $usage => 'void push_follow(BitSet fset)';
    croak $usage if @_ != 2;
    my ($self, $fset) = @_;

    push @{$self->following}, $fset;
}

sub get_rule_invocation_stack {
    Readonly my $usage => 'List get_rule_invocation_stack()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    my $rules = [];
    for (my $i = 0; ; ++$i) {
        my @frame = caller $i;
        last if !@frame;

        my ($package, $filename, $line, $subroutine) = @frame;

        if ($package =~ /^ANTLR::Runtime::/) {
            next;
        }

        if ($subroutine eq $NEXT_TOKEN_RULE_NAME) {
            next;
        }

        if ($package ne ref $self) {
            next;
        }

        push @{$rules}, $subroutine;
    }
}

sub get_backtracking_level {
    Readonly my $usage => 'int get_backtracking_level()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    return $self->backtracking;
}

sub get_token_names {
    return undef;
}

sub get_grammar_file_name {
    return undef;
}

sub to_strings {
    Readonly my $usage => 'List to_strings(List tokens)';
    croak $usage if @_ != 2;
    my ($self, $tokens) = @_;

    if (!defined $tokens) {
        return undef;
    }

    return map { $_->get_text() } @{$tokens};
}

sub get_rule_memoization {
    Readonly my $usage => 'int get_rule_memoization(int rule_index, int rule_start_index)';
    croak $usage if @_ != 3;
    my ($self, $rule_index, $rule_start_index) = @_;

    if (!defined $self->rule_memo->[$rule_index]) {
        $self->rule_memo->[$rule_index] = {};
    }

    my $stop_index = $self->rule_memo->[$rule_index]->{$rule_start_index};
    if (!defined $stop_index) {
        return $self->MEMO_RULE_UNKNOWN;
    }
    return $stop_index;
}

sub alredy_parsed_rule {
    Readonly my $usage => 'boolean alredy_parsed_rule(IntStream input, int rule_index)';
    croak $usage if @_ != 3;
    my ($self, $input, $rule_index) = @_;

    my $stop_index = get_rule_memoization($rule_index, $input->index());
    if ($stop_index == $self->MEMO_RULE_UNKNOWN) {
        return 0;
    }

    if ($stop_index == $self->MEMO_RULE_FAILED) {
        $self->failed(1);
    } else {
        $input->seek($stop_index + 1);
    }
    return 1;
}

sub memoize {
    Readonly my $usage => 'void memoize(IntStream input, int rule_index, int rule_start_index)';
    croak $usage if @_ != 4;
    my ($self, $input, $rule_index, $rule_start_index) = @_;

    my $stop_token_index = $self->failed ? $self->MEMO_RULE_FAILED : $input->index() - 1;
    if (defined $self->rule_memo->[$rule_index]) {
        $self->rule_memo->[$rule_index]->{$rule_start_index} = $stop_token_index;
    }
}

sub get_rule_memoization_cache_size {
    Readonly my $usage => 'int get_rule_memoization_cache_size()';
    croak $usage if @_ != 1;
    my ($self) = @_;

    my $n = 0;
    foreach my $m (@{$self->rule_memo}) {
        $n += keys %{$m} if defined $m;
    }

    return $n;
}

sub trace_in {
    Readonly my $usage => 'void trace_in(String rule_name, int rule_index, input_symbol)';
    croak $usage if @_ != 4;
    my ($self, $rule_name, $rule_index, $input_symbol) = @_;

    print "enter $rule_name $input_symbol";
    if ($self->failed) {
        print ' failed=', $self->failed;
    }
    if ($self->backtracking > 0) {
        print ' backtracking=', $self->backtracking;
    }
    print "\n";
}

sub trace_out {
    Readonly my $usage => 'void trace_out(String rule_name, int rule_index, input_symbol)';
    croak $usage if @_ != 4;
    my ($self, $rule_name, $rule_index, $input_symbol) = @_;

    print "exit $rule_name $input_symbol";
    if ($self->failed) {
        print ' failed=', $self->failed;
    }
    if ($self->backtracking > 0) {
        print ' backtracking=', $self->backtracking;
    }
    print "\n";
}

1;

__END__

=head1 NAME

ANTLR::Runtime::BaseRecognizer

=head1 DESCRIPTION

A generic recognizer that can handle recognizers generated from
lexer, parser, and tree grammars.  This is all the parsing
support code essentially; most of it is error recovery stuff and
backtracking.
