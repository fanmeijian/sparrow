// Expr.g4 (增强版)
grammar Expr;

expr
    : expr AND expr         # AndExpr
    | expr OR expr          # OrExpr
    | NOT expr              # NotExpr
    | '(' expr ')'          # ParenExpr
    | field comparator value # CompareExpr
    | field IS NULL         # IsNullExpr
    | field IS NOT NULL     # IsNotNullExpr
    | field IN '(' valueList ')' # InExpr
    ;

field       : ID ('.' ID)* ;
comparator  : '=' | '!=' | '<' | '<=' | '>' | '>=' | 'like' ;
value       : STRING | NUMBER ;
valueList   : value (',' value)* ;

AND         : 'and' ;
OR          : 'or' ;
NOT         : 'not' ;
IS          : 'is' ;
NULL        : 'null' ;
IN          : 'in' ;

ID          : [a-zA-Z_][a-zA-Z0-9_]* ;
STRING      : '\'' (~['\\] | '\\' .)* '\'' ;
NUMBER      : [0-9]+ ('.' [0-9]+)? ;

WS          : [ \t\r\n]+ -> skip ;