package edu.virginia.virgo;
import org.antlr.v4.runtime.CommonToken;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.RuleNode;
import org.antlr.v4.runtime.tree.TerminalNode;



public class SimpleVisitor {

    boolean verbose = false;
    
    public SimpleVisitor(boolean verbose)
    {
        setVerbose(verbose);
    }
    
    public void setVerbose(boolean verbose)
    {
        this.verbose = verbose;
    }

    public Value visit(ParseTree tree) 
    {
        if (tree instanceof RuleNode) 
        {
            return visitRuleNode((RuleNode)tree);
        }
        else /*if (tree instanceof TerminalNode) */
        {
            return visitTerminal((TerminalNode) tree);
        }
    }
    
    public Value visitRuleNode(RuleNode node) 
    {
        if (node instanceof VirgoQuery.QueryContext)
        {
            return (visitQuery(node) );
        }
        else if (node instanceof VirgoQuery.Query_partsContext)
        {
            return (visitQuery_parts( node) );
        }
        else if (node instanceof VirgoQuery.Field_queryContext)
        {
            return (visitField_query( node) );
        }
        else if (node instanceof VirgoQuery.Field_typeContext)
        {
            return (visitField_type( node) );
        }
        else if (node instanceof VirgoQuery.Range_field_typeContext)
        {
            return (visitRange_field_type( node) );
        }
        else if (node instanceof VirgoQuery.Range_search_stringContext)
        {
            return (visitRange_search_string( node) );
        }
        else if (node instanceof VirgoQuery.Search_stringContext)
        {
            return (visitSearch_string( node) );
        }
        else // generic action
        { 
            return (visitChildren(node)); 
        }
    }
    
    public Value visitChildren(RuleNode node) 
    {
        Value result = null;
        int n = node.getChildCount();
        for (int i=0; i<n; i++) 
        {
            ParseTree c = node.getChild(i);
            Value childResult = this.visit(c);
            result = aggregateResult(result, childResult);
        }

        return result;
    }


    public Value visitQuery(RuleNode ctx) 
    { 
        Value result = this.visit(ctx.getChild(0));
        return(result);
    }

    private Value visitQuery_parts(RuleNode ctx)
    {
        StringBuilder sb = new StringBuilder();
        //  query_parts : query_parts boolean_op query_parts 
        if (ctx.getChildCount() == 3 && ctx.getChild(1) instanceof VirgoQuery.Boolean_opContext)
        {
            Value query1 = this.visit(ctx.getChild(0));
            Value queryop = this.visit(ctx.getChild(1));
            Value query2 = this.visit(ctx.getChild(2));
            sb.append(" (").append(query1.asString()).append(queryop.asString()).append(query2).append(") ");
            return(new Value(sb.toString()));
        }
        // query_parts : LPAREN query_parts RPAREN
        else if (ctx.getChildCount() == 3 && ctx.getChild(0) instanceof TerminalNode)
        {
            Value query1 = this.visit(ctx.getChild(1));
            sb.append(" (").append(query1.asString()).append(") ");
            return(new Value(sb.toString()));
        }
        // query_parts : field_query
        else 
        {
            Value query1 = this.visit(ctx.getChild(0));
            return(query1);
        }
    }

    public Value visitField_query(RuleNode ctx) 
    { 
        // field_query : field_type COLON LBRACE search_string RBRACE
        //             | range_field_type COLON LBRACE range_search_string RBRACE
        //   (_query_:"{!edismax qf=$title_qf pf=$title_pf}(certification of teachers )"
        
        StringBuilder sb = new StringBuilder();
        
        CommonToken tok;
        String fieldType = this.visit(ctx.getChild(0)).asString();
        String fieldName = ((CommonToken)(ctx.getChild(0).getChild(0).getPayload())).getText();

        if (ctx.getChild(3) instanceof TerminalNode)
        {
            expand(sb, fieldName, fieldType, new Value("*"));
        }
        else 
        {
            Value query = this.visit(ctx.getChild(3));
            expand(sb, fieldName, fieldType, query);
        }
        return new Value(sb.toString()); 
    }

    // This expands the search_string inside a field_query
    // if there are no boolean operators in the search_string it merely takes the words and characters 
    // of the search_string and adds the proper query fragment syntax as required by Solr.
    // e.g. going from this:     title : {susan sontag} 
    //      to this:             _query_:"{!edismax qf=$title_qf pf=$title_pf}(susan sontag)"
    // if there ARE boolean operators in the search_string they need to be (recursively) expanded and 
    // wrapped with parentheses to ensure proper operator precedence
    // e.g. this field_query :   title : {"susan sontag" OR music title} 
    //expands to this:           (_query_:"{!edismax qf=$title_qf pf=$title_pf}(\" susan sontag \")" OR _query_:"{!edismax qf=$title_qf pf=$title_pf}(music title)")
    private void expand(StringBuilder sb, String fieldName, String fieldType, Value query)
    {
        if (verbose)
        {
            System.err.println("==> Expand inStr "+sb.toString()+" for field "+fieldName+", query: "+query);
        }
        if (query.isArray())
        {
            Value[] parts = query.asArray();
            sb.append("(");
            expand(sb, fieldName, fieldType, parts[0]);
            sb.append(parts[1].asString());
            expand(sb, fieldName, fieldType, parts[2]);
            sb.append(")");
        }
        else
        {
            sb.append("_query_:\"{").append(fieldType).append("}(").append(query.toString()).append(")\"");
        }
    }

