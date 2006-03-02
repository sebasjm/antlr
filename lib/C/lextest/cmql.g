
grammar cmql;

options 
{
	language=C;
}

// The following tokens are used to generate parser tree nodes for the
// parsed query. This nodes are passed to the query analyser, which is a
// tree walker, which in turn will rewrite the tree and pass it to the SQL
// Query generator, which as it's name suggests, generates a COS routine ;-)
// This COS routine does contain the embedded SQL query though :-)
//
tokens
{
    QUERY;              // Main node, the query itself

        DICT_SPEC;          // Introduces the dictionary specification
            DICT_ELEMENT;       // Introduces an individual dictionary element
        QUERY_BODY;         // Body of the query itself
            QUERY_SPECS;        // Elements describing the original query
            STATEMENT;                   // The actual statement that was parsed (finally eh?)
                CONNECTIVES;
       OUTPUT_SPEC;
       SELECT_TO;
       IIDS;
       SELECT_FACTOR;
       WITH_CLAUSE;
       WITH_FACTOR;
       AND_WITH;
       OR_WITH;
       AND_WHEN;
       OR_WHEN;
       VSP;
       VSP_OR;
       VSP_AND;

}


@members {

ANTLR3_BOOLEAN parseError;

}

@lexer::members {

ANTLR3_BOOLEAN lexError;

}

// A query is passed in in pre-lexed form from the query pre-processor.
// At this point, such elements as USING clauses have been resolved and the
// dictionary elements have all been  worked out and processed into a more
// reasonable form for a parser such as this to deal with.
//
query
    returns [boolean error]
    scope   {boolean isidlist}
    @init   {
                $query::isidlist    = ANTLR3_FALSE;
                
            }
	: (
            dictionary_spec?		// Elements that define the dictionary/tables for this query
            query_body			// The Query itself
            {
                $error  = parseError;
            }
          )
            
	;

dictionary_spec
	: (
	DICTIONARY 
                LBRACE
                    dict_elements+
		RBRACE
          )
          
	;

dict_elements
	: (
	ELEMENT
	LBRACE
		dict_entry+
	RBRACE
            )
            
	;

dict_entry
	:   dict_name
        |   dict_heading
        |   dict_assoc
        |   dict_attrno
        |   dict_reference
        |   dict_mv_indicator
        |   dict_attr8_itype
        |   dict_conv
        |   dict_just
        |   dict_width
        |   dict_format
	;

dict_assoc
    : ASSOC EQ STRING SEMI
    ;

dict_name
    : (NAME EQ STRING SEMI)
    ;

dict_heading
    : (HEADING EQ STRING SEMI)
    ;

dict_attrno
    : (ATTRNO EQ STRING SEMI)
    ;

dict_reference
    : REFNO EQ ATTRIBUTEVALUE SEMI
    ;

dict_mv_indicator
    : MV EQ STRING SEMI
    ;

// Either a UniVerse style ITYPE or an Attribute 8 is allowed, but not both
//
dict_attr8_itype
    : ATTR8 EQ STRING SEMI
    | ITYPE EQ STRING SEMI
    ;

dict_conv
    : CONV EQ STRING SEMI
    ;

dict_just
    : JUSTIFICATION EQ STRING SEMI
    ;

dict_width
    : WIDTH EQ STRING SEMI
    ;

dict_format
    : FORMAT EQ STRING SEMI
    ;

query_body
	: (
	QUERY
	LBRACE
		querySpecs
		queryLogic
	RBRACE
          )
          
	;

querySpecs
	: (
		FILENAME	EQ fname   =    STRING      SEMI
                FILETYPE        EQ ftype   =    NUMBER      SEMI
		COMMAND 	EQ command =    STRING      SEMI
		TYPE		EQ              query_type  SEMI
                (PROCESSOR      EQ output  =    STRING      SEMI)?
                OPTIONS         EQ opts    =    STRING      SEMI
                SORTED          EQ   truefalse   SEMI
                SELECTLIST      EQ     truefalse   SEMI
           )
	;

truefalse
        :   BOOLTRUE
        |   BOOLFALSE
        ;


query_type
	:
		IDLIST
                { $query::isidlist = ANTLR3_TRUE;  }
	|	ITEMSTREAM
	|	DATASTREAM
	;


// -----------------------------------------------------------
// Main query body grammar
//
queryLogic
    scope   {
              boolean firstExpression;
              boolean firstIDExpression;
            }
    @init   {   
                $queryLogic::firstExpression    = ANTLR3_TRUE;
                $queryLogic::firstIDExpression  = ANTLR3_TRUE;
            }
    : {  $query::isidlist}? => (selectTypeLogic)
    | {! $query::isidlist}? => (listTypeLogic)
    ;


