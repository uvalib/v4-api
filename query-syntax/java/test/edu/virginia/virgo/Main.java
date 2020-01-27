package edu.virginia.virgo;
import java.nio.CharBuffer;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;

import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.Parser;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.tree.Trees;

public class Main {
    
    static class MyErrorListener extends BaseErrorListener {
        @Override
        public void syntaxError( Recognizer<?, ?> recognizer, Object offendingSymbol, int line, int charPositionInLine,
                                 String msg, RecognitionException e ) {
          // method arguments should be used for more detailed report
          if (offendingSymbol != null && offendingSymbol instanceof Token)
              throw new RuntimeException("Parser error occurred at Symbol " + ((Token)offendingSymbol).getText() + " -- message is :" + msg);
          else if (msg != null)
              throw new RuntimeException("Lexer error occurred -- message is :" + msg);
          else
              throw new RuntimeException("Syntax error occurred -- deal with it.");
        }
      };
    public static boolean verbose = false;
    
    public static void main(String[] args) throws Exception {

        String test_strs[]  = {
/* not yet supported       "title:{(time OR fruit) AND flies}", */
                "keyword: {\"transmutation (chemistry)\"}", 
                "keyword: {\"transmutation AND (chemistry)\"}",
                "keyword: {(\"Death AND taxes\" OR \"war AND Peace\") AND (cats OR dogs)}",
                "keyword: {(calico OR \"tortoise shell\") AND (cats OR dogs)}",
                " keyword : { (calico OR \"tortoise shell\") AND cats } ",
               "keyword:{}",
                " keyword : { (calico OR tortoise shell) AND cats } ",
                " keyword : { (\"tortoise shell\" OR calico) AND cats } ",
                           "keyword:{cincinnati, ohio (home of the :reds:)}",
                           "title:{bannanas}",
                           "title:{bananas ( a fruit }",  // should be error
                           "title:{bananas a fruit ) }",  // should be error
                           "keyword:{digby OR duncan}",
                           "keyword:{digby AND duncan}",
                           "author:{Zhongguo Zang xue chu ban she  (publisher)}", 
                           "author:{ ʼJam-dbyangs-nyi-ma }",
                           "title:{Tragicheskai͡a istorii͡a kavkazskikh }",
                           "author:{Немирович-Данченко, Василий Иванович}",
                           "title : {\"susan sontag\" OR (music title)}   AND keyword:{ Maunsell }" ,
                           "( title : {\"susan sontag\" OR (music title)}   AND keyword:{ Maunsell } ) OR author:{ liberty }" ,
                           "title : {\"susan sontag\" OR (music title)}   AND ( keyword:{ Maunsell } OR author:{ liberty } )" ,
                           " author: {lovecraft} AND title: {madness NOT dunwich}",
                           "identifier:{35007007606860  OR 9780754645733 OR 38083649 OR 2001020407  OR u5670758 OR \"KJE5602.C73 2012\"}",
                           "title:{A = B}",
                           "date:{1945}",                 // _query_:"{!lucene df=published_daterange}(1945)"
                           "date:{BadDate}",                 // _query_:"{!lucene df=published_daterange}(1945)"
                           "date:{1945/12/07 TO 1949}",   // _query_:"{!lucene df=published_daterange}([1945-12-07 TO 1949])"
                           "date:{BEFORE 1945-12-06}",    // _query_:"{!lucene df=published_daterange}([* TO 1945-12-06])"
                           "date:{AFTER 1945}",           // _query_:"{!lucene df=published_daterange}([1945 TO *])"
                           "date:{<1945} AND date:{>1932} AND author:{Shelly}", //  ( (_query_:"{!lucene df=published_daterange}([* TO 1945])" AND _query_:"{!lucene df=published_daterange}([1932 TO *])")  AND _query_:"{!edismax qf=$author_qf pf=$author_pf}(Shelly)")
                           "garbage:{1954}",   // should be error
                           "rubbish:{bananas}",  // should be error
                           "date:{1932 TO 1945} HELLOOOOO author:{Shelly}",  // should be error
                           "keyword:{triceratops OR flameproof}",
                           };

//        Reader reader = new BufferedReader(new FileReader(args[0]));
//        ScriptLexer lexer = new ScriptLexer(reader);
        boolean showTokens = false;
        boolean showInput = false;
        boolean parseSolr = false;
        boolean parseEDS = false;
        boolean showTree = false;
        int offset = 0;
        if (args.length >= 1 && args[0].equals("-v"))
        {
            verbose = true;
            offset = 1;
        }
        if (args.length >= offset+1)
        {
            if (args[offset].contains("input"))  showInput = true;
            if (args[offset].contains("tokens")) showTokens = true;
            if (args[offset].contains("solr"))   parseSolr = true;
            if (args[offset].contains("eds"))    parseEDS = true;
            if (args[offset].contains("tree"))   showTree = true;
        }
        else 
        {
        	showInput = true;
            parseSolr = true;
            //parseEDS = true;
        }

        for (String test : test_strs)
        {
            if (showInput)   System.out.println(test);
            if (showTokens)  showTokens(test);
            if (showTree)
            {
                try { 
                    String result = showParseTree(test);
                    System.out.println(result);
                }
                catch (RuntimeException re)
                {
                    System.out.println("Error on query: "+ test);
                    System.out.println(re.toString());
                }
            }
            if (parseSolr)
            {
                try { 
                    String result = parseForSolr(test);
                    System.out.println(result);
                }
                catch (RuntimeException re)
                {
                    System.out.println("Error on query: "+ test);
                    System.out.println(re.toString());
                }
            }
            if (parseEDS)
            {
                try { 
                    HashMap<String, String> result = parseForEDSasHash(test);
                    String indent = "";
                    for (String key : result.keySet())
                    {
                        System.out.println(indent + key + "=" + result.get(key));
                        indent = "  ";
                    }
                }
                catch (RuntimeException re)
                {
                    System.out.println("Error on query: "+ test);
                    System.out.println(re.toString());
                }
            }
        }
    }

