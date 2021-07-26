import java.util.Vector;

public class Transition    //from "v.first" to "v.second" with guard "g" and update "varName := update"
{
    public static Vector<Transition> allTransitions=new Vector<>();
    CFGNode v,u;
    PolynomialPredicate detGuard;
    PolynomialPredicate nondetGuard;

    Vector<String> varName;
    Vector<Polynomial> update; //if update[i]=null then varName[i] is non-deterministicly assigned
    boolean hasGroup;


    Transition(CFGNode a,CFGNode b)
    {
        v = a;
        u = b;
        detGuard = new PolynomialPredicate();
        nondetGuard = new PolynomialPredicate();
        varName = new Vector<>();
        update = new Vector<>();
        hasGroup = false;
    }

    void addNondetTemplate()
    {
        for(Polynomial p:update)
        {
            if(p.toString().contains("_r_"))  // lc is a fresh nondet variable
            {
                // qc <= lc <= qc

                Polynomial poly= new Polynomial(Parser.allVars),poly2=poly.deepCopy();

                poly.add(p.negate()); //poly - p <=0
                nondetGuard.add(poly.negate());


                poly2.add(p.negate()); // poly2 - p >=0
                nondetGuard.add(poly2);

            }
        }
    }


    public void addToGraph()
    {
        allTransitions.add(this);
        v.out.add(this);
    }

    public Vector<Transition> reverse() throws Exception  //NOTE: this is only for C-Integer Programs!
    {
        Transition res=new Transition(u,v);
        for(Polynomial lc: detGuard.exprs)
            res.detGuard.add(lc.deepCopy());
        for(Polynomial qc: nondetGuard.exprs)
            res.nondetGuard.add(qc.deepCopy());
        for(int i=0;i<varName.size();i++)
        {
            Polynomial lc=update.elementAt(i);
            String var=varName.elementAt(i);
//            System.err.println(var+" := "+lc.toNormalString());
            if(lc.containsVar(var))
            {
                CFGNode tmp = new CFGNode(CFGNode.greaTestNodeIndex+1);
                Transition utmp=new Transition(u,tmp),tmpv=new Transition(tmp,v);
                Polynomial g1 = new Polynomial(var);
                g1.add(new Monomial("_v_",1),Rational.negate(Rational.one));

                utmp.detGuard.add(g1);
                utmp.detGuard.add(g1.negate());  // x- _v_ ==0
                utmp.varName.add(var);
                utmp.update.add(new Polynomial("_r_"+Parser.nondetCount));
                Parser.nondetCount++;

                Polynomial g2 = new Polynomial("_v_");
                g2.add(lc.negate());

                tmpv.detGuard.add(g2);
                tmpv.detGuard.add(g2.negate());
                tmpv.varName.add("_v_");
                tmpv.update.add(new Polynomial("_r_"+Parser.nondetCount));
                Parser.nondetCount++;

                Vector<Transition> ret= new Vector<>();
                ret.add(utmp);
                ret.add(tmpv);
                return ret;
            }
            else //var is not in lc
            {
                Polynomial g = new Polynomial(var);
                g.add(lc.negate());
                res.detGuard.add(g);
                res.detGuard.add(g.negate()); //var - lc == 0

                res.varName.add(var);
                res.update.add(new Polynomial("_r_"+Parser.nondetCount));
                Parser.nondetCount++;
            }
        }
        Vector<Transition> ret=new Vector<>();
        ret.add(res);
        return ret;
    }

    public Transition deepCopy()
    {
        Transition ret=new Transition(v,u);
        ret.detGuard = detGuard.deepCopy();
        ret.nondetGuard=nondetGuard.deepCopy();
        for(String var:varName)
            ret.varName.add(var.toString());
        for(Polynomial lc:update)
            if(lc!=null)
                ret.update.add(lc.deepCopy());
            else
                ret.update.add(null);
        return ret;
    }

    public String toString()
    {
        String res="";
        res+="from: "+v.id+"\nto: "+u.id+"\n";
        if(detGuard !=null)
            res+="detGuard: "+ detGuard +"\n";
        if(nondetGuard!=null)
            res+="nondetGuard: "+nondetGuard.toNormalString()+"\n";
        System.err.println("updates: ");
        for(int i=0;i<varName.size();i++)
            if(update.elementAt(i)!=null)
                res+=varName.elementAt(i)+" := "+update.elementAt(i).toNormalString()+"\n";
            else
                res+=varName.elementAt(i)+" := nondet()\n";
        return res;
    }
}
