
grammar cmql;

options 
{
	output      	= AST;
	language	= C;
        ASTLabelType    = CommonTree;
        //backtrack       = ANTLR3_TRUE;
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
       COMPNULL;		// Used to intercept things such as WITH X = ""
       BYEXP;
       SAVE;
}

//@header {
//   package cmql;

//   import java.io.*;
//}
//@lexer::header {
//   package cmql;

  // import java.io.*;
//}

@members {
    
    ANTLR3_BOOLEAN  parseError;

//    public void reportError(RecognitionException e)
//    {
//        parseError = ANTLR3_TRUE;
//        if ( errorRecovery ) {
//            return;
//	}
//	errorRecovery = ANTLR3_TRUE;
//        cmqlError.reportError(  this.getClass().getName(),
//				this.getTokenNames(),
//				e);
}

    // Converts a pattern quotes from an MV style pattern matching specification to
    // a COS style ? operator pattern for \%PATTERN()
    //
//    public  String convertPattern(String pattern)
//    {
//	String inPattern;
// 
//	if  (	   pattern.length() > 1
//		&& pattern.charAt(1) == '~')
//	{
//	    inPattern = pattern.substring(2, pattern.length()-1);
//	} else {
//	    inPattern = pattern.substring(1, pattern.length()-1);
//	}
//
//	CharStream              input   = new ANTLRStringStream(inPattern);
//	patternConvertLexer     lex     = new patternConvertLexer(input);
//        CommonTokenStream       tokens  = new CommonTokenStream(lex);
//        patternConvert                    parser  = new patternConvert(tokens);
//
//        // Call parser
//        //
//	try
//	{
//	    return parser.toCosPattern();
//	} catch (Exception e)
//	{
//	    return "''";
//	}
//
//    }
//}

//@lexer::members {
//
//    private ANTLR3_BOOLEAN lexError = ANTLR3_FALSE;
//
//    public void reportError(RecognitionException e)
//    {
//        lexError = ANTLR3_TRUE;
//
//        if ( errorRecovery ) {
//            return;
//	}
//        errorRecovery = ANTLR3_TRUE;
//        cmqlError.reportError(  this.getClass().getName(),
//				this.getTokenNames(),
//				e);
//    }
//}

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
            {
                $error  = parseError;
            }
          )
            -> {$query::byexp}? ^(BYEXP index_spec? dictionary_spec? query_body)
            ->                  ^(QUERY index_spec? dictionary_spec? query_body)
	;
	  
index_spec
	: INDEXES
		LBRACE
		indexes+
		RBRACE
           -> ^(INDEXES indexes+)
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
        -> ^(INDEX index_name index_type index_storage index_elements* index_nodes*)
	;

index_name
	: NAME EQ! iname=STRING SEMI!
    ;

index_type
	: TYPE EQ! (   INDEX
                    | BITMAP
                    | BITSLICE
                  ) SEMI!
	;

index_storage
        : STORAGE EQ! STRING SEMI!
        ;

index_elements
	: ELEMENT
	  LBRACE
           index_entry+  
	  RBRACE
          -> ^(INDEX_ELEMENT index_entry+)
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
          -> ^(INDEX_NODE node_entry+)
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
	:   COLLATED EQ! bool SEMI!
	;

dictionary_spec
	: (
	DICTIONARY 
                LBRACE
                    dict_elements+
		RBRACE
          )
          -> ^(DICT_SPEC dict_elements+)
	;

dict_elements
	: (
	ELEMENT
	LBRACE
		dict_entry+
	RBRACE
            )
            -> ^(DICT_ELEMENT dict_entry+)
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
    : ASSOC EQ! STRING SEMI!
    ;

dict_name
    : (NAME EQ! STRING SEMI!)
    ;

dict_heading
    : (HEADING EQ! STRING SEMI!)
    ;

dict_attrno
    : (ATTRNO EQ! STRING SEMI!)
    ;

dict_reference
    : REFNO EQ! ATTRIBUTEVALUE SEMI!
    ;

dict_mv_indicator
    : MV EQ! STRING SEMI!
    ;

// Either a UniVerse style ITYPE or an Attribute 8 is allowed, but not both
//
dict_attr8_itype
    : ATTR8 EQ! STRING SEMI!
    | ITYPE EQ! STRING SEMI!
    ;

dict_conv
    : CONV EQ! STRING SEMI!
    ;

dict_just
    : JUSTIFICATION EQ! STRING SEMI!
    ;

dict_width
    : WIDTH EQ! STRING SEMI!
    ;

