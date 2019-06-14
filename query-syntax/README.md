Generate the java version of the source code for the query parser using the following command:

java -cp "./tool/antlr-4.7.2-complete.jar" org.antlr.v4.Tool -visitor -no-listener -o java/generated/edu/virginia/virgo  -package edu.virginia.virgo VirgoQueryLexer.g4 VirgoQuery.g4

Compile the generated java source code via the following command:
 
 mkdir bin
 javac -sourcepath ./java/generated/:./java/test -cp ./tool/antlr-4.7.2-complete.jar -d ../bin -encoding UTF-8  ./java/generated/edu/virginia/virgo/*.java ./java/test/edu/virginia/virgo/*.java

Run the generated java code via the following command:

 java -cp "./tool/antlr-4.7.2-complete.jar;./bin" edu.virginia.virgo.Main

Generate the Go version of the parser via this command:

java -Xmx500M -cp "./tool/antlr-4.7.2-complete.jar" org.antlr.v4.Tool -o parser -package parser -visitor -no-listener -Dlanguage=Go VirgoQueryLexer.g4 VirgoQuery.g4

This will include the package declaration "parser" and will place the generated code in the subdirectory named "parser" 
