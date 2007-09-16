use Module::Build;

my $build = Module::Build->new(
   module_name => 'ANTLR::Runtime',
   license     => 'perl',
   requires    => {
                    'Carp'     => '1.04',
                    'Readonly' => '1.03',
                  },
);
$build->create_build_script;