dict_format
    : FORMAT EQ! STRING SEMI!
    ;

dict_colno
    : COLNO EQ! STRING SEMI!
    ;

query_body
	: (
	QUERY
	LBRACE
		querySpecs
		queryLogic
	RBRACE
          )
          -> ^(QUERY_BODY querySpecs queryLogic)
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
                SORTED          EQ sorted  =    bool        SEMI
                SELECTLIST      EQ sellist =    bool        SEMI
           )
                -> ^(QUERY_SPECS 
                        FILENAME    $fname 
                        GLOBAL      $global
                        FILETYPE    $ftype
                        COMMAND     $command 
                        TYPE        query_type 
                        (PROCESSOR   $output)?
                        OPTIONS     $opts
                        SORTED      $sorted 
                        SELECTLIST  $sellist
                    )
	;

bool
        :   BRUE
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
    options {k=1;}  // All alts covered by the gated predicate
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
                    (   (sc1 += saving_clause           )
                      | (se1 += selectExp               )
                      | (so1 += sort_exp                )
                      | (c1  += common_connectives      )
                      | (ic1 += itemid_clause           )
                      | (output_spec) => (os1 += output_spec             )
                    )*
                    ( tc1 += to_clause)?
                    (
                        // If the query had an @ or @LPTR record to deal with, the pre-lexer
                        // just spits it out here. However, these things are only used for LIST
                        // like statements, not select like statements so we just parse them and ignore them.
                        //
                        AT LBRACE
			(
                            (se2+=selectExp | so2+=sort_exp| os2+=output_spec | c2+=connective)*
                        )
                        RBRACE
                    )?
               RBRACE
             )
                -> { $c1 == NULL && $ic1 == NULL && $se1 == NULL && $so1 == NULL && $tc1 == NULL && $sc1 == NULL}?             
                -> 
                        ^(STATEMENT $c1* $ic1* $se1* $so1* $sc1* $tc1*)
	;


