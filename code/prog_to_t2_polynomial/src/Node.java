import java.util.Vector;

public class Node
{
    public static Vector<Node> allNodes=new Vector<>();

    int id;
    Node par;
    int beginIndex,endIndex;
    String type,rule;
    Vector<Node> children;
    String varName;
    Polynomial expr;
    PolynomialPredicate preCondition;

    boolean inLoop;

    Vector<PolynomialPredicate> guard;

    Node(Node par,int beginIndex,int endIndex,String type,String rule,boolean inLoop)
    {
        allNodes.add(this);
        id=allNodes.size()-1;
        this.par=par;
        this.beginIndex=beginIndex;
        this.endIndex=endIndex;
        this.type=type;
        this.rule=rule;
        this.inLoop=inLoop;
        children=new Vector<>();
        preCondition=null;

        if(par!=null)
            par.children.add(this);

        if(type.equals("bexpr") || type.equals("literal"))
            guard=new Vector<>();
        else
            guard=null;

        varName=null;
        expr=null;
    }

    public String toString()
    {
        String ret="";
        ret+="Node #"+id+"\n";
        if(par!=null)
            ret+="Par: "+par.id+"\n";
        else
            ret+="Par: null\n";
        ret+="beginIndex="+beginIndex+"\t"+"endIndex="+endIndex+"\n";
        ret+="type: "+type+"\n";
        ret+="rule: "+rule+"\n";
        ret+="inLoop: "+inLoop;

        if(type.equals("expr") || type.equals("term"))
            ret+="\nexpr: "+expr.toString();
        if(type.equals("bexpr"))
        {
            ret+="\nguard: ";
            for (PolynomialPredicate lp : guard)
                ret += lp + " or ";
        }
        if(type.equals("stmt") && rule.equals("assignment"))
        {
            if(expr!=null)
                ret += "\nassignment: " + varName + ":=" + expr.toString();
            else
                ret += "\nassignment: " + varName + ":= nondet()";
        }
        if(preCondition!=null)
            ret+="\npre-condition:"+preCondition;

        return ret;
    }
}
