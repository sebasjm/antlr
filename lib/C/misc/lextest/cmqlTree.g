

tree grammar cmqlTree;

options 
{
	language	= C;
        tokenVocab      = cmql;
        ASTLabelType    = pANTLR3_BASE_TREE;
}


@members {

    ANTLR3_BOOLEAN parseError;
}


query[pANTLR3_STRING processNo]
    returns [pANTLR3_STRING routineCode, pANTLR3_STRING definitionCode, ANTLR3_BOOLEAN treeError]

    // Scope variables are visible to rules that are invoked
    // by this rule, hence we use a number of pANTLR3_STRINGs declared
    // here to build the code we are generating for this query. A this
    // stage we are still using Java but will switch to C or possibly
    // C++ later on as it is easier to develop using the Java based tools
    // for ANTLR.
    //
    scope
    {
        pANTLR3_STRING      tableSelectors;         // FROM table As t1 table as t2 etc 
	pANTLR3_STRING	    sqlLeader;		    // The comments and &SQL ANTLR3_UINT64ro for the statement
        pANTLR3_STRING      sqlStatement;           // The SQL statement we need for this query
        pANTLR3_STRING      sqlIterator;            // Iteration code to retrieve the results
        pANTLR3_STRING      define;                 // The code to compile and run to define the table
        pANTLR3_STRING      routine;                // The final routine code to spit out
        pANTLR3_STRING      command;                // The original command line
        pANTLR3_STRING      filename;               // The original filename

        ANTLR3_BOOLEAN     haveSelectElements;     // Indicate that we have user defined output specs
        pANTLR3_STRING      selectElements;         // Columns we wish SQL to return for us
        pANTLR3_STRING      ANTLR3_UINT64oElements;           // COS variables we want to select ANTLR3_UINT64o
        pANTLR3_STRING      defaultSelectElements;  // Columns for SQL to return if query specified none
        pANTLR3_STRING      defaultANTLR3_UINT64oElements;    // ANTLR3_UINT64O clause for default output spec

        ANTLR3_BOOLEAN     haveCalculations;       // Indicates that we have some calculated columns
        pANTLR3_STRING      calculations;           // COS code for formatting conversions and correlatives

        pANTLR3_STRING      connectives;            // COS code to set up all teh output processor flags we come across here

        pANTLR3_STRING      orderClauses;           // Sort clauses (ORDER BY ...)
        ANTLR3_BOOLEAN     haveOrder;              // Indicates if the query contained any BY clauses
        ANTLR3_BOOLEAN     isExploded;             // Indicates if the query is an EXPLODED query

        pANTLR3_STRING      outputSpec;             // COS code for building output spec local

        pANTLR3_STRING      whereClauses;           // pANTLR3_STRING that builds the WHERE clauses with the correct precedence (CMQL style)
        pANTLR3_STRING      fromTables;             // Used to build the FROM clause to the query, hence it is a list of all the tables that we used.

        

        ANTLR3_BOOLEAN     suppressIndexes;        // Indicates that indexes shoudl not be defined
        ANTLR3_BOOLEAN     idOnly;                 // Indicates that the ONLY connective was picked up
        ANTLR3_BOOLEAN     requireIndex;           // Indicates that we fail if there is no index defined ... errr
        ANTLR3_BOOLEAN     requireSelect;          // Indicates that the query can only proceed if there is an active select list

        ANTLR3_UINT64         displayNo;              // Column display counter
        ANTLR3_UINT64         orderNo;                // BY column counter
        
        ANTLR3_UINT64         limitExprCount;         // Used for generating labels for prANTLR3_UINT64 limiting code generation

        ANTLR3_UINT64         refVarCount;            // Ensures distinct variable names for ICONV inputs

        pANTLR3_HASH_TABLE   columns;
        pANTLR3_HASH_TABLE   displayColumns;
        pANTLR3_HASH_TABLE   selectColumns;
        pANTLR3_LIST      savingColumns;
        pANTLR3_HASH_TABLE   tables;
        pANTLR3_HASH_TABLE   declaredTables;
        pANTLR3_HASH_TABLE   indexes;
	pANTLR3_HASH_TABLE   associations;	    // Collects pANTLR3_HASH_TABLEs of associated values

       // cmqlProcessor    iterator;

    }
    @init
    {
        //$query::iterator                    = new cmqlProcessor();
        //$query::iterator.suppressMissing    = ANTLR3_FALSE;        // Assume that we want a list of ids missing from input list
       // $query::orderClauses                = "";
        $query::haveOrder                   = ANTLR3_FALSE;
       // $query::connectives                 = "";
        $query::suppressIndexes             = ANTLR3_FALSE;
        $query::idOnly                      = ANTLR3_FALSE;
        $query::requireIndex                = ANTLR3_FALSE;
        $query::requireSelect               = ANTLR3_FALSE;
       // $query::calculations                = "";
        $query::haveCalculations            = ANTLR3_FALSE;
       // $query::fromTables                  = "FROM MV.T0";
        $query::limitExprCount              = 0;
        $query::refVarCount                 = 0;

       // $routineCode            = ""; 
       // $definitionCode         = ""; 
        $treeError              = ANTLR3_FALSE;

        //

        $query::displayNo       = 0;        // Display columns start at 0
        $query::orderNo         = 1000;    // BY columns start at 1000

         
}
    : ^(    (     QUERY
                    {
                        $query::isExploded  = ANTLR3_FALSE;
                    }
                | BYEXP
                    {
                        $query::isExploded          = ANTLR3_TRUE;
                        //$query::iterator.exploding  = ANTLR3_TRUE;
                    }
            )
            index_spec?
            dictionarySpec?
            defaultOutputSpec? 
            queryBody
          
        )
    ;
	  
