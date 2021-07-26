import java.util.Vector;

public class Transition    //from "v.first" to "v.second" with guard "g" and update "varName := update"
{
    public static Vector<Transition> allTransitions=new Vector<>();
    CFGNode v,u;
    LinearPredicate detGuard;
    QuadraticPredicate nondetGuard;

    Vector<String> varName;
    Vector<LinearCombination> update; //if update[i]=null then varName[i] is non-deterministicly assigned
    boolean hasGroup;


    Transition(CFGNode a,CFGNode b)
    {
        v = a;
        u = b;
        detGuard = new LinearPredicate();
        nondetGuard = new QuadraticPredicate();
        varName = new Vector<>();
        update = new Vector<>();
        hasGroup = false;
    }

    void addNondetTemplate()
    {
        for(LinearCombination lc:update)
        {
            if(lc.toString().contains("_r_"))  // lc is a fresh nondet variable
            {
                // qc <= lc <= qc

                QuadraticCombination qc= new QuadraticCombination(Parser.allVars),qcc=qc.deepCopy();

                qc.add(lc.negate()); //qc - lc <=0
                nondetGuard.add(qc.negate());


                qcc.add(lc.negate()); // qc - lc >=0
                nondetGuard.add(qcc);

            }
        }
    }


    public void addToGraph()
    {
        allTransitions.add(this);
        v.out.add(this);
    }

    public Transition reverse() throws Exception
    {
        Transition ret=new Transition(u,v);
        for(LinearCombination lc: detGuard.exprs)
            ret.detGuard.add(lc.deepCopy());
        for(QuadraticCombination qc: nondetGuard.exprs)
            ret.nondetGuard.add(qc.deepCopy());
        for(int i=0;i<varName.size();i++)
        {
            LinearCombination lc=update.elementAt(i);
            String var=varName.elementAt(i);
//            System.err.println(var+" := "+lc.toNormalString());
            if(lc.coef.containsKey(var) && lc.coef.get(var).numerator!=0)
            {
                Rational c=new Rational(lc.coef.get(var).numerator,lc.coef.get(var).denominator);
                LinearCombination upd=lc.deepCopy();
                upd.coef.put(var,Rational.negate(Rational.one));
                upd.multiplyByValue(Rational.inverse(Rational.negate(c)));

                ret.varName.add(var);
                ret.update.add(upd);
            }
            else //var is not in lc
            {
                LinearCombination g=lc.deepCopy();
                g.minus(new LinearCombination(var));
                //g==0
                ret.detGuard.add(g);  //g>=0
                ret.detGuard.add(g.negate());  //-g>=0

                ret.varName.add(var);
                ret.update.add(new LinearCombination("_r_"+Parser.nondetCount));
                Parser.nondetCount++;
            }
        }
        return ret;
    }

    public Transition deepCopy()
    {
        Transition ret=new Transition(v,u);
        ret.detGuard = detGuard.deepCopy();
        ret.nondetGuard=nondetGuard.deepCopy();
        for(String var:varName)
            ret.varName.add(var.toString());
        for(LinearCombination lc:update)
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
        for(int i=0;i<varName.size();i++)
            if(update.elementAt(i)!=null)
                res+=varName.elementAt(i)+" := "+update.elementAt(i).toNormalString()+"\n";
            else
                res+=varName.elementAt(i)+" := nondet()\n";
        return res;
    }
}
