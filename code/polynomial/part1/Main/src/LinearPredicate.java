import java.util.Vector;

public class LinearPredicate
{

    Vector<LinearCombination> exprs;

    LinearPredicate()
    {
        exprs = new Vector<>();
    }

    void add(LinearCombination lc)
    {
        for(LinearCombination l:exprs)
            if(l!=null && l.equals(lc))
                return;
        exprs.add(lc);
    }

    void add(LinearPredicate lp)
    {
        for (LinearCombination lc : lp.exprs)
            add(lc.deepCopy());
    }

    boolean isEmpty()
    {
        return exprs.isEmpty();
    }

    void replaceVarWithLinear(String var,LinearCombination lc)
    {
        for(LinearCombination l:exprs)
            l.replaceVarWithLinear(var,lc);
    }


    public Vector<LinearPredicate> negate()
    {
        Vector<LinearPredicate> ret = new Vector<>();

        for (LinearCombination lc : exprs)
        {
            LinearCombination l = lc.negate();
            l.add("1", Rational.negate(Main.eps));
            LinearPredicate lp = new LinearPredicate();
            lp.add(l);
            ret.add(lp);
        }
        return ret;
    }

    public static Vector<LinearPredicate> negate(Vector<LinearPredicate> g)
    {

        Vector<LinearPredicate> ret = new Vector<>();
        if (g.size() == 1)
            ret = g.firstElement().negate();
        else
        {
            Vector<LinearPredicate> notLast = g.lastElement().negate();
            g.removeElementAt(g.size() - 1);
            Vector<LinearPredicate> recurse = negate(g);
            for (LinearPredicate lp : notLast)
                for (LinearPredicate predicate : recurse)
                {
                    LinearPredicate copy = predicate.deepCopy();
                    copy.add(lp);
                    ret.add(copy);
                }
        }

        return ret;
    }

    public static Vector<LinearPredicate> conjunct(Vector<LinearPredicate> left, Vector<LinearPredicate> right)
    {
        Vector<LinearPredicate> ret = new Vector<>();

        if (left.isEmpty())
        {
            for (LinearPredicate lp : right)
                ret.add(lp.deepCopy());
            return ret;
        }
        if (right.isEmpty())
        {
            for (LinearPredicate lp : left)
                ret.add(lp.deepCopy());
            return ret;
        }

        for (LinearPredicate lp1 : left)
            for (LinearPredicate lp2 : right)
            {
                LinearPredicate lp = new LinearPredicate();
                lp.add(lp1.deepCopy());
                lp.add(lp2.deepCopy());
                ret.add(lp);
            }
        return ret;
    }

    public static Vector<LinearPredicate> disjunct(Vector<LinearPredicate> left, Vector<LinearPredicate> right)
    {
        Vector<LinearPredicate> ret = new Vector<>();
        for (LinearPredicate lp : left)
            ret.add(lp.deepCopy());
        for (LinearPredicate lp : right)
            ret.add(lp.deepCopy());
        return ret;
    }

    public LinearPredicate deepCopy()
    {
        LinearPredicate ret = new LinearPredicate();
        ret.add(this);
        return ret;

    }

    public boolean equalsLogic(LinearPredicate lp)
    {
        for (LinearCombination lc : exprs)
            if (!lp.contains(lc))
                return false;
        for (LinearCombination lc : lp.exprs)
            if (!this.contains(lc))
                return false;
        return true;
    }

    public boolean contains(LinearCombination lc)
    {
        for (LinearCombination l : exprs)
            if (l.equals(lc))
                return true;
        return false;
    }

    public String toString()
    {
        String ret = "";
        for (int i = 0; i < exprs.size(); i++)
        {

            LinearCombination lc = exprs.elementAt(i);
            if (i == 0)
                ret += lc.toNormalString() + ">=0";
            else
                ret += " && " + lc.toNormalString() + ">=0";
        }
        return ret;
    }
}
