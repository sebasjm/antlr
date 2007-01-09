grammar t014parser;
options {
  language = Python;
}

document:
        ( declaration
        | call
        )*
        EOF
    ;

declaration:
        'var' t=IDENTIFIER ';'
        {self.events.append(('decl', $t.text))}
    ;

call:
        t=IDENTIFIER '(' ')' ';'
        {self.events.append(('call', $t.text))}
    ;

IDENTIFIER: ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*;
WS:  (' '|'\r'|'\t'|'\n') {$channel=HIDDEN;};
