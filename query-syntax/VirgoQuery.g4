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
           | IDENTIFIER
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

search_string : LPAREN search_string RPAREN
              | search_string boolean_op search_string
              | search_string search_string
              | search_part
              | LPAREN
              | RPAREN 
              ;

search_part : QUOTE search_part QUOTE
            | search_part SEARCH_WORD
            | SEARCH_WORD
            ;

