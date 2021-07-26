import java.util.*;

public class Polynomial
{
    Map<Monomial, Rational> terms;

    Polynomial()
    {
        terms = new TreeMap<>();
    }

    Polynomial(Rational c)
    {
        terms = new TreeMap<>();
        terms.put(Monomial.one, c);
    }

    Polynomial(String var)
    {
        terms = new TreeMap<>();
        terms.put(new Monomial(var), Rational.one);
    }

    Polynomial(String var, Rational c)
    {
        terms = new TreeMap<>();
        terms.put(new Monomial(var), c);
    }

    Polynomial(Monomial m)
    {
        terms = new TreeMap<>();
        terms.put(m, Rational.one);
    }


    Polynomial(Monomial m, Rational c)
    {
        terms = new TreeMap<>();
        terms.put(m, c);
    }

    Polynomial(Set<String> vars)  //generates the polynomial \sum c_i x_i
    {
        terms = new TreeMap<>();
        for (String var : vars)
        {
            Monomial m = new Monomial();
            m.addVar("c_" + InvariantGeneration.cCount, 1);
            InvariantGeneration.cCount++;
            m.addVar(var, 1);
            add(m, Rational.one);
        }
    }

    Polynomial(Set<String> vars, int degree)
    {
        terms = new TreeMap<>();
        Set<Monomial> monomials = Monomial.getAllMonomials(vars, degree);
        for (Monomial m : monomials)
        {
            m.addVar("c_" + InvariantGeneration.cCount, 1);
            InvariantGeneration.cCount++;
            add(m, Rational.one);
        }
    }

    void add(Monomial m, Rational c)
    {
        if (terms.containsKey(m))
            terms.put(m, Rational.add(terms.get(m), c));
        else
            terms.put(m, c);
    }

    void add(Polynomial poly)
    {
        for (Monomial term : poly.terms.keySet())
            add(term, poly.terms.get(term));
    }

    void minus(Polynomial poly)
    {
        add(poly.negate());
    }

    public void multiplyByValue(Rational val)
    {
        for (Monomial term : terms.keySet())
            terms.put(term, Rational.mul(terms.get(term), val));
    }

    public void multiplyByMonomial(Monomial m)
    {
        Map<Monomial, Rational> tmp = new HashMap<>();
        for (Monomial term : terms.keySet())
            tmp.put(Monomial.mul(term, m), terms.get(term));
        terms = tmp;
    }

    public void multiplyByPolynomial(Polynomial poly)
    {
        Polynomial res = new Polynomial();
        for (Monomial m : poly.terms.keySet())
            for (Monomial n : this.terms.keySet())
                res.add(Monomial.mul(m, n), Rational.mul(poly.terms.get(m), this.terms.get(n)));
        terms = res.terms;
    }

    boolean isConstant()
    {
        removeZeros();
        return (terms.size() <= 1 && (terms.size() != 1 || terms.containsKey(Monomial.one)));
    }

    void removeZeros()
    {
        Vector<Monomial> allTerms = new Vector<>(terms.keySet());
        for (Monomial term : allTerms)
            if (terms.get(term).equals(Rational.zero))
                terms.remove(term);
    }

    Polynomial deepCopy()
    {
        removeZeros();
        Polynomial ret = new Polynomial();
        for (Monomial term : terms.keySet())
            ret.add(term.deepCopy(), terms.get(term));
        return ret;
    }

    void replaceVarWithPoly(String var, Polynomial poly)
    {
        Vector<Monomial> allTerms = new Vector<>(terms.keySet());
        for (Monomial term : allTerms)
            if (term.containsVar(var))
            {
                Rational coef = terms.get(term);
                Monomial m = term.removeOneVar(var);

                Polynomial left = new Polynomial(m);
                Polynomial right = poly.deepCopy();

                for (int i = 1; i < term.getPower(var); i++)
                    right.multiplyByPolynomial(poly);
                left.multiplyByPolynomial(right);
                left.multiplyByValue(coef);

                terms.remove(term);

                add(left);
            }
    }

    public Set<Monomial> getProgramVariableMonomials()
    {
        Set<Monomial> ret= new TreeSet<>();
        for(Monomial m:terms.keySet())
            ret.add(m.getProgramVarsPart());
        return ret;
    }

    public Polynomial getCoef(Monomial m)
    {
        Polynomial ret=new Polynomial();
        for(Monomial monomial:terms.keySet())
        {
            if(monomial.programVarsPart().equals(m))
                ret.add(monomial.unknownPart(),terms.get(monomial));
        }
        return ret;
    }

    public boolean equals(Polynomial poly)
    {
        for (Monomial m : terms.keySet())
            if (!poly.terms.containsKey(m) || !poly.terms.get(m).equals(terms.get(m)))
                return false;
        return true;
    }

    Polynomial negate()
    {
        Polynomial ret = new Polynomial();
        for (Monomial term : terms.keySet())
            ret.add(term.deepCopy(), Rational.negate(terms.get(term)));
        return ret;
    }

    public  static  Polynomial mul(Polynomial left,Polynomial right)
    {
        Polynomial ret=left.deepCopy();
        ret.multiplyByPolynomial(right);
        return ret;
    }

    public String toString()
    {
        String ret = "";
        if (terms.isEmpty())
            return "0";
        Vector<Monomial> monomials = new Vector<>(terms.keySet());
        if (monomials.size() == 1)
            return "(* " + terms.get(monomials.firstElement()) +" "+ monomials.firstElement().toString()+")";
        ret = "(+ ";
        for (Monomial m : monomials)
            ret += "(* "+ terms.get(m)+ " " +m.toString()+") ";
        ret += ")";
        return ret;
    }

    public String toNormalString()
    {
        String ret = "";
        if (terms.isEmpty())
            return "0";
        Vector<Monomial> monomials = new Vector<>(terms.keySet());
        for (Monomial m : monomials)
        {
            if(!ret.equals(""))
                ret+="  +  ";
            if(!terms.get(m).equals(Rational.one))
                ret += terms.get(m).toNormalString()+" * ";

            ret += m.toNormalString();
        }
        return ret;
    }
}