index_spec
@init
          {
          
            }
	: ^(INDEXES
  
		indexes+
            )
	;

indexes
scope
{
    int newIndex;
}
@init
        {
           $indexes::newIndex      = 0;
        }
    :
	^(INDEX

		index_name          [$indexes::newIndex]
		index_type          [$indexes::newIndex]
                index_storage       [$indexes::newIndex]
		(index_elements     [$indexes::newIndex]   )*
		(index_nodes        [$indexes::newIndex]   )*	
        )
	;

index_storage [int ind]
        : STORAGE sname=uqs

        ;

index_name [int ind]
	: NAME iname=uqs

    ;

index_type  [int ind]
	: TYPE (   INDEX   
                    | BITMAP    
                    | BITSLICE  
                  )
	;

index_elements  [int ind]
scope
{
    int col;
}
@init
{
    $index_elements::col = 0;
}
	: ^(INDEX_ELEMENT
               (
                    index_entry   [$index_elements::col]
                )+
                 
            )
	;

index_entry [int col]
	:   dictName           [$col]
        |   dictAttrno         [$col]
        |   dictMvIndicator    [$col]
        |   dictAttr8Itype     [$col]
        |   dictConv           [$col]
        |   dictJust           [$col]
        |   dictColno          [$col]
	|   index_order        [$col]
	;
	

index_order     [int col]
	: ORDER (    
                      ASCENDING     
                    | DESCENDING    
                    )
	;
	
index_nodes [int ind]
scope
{
    int col;
}
@init
{
    $index_nodes::col = 0;
}
	: ^(INDEX_NODE

	    ( node_entry [$index_nodes::col])+
     
	  )
	;
	
node_entry [int col]
	:   dictName           [$col]
        |   dictAttrno         [$col]
        |   dictMvIndicator    [$col]
        |   dictAttr8Itype     [$col]
        |   dictConv           [$col]
        |   dictJust           [$col]
	|   node_collation     [$col]
        |   dictColno          [$col]
	;

node_collation [int col]
	:   COLLATED b=bool

        ;

dictionarySpec:

    ^(DICT_SPEC 

        dictElements+
    )
;

dictElements
        scope
        {
            // Create a scope level object to track this column
            //
            int  dictElement;
        }
	: ^(DICT_ELEMENT
            
            dictEntry+
           )
        
	;

dictEntry
	:   dictName            [$dictElements::dictElement]
        |   dictHeading         [$dictElements::dictElement]
        |   dictAssoc           [$dictElements::dictElement]
        |   dictAttrno          [$dictElements::dictElement]
        |   dictReference       [$dictElements::dictElement]
        |   dictMvIndicator     [$dictElements::dictElement]
        |   dictAttr8Itype      [$dictElements::dictElement]
        |   dictConv            [$dictElements::dictElement]
        |   dictJust            [$dictElements::dictElement]
        |   dictWidth           [$dictElements::dictElement]
        |   dictFormat          [$dictElements::dictElement]
        |   dictColno           [$dictElements::dictElement]
	;

dictName                    [int col]
    : NAME name=uqs

    ;

dictHeading                    [int col]
    : HEADING hd=uqs

    ;

