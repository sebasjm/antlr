use ExtUtils::MakeMaker;

sub MY::postamble {
    my $postamble = <<'MAKE_FRAG';
.PHONY: tags

tags:
	ctags -f tags --recurse --totals \
		--exclude=blib \
		--exclude='*~' \
		--languages=Perl --langmap=Perl:+.t \
MAKE_FRAG
}

WriteMakefile(
    NAME          => 'ANTLR::Runtime',
    VERSION_FROM  => 'lib/ANTLR/Runtime.pm',
    LICENSE       => 'perl',
    ABSTRACT_FROM => 'lib/ANTLR/Runtime.pm',
    AUTHOR        => 'Ronald Blaschke <ron@rblasch.org>',
    PREREQ_PM     => { 
                      'Carp'              => '1.04',
                      'Readonly'          => '1.03',
                      'Object::InsideOut' => '3.35',
                     },
    test          => { TESTS => 't/class/ANTLR/Runtime/*.t t/*.t' },
);
