
grammar cmql;

options 
{
	output      	= AST;
	language		= C;
        ASTLabelType    = pANTLR3_BASE_TREE;
        //backtrack       = true;
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
       IID;
       IIDSELECT;
       SELECT_FACTOR;
       WITH_CLAUSE;
       WITH_FACTOR;
       AND_WITH;
       OR_WITH;
       WHEN_CLAUSE;
       AND_WHEN;
       OR_WHEN;
       ASP;
       ASP_OR;
       ASP_AND;
       VSP;
       VSP_OR;
       VSP_AND;
       INDEX_ELEMENT;
       INDEX_NODE;
       KNULL;		// Used to intercept things such as WITH X = ""
       BYEXP;
       SAVE;
}


// A query is passed in in pre-lexed form from the query pre-processor.
// At this point, such elements as USING clauses have been resolved and the
// dictionary elements have all been  worked out and processed into a more
// reasonable form for a parser such as this to deal with.
//
query
    returns [ANTLR3_BOOLEAN error]
    scope   {
		ANTLR3_BOOLEAN isidlist; 
		ANTLR3_BOOLEAN byexp; 
		ANTLR3_BOOLEAN isSum; 
		ANTLR3_BOOLEAN isStat;
	    }
    @init   {
                $query::isidlist    = ANTLR3_FALSE;
		$query::isSum	    = ANTLR3_FALSE;
		$query::isStat	    = ANTLR3_FALSE;
                $query::byexp       = ANTLR3_FALSE;
            }
	: (
            index_spec?                 // Definition of all indexes on this file
            dictionary_spec?		// Elements that define the dictionary/tables for this query
            query_body			// The Query itself
          )
	;
	  
index_spec
	: INDEXES
		LBRACE
		indexes+
		RBRACE
	;

indexes
    :	INDEX
	LBRACE
                index_name
		index_type
                index_storage
		index_elements*
		index_nodes*	
	RBRACE
	;

index_name
	: NAME EQ iname=STRING SEMI
    ;

index_type
	: TYPE EQ (   INDEX
                    | BITMAP
                    | BITSLICE
                  ) SEMI
	;

index_storage
        : STORAGE EQ STRING SEMI
        ;

index_elements
	: ELEMENT
	  LBRACE
           index_entry+  
	  RBRACE
	;

index_entry
	:   dict_name
        |   dict_attrno
        |   dict_mv_indicator
        |   dict_attr8_itype
        |   dict_conv
        |   dict_just
        |   dict_colno
	;

index_nodes
	: NODE
	  LBRACE
	     node_entry+
	  RBRACE
	;
	
node_entry
	:   dict_name
        |   dict_attrno
        |   dict_mv_indicator
        |   dict_attr8_itype
        |   dict_conv
        |   dict_just
	|   node_collation
        |   dict_colno
	;

node_collation
	:   COLLATED EQ bool SEMI
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
        |   dict_colno
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

dict_colno
    : COLNO EQ STRING SEMI
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
                GLOBAL          EQ global  =    STRING      SEMI
                FILETYPE        EQ ftype   =    NUMBER      SEMI
		COMMAND 	EQ command =    STRING      SEMI
		TYPE		EQ              query_type  SEMI
                (PROCESSOR      EQ output  =    STRING      SEMI)?
                OPTIONS         EQ opts    =    STRING      SEMI
                SORTED          EQ			    bool        SEMI
                SELECTLIST      EQ			    bool        SEMI
           )
	;

bool
        :   BTRUE
        |   BFALSE
        ;


query_type
	:
		IDLIST
                { $query::isidlist  = ANTLR3_TRUE;  }
	|	ITEMSTREAM
	|	DATASTREAM
        |       INTERNAL
                { $query::isidlist  = ANTLR3_TRUE;  }
	|	SUM
		{ $query::isSum	    = ANTLR3_TRUE; }
	|	STAT
		{ $query::isStat    = ANTLR3_TRUE; }
	;


