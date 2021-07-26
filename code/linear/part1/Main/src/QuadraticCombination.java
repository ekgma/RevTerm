import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.Vector;

public class QuadraticCombination
{
    Map<String, LinearCombination> coef;


    QuadraticCombination()
    {
        coef = new HashMap<>();
    }

    QuadraticCombination(String var, LinearCombination lc)
    {
        coef = new HashMap<>();
        add(var, lc);
    }

    QuadraticCombination(Set<String> vars)
    {
        coef= new HashMap<>();
        for(String var:vars)
        {
            add(var,new LinearCombination("c_"+InvariantGeneration.cCount));
            InvariantGeneration.cCount++;
        }
    }


    public void add(String var, LinearCombination lc)
    {
        if (coef.containsKey(var))
            coef.get(var).add(lc);
        else
            coef.put(var, lc);
    }

    public void add(QuadraticCombination qc)
    {
        for (String var : qc.coef.keySet())
            add(var, qc.coef.get(var));
    }

    public void add(LinearCombination lc)
    {
        for (String var : lc.coef.keySet())
        {
            add(var, new LinearCombination(lc.coef.get(var)));
        }
    }

    public void add(Rational val)
    {
        add("1", new LinearCombination(val));
    }

    public QuadraticCombination negate()
    {
        QuadraticCombination qc = new QuadraticCombination();
        for (String var : coef.keySet())
            qc.add(var, coef.get(var).negate());
        return qc;
    }

    public LinearCombination getCoef(String var)
    {
        return coef.get(var);
    }

    public Rational getCoef(String var1, String var2)
    {
        if (coef.containsKey(var1) && coef.get(var1).coef.containsKey(var2))
            return coef.get(var1).coef.get(var2);
        else if (coef.containsKey(var2) && coef.get(var2).coef.containsKey(var1))
            return coef.get(var2).coef.get(var1);
        else
            return Rational.zero;
    }

    public QuadraticCombination deepCopy()
    {
        QuadraticCombination qc = new QuadraticCombination();
        for (String var : coef.keySet())
            qc.add(var, coef.get(var).deepCopy());
        return qc;
    }

    public void replaceVarWithLinear(String var, LinearCombination lc)
    {
        if(!coef.containsKey(var))
            return;
        LinearCombination l=coef.get(var);
        coef.remove(var);
        for(String v:lc.coef.keySet())
        {
            LinearCombination tmp=l.deepCopy();
            tmp.multiplyByValue(lc.coef.get(v));
            add(v,tmp);
        }
    }

    public String toNormalString()
    {

        String ret = "";
        for (String s : coef.keySet())
        {
            if (ret.equals(""))
                ret += s + "*(" + coef.get(s).toNormalString() + ")";
            else
                ret += " + " + s + "*(" + coef.get(s).toNormalString() + ")";
        }
        return ret;

    }

    public String toString()
    {
        String ret = "";
        if (coef.keySet().size() > 1)
            ret = "(+ ";
        for (String var : coef.keySet())
        {
            LinearCombination lc = coef.get(var);
            if (ret == "")
                ret = "(* " + lc.toString() + " " + var + ")";
            else
                ret = ret + " (* " + lc.toString() + " " + var + ")";
        }
        if (coef.keySet().size() > 1)
            ret += ")";
        if (ret.equals(""))
            ret = "0";
        return ret;
    }

}