dictAssoc                    [int col]
    : ASSOC as=uqs

    ;

dictAttrno                    [int col]
    : ATTRNO attr=uqs

    ;

dictReference                    [int col]
    : REFNO attr=ATTRIBUTEVALUE

    ;

dictMvIndicator                    [int col]
    : MV mv=uqs

    ;

dictAttr8Itype                    [int col]
    @init
    {
        // Need to indicate that we will require the ROW field for calculated fields
        //
        $query::haveCalculations  = ANTLR3_TRUE;
    }
    : ATTR8 a8=uqs

    | ITYPE it=uqs

    ;

dictConv                    [int col]
    @init
    {
        // Need to indicate that we will require the ROW field for calculated fields
        //
        $query::haveCalculations  = ANTLR3_TRUE;
    }
    : CONV c=uqs

    ;

dictJust                    [int col]
    : JUSTIFICATION j=uqs

    ;

dictWidth                    [int col]
    : WIDTH w=uqs

    ;

dictFormat                    [int col]
    : FORMAT fmt=uqs

    ;

dictColno                    [int col]
    : COLNO colno=uqs

    ;

defaultOutputSpec
	: ^(OUTPUT_SPEC genericAttr+)
	;

queryBody
	: ^(QUERY_BODY querySpecs queryLogic)
	;

querySpecs
	: ^(QUERY_SPECS FILENAME    fn      = STRING 
                        GLOBAL      gl      = uqs
                        FILETYPE    ft      = integerparam
                        COMMAND     com     = uqs
                        TYPE                  queryType
                        PROCESSOR   proc    = uqs
                        OPTIONS     opts    = uqs
                        SORTED      so      = bool
                        SELECTLIST  sl      = bool
            )
         
	;

bool
    returns [ANTLR3_BOOLEAN   value]
    @init   
    {
        value = ANTLR3_FALSE; 
    }
        :   BTRUE
                {
                    value = ANTLR3_TRUE; 
                }
        |   BFALSE
                {
                    value = ANTLR3_FALSE;    
                }
        ;

queryType
	:   IDLIST

	|   ITEMSTREAM
       
	|   DATASTREAM
     
        |   INTERNAL
    
	|   SUM

	|   STAT

	;

queryLogic
	:   statement
	;

// -----------------------------------------------------------
// Main query body grammar
//
statement	
    returns [pANTLR3_STRING sqlStatement]
    scope    
    {
        ANTLR3_BOOLEAN inquiring;
    }
    @init    
    {
        sqlStatement                = NULL;
        //$query::iterator.breakCount = 0;
        $statement::inquiring        = ANTLR3_FALSE;
    }
	:   ^(  STATEMENT
                connective*
                itemidClause*
		outputSpec*
                selectExp* 
                sortExp* 
                savingClause*
                toClause?
            )
	
         |
;

toClause
        : ^(SELECT_TO st=integerparam)

        ;

connective
	: COL_HDR_SUPP
	| ^(COL_SPACES i=integerparam)
	| COL_SUPP
	| COUNT_SUPP
	| DBL_SPACE
	| DET_SUPP
	| ^(FOOTING  fs=uqs)
	| ^(GRAND_TOTAL	gts=uqs  )
	| ^(HEADING hs=uqs    )
	| HDR_SUPP
	| ID_SUPP
	| OPTLPTR
	| ^(MARGIN integerparam)
	| NOPAGE
	| NOSPLIT
	| NO_INDEX
	| ONLY
	| REQUIRE_INDEX
	| REQUIRE_SELECT
	| ^(SAMPLE i=integerparam)
	| ^(SAMPLED i=integerparam)
	| VERT
	| WITHIN
	| ^(FROM i=integerparam)
            
	;

integerparam
        returns [pANTLR3_STRING strVal, ANTLR3_UINT64 intVal]
        : nu=NUMBER 
            
	;

savingClause
scope   { int col; }
@init {$query::savingColumns   = NULL; } 
    :
        ^(SAVE sa=dispAttrVal[ANTLR3_FALSE] 
            (     UNIQUE
            )?
            (     NO_NULLS
                    {
                    }
                |   {
                    }
            )
         )
	;

//  Item Id selection clause
//		
itemidClause
        : ^(
                IIDSELECT ids=idselectExp
            )
	| INQUIRING
        {
            fprintf(stderr, "Apologies: INQUIRING is not supported by CMQL!");
            parseError = ANTLR3_TRUE;
        }
	;

