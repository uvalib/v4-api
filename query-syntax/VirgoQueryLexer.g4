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
IDENTIFIER : 'identifier';
LBRACE : '{' -> pushMode(SEARCH);
WS1: WS -> skip;

mode SEARCH;

QUOTE : '"' ;
LPAREN2 : '(' ->type(LPAREN);
RPAREN2 : ')' ->type(RPAREN);
LBRACKET : '[' ;
RBRACKET : ']' ;
LBRACE1 : '{' -> pushMode(SEARCH), type(LBRACE);
RBRACE : '}' -> popMode;
BOOLEAN1 : ('AND'|'OR'|'NOT') ->type(BOOLEAN) ;
SEARCH_WORD : (~([{}" \t\r\n]))+ ;
WS2: WS -> skip;


