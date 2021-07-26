import java.util.Map;
import java.util.TreeMap;
import java.util.Vector;

public class CFGNode
{
    public static Vector<CFGNode> allCFGNodes=new Vector<>();
    public static Map<Integer,CFGNode> idToNode=new TreeMap<>();
    public static int greaTestNodeIndex=0;

    public Vector<Transition> out;
    public Vector<Transition> rout; //outgoing transitions in BCFG
    int id;
    boolean isCutPoint,visited;

    Vector<PolynomialPredicate> inv;
    Vector<PolynomialPredicate> computedInv;
    PolynomialPredicate preCondition;

    CFGNode(int ind)
    {
        id = ind;
        idToNode.put(ind, this);
        isCutPoint=visited=false;
        out = new Vector<>();
        rout = new Vector<>();
        allCFGNodes.add(this);
        inv = new Vector<>();
        computedInv = new Vector<>();
        preCondition = null;
        if(ind>greaTestNodeIndex)
            greaTestNodeIndex=ind;
    }
    void addTransition(Transition t)
    {
        out.add(t);
    }

    void addNecessaryNondet()
    {
        boolean hasEqual=false;
        Vector <Vector<Transition>> groups= new Vector<>();
        for (Transition t : out)
            if(!t.hasGroup)
            {
                Vector<Transition> g=new Vector<>();
                for(Transition tp:out)
                    if (t.detGuard.equalsLogic(tp.detGuard))
                    {
                        tp.hasGroup=true;
                        g.add(tp);
                    }
//                for(Transition tmp:g)
//                    System.err.println("from: "+id+" to "+tmp.u.id+" "+g.size());
                if(g.size()==1)
                    t.hasGroup = false;
                else
                    groups.add(g);
            }
        for(int i=0;i<out.size();i++)
            if(out.elementAt(i).hasGroup)
            {
                Transition.allTransitions.removeElement(out.elementAt(i));
                out.removeElementAt(i);
                i--;
            }

        for(Vector<Transition> g:groups)
        {
//            System.err.println("----------------");
//            for(Transition tau:g)
//                System.err.println("transition from "+tau.v.id+" to: "+tau.u.id);
//            System.err.println("----------------");
            PolynomialPredicate commonGuard=g.firstElement().detGuard.deepCopy();

            CFGNode n=new CFGNode(greaTestNodeIndex+1);
            String nontdetTmp="_tmp_";
            Parser.allVars.add("_tmp_");

            for(int i=0;i<g.size();i++)
            {
                Transition t = new Transition(n,g.elementAt(i).u);
                t.varName=g.elementAt(i).varName;
                t.update=g.elementAt(i).update;
                t.nondetGuard=g.elementAt(i).nondetGuard;

                Polynomial lower = new Polynomial("_tmp_", Rational.one);
                lower.add(Monomial.one, new Rational(-i, 1));  // _tmp_ >= i

                Polynomial upper = new Polynomial("_tmp_", Rational.negate(Rational.one));
                upper.add(Monomial.one, new Rational(i, 1)); //  _tmp_ <= i

                if (i == 0)
                    t.detGuard.add(upper);    //t <= 0
                else if (i == g.size() - 1)
                    t.detGuard.add(lower);    //t >= out.size()-1
                else
                {
                    t.detGuard.add(upper);   // t >= i
                    t.detGuard.add(lower);   // t <= i
                }
                t.addToGraph();
            }

            Transition t = new Transition(this, n);
            t.detGuard.add(commonGuard);

            String nondetr="_r_"+Parser.nondetCount;
            t.varName.add(nontdetTmp);
            t.update.add(new Polynomial(nondetr));
            Parser.nondetCount++;
            t.addToGraph();
        }
    }

    void addTerminalTransitions()
    {
//        if(out.size()>0)
//        {
//            Vector<LinearPredicate> predicates = new Vector<>();
//            int cnt=0;
//            for (Transition t : out)
//            {
//                LinearPredicate lp=new LinearPredicate();
//                for(LinearCombination lc:t.detGuard.exprs)
//                    lp.add(lc.deepCopy());
//                predicates.add(lp);
//                cnt+=lp.exprs.size();
//            }
//            if(cnt==0)
//                return;
//            Vector<LinearPredicate> negation = LinearPredicate.negate(predicates);
//            CFGNode term=idToNode.get(-2);
//            for(LinearPredicate lp:negation)
//            {
//                Transition tau=new Transition(this,term);
//                tau.detGuard =lp;
//                tau.addToGraph();
//            }
//        }
//        else
        if(out.size()==0)
        {
            CFGNode term=idToNode.get(-2);
            Transition tau=new Transition(this,term);
            tau.addToGraph();
        }
    }

    public static CFGNode addNode(int x)
    {
        if(idToNode.containsKey(x))
            return idToNode.get(x);
        CFGNode n=new CFGNode(x);
        return n;
    }

    public static CFGNode getCFGNode(int x)
    {
        return idToNode.get(x);
    }
}
