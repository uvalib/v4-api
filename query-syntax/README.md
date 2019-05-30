Generate the source code for the query parser using the following command:

java -Xmx500M -cp "./tool/antlr-4.7.2-complete.jar" org.antlr.v4.Tool -o parser -package parser -visitor -no-listener   -Dlanguage=Go VirgoQueryLexer.g4 VirgoQuery.g4


This will include the package declaration "parser" and will place the generated code in the subdirectory named "parser" 
