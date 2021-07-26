public class Transition    //from "v.first" to "v.second" with guard "g" and update "varName := update"
{
    PairNode v;
    PolynomialPredicate guard;

    String varName;
    Polynomial update;  //when varName is not null but update is null then 'varName \in (-inf,inf)' is the assignment

    String type;


    Transition(Node a,Node b,String type)
    {

        v=new PairNode(a,b);
        this.type=type;
        guard=null;
        varName=null;
        update=null;
    }

    Transition(Node a,Node b,String type,PolynomialPredicate guard)
    {
        v=new PairNode(a,b);
        this.type=type;
        this.guard=guard;
        varName=null;
        update=null;
    }

    Transition(Node a,Node b,String type,String varName,Polynomial update)
    {
        v=new PairNode(a,b);
        this.type=type;
        this.varName=varName;
        this.update=update;

        guard=null;
    }


    public String toString()
    {
        String res="";
        res+="FROM: "+v.first.id+";";
        if(guard!=null)
        {
            for(Polynomial lc:guard.exprs)
                if(lc!=null)
                    res += "\nassume(" + lc + ">=0);";
                else
                    res+="\nassume(nondet()!=0)";
        }
        else if(type.equals("if*-then"))
            ; //it will be handled in T2 parser/cfg maker
        if(update!=null)
            res+="\n"+varName+" := "+update.toString()+";";
        else if(varName!=null)
            res+="\n"+varName+" := nondet();";
        res+="\nTO: "+v.second.id+";";
        return res;
    }
}