// We only get ANTLR3_UINT64o expressions if there is a relational operator present 
// 
idselectExp   
    returns [pANTLR3_STRING expression]
    @init    {   expression = NULL;    }

        : ids=idselectPrimary
        | ^( AND ids1=idselectExp ids2=idselectExp)
        | ^( OR ids1=idselectExp ids2=idselectExp)
	;

idselectPrimary
    returns [pANTLR3_STRING expression]
    scope { ANTLR3_BOOLEAN haveMany; }
    @init    {   expression = NULL; $idselectPrimary::haveMany  = ANTLR3_FALSE;   }
	:
	^(IID s=uqis  
                 )
        | NE 
            val=uqis

        | EQ 
            val=uqis
 
	|	(  
                   OPLT 
                 | GT 
                 | LE 
                 | GE 
                ) 
                val=uqis
                
	|	BETWEEN b1=uqis b2=uqis
                
	|	LIKE pat=(STRING | IDSTRING)
             
	|	UNLIKE pat=(STRING | IDSTRING)
               
	; 

//
// Value selection clause
//
// First time thru accept only WITH
//
selectExp 
    scope   { pANTLR3_STRING	expression; }
    @init   { $selectExp::expression = NULL; }
    :
      ^(WITH_CLAUSE selectFactorSet[ANTLR3_FALSE]+ )

    | ^(OR_WITH    selectFactorSet[ANTLR3_FALSE]+    )

    | ^(AND_WITH    selectFactorSet[ANTLR3_FALSE]+   )

    | ^(WHEN_CLAUSE selectFactorSet[ANTLR3_TRUE]+ )

    | ^(OR_WHEN    selectFactorSet[ANTLR3_TRUE]+    )

    | ^(AND_WHEN    selectFactorSet[ANTLR3_TRUE]+   )

    ;

selectFactorSet[ANTLR3_BOOLEAN isWhen]
    : ^(ASP	sel=selectFactor[isWhen] )

    | ^(ASP_AND sel=selectFactor[isWhen] )
    | ^(ASP_OR	sel=selectFactor[isWhen] )
    ;

// selectFactor covers both WHEN and WITH clauses. A when clause is 
// indicated by passing 'ANTLR3_TRUE' to the production, which causes prANTLR3_UINT64 limiting
// code to be generated for the referenced dictionary element as well as the
// generation of the SQL to select the records. A WHEN clause issues both
// selection criteria to the SQL engine, but also limits the displayed
// output to just those values that pass the output criteria. We could attempt
// to do this with grouping multivalues ANTLR3_UINT64o child tables but there are issues
// with child tables having more than one column at the moment and anyway
// it would be duplicate work for prANTLR3_UINT64 limiting and so on. So we are going to 
// limit the use of child tables to BY-EXP and the output will be fine.
//
selectFactor[ANTLR3_BOOLEAN isWhen]
    returns [pANTLR3_STRING expression]
    scope   {
                int      col;
                pANTLR3_STRING          vsExpression;
                ANTLR3_BOOLEAN         isALL;
                ANTLR3_BOOLEAN         isNOT;
                ANTLR3_BOOLEAN         hasNO;
		int      limCol;         
            }
    @init   { $expression                   = NULL; 
              $selectFactor::vsExpression   = NULL;
	      $selectFactor::limCol         = 0;
            }
    :
        ^(SELECT_FACTOR
            (     NO {$selectFactor::hasNO = ANTLR3_TRUE;  }
                |    {$selectFactor::hasNO = ANTLR3_FALSE; }
            )
            (     NOT   { $selectFactor::isNOT = ANTLR3_TRUE;  } 
                |       { $selectFactor::isNOT = ANTLR3_FALSE; }
            ) 
            (       EACH    // Implies all multivalued elements must match the test 
                    {
                        $selectFactor::isALL  = ANTLR3_TRUE;
                    }
                |
                    {
                        $selectFactor::isALL  = ANTLR3_FALSE;
                    }
            )
            av=genericAttr 
                            
                        (
			    // Work out the SQL code for the WHERE clause and accumulate
			    // prANTLR3_UINT64 limiting code for any output column that this
			    // WHEN clause (if it is a WHEN clause) touches
			    //
                                valueSelectionExp[isWhen,$selectFactor::limCol] +
                                
                            | // Epsilon means test for some values that are NOT NULL or if NO $n then ALL NULL
                              //
                              
                        )
        )
    ;


