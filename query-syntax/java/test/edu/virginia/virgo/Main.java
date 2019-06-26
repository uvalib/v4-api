package edu.virginia.virgo;
import java.nio.CharBuffer;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CodePointBuffer;
import org.antlr.v4.runtime.CodePointCharStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;

public class Main {
    public static void main(String[] args) throws Exception {
        
        String test_strs[]  = {
/* not yet supported       "title:{(time OR fruit) AND flies}", */
                           "title:{bannanas}",
                           "keyword:{digby OR duncan}",
                           "keyword:{digby AND duncan}",
                           "author:{Zhongguo Zang xue chu ban she  (publisher)}", 
                           "author:{ ʼJam-dbyangs-nyi-ma }",
                           "title:{Tragicheskai͡a istorii͡a kavkazskikh }",
                           "author:{Немирович-Данченко, Василий Иванович}",
                           "( title : {\"susan sontag\" OR (music title)}   AND keyword:{ Maunsell } ) OR author:{ liberty }" ,
                           " author: {lovecraft} AND title: {madness NOT dunwich}",
                           "identifier:{35007007606860  OR 9780754645733 OR 38083649 OR 2001020407  OR u5670758 OR \"KJE5602.C73 2012\"}",
                           "title:{A = B}",
                           "date:{1945}",                 // _query_:"{!lucene df=published_daterange}(1945)"
                           "date:{1945/12/07 TO 1949}",   // _query_:"{!lucene df=published_daterange}([1945-12-07 TO 1949])"
                           "date:{BEFORE 1945-12-06}",    // _query_:"{!lucene df=published_daterange}([* TO 1945-12-06])"
                           "date:{AFTER 1945}",           // _query_:"{!lucene df=published_daterange}([1945 TO *])"
                           "date:{<1945} AND date:{>1932} AND author:{Shelly}", //  ( (_query_:"{!lucene df=published_daterange}([* TO 1945])" AND _query_:"{!lucene df=published_daterange}([1932 TO *])")  AND _query_:"{!edismax qf=$author_qf pf=$author_pf}(Shelly)") 
                           };
         
//        Reader reader = new BufferedReader(new FileReader(args[0]));
//        ScriptLexer lexer = new ScriptLexer(reader);
        
        for (String test : test_strs)
        {
            CharBuffer cb = CharBuffer.wrap(test.toCharArray());
            CodePointBuffer cpb = CodePointBuffer.withChars(cb);
            CharStream cs = CodePointCharStream.fromBuffer(cpb);
    
            VirgoQueryLexer lexer = new VirgoQueryLexer(cs);
            
           
            for (Token token = lexer.nextToken();
                    token.getType() != Token.EOF;
                    token = lexer.nextToken())
            {
                System.out.println("type : " + lexer.getVocabulary().getSymbolicName(token.getType()) + "   value: "+ token.getText());
            }
            
            cb = CharBuffer.wrap(test.toCharArray());
            cpb = CodePointBuffer.withChars(cb);
            cs = CodePointCharStream.fromBuffer(cpb);
    
            lexer = new VirgoQueryLexer(cs);
            
           
            CommonTokenStream tokens = new CommonTokenStream(lexer);
            VirgoQuery parser = new VirgoQuery(new CommonTokenStream(lexer));
            
            
            
            ParseTree tree = parser.query();
    //        ParseTreeWalker.DEFAULT.walk(new SolrListener(), tree);
            SimpleVisitor visitor = new SimpleVisitor();
            Value traverseResult = visitor.visit(tree);
            System.out.println(traverseResult.asString());
        }

        
//        SolrVisitor solrVisitor = new SolrVisitor();
//        Value traverseResult2 = solrVisitor.visit(tree);
//        System.out.println(traverseResult2.asString());
    }
}