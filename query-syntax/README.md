Generate the java version of the source code for the query parser using the following command:

    java -cp "./tool/antlr-4.7.2-complete.jar" org.antlr.v4.Tool -visitor -no-listener -o java/generated/edu/virginia/virgo  -package edu.virginia.virgo VirgoQueryLexer.g4 VirgoQuery.g4

Compile the generated java source code via the following command:
 
     mkdir bin
     javac -sourcepath ./java/generated/:./java/test -cp ./tool/antlr-4.7.2-complete.jar -d ../bin -encoding UTF-8  ./java/generated/edu/virginia/virgo/*.java ./java/test/edu/virginia/virgo/*.java

Run the generated java code via the following command:

     java -cp "./tool/antlr-4.7.2-complete.jar;./bin" edu.virginia.virgo.Main

Generate the Go version of the parser via these commands:

    java -Xmx500M -cp ./tool/antlr-4.7.2-complete.jar org.antlr.v4.Tool -o v4parser -package v4parser -visitor -listener -Dlanguage=Go VirgoQueryLexer.g4 VirgoQuery.g4
    sed -i 's,/antlr/antlr4,/uvalib/antlr4,g' v4parser/*.go   # to point to the locally-maintained antlr fork with golang target performance improvements
    mv v4parser/* /path/to/virgo4-parser/v4parser/  # to update separate virgo4-parser repo with new code
    rmdir v4parser