valueSelectionExp[ANTLR3_BOOLEAN isWhen, int limCol]
                : ^(VSP vsp=valueSelectionPrimary[isWhen, limCol])
                        
                | ^(VSP_AND vsp=valueSelectionPrimary[isWhen, limCol])          
                        
                | ^(VSP_OR vsp=valueSelectionPrimary[isWhen, limCol])
                        
                ;


valueSelectionPrimary[ANTLR3_BOOLEAN isWhen, int limCol]
    returns [pANTLR3_STRING expression, pANTLR3_STRING limitInit, pANTLR3_STRING limitExp]
    scope  {
                pANTLR3_STRING      operator;
            }
    @init    {
                $expression  = NULL; 
                 
            }
	:	(not=NOT)? COMPNULL
                

	// Comparisons with constant values
	//
	|	// If we have a simple single valued element then the operator stands
                // as we see it.
                // If this is an ALL clause then this a clause of a sub query and we complement
                // all the operators such that X = "A" OR = "B" becomes X <> "A" AND X <> "B" because
                // this becomes an EXISTS clause: NOT EXISTS (SELECT 1 .. WHERE X <> "A" AND X <> "B")
                // If this is an ANY clause then the operators stand as they are.
                // In addition, certain operators include an OR IS NULL or OR IS NOT NULL (if it is an ALL clause)
                // such that the empty pANTLR3_STRING "" is included in the result set for that condition
                //
                // N.B. This may be accumulating limiter expressions for all instances of this
                // selectFactor that also occur in the output set. WHEN clauses are subtley different
                // to WHERE clauses in this regard as they are used both to SELECT the records that
                // we are going to output data about and also limit the displayed values to this criteria
                // This means we need to separate out the ALL type cases because ALL means select the
                // the item for processing when ALL the sub elements match, but when we add the output limit
                // code, we just treat it as we woudl if it did not have an ALL clause. Use of WHEN in this
                // case is not useful as there is no poANTLR3_UINT64 limiting the prANTLR3_UINT64 output as all cases will pass.
                // As I don't expect anyone to do this expect when they are not thinking it through I don't
                // bother to optimize this out by not providing the limiting code, later I will perhaps
                // detect that this is an ALL type qualifier and just not bother with the output limiting code.
                //
                (  op=NE s=uqs 
                    
                 | op=OPLT s=uqs 
                    
                 | op=GT s=uqs 
                    
                 | op=LE s=uqs 
                    
                 | op=GE s=uqs 
                    
                 | op=EQ s=uqs 
                    
                ) 

                

	// Comparisons with other dictionary elements
	//
	|	// If we have a simple single valued element then the operator stands
                // as we see it.
                // If this is an ALL clause then this a clause of a sub query and we complement
                // all the operators such that X = "A" OR = "B" becomes X <> "A" AND X <> "B" because
                // this becomes an EXISTS clause: NOT EXISTS (SELECT 1 .. WHERE X <> "A" AND X <> "B")
                // If this is an ANY clause then the operators stand as they are.
                // In addition, certain operators include an OR IS NULL or OR IS NOT NULL (if it is an ALL clause)
                // such that the empty pANTLR3_STRING "" is included in the result set for that condition
                //
                (  op=NE e=genericAttr
                    
                 | op=OPLT e=genericAttr
                    
                 | op=GT e=genericAttr
                    
                 | op=LE e=genericAttr
                    
                 | op=GE e=genericAttr
                    
                 | op=EQ e=genericAttr
                    
                ) 
                // Having built the sub clause, we now need to add the value
                // that we are comparing against.
                //
                
	|	BETWEEN
                
                    (     str1=uqs 
                            
                        | attr=genericAttr
                                                )
                    (     str2=uqs 
                            
                        | attr=genericAttr
                           
                    )
	|	LIKE pat=STRING
                
	|	UNLIKE pat=STRING
                
	|	SAID STRING
	;


//  sort expression
//
sortExp:
	sortclause 
	;

// Sorting:
// Note that a BY caluse implies a single valued sort, and so we specify an ORDER BY
// to use the display column definition and must add it to the list of
// displayed columns so that it is declared.
// A BY-EXP clause implies a multivalued field. However, we do not give an 
// error if the subject is not multivalued - perhaps we should...
//
sortclause:
			BY 				ga=dispAttrVal[ANTLR3_TRUE] 
                        
		|	BY_DSND 			ga=dispAttrVal[ANTLR3_TRUE]
                        
		|	BY_EXP 				ga=genericAttrLimited
                        
		|	BY_EXP_DSND			ga=genericAttrLimited
                        
		|	BY_EXP_SUB			ga=genericAttrLimited
		|	BY_EXP_SUB_DSND                 ga=genericAttrLimited
		;

