use Module::Build;

my $build = Module::Build->new(
   module_name => 'ANTLR::Runtime',
   license     => 'perl',
   requires    => {
       'Carp'              => '1.04',
       'Readonly'          => '1.03',
       'Object::InsideOut' => '3.35',
   },
   build_requires => {
       'Test::Class'       => '0.28',
   }
);
$build->create_build_script;
