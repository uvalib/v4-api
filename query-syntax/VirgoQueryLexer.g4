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
DATE : 'date' -> pushMode(DATE_MODE);
LBRACE : '{' -> pushMode(SEARCH);
WS1: WS -> skip;
ERROR_CHARACTER: .;

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
ERROR_CHARACTER2: . ->type(ERROR_CHARACTER);

mode DATE_MODE;

COLON2 : ':' ->type(COLON);
TO : ('TO'|'--');
AFTER : ('AFTER'|'>');
BEFORE : ('BEFORE'|'<');
DATE_STRING : ([-/0-9]+) ;
LBRACE2 : '{' -> type(LBRACE);
RBRACE1 : '}' -> popMode, type(RBRACE);
WS3: WS -> skip;
ERROR_CHARACTER3: . ->type(ERROR_CHARACTER);

