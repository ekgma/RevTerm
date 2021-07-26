import java.util.EnumMap;

public class Rational implements Comparable<Rational>
{
    public static final Rational one=new Rational(1,1),zero=new Rational(0,1);

    int numerator,denominator;

    public int gcd(int a, int b) {
        if (b==0) return a;
        return gcd(b,a%b);
    }

    Rational(int numerator,int denominator)
    {
        if(numerator==0)
        {
            this.numerator=0;
            this.denominator=1;
            return;
        }
        if(denominator<0)
        {
            denominator*=-1;
            numerator*=-1;
        }
        int g=gcd(numerator,denominator);
        this.numerator=numerator/g;
        this.denominator=denominator/g;
    }

    public static Rational negate(Rational a)
    {
        return new Rational(-a.numerator,a.denominator);
    }

    public static Rational inverse(Rational a) throws Exception
    {
        if(a.numerator==0)
            throw new Exception("getting inverse of "+a+" which is not defined");
        return new Rational(a.denominator,a.numerator);
    }

    public static Rational add(Rational a,Rational b)
    {
        return new Rational(a.numerator*b.denominator+b.numerator*a.denominator,a.denominator*b.denominator);
    }


    public static Rational minus(Rational a,Rational b)
    {
        return add(a,negate(b));
    }

    public static Rational mul(Rational a,Rational b)
    {
        return new Rational(a.numerator*b.numerator,a.denominator*b.denominator);
    }

    public static Rational div(Rational a,Rational b) throws Exception
    {
        return mul(a,inverse(b));
    }

    public boolean equals(Rational a)
    {
        return (a.numerator== numerator && a.denominator==denominator);
    }

    public boolean isNonNegative()
    {
        return (numerator>=0);
    }

    @Override
    public int compareTo(Rational a)
    {
        return numerator*a.denominator-a.numerator*denominator;
    }




    public void normalize()
    {
        if(denominator<0)
        {
            numerator*=-1;
            denominator*=-1;
        }
    }

    public String toString()
    {
        if(denominator==1)
            return ""+numerator;
        return "("+numerator+"/"+denominator+")";
    }

}
