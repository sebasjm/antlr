grammar lextest;
options {
  language=C;

}

creates	: create+;

create
	: x=CREATEINDEX 
	{
	    printf("found create.index\n");
	    }
	filename indexname by_specs assoc_specs 
	{
	    printf("Done! :-)\n");
	}
	;

filename
	: n=NAME
	{
	    printf("Found filename is \%s\n", $n.text);
	}
	;
	
indexname
       //scope { int jimi;}
	: NAME
	{
	    printf("Found index name\n");
	}
	;
	
by_specs
	: (BY DREF
	    {printf("Found another BY clause\n");}
	)+
	;
	
assoc_specs
	: ASSOCIATE DREF+
	{
	    printf("Associated clause parsed!\n");
	    }
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