listTypeLogic
	:   (
		BODY
		LBRACE
                    (
                            (   os1 += output_spec
                              | c1  += connective
                              | se1 += selectExp 
                              | so1 += sort_exp
                              | ic1 += itemid_clause
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
                            (se2+=selectExp | so2+=sort_exp| os2+=output_spec | c2+=connective)*
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
                -> { $c1 == NULL && $c2 == NULL && $ic1 == NULL && $se1 == NULL && $se2 == NULL && $so1 == NULL && $so2 == NULL && $os1 == NULL && $os2 == NULL}?
                -> { $os1 == NULL }?     // No output spec, use the default
                        ^(STATEMENT $c1* $c2* $ic1* $os2* $se1* $se2* $so1* $so2* )                     
                -> 
                        ^(STATEMENT $c1* $ic1* $os1* $se1* $so1* )
	;


to_clause
        : TO integerparam -> ^(SELECT_TO integerparam)
        ;

connective
	:	COL_HDR_SUPP                    ->   COL_HDR_SUPP
	|	COL_SPACES integerparam         -> ^(COL_SPACES integerparam)
	|	COL_SUPP                        ->   COL_SUPP
	|	COUNT_SUPP                      ->   COUNT_SUPP
	|	DBL_SPACE                       ->   DBL_SPACE
	|	DET_SUPP                        ->   DET_SUPP
	|	FOOTING  STRING                 -> ^(FOOTING STRING)
	|	GRAND_TOTAL	STRING          -> ^(GRAND_TOTAL STRING)
	|	HEADING STRING                  -> ^(HEADING STRING)
	|	HDR_SUPP                        ->   HDR_SUPP
	|	ID_SUPP                         ->   ID_SUPP
	|	OPTLPTR                         ->   OPTLPTR                          
	|	MARGIN integerparam             -> ^(MARGIN integerparam)
	|	NOPAGE                          ->   NOPAGE
	|	NOSPLIT                         ->   NOSPLIT
	|	NO_INDEX                        ->   NO_INDEX
	|	ONLY                            ->   ONLY
	|	REQUIRE_INDEX                   ->   REQUIRE_INDEX
	|	REQUIRE_SELECT                  ->   REQUIRE_SELECT
	|	VERT                            ->   VERT
        |       common_connectives
	;

// Connectives that can be used both with SELECT and LIST
//
common_connectives
        :	FROM        integerparam      -> ^(FROM integerparam)
	|	SAMPLE      integerparam      -> ^(SAMPLE integerparam)
	|	SAMPLED     integerparam      -> ^(SAMPLED integerparam)
        ;

integerparam:
		NUMBER 
	;

saving_clause:
              SAVING?
                 UNIQUE?
                 dict_element
                 NO_NULLS? 

                -> ^(SAVE dict_element UNIQUE? NO_NULLS?)
	;

//  Item Id selection clause
//		
itemid_clause
    :   idselectExp
    |	INQUIRING
	{ fprintf(stderr, "INQURING is not supported!!\n");
            //cmqlError.logError("CMQL: INQURING is not supported!");
	    parseError = ANTLR3_TRUE;
	}
    ;

// We only get into expressions if there is a relational operator present 
//
idselectExp:
                ids=idselectSubExpr -> ^(IIDSELECT $ids)
        ;

idselectSubExpr:
		idselect_primary 
                    ( iselectConjuntive^^ idselect_secondary )* 
	;

iselectConjuntive
        : AND -> AND
        | OR  -> OR
        ; 


idselect_primary
	:   (str += IDSTRING)
            
            -> ^(IID $str)
        | (de += baddict)
            
            -> ^(IID $de)
        | idss=idselect_secondary

            -> $idss
        ;


idselect_secondary

    scope   { ANTLR3_BOOLEAN reverseMatch;}

        :  (op=EQ|op=NE|op=OPLT|op=GT|op=LE|op=GE) (opde=baddict | opstr=IDSTRING)

            -> $op $opstr? $opde?

	|   BETWEEN UQS? (lower=STRING | lower=IDSTRING) UQS? (higher=STRING | higher=IDSTRING)

            -> BETWEEN $lower $higher

	|   LIKE UQS? (opstr=STRING | opstr=IDSTRING)	
		{ 
		    if	($opstr.text[0] == '~')  
		    {
			$idselect_secondary::reverseMatch = ANTLR3_TRUE;
		    } 
		    else 
		    {
			$idselect_secondary::reverseMatch = ANTLR3_FALSE;
		    }
		   // $opstr->setText(convertPattern($opstr.text));
		   $opstr->setText($opstr, "pattern convert goes here");
		}
            -> {  $idselect_secondary::reverseMatch }? UNLIKE $opstr
	    -> {! $idselect_secondary::reverseMatch }? LIKE $opstr
	    ->

	|   UNLIKE UQS? (opstr=STRING | opstr=IDSTRING)
		{ 
		    if	($opstr.text[0] == '~')  
		    {
			$idselect_secondary::reverseMatch = ANTLR3_TRUE;
		    } 
		    else 
		    {
			$idselect_secondary::reverseMatch = ANTLR3_FALSE;
		    }
		   // $opstr->setText(convertPattern($opstr.text));
		   $opstr->setText($opstr, "pattern convert goes here");
		}
            -> {  $idselect_secondary::reverseMatch }? LIKE $opstr
	    -> {! $idselect_secondary::reverseMatch }? UNLIKE $opstr
	    ->
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

			s1=selectTerm (s1a+=selectTermSet  )*

                    {  $queryLogic::firstExpression = ANTLR3_FALSE; }
	  )
             -> {   $selectExp::isWhen}? ^(WHEN_CLAUSE ^(ASP $s1) $s1a*)
	     -> { ! $selectExp::isWhen}? ^(WITH_CLAUSE ^(ASP $s1) $s1a*)
	     ->
        | (
            // Syntactic predicate failed, hence this subsequent expression must begin
            // with "AND" or "OR". Note that we allow the user to miss out the WITH that
            // in theory MUST follow this as it is not ambiguous. However I have decided
            // that allowing WITH X = 8 WITH G=9 and implying either AND WITH or OR WITH
            // is stupid and we are going to make people specify which logical connection
            // they require.
            //
            se=selectExpSubsequent
	    )
             -> $se
        ;

selectExpSubsequent
        :
           ( AND (    WITH  { $selectExp::isWhen = ANTLR3_FALSE; }
		    | WHEN  { $selectExp::isWhen = ANTLR3_TRUE;  }
		 ) 
		    s1=selectTerm (s1a+=selectTermSet  )*
	    

			    -> {   $selectExp::isWhen}? ^(AND_WHEN ^(ASP $s1) $s1a*)
			    -> { ! $selectExp::isWhen}? ^(AND_WITH ^(ASP $s1) $s1a*)
			    ->
	   )
        |  ( OR  (    WITH  { $selectExp::isWhen = ANTLR3_FALSE; }
		    | WHEN  { $selectExp::isWhen = ANTLR3_TRUE;  }
		 ) 
		    s2=selectTerm (s2a+=selectTermSet	)*   
	    
			    -> {   $selectExp::isWhen}? ^(OR_WHEN ^(ASP $s2) $s2a*)
			    -> { ! $selectExp::isWhen}? ^(OR_WITH ^(ASP $s2) $s2a*)
			    ->
	    )
        ;

selectTermSet
	: AND sta=selectTerm	-> ^(ASP_AND $sta)
	| OR  sto=selectTerm	-> ^(ASP_OR  $sto)
	;

selectTerm
    scope   { ANTLR3_BOOLEAN isFirst;  }
    @init   { $selectTerm::isFirst   = ANTLR3_TRUE; }
        : ( NO? NOT? EACH?  dict_element  ((value_selection_exp) => value_selection_exp)* )

                -> ^(SELECT_FACTOR NO? NOT? EACH? dict_element value_selection_exp*)
 	;

value_selection_exp
        : (
            value_selection_primary         

                -> {  $selectTerm::isFirst}?     ^(VSP value_selection_primary        ) 
                -> {! $selectTerm::isFirst}?     ^(VSP_OR  value_selection_primary    )
                ->
            
            |   AND value_selection_primary     -> ^(VSP_AND value_selection_primary    )
            |   OR  value_selection_primary     -> ^(VSP_OR  value_selection_primary    )
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
		-> {   strcmp($s.text, "\"\"") == 0 }?  COMPNULL
		-> {   strcmp($s.text, "\"\"") != 0 }?  EQ $s
		->
	|   ISNULL
		-> COMPNULL

	|   ISNOTNULL
		-> NOT COMPNULL

	|   EQ
		(	UQS? s=STRING	    -> {   strcmp($s.text, "\"\"") == 0 }? COMPNULL
					    -> {   strcmp($s.text, "\"\"") != 0 }? EQ $s
					    ->
		    |	d=dict_element	    -> EQ $d
		)	
	
	|   NE     
		(	UQS? s=STRING	    -> {   strcmp($s.text, "\"\"") == 0 }? NOT COMPNULL
					    -> {   strcmp($s.text, "\"\"") != 0 }? NE $s
					    ->
		    |	d=dict_element	    -> NE $d
		)	

	|   OPLT     
		(	UQS? s=STRING
			    {
				if  (strcmp($s.text, "\"\"") == 0) 
				{
				    fprintf(stderr, "[CMQL - WARNING] : LT \"\" has no meaning. EQ \"\" assumed.\n");
				}
			    }
					    -> {   strcmp($s.text, "\"\"") == 0 }? COMPNULL
					    -> {   strcmp($s.text, "\"\"") != 0 ) }? OPLT $s
					    ->
		    |	d=dict_element	    -> OPLT $d
		)

	|   GT
		(	UQS? s=STRING	    -> {   strcmp($s.text, "\"\"") == 0 }? NOT COMPNULL
					    -> {   strcmp($s.text, "\"\"") != 0 }? GT $s
					    ->
		    |	d=dict_element	    -> GT $d
		)

	|   LE     
		(	UQS? s=STRING
			    {
				if  (strcmp($s.text, "\"\"") == 0)  
				{
				    fprintf(stderr, "[CMQL - WARNING] : LT \"\" has no meaning. EQ \"\" assumed.\n");
				}
			    }
					    -> {   strcmp($s.text, "\"\"") == 0 }? COMPNULL
					    -> {   strcmp($s.text, "\"\"") != 0 }? LE $s
					    ->
		    |	d=dict_element	    -> LE $d
		)

	|   GE     
		(	UQS? s=STRING
			    {
				if  (strcmp($s.text, "\"\"") 
				{
				    fprintf(stderr, "[CMQL - WARNING] : GE \"\" has no meaning (selects everything). NE \"\" assumed.");
				}
			    }
					    -> {   strcmp($s.text, "\"\"") == 0 }? NOT COMPNULL
					    -> {   strcmp($s.text, "\"\"") != 0 }? GE $s
					    ->
		    |	d=dict_element	    -> GE $d
		)

	|       BETWEEN wb1=withbetween1 wb2=withbetween2
		
		    -> BETWEEN $wb1 $wb2

	|	LIKE UQS? opstr=STRING	
		    {
			if	($opstr.text[0] = '~')  
			{
			    $value_selection_primary::reverseMatch = ANTLR3_TRUE;
			} 
			else 
			{
			    $value_selection_primary::reverseMatch = ANTLR3_FALSE;
			}
			//$opstr->setText(convertPattern($opstr.text));
			$opstr->setText($opstr, "convert pattern goes here");
		    }
		 -> {  $value_selection_primary::reverseMatch }? UNLIKE $opstr
		 -> {! $value_selection_primary::reverseMatch }? LIKE $opstr
		 ->

	|	UNLIKE UQS? opstr=STRING	
		    { 
			if	($opstr.text[0] == '~')
			{
			    $value_selection_primary::reverseMatch = ANTLR3_TRUE;
			} 
			else 
			{
			    $value_selection_primary::reverseMatch = ANTLR3_FALSE;
			}
			//$opstr->setText(convertPattern($opstr.text));
			$opstr->setText($opstr, "convert pattern goes here");
		    }
		 -> {  $value_selection_primary::reverseMatch }? LIKE $opstr
		 -> {! $value_selection_primary::reverseMatch }? UNLIKE $opstr
		 ->

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
            ->^(OUTPUT_SPEC output_elements)
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
        :       ATTRIBUTEVALUE ((limiter_exp)=> lie=limiter_exp)? formatting*
		-> {$query::isSum}?	TOTAL ^(DICT_ELEMENT ATTRIBUTEVALUE)
		-> {$query::isStat}?	TOTAL ^(DICT_ELEMENT ATTRIBUTEVALUE) AVERAGE ^(DICT_ELEMENT ATTRIBUTEVALUE) ENUM ^(DICT_ELEMENT ATTRIBUTEVALUE)
                -> ^(DICT_ELEMENT ATTRIBUTEVALUE $lie? formatting*)
        ;

dict_element
        : ATTRIBUTEVALUE formatting*
            -> ^(DICT_ELEMENT ATTRIBUTEVALUE formatting*)
        ;
        
limiter_exp
        : 
		limiter ((limiter_op)=> (limiter_op^ limiter))*
	;

limiter_op
        : AND   -> AND
        | OR    -> OR
        |       -> OR
        ;

limiter:
            (NOT    NE     s=baddict) -> EQ $s
          | (NOT    OPLT   s=baddict) -> GE $s
          | (NOT    GT     s=baddict) -> LE $s
          | (NOT    LE     s=baddict) -> GT $s
          | (NOT    GE     s=baddict) -> OPLT $s
          | (NOT    opt_eq s=baddict) -> NE $s
          | (       NE     s=baddict) -> NE $s
          | (       OPLT   s=baddict) -> OPLT $s
          | (       GT     s=baddict) -> GT $s
          | (       LE     s=baddict) -> LE $s
          | (       GE     s=baddict) -> GE $s
          | (       opt_eq s=baddict) -> EQ $s
	;

opt_eq
    : EQ    -> EQ
    |       -> EQ
    ;

baddict
    scope   { ANTLR3_BOOLEAN bad;              }
    @init   { $baddict::bad = ANTLR3_FALSE;  }

    :   (
            UQS { $baddict::bad = ANTLR3_TRUE; } 
        )? 
            bs=STRING
    
    {
        if  ($baddict::bad) 
        {
            fprintf(stderr, "CMQL: The dictionary entry \%s is not defined!\n", $bs.text);
            parseError = ANTLR3_TRUE;
        }
    }
        ->$bs
    ;

 formatting
	:	FMT UQS? STRING             -> FMT  STRING
	|	CONV UQS? STRING            -> CONV STRING
	|	DISPLAY_LIKE ATTRIBUTEVALUE -> DISPLAY_LIKE ATTRIBUTEVALUE
	|	COL_HDG UQS? STRING         -> COL_HDG STRING 
	|	ASSOC UQS? STRING           -> ASSOC STRING 
	|	ASSOC_WITH ATTRIBUTEVALUE   -> ASSOC_WITH ATTRIBUTEVALUE 
	|	MULTI_VALUE                 -> MULTI_VALUE 
	|	SINGLE_VALUE                -> SINGLE_VALUE
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
BFALSE          :      'false';
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
OPTLPTR         :	'LPTR';
OPLT            :       'LT' | 'BEFORE';
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
BTRUE           :       'true';
TYPE            :	'type';
UNIQUE          :       'UNIQUE';
UNLIKE          :       'UNLIKE' | 'NOT.MATCHING';
UQS             :       'UQS';
VERT            :	'VERT';
WHEN            :       'WHEN';
WIDTH           :       'width';
WITH            :       'WITH' | 'WHERE';
WITHIN          :	'WITHIN';