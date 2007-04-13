grammar t042ast;
options {
    language = Python;
    output = AST;
}

tokens {
    VARDEF;
    FLOAT;
    EXPR;
    BLOCK;
}

r1
    : INT ('+'^ INT)*
    ;

r2
    : 'assert'^ x=expression (':'! y=expression)? ';'!
    ;

r3
    : 'if'^ expression s1=statement ('else'! s2=statement)?
    ;

r4
    : 'while'^ expression statement
    ;

r5
    : 'return'^ expression? ';'!
    ;

r6
    : (INT|ID)+
    ;

r7
    : INT -> 
    ;

r8
    : 'var' ID ':' type -> ^('var' type ID) 
    ;

r9
    : type ID ';' -> ^(VARDEF type ID) 
    ;

r10
    : INT -> {antlr3.tree.CommonTree(antlr3.CommonToken(type=FLOAT, text=$INT.text + ".0"))}
    ;

r11
    : expression -> ^(EXPR expression)
    | -> EXPR
    ;

r12
    : ID (',' ID)* -> ID+
    ;

r13
    : type ID (',' ID)* ';' -> ^(type ID+)
    ;

r14
    :   expression? statement* type+
        -> ^(EXPR expression? statement* type+)
    ;

r15
    : INT -> INT INT
    ;

r16
    : 'int' ID (',' ID)* -> ^('int' ID)+
    ;

r17
    : 'for' '(' start=statement ';' expression ';' next=statement ')' statement
        -> ^('for' $start expression $next statement)
    ;

r18
    : t='for' -> ^(BLOCK)
    ;

r19
    : t='for' -> ^(BLOCK[$t])
    ;

r20
    : t='for' -> ^(BLOCK[$t,"FOR"])
    ;

r21
    : t='for' -> BLOCK
    ;

r22
    : t='for' -> BLOCK[$t]
    ;

r23
    : t='for' -> BLOCK[$t,"FOR"]
    ;

r24
    : r=statement expression -> ^($r expression)
    ;

r25
    : r+=statement (',' r+=statement)+ expression -> ^($r expression)
    ;

r26
    : r+=statement (',' r+=statement)+ -> ^(BLOCK $r+)
    ;

r27
    : r=statement expression -> ^($r ^($r expression))
    ;

r28
    : ('foo28a'|'foo28b') ->
    ;

r29
    : (r+=statement)* -> ^(BLOCK $r+)
    ;

r30
    : statement* -> ^(BLOCK statement?)
    ;

expression
    : r1
    ;

statement
    : 'fooze'
    | 'fooze2'
    ;

type
    : 'int'
    | 'bool'
    ;

ID : 'a'..'z' + ;
INT : '0'..'9' +;
WS: (' ' | '\n' | '\t')+ {$channel = HIDDEN;};