    public Value visitRange_field_type(RuleNode ctx) 
    { 
        // range_field_type : DATE
        String fieldType = ctx.getChild(0).toString();
        String fieldname = "";
        String df = ""; 
        if (fieldType.equals("date"))
        {
            fieldname = "published_daterange"; 
            df = "!lucene df="+ fieldname;
        }
        return new Value(df); 
    }

    
    public Value visitField_type(RuleNode ctx) 
    { 
        // field_type : TITLE | AUTHOR | SUBJECT | KEYWORD |  IDENTIFIER
        String fieldType = ctx.getChild(0).toString();
        String qf = "";
        String pf = ""; 
        if (fieldType.equals("title") || fieldType.equals("subject") || fieldType.equals("author") || fieldType.equals("identifier"))
        {
            qf = "qf=$"+ fieldType + "_qf";
            pf = " pf=$"+ fieldType + "_pf";
        }
        return new Value("!edismax "+ qf + pf); 
    }
    
    
    public Value visitRange_search_string(RuleNode ctx) 
    { 
        //    range_search_string : date_string TO date_string
        //                        | BEFORE date_string
        //                        | AFTER date_string
        //                        | date_string
        if (ctx.getChildCount() == 1)
        {
            return this.visit(ctx.getChild(0));
        }
        else
        {
            String rangeStart = "*";
            String rangeEnd = "*";
            if (ctx.getChildCount() == 3)
            {
                rangeStart = this.visit(ctx.getChild(0)).asString();
                rangeEnd = this.visit(ctx.getChild(2)).asString();
            }
            else if (ctx.getChildCount() == 2)
            {
                String value = this.visit(ctx.getChild(1)).asString();
                if (ctx.getChild(0) instanceof TerminalNode)
                {
                    TerminalNode tn = (TerminalNode)ctx.getChild(0);
                    if (tn.getSymbol().getType() == VirgoQueryLexer.BEFORE)
                    {
                        rangeEnd = value;
                    }
                    else
                    {
                        rangeStart = value;
                    }
                }
            }
     //       return new Value("[\\\""+rangeStart+"\\\" TO \\\""+rangeEnd+"\\\"]");
            return new Value("["+rangeStart+" TO "+rangeEnd+"]");
        }
    }
    
    public Value visitSearch_string(RuleNode ctx) 
    { 
        StringBuilder sb = new StringBuilder();
        // search_string : search_string boolean_op search_string
        // n.b.  this returns an array of three objects.   the first or third of the objects could be either a string or an array of three objects 
        if (ctx.getChildCount() == 3 && ctx.getChild(1) instanceof VirgoQuery.Boolean_opContext)
        {
            Value[] result = new Value[3];
            result[0] =  this.visit(ctx.getChild(0));
            result[1] =  this.visit(ctx.getChild(1));
            result[2] =  this.visit(ctx.getChild(2));
            return(new Value(result));
        }
        // search_string : LPAREN search_string RPAREN
        else if (ctx.getChildCount() == 3 && ctx.getChild(0) instanceof TerminalNode && 
                ((TerminalNode)ctx.getChild(0)).getSymbol().getType() == VirgoQueryLexer.LPAREN && 
                ctx.getChild(2) instanceof TerminalNode && 
                ((TerminalNode)ctx.getChild(2)).getSymbol().getType() == VirgoQueryLexer.RPAREN )
        {
            ParseTree c = ctx.getChild(1);
            Value childResult = this.visit(c);
            return(childResult);
        }
        //               | search_string search_part
        //               | search_part 
        // for any non-boolean containing search string, simply concatenate the pieces together, with spaces added between them.
        else 
        {
            for (int i = 0; i < ctx.getChildCount(); i++)  
            { 
                if (i != 0) sb.append(" ");
                {
                    ParseTree c = ctx.getChild(i);
                    Value childResult = this.visit(c);
                    sb.append(childResult.toString());
                }
            }
            return new Value(sb.toString()); 
        }
    }

    protected Value aggregateResult(Value aggregate, Value nextResult) 
    {
        if (aggregate != null)
        {
        	if (aggregate.asString().endsWith("\"") || aggregate.asString().endsWith("(") ||
        			nextResult.asString().startsWith("\"") || nextResult.asString().startsWith(")"))
        	{
        		return new Value(aggregate.asString() + nextResult.asString());
        	}
        	else 
        	{
        		return new Value(aggregate.asString() + " " + nextResult.asString());
        	}
        }
        return nextResult;
    }
    
    public Value visitTerminal(TerminalNode node) 
    {
        if (node.getSymbol().getType() == VirgoQueryLexer.QUOTE) 
            return new Value("\\\"");
        else if (node.getSymbol().getType() == VirgoQueryLexer.BOOLEAN)
            return new Value(" "+ node.getText() + " ");
        else if (node.getSymbol().getType() == VirgoQueryLexer.DATE_STRING)
            return new Value(node.getText().replace('/', '-'));
        return new Value(node.getText());
    }
}