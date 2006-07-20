lexer grammar SimpleCLexer;
options {
  language=ObjC;

}

T7 : ';' ;
T8 : '(' ;
T9 : ',' ;
T10 : ')' ;
T11 : 'int' ;
T12 : 'char' ;
T13 : 'void' ;
T14 : '{' ;
T15 : '}' ;
T16 : 'for' ;
T17 : '=' ;
T18 : '==' ;
T19 : '<' ;
T20 : '+' ;

// $ANTLR src "simplec.g" 94
ID  :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

// $ANTLR src "simplec.g" 97
INT :	('0'..'9')+
    ;

// $ANTLR src "simplec.g" 100
WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n'
        )+
        { channel=99; }
    ;    