selectTypeLogic
	:   (
		BODY
		LBRACE
                    (itemid_clause)?
                    (   saving_clause 
                      | selectExp 
                      | sort_exp
                    )*
                    ( to_clause)?
                    (
                        // If the query had an @ or @LPTR record to deal with, the pre-lexer
                        // just spits it out here. However, these things are only used for LIST
                        // like statements, not select like statements so we just parse them and ignore them.
                        //
                        AT LBRACE
			(
                            (selectExp | sort_exp| output_spec | connective)*
                        )
                        RBRACE
                    )?
               RBRACE
             )
                
	;


listTypeLogic
	:   (
		BODY
		LBRACE
                    ( (connective)* itemid_clause )?
                    (   output_spec
                      | connective
                      | selectExp 
                      | sort_exp
                    )*	
                    (
                        // If the query had an @ or @LPTR record to deal with, the pre-lexer
                        // just spits it out here. If the first part of the statement body found
                        // any display elements ($n, BREAK.ON etc) then this bit is parsed
                        // but ignored.
                        //
                        AT LBRACE
			(
                            (selectExp | sort_exp| output_spec | connective)*
                        )
                        RBRACE
                    )?
               RBRACE
             )
                // We rewrite the tree to include the default output spec and clauses if there
                // there was no output spec in the statement actually typed in or submitted.
                // This is because LIST FILE should include the elements of @ or @LPTR but
                // LIST FILE F1 should NOT include them, even though they were passed in to us.
                // Similarly LIST FILE WITH F1 > "77" should include it as it has no output spec.
                //
                
	;
        exception catch[ANTLR3_RECOGNITION_EXCEPTION] 
        {
            fprintf(stderr, "Query statement is in error. Check dictionary definitions!\n");
        }

to_clause
        : TO integerparam 
        ;

connective
	:	COL_HDR_SUPP                    
	|	COL_SPACES integerparam         
	|	COL_SUPP                        
	|	COUNT_SUPP                      
	|	DBL_SPACE                       
	|	DET_SUPP                        
	|	FOOTING  STRING                 
	|	GRAND_TOTAL	STRING          
	|	HEADING STRING                  
	|	HDR_SUPP                        
	|	ID_SUPP                         
	|	LPTRQUAL                         
	|	MARGIN integerparam             
	|	NOPAGE                          
	|	NOSPLIT                         
	|	NO_INDEX                        
	|	ONLY                            
	|	REQUIRE_INDEX                   
	|	REQUIRE_SELECT                  
	|	SAMPLE        integerparam      
	|	SAMPLED	integerparam            
	|	VERT                            
	|	WITHIN                          
	|	FROM		integerparam    
	;

integerparam:
		NUMBER 
	;

saving_clause:
              SAVING
                 UNIQUE?
                 dict_element+
                 NO_NULLS? 

                
	;

//  Item Id selection clause
//		
itemid_clause
    :   idselectExp
    |	INQUIRING
    ;

// We only get into expressions if there is a relational operator present 
//
idselectExp:
		idselect_primary 
                    ( iselectConjuntive idselect_secondary )* 
	;


iselectConjuntive
        : AND 
        | OR  
        |     
        ; 


idselect_primary
	:   (str=STRING)+
            
            

        | idselect_secondary

            
        ;


idselect_secondary
        :  (op=EQ|op=NE|op=LTHAN|op=GT|op=LE|op=GE) opstr=STRING

            

	|   BETWEEN lower=STRING higher=STRING

            

	|   LIKE opstr=STRING

            

	|   UNLIKE opstr=STRING

            

	|   LPAREN idselectExp RPAREN

            
	;

when_exp
        : WHEN when_exp2
	;

when_exp2
        : when_term (when_or_when when_term )*
	;


when_or_when
        : OR WHEN 
        ;

when_term
        : when_exp3 (when_and_when when_exp3 )*
	;

when_and_when
        : AND WHEN 
        ;

when_exp3
        : when_term2 (OR when_term2 )*
	;

when_term2
        : when_factor (AND when_factor )*
	;

when_factor
        :   NOT?  dict_element  (value_selection_exp)?
                
	|   LPAREN when_exp2 RPAREN
	;

//
// Value selection clause
//
// First time thru accept only WITH
//

