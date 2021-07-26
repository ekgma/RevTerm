import java.util.Vector;

public class PolynomialPredicate
{
    Vector<Polynomial> exprs;
    public static PolynomialPredicate TRUE = new PolynomialPredicate(new Polynomial(Monomial.one));

    PolynomialPredicate()
    {
        exprs = new Vector<>();
    }

    PolynomialPredicate(Polynomial poly)
    {
        exprs=new Vector<>();
        add(poly);
    }

    void add(Polynomial poly)
    {
        for (Polynomial p : exprs)
            if (p!=null && p.equals(poly))
                return;
        exprs.add(poly);
    }

    void add(PolynomialPredicate pp)
    {
        for (Polynomial poly : pp.exprs)
            if(poly!=null)
                add(poly.deepCopy());
            else
                exprs.add(null);
    }

    void replaceVarWithPoly(String var, Polynomial poly)
    {
        for (Polynomial p : exprs)
            p.replaceVarWithPoly(var, poly);
    }

    public Vector<PolynomialPredicate> negate()
    {
        Vector<PolynomialPredicate> ret = new Vector<>();
        for (Polynomial poly : exprs)
        {
//            Polynomial p = poly.negate();
            Polynomial p = null;
            if(poly!=null)
            {
                p = poly.negate();
                p.add(Monomial.one, Rational.negate(prog_to_t2.eps));
            }
            PolynomialPredicate pp = new PolynomialPredicate();
            pp.add(p);
            ret.add(pp);
        }
        return ret;
    }

    public static Vector<PolynomialPredicate> negate(Vector<PolynomialPredicate> g,int first)
    {
        if(first==g.size())
            return new Vector<>();
        else if (first== g.size()-1 )
        {
            return g.elementAt(first).negate();
        }
        else
        {
//            Vector<PolynomialPredicate> ret = new Vector<>();
            Vector<PolynomialPredicate> notFirst = g.elementAt(first).negate();
            Vector<PolynomialPredicate> recurse = negate(g,first+1);
//            for (PolynomialPredicate pp : notFirst)
//                for (PolynomialPredicate predicate : recurse)
//                {
//                    PolynomialPredicate copy = predicate.deepCopy();
//                    copy.add(pp);
//                    ret.add(copy);
//                }
            return conjunct(notFirst,recurse);
        }
    }

    public static Vector<PolynomialPredicate> conjunct(Vector<PolynomialPredicate> left, Vector<PolynomialPredicate> right)
    {
        Vector<PolynomialPredicate> ret = new Vector<>();

        if (left.isEmpty())
        {
            for (PolynomialPredicate pp : right)
                ret.add(pp.deepCopy());
        } else if (right.isEmpty())
        {
            for (PolynomialPredicate pp : left)
                ret.add(pp.deepCopy());
        } else
        {
            for (PolynomialPredicate pp1 : left)
                for (PolynomialPredicate pp2 : right)
                {
                    PolynomialPredicate pp = new PolynomialPredicate();
                    pp.add(pp1.deepCopy());
                    pp.add(pp2.deepCopy());
                    ret.add(pp);
                }
        }
        return ret;
    }

    public static Vector<PolynomialPredicate> disjunct(Vector<PolynomialPredicate> left, Vector<PolynomialPredicate> right)
    {
        Vector<PolynomialPredicate> ret = new Vector<>();
        for (PolynomialPredicate pp : left)
            ret.add(pp.deepCopy());
        for (PolynomialPredicate pp : right)
            ret.add(pp.deepCopy());
        return ret;
    }

    boolean equalsLogic(PolynomialPredicate pp)
    {
        for (Polynomial p : exprs)
            if (!pp.contains(p))
                return false;
        for (Polynomial p : pp.exprs)
            if (!this.contains(p))
                return false;
        return true;
    }

    boolean contains(Polynomial p)
    {
        for (Polynomial poly : exprs)
            if (p.equals(poly))
                return true;
        return false;
    }

    PolynomialPredicate deepCopy()
    {
        PolynomialPredicate ret = new PolynomialPredicate();
        ret.add(this);
        return ret;
    }

    public String toNormalString()
    {
        String ret = "";
        for (int i = 0; i < exprs.size(); i++)
        {
            Polynomial p = exprs.elementAt(i);
            if (i == 0)
                ret += p.toNormalString() + ">=0";
            else
                ret += " && " + p.toNormalString() + ">=0";
        }
        return ret;
    }

    public String toString()
    {
        return toNormalString();
    }
}