// -----------------------------------------------------------
// Main query body grammar
//
queryLogic
    options {k=1;}  // All alts coverd by the gated predicate
    scope   {
              ANTLR3_BOOLEAN firstExpression;
              ANTLR3_BOOLEAN firstIDExpression;
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
                    (   (saving_clause           )
                      | (selectExp               )
                      | (sort_exp                )
                      | (common_connectives      )
                      | (itemid_clause           )
                      | (output_spec) => (output_spec             )
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
                    (
                            (   output_spec
                              | connective
                              | selectExp 
                              | sort_exp
                              | itemid_clause
                            )*	
                    )
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
	|	OPT_LPTR                            
	|	MARGIN integerparam             
	|	NOPAGE                         
	|	NOSPLIT                         
	|	NO_INDEX                        
	|	ONLY                            
	|	REQUIRE_INDEX                   
	|	REQUIRE_SELECT                  
	|	VERT                            
        |       common_connectives
	;

// Connectives that can be used both with SELECT and LIST
//
common_connectives
        :	FROM        integerparam  
	|	SAMPLE      integerparam      
	|	SAMPLED     integerparam      
        ;

integerparam:
		NUMBER 
	;

saving_clause:
              SAVING?
                 UNIQUE?
                 dict_element
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
                idselectSubExpr
        ;

idselectSubExpr:
		idselect_primary 
                    ( iselectConjuntive idselect_secondary )* 
	;

iselectConjuntive
        : AND
        | OR
        ; 


idselect_primary
	:   (IDSTRING)
            
        | (baddict)
            
        | idselect_secondary

        ;


idselect_secondary

    scope   { ANTLR3_BOOLEAN reverseMatch;}

        :  (op=EQ|op=NE|op=OP_LT|op=GT|op=LE|op=GE) (baddict | opstr=IDSTRING)


	|   BETWEEN UQS? (lower=STRING | lower=IDSTRING) UQS? (higher=STRING | higher=IDSTRING)


	|   LIKE UQS? (opstr=STRING | opstr=IDSTRING)	

	|   UNLIKE UQS? (opstr=STRING | opstr=IDSTRING)
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
    scope
    {
	ANTLR3_BOOLEAN	isWhen;
	ANTLR3_BOOLEAN isFirst;
    }
    @init   { $selectExp::isFirst   = ANTLR3_TRUE; }
        : (
            // Symantic predicate ensures that first expression has a "WITH"
            //
            { $queryLogic::firstExpression }?
 
                    (	  WITH	{ $selectExp::isWhen = ANTLR3_FALSE; }
			| WHEN	{ $selectExp::isWhen = ANTLR3_TRUE; }
		    )

			selectTerm (selectTermSet  )*

                    {  $queryLogic::firstExpression = ANTLR3_FALSE; }
	  )
        | (
            // Syntactic predicate failed, hence this subsequent expression must begin
            // with "AND" or "OR". Note that we allow the user to miss out the WITH that
            // in theory MUST follow this as it is not ambiguous. However I have decided
            // that allowing WITH X = 8 WITH G=9 and implying either AND WITH or OR WITH
            // is stupid and we are going to make people specify which logical connection
            // they require.
            //
            selectExpSubsequent
	    )
        ;

selectExpSubsequent
        :
           ( AND (    WITH  { $selectExp::isWhen = ANTLR3_FALSE; }
		    | WHEN  { $selectExp::isWhen = ANTLR3_TRUE;  }
		 ) 
		    selectTerm (selectTermSet  )*
	    
	   )
        |  ( OR  (    WITH  { $selectExp::isWhen = ANTLR3_FALSE; }
		    | WHEN  { $selectExp::isWhen = ANTLR3_TRUE;  }
		 ) 
		    selectTerm (selectTermSet	)*   
	    
	    )
        ;

selectTermSet
	: AND selectTerm	
	| OR  selectTerm	
	;

selectTerm
    scope   { ANTLR3_BOOLEAN isFirst;  }
    @init   { $selectTerm::isFirst   = ANTLR3_TRUE; }
        : ( NO? NOT? EACH?  dict_element  ((value_selection_exp) => value_selection_exp)* )
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

	scope	{ ANTLR3_BOOLEAN reverseMatch; }

	:  UQS? s=STRING

		// The expression  X ""    is the same as  NO X 
		// The expression  X "abc" is the same as  X EQ "abc"
		//
	|   ISNULL

	|   ISNOTNULL

	|   EQ
		(	UQS? s=STRING	    
		    |	dict_element	
		)	
	
	|   NE     
		(	UQS? s=STRING	    
		    |	dict_element
		)	

	|   OP_LT     
		(	UQS? s=STRING
		    |	dict_element	    
		)

	|   GT
		(	UQS? s=STRING	    
		    |	dict_element	  
		)

	|   LE     
		(	UQS? s=STRING
		    |	dict_element	   
		)

	|   GE     
		(	UQS? s=STRING
		    |	dict_element	 
		)

	|       BETWEEN withbetween1 withbetween2
		

	|	LIKE UQS? opstr=STRING	

	|	UNLIKE UQS? opstr=STRING	

	;

withbetween1
        :
		(UQS? STRING | dict_element)
		;

withbetween2
        :
		(UQS? STRING | dict_element)
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
                {
                    $query::byexp     = ANTLR3_TRUE;
                }
	|	BY_EXP_DSND		dict_display
                {
                    $query::byexp     = ANTLR3_TRUE;
                }
	|	BY_EXP_SUB		dict_display
                {
                    $query::byexp     = ANTLR3_TRUE;
                }
	|	BY_EXP_SUB_DSND         dict_display
                {
                    $query::byexp     = ANTLR3_TRUE;
                }
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
	|	MIN                     dict_display (NO_NULLS)?
	|	PERCENT                 dict_display
	|	TRANSPORT               dict_display
	|	BREAK_ON    (STRING)?   dict_element ((STRING)=>STRING)?
	|	BREAK_SUP   (STRING)?   dict_element ((STRING)=>STRING)?
        |       dict_display
	|	CALC                    dict_element
	;

dict_display
        :       ATTRIBUTEVALUE ((limiter_exp)=> limiter_exp)? formatting*
        ;

dict_element
        : ATTRIBUTEVALUE formatting*
        ;
        
limiter_exp
        : 
		limiter ((limiter_op)=> (limiter_op limiter))*
	;

limiter_op
        : AND   
        | OR   
        |     
        ;

limiter:
            (NOT    NE     baddict)
          | (NOT    OP_LT     baddict)
          | (NOT    GT     baddict)
          | (NOT    LE     baddict)
          | (NOT    GE     baddict)
          | (NOT    opt_eq baddict)
          | (       NE     baddict)
          | (       OP_LT     baddict)
          | (       GT     baddict)
          | (       LE     baddict)
          | (       GE     baddict)
          | (       opt_eq baddict)
	;

opt_eq
    : EQ
    |
    ;

baddict
    scope   { ANTLR3_BOOLEAN bad;              }
    @init   { $baddict::bad = ANTLR3_FALSE;  }

    :   (
            UQS { $baddict::bad = ANTLR3_TRUE; } 
        )? 
            bs=STRING
    
    ;

 formatting
	:	FMT UQS? STRING             
	|	CONV UQS? STRING            
	|	DISPLAY_LIKE ATTRIBUTEVALUE 
	|	COL_HDG UQS? STRING         
	|	ASSOC UQS? STRING           
	|	ASSOC_WITH ATTRIBUTEVALUE
	|	MULTI_VALUE                 
	|	SINGLE_VALUE                
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
    ;

IDSTRING
    : '\'' ( ~('\'' | '\\') | ESCAPE_SEQUENCE )* '\''
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

NUMBER: '-'? DIGIT+ ;

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
BITMAP          :       'bitmap';
BITSLICE        :       'bitslice';
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
COL_HDG         :	'COL.HDG';
CALC            :       'CALC';
COL_HDR_SUPP	:	'COL.HDR.SUP';
COL_SPACES      :	'COL.SPACES';
COL_SUPP	:	'COL.SUP';
COLNO           :       'colno';
COLLATED        :       'collated';
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
BFALSE           :       'false';
FILENAME        :	'filename';
FILETYPE        :       'filetype';
FMT             :	'FMT';
FOOTING         :	'FOOTING';
FORMAT          :       'format';
FROM            :	'FROM';
GE              :       'GE';
GLOBAL          :       'global';
GRAND_TOTAL	:	'GRAND.TOTAL';
GT              :       'GT'| 'AFTER';
HDR_SUPP	:	'HDR.SUP';
HEADING         :       'heading' | 'HEADING';
ID_SUPP         :	'ID.SUP' 'P'?;
IDLIST		:       'IDLIST';
INQUIRING       :       'INQUIRING';
INDEX           :       'index';
INDEXES         :       'indexes';
INTERNAL        :       'INTERNAL';
ISNULL		:	'IS.NULL';
ISNOTNULL	:	'IS.NOT.NULL';
ORDER           :       'order';
ITYPE           :       'itype';
ITEMSTREAM	:	'ITEMSTREAM';
JUSTIFICATION   :       'justification';
LE              :       'LE';
LIKE            :	'LIKE' | 'MATCHES' | 'MATCHING';
OPT_LPTR            :	'LPTR';
OP_LT              :       'LT' | 'BEFORE';
MARGIN          :	'MARGIN';
MAX             :       'MAX';
MIN             :       'MIN';
MULTI_VALUE	:	'MULTI.VALUE';
MV              :       'mv';
NAME            :       'name';
NE              :       'NE';
NO              :       'NO';
NODE            :       'NODE';
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
REGULAR		: 'regular' | 'REGULAR';
REQUIRE_INDEX	:	'REQUIRE.INDEX';
REQUIRE_SELECT	:	'REQUIRE.SELECT';
SAID            :       'SAID' | 'SPOKEN';
SAMPLE          :	'SAMPLE';
SAMPLED         :	'SAMPLED';
SAVING          :       'SAVING';
SELECTLIST      :       'selectlist';
SINGLE_VALUE	:	'SINGLE.VALUE';
SORTED          :       'sorted';
STAT		:	'STAT';
STORAGE         :       'storage';
SUM		:	'SUM';
TERMINAL        :       'TERMINAL';
TO              :       'TO';
TOTAL           :       'TOTAL';
TRANSPORT       :       'TRANSPORT';
BTRUE            :       'true';
TYPE            :	'type';
UNIQUE          :       'UNIQUE';
UNLIKE          :       'UNLIKE' | 'NOT.MATCHING';
UQS             :       'UQS';
VERT            :	'VERT';
WHEN            :       'WHEN';
WIDTH           :       'width';
WITH            :       'WITH' | 'WHERE';
WITHIN          :	'WITHIN';

