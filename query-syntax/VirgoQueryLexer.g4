// Define a grammar called Query
lexer grammar VirgoQueryLexer;
fragment WS : [ \t\r\n]+;
LPAREN : '(' ;
RPAREN : ')' ;

BOOLEAN : ('AND'|'OR'|'NOT') ;
COLON : ':' ;
TITLE : 'title';
AUTHOR : 'author';
SUBJECT : 'subject';
KEYWORD : 'keyword';
LBRACE : '{' -> pushMode(SEARCH);
WS1: WS -> skip;

mode SEARCH;

QUOTE : '"' ;
LPAREN2 : '(' ->type(LPAREN);
RPAREN2 : ')' ->type(RPAREN);
LBRACKET : '[' ;
RBRACKET : ']' ;
COMMA : ',' ;
RBRACE : '}' -> popMode;
BOOLEAN1 : ('AND'|'OR'|'NOT') ->type(BOOLEAN) ;
SEARCH_WORD :  [\p{L}\p{Nl}\p{Nd}]+ ;
WS2: WS -> skip;


