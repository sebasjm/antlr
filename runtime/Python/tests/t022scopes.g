grammar t022scopes;

options {
    language=Python;
}

scope Symbols {
names
}

prog
scope Symbols;
    :   {$Symbols::names = []} ID*
    ;

ID  :   ('a'..'z')+
    ;

WS  :   (' '|'\n'|'\r')+ {$channel=HIDDEN}
    ;
