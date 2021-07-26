

import java.util.*;

public class Monomial implements Comparable<Monomial>
{
    Map<String, Integer> vars;
    public static final Monomial one = new Monomial();

    Monomial()
    {
        vars = new TreeMap<>();
    }

    Monomial(String var)
    {
        vars=new TreeMap<>();
        addVar(var,1);
    }

    Monomial(String var,int power)
    {
        vars= new TreeMap<>();
        addVar(var,power);
    }


    void addVar(String varName, int power)
    {
        if (vars.containsKey(varName))
            vars.put(varName, vars.get(varName) + power);
        else
            vars.put(varName, power);
    }

    Map<String, Integer> getVars()
    {
        return vars;
    }


    public static Monomial mul(Monomial a, Monomial b)
    {
        Monomial ret = new Monomial();
        Map<String, Integer> avars = a.getVars(), bvars = b.getVars();

        for (String varName : avars.keySet())
            ret.addVar(varName, avars.get(varName));
        for (String varName : bvars.keySet())
            ret.addVar(varName, bvars.get(varName));

        return ret;
    }

    Monomial deepCopy()
    {
        Monomial ret = new Monomial();
        for (String varName : vars.keySet())
            ret.addVar(varName, vars.get(varName));
        return ret;
    }

    boolean containsVar(String var)
    {
        return vars.containsKey(var);
    }

    Monomial removeOneVar(String var)
    {
        Monomial ret = new Monomial();
        for (String s : vars.keySet())
            if (!s.equals(var))
                ret.addVar(s, vars.get(s));
        return ret;
    }

    int getPower(String var)
    {
        return vars.get(var);
    }

    public Monomial programVarsPart()
    {
        Monomial ret = new Monomial();
        for(String var:vars.keySet())
            if(!var.startsWith("c_") && !var.startsWith("t_") && !var.startsWith("l_"))  //TODO: write a function for this if
                ret.addVar(var,getPower(var));
        return ret;
    }

    public Monomial unknownPart()
    {
        Monomial ret = new Monomial();
        for(String var:vars.keySet())
            if(var.startsWith("c_") || var.startsWith("t_") || var.startsWith("l_"))  //TODO: write a function for this if
                ret.addVar(var,getPower(var));
        return ret;
    }

    public Monomial getProgramVarsPart()
    {
        Monomial ret=new Monomial();
        for(String var:vars.keySet())
            if(!var.startsWith("c_") && !var.startsWith("t_") && !var.startsWith("l_"))
                ret.addVar(var,getPower(var));
        return ret;
    }

    public Polynomial replaceVarsWithValue(Map<String,Integer> dict)
    {
        Monomial p=new Monomial();
        Rational coef=Rational.one.deepCopy();
        for(String var:vars.keySet())
            if(dict.containsKey(var))
                coef.numerator*=dict.get(var);
            else
                p.addVar(var,vars.get(var));
        return new Polynomial(p,coef);
    }

    public static Set<Monomial> getAllMonomials(Set<String> vars, int degree)
    {
        Set<Monomial> ret=new TreeSet<>();
        if(degree==0)
            ret.add(Monomial.one.deepCopy());
        else
        {
            Set<Monomial> recurse = getAllMonomials(vars,degree-1);
            for(Monomial m:recurse)
                ret.add(m.deepCopy());
            for(String var:vars)
                for(Monomial m:recurse)
                {
                    if(m.degree()==degree-1)
                    {
                        Monomial tmp = m.deepCopy();
                        tmp.addVar(var, 1);
                        ret.add(tmp);
                    }
                }
        }

        return ret;
    }

    int degree()
    {
        int ret=0;
        for(String var:vars.keySet())
            ret+=vars.get(var);
        return ret;
    }

    public int compareTo(Monomial m)
    {
        return (toNormalString().compareTo(m.toNormalString()));
    }

    void removeZeros()
    {
        Vector<String> allVars=new Vector<>(vars.keySet());
        for(String var:allVars)
            if(getPower(var)==0)
                vars.remove(var);
    }

    public boolean equals(Monomial m)
    {
        removeZeros();
        m.removeZeros();
        for(String var:vars.keySet())
            if(!m.containsVar(var) || m.getPower(var)!=getPower(var))
                return false;
        for(String var:m.vars.keySet())
            if(!this.containsVar(var))
                return false;
        return true;
    }

    String toNormalString()
    {
        if (vars.isEmpty())
            return "1";

        String ret = "";
        for (String varName : vars.keySet())
        {
            if (!ret.equals(""))
                ret+=" * ";

            ret += varName;
            if(vars.get(varName)!=1)
                ret+="^"+vars.get(varName);
        }
        return ret;
    }

    public String toString()
    {
        if (vars.isEmpty())
            return "1";


        String ret = "";
        for (String varName : vars.keySet())
        {
            for (int i = 0; i < vars.get(varName); i++)
                ret += varName + " ";
        }
//        ret += ")";
        return ret;
    }
}
