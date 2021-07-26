import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

public class LinearCombination
{
    Map<String,Rational> coef;

    public static LinearCombination one=new LinearCombination(Rational.one);

    LinearCombination()
    {
        coef=new HashMap<>();
    }
    LinearCombination(Rational c)
    {
        coef=new HashMap<>();
        coef.put("1",c);
    }

    LinearCombination(String var)
    {
        coef=new HashMap<>();
        coef.put(var,Rational.one);
    }

    LinearCombination(String var,Rational c)
    {
        coef=new HashMap<>();
        coef.put(var,c);
    }

    public void add(String var,Rational c)
    {
        if(coef.containsKey(var))
            coef.put(var,Rational.add(coef.get(var),c));
        else
            coef.put(var,c);
    }

    public void add(LinearCombination lc)
    {
        for(String var:lc.coef.keySet())
            add(var,lc.coef.get(var));
    }

    public void minus(LinearCombination lc)
    {
        add(lc.negate());
    }

    public void multiplyByValue(Rational val)
    {
        for(String var:coef.keySet())
            coef.put(var,Rational.mul(coef.get(var),val));
    }

    public LinearCombination negate()  //does not negate "this". returns the negate of "this".
    {
        removeZeros();
        LinearCombination lc=new LinearCombination();
        for(String var:coef.keySet())
            lc.coef.put(var,Rational.negate(coef.get(var)));
        return lc;
    }


    public LinearCombination deepCopy()
    {
        removeZeros();
        LinearCombination lc=new LinearCombination();
        for(String var:coef.keySet())
        {
            Rational c=coef.get(var);
            lc.add(var, c);
        }
        return lc;
    }

    public void multiplyByLin(LinearCombination lc) throws Exception
    {
        if (!isConstant() && !lc.isConstant())
            throw new Exception("multiplication of two linear Expressions is not linear");
        if (isConstant())
        {
            Rational x = coef.get("1");
            if (x == null)
                x = Rational.zero;
            coef.clear();
            for (String var : lc.coef.keySet())
                coef.put(var, Rational.mul(lc.coef.get(var), x));
        }
        else
        {
            Rational x = lc.coef.get("1");
            multiplyByValue(x);
        }
    }

    public boolean isConstant()
    {
        if (coef.size() > 1 || (coef.size() == 1 && !coef.containsKey("1")))
            return false;
        return true;
    }

    public void removeZeros()
    {
        Vector<String> allVars=new Vector<>();
        allVars.addAll(coef.keySet());
        for(String s:allVars)
            if(coef.get(s).equals(Rational.zero))
                coef.remove(s);

    }

    public boolean equals(LinearCombination lc)
    {
        removeZeros();
        lc.removeZeros();
        for (String var : coef.keySet())
            if (!lc.coef.containsKey(var) || !lc.coef.get(var).equals(this.coef.get(var)))
                return false;
        for (String var : lc.coef.keySet())
            if (!this.coef.containsKey(var) || !lc.coef.get(var).equals(this.coef.get(var)))
                return false;
        return true;
    }


    public String toString()
    {
        removeZeros();
        if(coef.size()==0)
            return "0";
        String ret="";
        for(String s:coef.keySet())
        {
            Rational c=coef.get(s);
            if(ret.equals(""))
                ret+=c.toString()+"*"+s;
            else if(coef.get(s).compareTo(Rational.zero)<0)
                ret+=" - "+(Rational.negate(c)).toString()+"*"+s;
            else
                ret+=" + "+c.toString()+"*"+s;
        }
        return ret;
    }




}
