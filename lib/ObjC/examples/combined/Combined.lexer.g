lexer grammar CombinedLexer;
options {
  language=ObjC;

}

// $ANTLR src "Combined.g" 15
ID  :   ('a'..'z'|'A'..'Z'|'_') ('a'..'z'|'A'..'Z'|'0'..'9'|'_')*
    ;

// $ANTLR src "Combined.g" 18
INT :   ('0'..'9')+
    ;

// $ANTLR src "Combined.g" 21
WS  :   (   ' '
        |   '\t'
        |   '\r'
        |   '\n'
        )+
        { channel=99; }
    ;    
