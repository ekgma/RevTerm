import java.util.Vector;

public class QuadraticPredicate
{
    public static QuadraticPredicate TRUE=new QuadraticPredicate(new QuadraticCombination("1",new LinearCombination("1",Rational.one))); // 1>=0
    Vector<QuadraticCombination> exprs;

    QuadraticPredicate() //conjunctions
    {
        exprs = new Vector<>();
    }

    QuadraticPredicate(QuadraticCombination qc)
    {
        exprs=new Vector<>();
        add(qc);
    }

    void add(QuadraticCombination qc)
    {
        exprs.add(qc);
    }

    void add(QuadraticPredicate qp)
    {
        exprs.addAll(qp.exprs);
    }

    void replaceVarWithLinear(String var,LinearCombination update)
    {
        for(QuadraticCombination qc:exprs)
            qc.replaceVarWithLinear(var,update);
    }


    public static Vector<QuadraticPredicate> negate(Vector<QuadraticPredicate> vqp)
    {
        Vector<QuadraticPredicate> ret = new Vector<>();
        for (QuadraticPredicate qp : vqp)
        {
            Vector<QuadraticCombination> vqc = qp.negate();
            if (ret.isEmpty())
            {
                for (QuadraticCombination qc : vqc)
                {
                    QuadraticPredicate c = new QuadraticPredicate();
                    c.add(qc);
                    ret.add(c);
                }
                continue;
            }

            Vector<QuadraticPredicate> tmp = new Vector<>();
            for (QuadraticCombination cur : vqc)
                for (QuadraticPredicate q : ret)
                {
                    QuadraticPredicate c = q.deepCopy();
                    c.add(cur);
                    tmp.add(c);
                }
            ret.addAll(tmp);
        }
        return ret;
    }

    Vector<QuadraticCombination> negate()
    {
        Vector<QuadraticCombination> ret = new Vector<>();
        for (QuadraticCombination qc : exprs)
        {
            QuadraticCombination q = qc.negate();
            q.add("1", new LinearCombination(Rational.negate(Main.eps))); // 1*(-1)
            ret.add(q);
        }
        return ret;
    }

    QuadraticCombination getTerm(int ind)
    {
        return exprs.elementAt(ind);
    }

    public QuadraticPredicate deepCopy()
    {
        QuadraticPredicate qp = new QuadraticPredicate();
        for (QuadraticCombination qc : exprs)
            qp.add(qc.deepCopy());
        return qp;
    }

    public String toString()
    {
        String ret = "";
        for (QuadraticCombination qc : exprs)
            ret += "(>= " + qc.toString() + " 0) ";
        if (exprs.size() > 1)
            ret = "(and " + ret + ") ";
        return ret;
    }

    public String toNormalString()
    {
        String ret = "";
        for (QuadraticCombination qc : exprs)
            ret += qc.toNormalString() + ">=0      and      ";
        return ret;
    }
}