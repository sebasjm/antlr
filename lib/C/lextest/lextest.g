lexer grammar lextest;
options {
  language=C;

}

DOLLAR	: '$' ;

// Keywords
//
ASSOCIATE
	:	'ASSOCIATE' ;

BY	:	'BY' ;
	
CREATEINDEX 
	:	'CREATE.INDEX' ;


// Composites
//
DREF	:	DOLLAR NAME ;

NAME 	:	UPPERLOWERL ULN* ;

// Sub rules - viewable by the lexer only
//
fragment
ULN	: UPPERLOWERL | NUMBER ;

fragment
UPPERLOWERL 
	: UPPERL | LOWERL ;

fragment	
UPPERL	: 'A' .. 'Z' ;

fragment
LOWERL	: 'a' .. 'z' ; 

fragment
NUMBER	: '0' .. '9' ;