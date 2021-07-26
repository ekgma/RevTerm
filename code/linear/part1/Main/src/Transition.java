import java.util.Vector;

public class Transition    //from "v.first" to "v.second" with guard "g" and update "varName := update"
{
    public static Vector<Transition> allTransitions = new Vector<>();
    CFGNode v, u;
    LinearPredicate detGuard;
    QuadraticPredicate nondetGuard;

    Vector<String> varName;
    Vector<LinearCombination> update;
    boolean hasGroup;

    Transition(CFGNode a, CFGNode b)
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
                // gen: qc1<=qc2

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

    public Transition deepCopy()
    {
        Transition ret = new Transition(v, u);
        ret.detGuard = detGuard.deepCopy();
        ret.nondetGuard = nondetGuard.deepCopy();
        for (String var : varName)
            ret.varName.add(var.toString());
        for (LinearCombination lc : update)
            if (lc != null)
                ret.update.add(lc.deepCopy());
            else
                ret.update.add(null);
        return ret;
    }

    public String toString()
    {
        String res = "";
        res += "from: " + v.id + "\nto: " + u.id + "\n";
        if (detGuard != null)
            res += "detGuard: " + detGuard + "\n";
        if (nondetGuard != null)
            res += "nondetGuard: " + nondetGuard.toNormalString() + "\n";
        for (int i = 0; i < varName.size(); i++)
            if (update.elementAt(i) != null)
                res += varName.elementAt(i) + " := " + update.elementAt(i).toNormalString() + "\n";
            else
                res += varName.elementAt(i) + " := nondet()\n";
        return res;
    }
}