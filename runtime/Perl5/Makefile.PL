use ExtUtils::MakeMaker;

WriteMakefile(
    NAME          => 'ANTLR::Runtime',
    VERSION_FROM  => 'lib/ANTLR/Runtime.pm',
    LICENSE       => 'perl',
    ABSTRACT_FROM => 'lib/ANTLR/Runtime.pm',
    AUTHOR        => 'Ronald Blaschke <ron@rblasch.org>',
    PREREQ_PM     => { 
                      'Carp'     => '1.04',
                      'Readonly' => '1.03',
                     },
);
