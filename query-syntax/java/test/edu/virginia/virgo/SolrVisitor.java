package edu.virginia.virgo;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.TerminalNode;


public class SolrVisitor extends VirgoQueryBaseVisitor<Value> {

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Value visitQuery(VirgoQuery.QueryContext ctx) 
    { 
        Value result = ctx.getChild(0).accept(this);
        return(result);
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Value visitQuery_parts(VirgoQuery.Query_partsContext ctx)
    { 
        StringBuilder sb = new StringBuilder();
        //  query_parts : query_parts boolean_op query_parts 
        if (ctx.getChildCount() == 3 && ctx.getChild(1) instanceof VirgoQuery.Boolean_opContext)
        {
            Value query1 = ctx.getChild(0).accept(this);
            Value queryop = ctx.getChild(1).accept(this);
            Value query2 = ctx.getChild(2).accept(this);
            sb.append(" (").append(query1.asString()).append(queryop.asString()).append(query2).append(") ");
            return(new Value(sb.toString()));
        }
        // query_parts : LPAREN query_parts RPAREN
        else if (ctx.getChildCount() == 3 && ctx.getChild(0) instanceof TerminalNode)
        {
            Value query1 = ctx.getChild(1).accept(this);
            sb.append(" (").append(query1.asString()).append(") ");
            return(new Value(sb.toString()));
        }
        // query_parts : field_query
        else 
        {
            Value query1 = ctx.getChild(0).accept(this);
            return(query1);
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override 
    public Value visitField_query(VirgoQuery.Field_queryContext ctx) 
    { 
        // field_query : field_type COLON LBRACE search_string RBRACE
        //   (_query_:"{!edismax qf=$title_qf pf=$title_pf}(certification of teachers )"
        
        StringBuilder sb = new StringBuilder();
        
        Value fieldType = ctx.getChild(0).accept(this);
        Value query = ctx.getChild(3).accept(this);
        expand(sb, fieldType, query);
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
    private void expand(StringBuilder sb, Value fieldType, Value query)
    {
        if (query.isArray())
        {
            Value[] parts = query.asArray();
            sb.append("(");
            expand(sb, fieldType, parts[0]);
            sb.append(parts[1].asString());
            expand(sb, fieldType, parts[2]);
            sb.append(")");
        }
        else
        {
            sb.append("_query_:\"{!edismax").append(fieldType.toString()).append("}(").append(query.toString()).append(")\"");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override 
    public Value visitField_type(VirgoQuery.Field_typeContext ctx) 
    { 
        // field_type : TITLE | AUTHOR | SUBJECT | KEYWORD
        String fieldType = ctx.getChild(0).toString();
        String qf = "";
        String pf = ""; 
        if (fieldType.equals("title") || fieldType.equals("subject") | fieldType.equals("author") )
        {
            qf = " qf=$"+ fieldType + "_qf";
            pf = " pf=$"+ fieldType + "_pf";
        }
        return new Value(qf + pf); 
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Value visitBoolean_op(VirgoQuery.Boolean_opContext ctx) 
    { 
        return new Value(" " + ctx.getText() + " "); 
    }

    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Value visitSearch_string(VirgoQuery.Search_stringContext ctx) 
    { 
        StringBuilder sb = new StringBuilder();
        // search_string : search_string boolean_op search_string
        // n.b.  this returns an array of three objects.   the first or third of the objects could be either a string or an array of three objects 
        if (ctx.getChildCount() == 3 && ctx.getChild(1) instanceof VirgoQuery.Boolean_opContext)
        {
            Value[] result = new Value[3];
            result[0] =  ctx.getChild(0).accept(this);
            result[1] = ctx.getChild(1).accept(this);
            result[2] =  ctx.getChild(2).accept(this);
            return(new Value(result));
        }
        // search_string : LPAREN search_string RPAREN
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
                    Value childResult = c.accept(this);
                    sb.append(childResult.toString());
                }
            }
            return new Value(sb.toString()); 
        }
    }
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of calling
     * {@link #visitChildren} on {@code ctx}.</p>
     */
    @Override public Value visitSearch_part(VirgoQuery.Search_partContext ctx) 
    { 
        // search_part : QUOTE search_part QUOTE
        //             | search_part SEARCH_WORD
        //             | SEARCH_WORD
        //  simply concatenate the SEARCH_WORDS and QUOTES together in the order they occurred with spaces in between 
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < ctx.getChildCount(); i++)  
        { 
            if (i != 0) sb.append(" ");
            {
                ParseTree c = ctx.getChild(i);
                Value childResult = c.accept(this);
                sb.append(childResult.toString());
            }
        }
        return new Value(sb.toString()); 
    }
    
    /**
     * {@inheritDoc}
     *
     * <p>The default implementation returns the result of
     * {@link #defaultResult defaultResult}.</p>
     */
    @Override
    public Value visitTerminal(TerminalNode node) 
    {
        // if a QUOTE is found in a search string, return an escaped quote
        // for any other "terminal" symbol return the text of the symbol.
        if (node.getSymbol().getType() == VirgoQueryLexer.QUOTE) 
            return new Value("\\\"");
        return new Value(node.getText());
    }


}