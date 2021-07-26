import java.util.Vector;

public class Node
{
    public static Vector<Node> allNodes=new Vector<>();

    int id;
    Node par;
    int beginIndex,endIndex;
    String type;
    Vector<Node> children;
    String varName;
    Polynomial expr;
    String nondetLow,nondetUp;
    Vector <PolynomialPredicate> guard;

    Node(Node par,int beginIndex,int endIndex,String type)
    {
        allNodes.add(this);
        id=allNodes.size()-1;
        this.par=par;
        this.beginIndex=beginIndex;
        this.endIndex=endIndex;
        this.type=type;
        this.expr=null;
        this.guard=new Vector<>();
        children=new Vector<>();

        if(par!=null)
            par.children.add(this);
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
        ret+="guard:";
        for(PolynomialPredicate pp:guard)
            ret+=pp.toString()+"\n";
        return ret;
    }
}
