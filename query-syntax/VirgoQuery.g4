// Define a grammar called Query
parser grammar VirgoQuery;

options { tokenVocab=VirgoQueryLexer;  }

query : query_parts EOF 
      ;

query_parts :  query_parts boolean_op query_parts 
            |  LPAREN query_parts RPAREN 
            |  field_query 
            ;

field_query : field_type COLON LBRACE search_string RBRACE
            | field_type COLON LBRACE RBRACE
            | range_field_type COLON LBRACE range_search_string RBRACE
            ;

field_type : TITLE
           | AUTHOR
           | SUBJECT
           | KEYWORD
           | PUBLISHED
           | IDENTIFIER
           | FILTER
           ;

range_field_type : DATE
                 ;

boolean_op :  BOOLEAN
           ;

range_search_string : date_string TO date_string
                    | BEFORE date_string
                    | AFTER date_string
                    | date_string
                    ; 

date_string : DATE_STRING ;

search_string : QUOTE QUOTE_STR QUOTE
              | LPAREN search_string RPAREN
              | search_string boolean_op search_string
              | search_string search_string
              | search_part
              ;

search_part : search_part SEARCH_WORD
            | SEARCH_WORD
            ;

quoted_search_part : quoted_search_part SEARCH_WORD
                   | quoted_search_part LPAREN
                   | quoted_search_part RPAREN
                   | quoted_search_part boolean_op
                   | SEARCH_WORD
                   | LPAREN
                   | RPAREN
                   | boolean_op
                   ;