    public static String printSyntaxTree(Parser parser, ParseTree root) 
    {
        StringBuilder buf = new StringBuilder();
        recursive(root, buf, 0, Arrays.asList(parser.getRuleNames()));
        return buf.toString();
    }

    private static void recursive(ParseTree aRoot, StringBuilder buf, int offset, List<String> ruleNames) 
    {
        for (int i = 0; i < offset; i++) 
        {
            buf.append("  ");
        }
        buf.append(Trees.getNodeText(aRoot, ruleNames)).append("\n");
        if (aRoot instanceof ParserRuleContext) 
        {
            ParserRuleContext prc = (ParserRuleContext) aRoot;
            if (prc.children != null) 
            {
                for (ParseTree child : prc.children) 
                {
                    recursive(child, buf, offset + 1, ruleNames);
                }
            }
        }
    }

    public static String parseForSolr(String input)
    {
        MyErrorListener errorListener = new MyErrorListener();

        CharBuffer cb = CharBuffer.wrap(input.toCharArray());
        CodePointBuffer cpb = CodePointBuffer.withChars(cb);
        CharStream cs = CodePointCharStream.fromBuffer(cpb);

        VirgoQueryLexer lexer = new VirgoQueryLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener( errorListener );

        VirgoQuery parser = new VirgoQuery(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener( errorListener );

        ParseTree tree = parser.query();
        SimpleVisitor visitor = new SimpleVisitor(verbose);
        Value traverseResult = visitor.visit(tree);
        return traverseResult.asString();
    }

    public static String showParseTree(String input)
    {
        MyErrorListener errorListener = new MyErrorListener();

        CharBuffer cb = CharBuffer.wrap(input.toCharArray());
        CodePointBuffer cpb = CodePointBuffer.withChars(cb);
        CharStream cs = CodePointCharStream.fromBuffer(cpb);

        VirgoQueryLexer lexer = new VirgoQueryLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener( errorListener );

        VirgoQuery parser = new VirgoQuery(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener( errorListener );

        ParseTree tree = parser.query();
        String pretty = printSyntaxTree(parser, tree);
        return(pretty); 
    }

    public static String parseForEDS(String input)
    {
        MyErrorListener errorListener = new MyErrorListener();

        CharBuffer cb = CharBuffer.wrap(input.toCharArray());
        CodePointBuffer cpb = CodePointBuffer.withChars(cb);
        CharStream cs = CodePointCharStream.fromBuffer(cpb);

        VirgoQueryLexer lexer = new VirgoQueryLexer(cs);
        lexer.removeErrorListeners();
        lexer.addErrorListener( errorListener );

        VirgoQuery parser = new VirgoQuery(new CommonTokenStream(lexer));
        parser.removeErrorListeners();
        parser.addErrorListener( errorListener );

        ParseTree tree = parser.query();
        EDSVisitor visitor = new EDSVisitor();
        Value traverseResult = visitor.visit(tree);
        return traverseResult.asString();
    }

    public static HashMap<String,String> parseForEDSasHash(String input)
    {
        String traverseResult = parseForEDS(input);
        String[] parts = traverseResult.split("&&&");
        HashMap<String, String> result = new LinkedHashMap<String, String>();
        for (String part : parts)
        {
            String[] pieces = part.split("[ ]*=[ ]*", 2);
            result.put(pieces[0], pieces[1]);
        }
        return result;
    }

    public static void showTokens(String input)
    {
        CharBuffer cb = CharBuffer.wrap(input.toCharArray());
        CodePointBuffer cpb = CodePointBuffer.withChars(cb);
        CharStream cs = CodePointCharStream.fromBuffer(cpb);

        VirgoQueryLexer lexer = new VirgoQueryLexer(cs);

        for (Token token = lexer.nextToken();
                token.getType() != Token.EOF;
                token = lexer.nextToken())
        {
            System.out.println("type : " + lexer.getVocabulary().getSymbolicName(token.getType()) + "   value: "+ token.getText());
        }
    }
}