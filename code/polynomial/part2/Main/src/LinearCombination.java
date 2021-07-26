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


    public QuadraticCombination toQuadratic()
    {
        QuadraticCombination qc=new QuadraticCombination();
        for(String var:coef.keySet())
            qc.add(var,new LinearCombination(coef.get(var)));
        return qc;
    }

    public boolean containsNondetVar()
    {
        for(String var:coef.keySet())
            if(var.startsWith("_a_") || var.startsWith("_b_") || var.startsWith("_tmp_"))
                return true;
        return false;
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

    public QuadraticCombination mulByVar(String var)
    {
        QuadraticCombination ret=new QuadraticCombination();
        for(String s:coef.keySet())
            ret.add(s,new LinearCombination(var,coef.get(s)));
        return ret;
    }

    public void multiplyByLin(LinearCombination lc) throws Exception
    {
        if(!isConstant() && !lc.isConstant())
            throw new Exception("multiplication of two linear Expressions is not linear");
        if(isConstant())
        {
            Rational x=coef.get("1");
            if(x==null)
                x=Rational.zero;
            coef.clear();
            for(String var:lc.coef.keySet())
                coef.put(var,Rational.mul(lc.coef.get(var),x));
        }
        else
        {
            Rational x=lc.coef.get("1");
            multiplyByValue(x);
        }
    }

    public boolean isConstant()
    {
        if(coef.size()>1 || (coef.size()==1 && !coef.containsKey("1")))
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

    public LinearCombination primize()
    {
        LinearCombination lc=new LinearCombination();
        for(String var:coef.keySet())
        {
            if(!var.equals("1"))
            {
                if(!var.contains("_prime"))
                    lc.add(var + "_prime", coef.get(var));
            }
            else
                lc.add(var,coef.get(var));
        }
        return lc;
    }

    public LinearCombination unprime()
    {
        LinearCombination lc=new LinearCombination();
        for(String var:coef.keySet())
        {
            if(var.endsWith("_prime"))
            {
                String orig=var.substring(0, var.length()-6);
                lc.add(orig, coef.get(var));
            }
            else
                lc.add(var,coef.get(var));
        }
        return lc;
    }

    void replaceVarWithLinear(String var,LinearCombination lc)
    {
        if(!coef.containsKey(var))
            return;
        Rational r=coef.get(var);
        coef.put(var,Rational.zero);
        LinearCombination tmp=lc.deepCopy();
        tmp.multiplyByValue(r);
        add(tmp);
        removeZeros();

    }

    public int replaceVarsWithValue(Map<String,Integer> dict) throws  Exception
    {
        int ret=0;
        for(String var:coef.keySet())
        {
            if(!dict.containsKey(var))
                throw new Exception("dictionary cannot support "+toNormalString());
            int varVal=dict.get(var);
            ret+=varVal*coef.get(var).numerator/coef.get(var).denominator;
        }
        return ret;
    }

    public boolean equals(LinearCombination lc)
    {
        lc.removeZeros();
        removeZeros();
        for(String var:coef.keySet())
            if(!lc.coef.containsKey(var) || !lc.coef.get(var).equals(this.coef.get(var)))
                return false;
        for(String var:lc.coef.keySet())
            if(!this.coef.containsKey(var) || !lc.coef.get(var).equals(this.coef.get(var)))
                return false;
        return true;
    }

    public String toNormalString()
    {
        removeZeros();
        if(coef.size()==0)
            return "0";
        String ret="";
        for(String s:coef.keySet())
        {
            Rational c=coef.get(s);
            if(ret.equals(""))
                ret+=c.toNormalString()+"*"+s;
            else if(coef.get(s).compareTo(Rational.zero)<0)
                ret+=" - "+(Rational.negate(c)).toNormalString()+"*"+s;
            else
                ret+=" + "+c.toNormalString()+"*"+s;
        }
        return ret;
    }

    public String toString()
    {
        removeZeros();
        String ret="";
        for(String s:coef.keySet())
        {
            Rational c=coef.get(s);
            if(c.equals(Rational.one))
                ret += " " + s;
            else if(s.equals("1"))
            {
                if (!c.isNonNegative())
                    ret += " (- " + Rational.negate(c) + ")";
                else
                    ret +=" "+c+" ";
            }
            else if(c.isNonNegative())
                ret += " (* " + (c) + " " + s + ")";
            else
                ret += " (* (- " + Rational.negate(c) + ") " + s + ")";
        }
        if(ret.equals(""))
            return "0";
        if(coef.size()>1)
            return "(+ "+ret+")";
        else
            return ret;
    }


}
