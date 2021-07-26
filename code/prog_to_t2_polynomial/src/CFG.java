import java.util.Vector;

public class CFG
{
    public static Node startNode,endNode;
    public static Vector<Transition> edges=new Vector<>();

    public static PairNode make(Node cur) throws Exception    //makes the CFG of cur and returns the first and last statement.
    {
        if(cur.type.equals("stmtlist"))
        {
            Vector<PairNode> pairs=new Vector<>();
            for(int i=0;i<cur.children.size();i++)
            {
                PairNode p= make(cur.children.elementAt(i));
                pairs.add(p);
            }

            PairNode ret=new PairNode(pairs.firstElement().first,pairs.lastElement().second);

            boolean isStillFirst=false;
            for(int i=1;i<pairs.size();i++)
            {
                Transition t=getTransition(pairs.elementAt(i-1).second, pairs.elementAt(i).first,"consecution");
                edges.add(t);
            }
            return ret;
        }
        else if(cur.type.equals("stmt"))
        {
            if(cur.rule.equals("if*"))
            {
                Node ifNode=cur;
                Node thenNode=cur.children.elementAt(0);
                Node elseNode=cur.children.elementAt(1);
                Node fiNode=cur.children.elementAt(2);

                PairNode p1= make(thenNode);
                PairNode p2= make(elseNode);

                Transition t1=getTransition(ifNode,p1.first,"if*-then");
                Transition t2=getTransition(ifNode,p2.first,"if*-else");
                Transition t3=getTransition(p1.second,fiNode,"then-fi*");
                Transition t4=getTransition(p2.second,fiNode,"else-fi*");

                edges.add(t1);
                edges.add(t2);
                edges.add(t3);
                edges.add(t4);

                return new PairNode(ifNode,fiNode);
            }
            else if(cur.rule.equals("affif"))
            {
                Node ifNode=cur;
                Node bexprNode=cur.children.elementAt(0);
                Node thenNode=cur.children.elementAt(1);
                Node elseNode=cur.children.elementAt(2);
                Node fiNode=cur.children.elementAt(3);

                PairNode p1= make(thenNode);
                PairNode p2= make(elseNode);

                Vector<PolynomialPredicate> guards=bexprNode.guard;

                for(PolynomialPredicate lp:guards)
                {
                    Transition t = new Transition(ifNode, p1.first, "if-then",lp);
                    edges.add(t);
                }

                Vector<PolynomialPredicate> notGuards=PolynomialPredicate.negate(guards,0);
                for(PolynomialPredicate lp:notGuards)
                {
                    Transition t= new Transition(ifNode,p2.first,"if-else",lp);
                    edges.add(t);
                }

                Transition t=getTransition(p1.second,fiNode,"then-fi");
                edges.add(t);
                t=getTransition(p2.second,fiNode,"else-fi");
                edges.add(t);

                return new PairNode(ifNode,fiNode);
            }
            else if(cur.rule.equals("while"))
            {
                Node whileNode=cur;
                Node bexprNode=cur.children.elementAt(0);
                Node doNode=cur.children.elementAt(1);
                Node odNode=cur.children.elementAt(2);

                PairNode p= make(doNode);

                Vector<PolynomialPredicate> guards=bexprNode.guard;
                for(PolynomialPredicate lp:guards)
                {
                    Transition t = new Transition(whileNode, p.first, "while-do", lp);
                    edges.add(t);
                }
                edges.add(getTransition(p.second,whileNode,"do-while"));
                Vector<PolynomialPredicate> notGuards=PolynomialPredicate.negate(guards,0);
                for(PolynomialPredicate lp:notGuards)
                {
                    Transition t=new Transition(whileNode,odNode,"while-od",lp);
                    edges.add(t);
                }

                return new PairNode(whileNode,odNode);
            }
            else
                return new PairNode(cur,cur);
        }
        else
            throw new Exception("requested CFG for node #"+cur.id+"which is of type:"+cur.type);

    }
    public static Transition getTransition(Node v,Node u,String type)
    {
        Transition t=new Transition(v,u,type);
        if(v.type.equals("stmt") && v.rule.equals("assignment"))
        {
            t.varName=v.varName;
            t.update=v.expr;
        }
        return t;
    }
}
