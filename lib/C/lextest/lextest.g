grammar lextest;
options {
  language=C;

}

create
	: CREATEINDEX filename indexname by_specs assoc_specs 
	;

filename
	: NAME
	;
	
indexname
       scope { int jimi;}
	: NAME
	;
	
by_specs
	: (BY DREF)+
	;
	
assoc_specs
	: ASSOCIATE DREF+
	;

// LEXER STARTS HERE --------------------------------------
//

// Single character tokens
//
fragment
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
DREF	:	DOLLAR NUMBER ;

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

WS: ('\n' | '\r' |' ' | '\t' )+ { channel = 99; }
;