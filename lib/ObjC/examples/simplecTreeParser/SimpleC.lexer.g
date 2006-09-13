lexer grammar SimpleCLexer;
options {
  language=ObjC;

}

T21 : ';' ;
T22 : '(' ;
T23 : ',' ;
T24 : ')' ;
T25 : '{' ;
T26 : '}' ;

// $ANTLR src "simplec.g" 91
FOR : 'for' ;
// $ANTLR src "simplec.g" 92
INT_TYPE : 'int' ;
// $ANTLR src "simplec.g" 93
CHAR: 'char';
// $ANTLR src "simplec.g" 94
VOID: 'void';

// $ANTLR src "simplec.g" 96
ID  :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

// $ANTLR src "simplec.g" 99
INT :	('0'..'9')+
    ;

// $ANTLR src "simplec.g" 102
EQ   : '=' ;
// $ANTLR src "simplec.g" 103
EQEQ : '==' ;
// $ANTLR src "simplec.g" 104
LT   : '<' ;
// $ANTLR src "simplec.g" 105
PLUS : '+' ;

// $ANTLR src "simplec.g" 107
WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n'
        )+
        { channel=99; }
    ;    
