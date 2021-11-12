Optional: get desired version of antlr-complete jar from the following site, and place in tool/ directory:

    https://www.antlr.org/download/index.html

Set version of antlr jar in tool directory to use for the remaining commands, e.g. "4.7.2", "4.9.3", etc.

    version="4.9.3"

Java target:

    # generate java v4 parser source code:
    rm -rf java/generated
    java -cp "./tool/antlr-${version}-complete.jar" org.antlr.v4.Tool -visitor -no-listener -o java/generated/edu/virginia/virgo -package edu.virginia.virgo *.g4
    
    # compile java test code:
    rm -rf bin ; mkdir bin
    javac -sourcepath "./java/generated/:./java/test" -cp "./tool/antlr-${version}-complete.jar" -d bin -encoding UTF-8 ./java/{generated,test}/edu/virginia/virgo/*.java
    
    # run java test code:
    java -cp "./tool/antlr-${version}-complete.jar:./bin" edu.virginia.virgo.Main

Golang target:

    # generate golang v4 parser source code:
    java -Xmx500M -cp "./tool/antlr-${version}-complete.jar" org.antlr.v4.Tool -o v4parser -package v4parser -visitor -listener -Dlanguage=Go *.g4
    
    # update separate virgo4-parser repo with new antlr-specific parser code
    mv -f v4parser/* /path/to/virgo4-parser/v4parser/
    rmdir v4parser
