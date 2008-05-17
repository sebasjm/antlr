package ANTLR::Runtime::BitSetTest;

use strict;
use warnings;

use base qw( Test::Class );
use Test::More;

use ANTLR::Runtime::BitSet;

sub test_new_default :Test(1) {
    my $bs = ANTLR::Runtime::BitSet->new();
    is("$bs", "{}");
}

sub test_new_bits :Test(5) {
    my $bs = ANTLR::Runtime::BitSet->new({ bits => '001' });
    ok(!$bs->member(0));
    ok(!$bs->member(1));
    ok($bs->member(2));
    ok(!$bs->member(3));
    is("$bs", "{2}");
}

sub test_new_number :Test(2) {
    my $bs = ANTLR::Runtime::BitSet->new({ number => 0x10 });
    ok($bs->member(4));
    is("$bs", "{4}");
}

sub test_of :Test(2) {
    my $bs = ANTLR::Runtime::BitSet->of(0x10);
    ok($bs->member(16));
    is("$bs", "{16}");
}

sub test_add :Test(1) {
    my $bs = ANTLR::Runtime::BitSet->new();
    $bs->add(2);
    $bs->add(7);
    is("$bs", "{2,7}");
}

sub test_remove :Test(2) {
    my $bs = ANTLR::Runtime::BitSet->new();
    $bs->add(3);
    $bs->add(12);
    is("$bs", "{3,12}");
    $bs->remove(3);
    is("$bs", "{12}");
}

sub test_words64 :Test(1) {
    my $bs = ANTLR::Runtime::BitSet->new(
        { words64 => [ '0x0000004000000001', '0x1000000000800000' ] });
    is("$bs", "{0,38,87,124}");
}

sub test_op_or :Test(1) {
    my $bs = ANTLR::Runtime::BitSet->of(4);
    $bs |= ANTLR::Runtime::BitSet->of(5);
    is("$bs", "{4,5}");
}

1;
