import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

public class Farkas
{
    public static int countD = 0;
    int startDIndex;
    int startNode, endNode;
    private QuadraticPredicate invConstraint;
    private LinearPredicate linearConstraints;

    private QuadraticCombination objective;

    Farkas(int startNode, int endNode)
    {
        this.startNode = startNode;
        this.endNode = endNode;
        linearConstraints = new LinearPredicate();
        LinearCombination lc = new LinearCombination();
        lc.add("1", Rational.one);
        linearConstraints.add(lc); // 1>=0 is always true

        invConstraint = new QuadraticPredicate();

        startDIndex = countD;
        countD++; // for 1>=0
    }


    public Farkas deepCopy()
    {
        Farkas ret = new Farkas(startNode, endNode);
        countD--; // for 1>=0 which is added in new
        ret.startDIndex = startDIndex;
        ret.invConstraint.exprs.addAll(invConstraint.exprs);

        //countD+=invConstraint.size();

        ret.linearConstraints = linearConstraints.deepCopy();
        //countD+=linearConstraints.exprs.size()-1; //-1 for 1>=0 which is already added

        ret.objective = objective.deepCopy();
        return ret;
    }

//    public Farkas disabled()
//    {
//        Farkas ret=deepCopy();
////        LinearCombination lc=new LinearCombination("n_"+InvariantGeneration.nCount);
////        QuadraticCombination obj=new QuadraticCombination("1",lc);
//
//        InvariantGeneration.nCount++;
//        ret.objective=QuadraticCombination.minus1.deepCopy();
//        return ret;
//    }

    void addInvConstraint(QuadraticPredicate inv)
    {
        invConstraint.add(inv);
        countD += inv.exprs.size();
    }

    void addPredicate(LinearPredicate lp)
    {
        linearConstraints.add(lp);
        countD += lp.exprs.size();
    }


    void setObjective(QuadraticCombination obj)
    {
        objective = obj.deepCopy();
    }

    void replaceVarWithLinear(String var, LinearCombination lc)
    {
        linearConstraints.replaceVarWithLinear(var,lc);
        objective.replaceVarWithLinear(var,lc);
        invConstraint.replaceVarWithLinear(var,lc);
    }

    public Vector<QuadraticCombination> generateEqualities()
    {
        Vector<QuadraticCombination> ret = new Vector<>();
        Set<String> allVars= getAllVars();
        for (String var : allVars)
        {
            QuadraticCombination tmp = makeEquality(var);
            ret.add(tmp);
        }
        return ret;
    }

    public Set<String> getAllVars()
    {
        Set<String> ret=new HashSet<>();
        for(LinearCombination lc:linearConstraints.exprs)
            ret.addAll(lc.coef.keySet());
        for(QuadraticCombination qc: invConstraint.exprs)
            ret.addAll(qc.coef.keySet());
        ret.addAll(objective.coef.keySet());
        return ret;
    }

    public QuadraticCombination makeEquality(String var)
    {
        QuadraticCombination qc = new QuadraticCombination();
        int dIndex = startDIndex;
        if (!invConstraint.exprs.isEmpty())
        {
            //for(int i=0;i<invConstraint.exprs.size();i++)
            for (QuadraticCombination invc : invConstraint.exprs)
            {
                if (invc.coef.containsKey(var))
                {
                    String invMultiplier = "d_" + dIndex;
                    //InvariantGeneration.addUnknownVar("d_" + dIndex);


                    LinearCombination lc = invc.coef.get(var);
                    qc.add(invMultiplier, lc);
                }
                dIndex++;
            }
        }

        for (LinearCombination lp : linearConstraints.exprs) // lp>=0
        {
            String multiplier = "d_" + dIndex;
            if (lp.coef.containsKey(var))
            {
                Rational coef = lp.coef.get(var);
                qc.add(multiplier, new LinearCombination(coef));
                //InvariantGeneration.addUnknownVar("d_" + dIndex);
            }
            dIndex++;
        }

        LinearCombination coef = objective.getCoef(var);
        //qc=coef  <=>  qc-coef=0
        if (coef != null)
        {
            LinearCombination lc = coef.negate();
            qc.add(lc);
        }
//        System.err.println("var: "+var+" => "+qc.toNormalString());
        return qc;
    }

    public String toString()
    {
        String ret = "";
        ret += "\n---------------------------------------------\n";
        ret += "from: " + startNode + " to: " + endNode + "\n";
        int dIndex = startDIndex;
        for (int i = 0; i < invConstraint.exprs.size(); i++)
        {
            ret += "d_" + dIndex + ": " + invConstraint.exprs.elementAt(i).toNormalString() + "\n";
            dIndex++;
        }
        for (LinearCombination lc : linearConstraints.exprs)
        {
            ret += "\nd_" + dIndex + ": " + lc.toNormalString();
            dIndex++;
        }
        ret += "\n---------------------------------------------\n";

        ret += objective.toNormalString();
        return ret;
    }
}