// The first expression begins "WITH ...", but subsequent expressions
// MUST begin with either AND {WITH} or OR {WITH}
//
selectExp
        :
            // Symantic predicate ensures that first expresion has a "WITH"
            //
            { $queryLogic::firstExpression }?
 
                    WITH selectTerm

                    {  $queryLogic::firstExpression = ANTLR3_FALSE; }

             
        |
            // Syntactic predicate failed, hence this subsequent expression must begin
            // with "AND" or "OR". Note that we allow the user to miss out the WITH that
            // in theory MUST follow this as it is not ambiguous. However I have decided
            // that allowing WITH X = 8 WITH G=9 and implying either AND WITH or OR WITH
            // is stupid and we are going to make people specify which logical connection
            // they require.
            //
           
            selectExpSubsequent

             
            
        ;

selectExpSubsequent
        :
            AND WITH selectTerm    
        |   OR  WITH selectTerm    
        ;
        
selectTerm
    scope   { boolean isFirst;  }
    @init   { $selectTerm::isFirst   = ANTLR3_TRUE; }
        :	 
	( NO? NOT? EACH?  dict_element  value_selection_exp* )

                

        | LPAREN selectTerm (selectExpSubsequent)* RPAREN

                
         
	;

value_selection_exp
        : (
            value_selection_primary         

                
                
                
            
            |   AND value_selection_primary     
            |   OR  value_selection_primary     
          )
          { 
            $selectTerm::isFirst = ANTLR3_FALSE;   // No longer first selection element, subsequent ones default to VSP_OR
          }
	;

value_selection_primary
	:	
                STRING
	|	(NE|LTHAN|GT|LE|GE|EQ) (STRING | dict_element)	
	|       BETWEEN withbetween1 withbetween2
	|	LIKE STRING	
	|	UNLIKE STRING	
	|	SAID STRING
	|	LPAREN value_selection_exp RPAREN
	;

withbetween1
        :
		(STRING | dict_element)
		;

withbetween2
        :
		(STRING | dict_element)
		;

//  sort expression
//
sort_exp
        :
            sortclause 
	;

sortclause
        :
		BY			dict_element 
	|	BY_DSND 		dict_element
	|	BY_EXP 			dict_display
	|	BY_EXP_DSND		dict_display
	|	BY_EXP_SUB		dict_display
	|	BY_EXP_SUB_DSND         dict_display
	;

//  Output section
//
output_spec
        : output_elements
            
        ;

output_elements
	:
                TOTAL                   dict_display
	|	AVERAGE                 dict_element (NO_NULLS)?
	|	ENUM                    dict_element (NO_NULLS)? 
	|	MAX                     dict_display
	|	MIN                     dict_display
	|	PERCENT                 dict_display
	|	TRANSPORT               dict_display
	|	BREAK_ON    (STRING)?   dict_element (STRING)?
	|	BREAK_SUP   (STRING)?   dict_element (STRING)?
        |       dict_display
	|	CALC                    dict_element
        |       when_exp
	;

dict_display
        :       ATTRIBUTEVALUE limiter_exp?
                
        ;

dict_element
        : ATTRIBUTEVALUE
            
        ;
        
limiter_exp
        :
		limiter (limiter_op limiter)*
	;

limiter_op
        : AND   
        | OR    
        |       
        ;

limiter:
            (NOT NE s=STRING) 
          | (NOT LTHAN s=STRING) 
          | (NOT GT s=STRING) 
          | (NOT LE s=STRING) 
          | (NOT GE s=STRING) 
          | (NOT EQ s=STRING) 
          | (NE s=STRING)     
          | (LTHAN s=STRING)     
          | (GT s=STRING)     
          | (LE s=STRING)     
          | (GE s=STRING)     
          | (EQ s=STRING)    
	;
 
// -----------------------------------------------------------
//
// Lexer
//



WS	:   (	' '
		|	'\t'
		|	'\f'
			// handle newlines
		|	(	//'\r\n'  // Evil DOS
				'\r'    // Macintosh
			|	'\n'    // Unix (the right way)
			)
            )+
		{ channel=99; /* Throw away whitespace */ }
	;

STRING
    : '"' ( ~('\"' | '\\') | ESCAPE_SEQUENCE )* '\"'
    | '\'' ( ~('\'' | '\\') | ESCAPE_SEQUENCE )* '\''
    ;

fragment
ESCAPE_SEQUENCE
    :	'\\' '\"'
    |   '\\' '\''
    |   '\\' '\\'
	;


QUOTE		: '_'	;
LBRACE		: '{'	;
RBRACE		: '}'	;
SEMI		: ';'	;
LPAREN		: '('	;
RPAREN		: ')'	;
COMMA           : ','   ;