//  Output section
//
outputSpec
        : ^(
                OUTPUT_SPEC outputElements+
            )
            
        ;

outputElements
	:	TOTAL da=dispAttrVal[ANTLR3_FALSE] limiterExp[$da.col]?
                
	|	AVERAGE da=dispAttrVal[ANTLR3_FALSE] (
                                            NO_NULLS
                                            
                                        )? limiterExp[$da.col]?
                
	|	ENUM da=dispAttrVal[ANTLR3_FALSE] (
                                        NO_NULLS
                                        
                                   )? limiterExp[$da.col]?
                
	|	MAX da=dispAttrVal[ANTLR3_FALSE] limiterExp[$da.col]?
                
	|	MIN da=dispAttrVal[ANTLR3_FALSE] NO_NULLS
                                       
                    limiterExp[$da.col]?
                
	|	PERCENT da=dispAttrVal[ANTLR3_FALSE] limiterExp[$da.col]?
                
	|	TRANSPORT da=dispAttrVal[ANTLR3_FALSE] limiterExp[$da.col]?
                
	|	BREAK_ON    (     quals=STRING   da=dispAttrVal[ANTLR3_FALSE] 
                                | da=dispAttrVal[ANTLR3_FALSE]  (quals=STRING)?
                            )
                
	|	BREAK_SUP   (     quals=STRING   da=dispAttrVal[ANTLR3_FALSE] 
                                | da=dispAttrVal[ANTLR3_FALSE]  (quals=STRING)?
                            )
               
	|      dictDisplay
	;

// This limiter is used on output spec elements to generate the testing code for a set
// of limiting expression. PrANTLR3_UINT64 limiting is applied at output time only, other forms of 
// limiting such as total limits are used to limit the values of calculations.
// This rule generates code that deletes any subvalues in the input variable that
// do not match the criteria parsed.
//
limiterExp[int col]

	: le=limitExpressions[$col]

	;

limitExpressions[int col]
    returns [pANTLR3_STRING initCode, pANTLR3_STRING expression]

        :   li=limiter[$col]
		
	    | ^(AND l1=limitExpressions[$col] l2=limitExpressions[$col])
		
	    | ^(OR  l1=limitExpressions[$col] l2=limitExpressions[$col])
		
	;

limiter[int col]
    returns [pANTLR3_STRING expression, pANTLR3_STRING initCode]
    :
	op=( NE | OPLT | GT | LE | GE | EQ ) qual=uqs
        
    ;

dictDisplay
        returns [  int  col]


        :       ^(DICT_ELEMENT attr=dispAttr[ANTLR3_FALSE] 

                        limiterExp[$col]? (formatting[$col])*)

        ;

 formatting [  int  col]
	: FMT               s=uqs
       
	| CONV              s=uqs
        
	| DISPLAY_LIKE      attr=ATTRIBUTEVALUE
        
	| COL_HDG           s=uqs
        
	| ASSOC             s=uqs
            
	| ASSOC_WITH        attr=ATTRIBUTEVALUE
        
	| MULTI_VALUE
        
	| SINGLE_VALUE
        
	;

dispAttrVal [ANTLR3_BOOLEAN isOrder]
        returns [  int  col]
        
        : ^(DICT_ELEMENT attr=dispAttr[isOrder] 
                            
                (formatting[$col])*
            )

        ;

dispAttr [ANTLR3_BOOLEAN isOrder]
        returns [  int  col]

        :
                attr=ATTRIBUTEVALUE
                
        ;

genericAttrLimited
        returns [int    col]
        
    : ^( DICT_ELEMENT attr=genat limiterExp[$col]?)
 
    ;

genericAttr
        returns [int    col]
       
        :
            ^( DICT_ELEMENT attr=genat )
           
        ;

genat
        returns [int    col]

    : attr=ATTRIBUTEVALUE
        
        ;

// Rules to remove quotes from STRINGs
//
uqs
    returns [pANTLR3_STRING value]
    :
        s=STRING

    ;

uqis
    returns [pANTLR3_STRING value]
    @init { value = NULL; }
    :
        (s=STRING | s=IDSTRING)
    ;
    