ATTRIBUTEVALUE
	: '$'  DIGIT+
	;

NUMBER: DIGIT+ ;

fragment
DIGIT: '0'..'9' ;

// Keyword tokens that pass on to the tree parser
//
AT              :       '@';
AND             :       'AND';
AS              :       'AS';
ASSOC           :	'ASSOC' | 'assoc';
ASSOC_WITH	:	'ASSOC' 'WITH';
ATTR            :       'ATTR';
ATTR8           :       'attr8';
ATTRNO          :       'attrno';
AVERAGE         :       'AVERAGE';
BETWEEN         :       'BETWEEN';
BODY            :       'body';
BREAK_ON        :       'BREAK.ON';
BREAK_SUP       :       'BREAK.SUP';
BY              :       'BY';
BY_DSND         :       'BY.DSND';
BY_EXP          :       'BY.EXP';
BY_EXP_DSND     :       'BY.EXP.DSND';
BY_EXP_SUB      :       'BY.EXP.SUB';
BY_EXP_SUB_DSND :       'BY.EXP.SUB.DSND';
COL_HDR         :	'COL.HDR';
CALC            :       'CALC';
COL_HDR_SUPP	:	'COL.HDR.SUP';
COL_SPACES      :	'COL.SPACES';
COL_SUPP	:	'COL.SUP';
COMMAND         :	'command';
CONV            :	'CONV' | 'conv';
COUNT_SUPP	:	'COUNT.SUP';
DATASTREAM	:	'DATASTREAM';
DBL_SPACE	:	'DBL.SPACE';
DET_SUPP	:	'DET.SUP';
DICTIONARY      :       'dictionary';
DISPLAY_LIKE	:	'DISPLAY' 'LIKE';
EACH            :       'EACH' | 'EVERY';
ELEMENT         :       'element';
ENUM            :       'ENUM';
EQ              :       'EQ' | '=';
EVAL            :       'EVAL';
BOOLFALSE       :       'false';
FILENAME        :	'filename';
FILETYPE        :       'filetype';
FMT             :	'FMT';
FOOTING         :	'FOOTING';
FORMAT          :       'format';
FROM            :	'FROM';
GE              :       'GE';
GRAND_TOTAL	:	'GRAND.TOTAL';
GT              :       'GT';
HDR_SUPP	:	'HDR.SUP';
HEADING         :       'heading' | 'HEADING';
ID_SUPP         :	'ID.SUP' 'P'?;
IDLIST		:       'IDLIST';
INQUIRING       :       'INQUIRING';
ITYPE           :       'itype';
ITEMSTREAM	:	'ITEMSTREAM';
JUSTIFICATION   :       'justification';
LE              :       'LE';
LIKE            :	'LIKE';
LPTRQUAL        :	'LPTR';
LTHAN           :       'LT';
MARGIN          :	'MARGIN';
MAX             :       'MAX';
MIN             :       'MIN';
MULTI_VALUE	:	'MULTI.VALUE';
MV              :       'mv';
NAME            :       'name';
NE              :       'NE';
NO              :       'NO';
NO_INDEX	:	'NO.INDEX';
NO_NULLS        :       'NO.NULLS';
NOPAGE          :	'NOPAGE';
NOSPLIT         :	'NOSPLIT';
NOT             :       'NOT';
ONLY            :	'ONLY';
OPTIONS         :       'options';
OR              :       'OR';
LIST_SPEC       :       'output';
PERCENT         :       'PERCENT';
PRINTER         :       'printer';
PROCESSOR       :       'processor';
QUERY           :       'query';
REFNO           :       'refno';
REQUIRE_INDEX	:	'REQUIRE.INDEX';
REQUIRE_SELECT	:	'REQUIRE.SELECT';
SAID            :       'SAID';
SAMPLE          :	'SAMPLE';
SAMPLED         :	'SAMPLED';
SAVING          :       'SAVING';
SELECTLIST      :       'selectlist';
SINGLE_VALUE	:	'SINGLE.VALUE';
SORTED          :       'sorted';
TERMINAL        :       'TERMINAL';
TO              :       'TO';
TOTAL           :       'TOTAL';
TRANSPORT       :       'TRANSPORT';
BOOLTRUE        :       'true';
TYPE            :	'type';
UNIQUE          :       'UNIQUE';
UNLIKE          :       'UNLIKE';
VERT            :	'VERT';
WHEN            :       'WHEN';
WIDTH           :       'width';
WITH            :       'WITH' | 'WHERE';
WITHIN          :	'WITHIN';
