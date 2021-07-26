import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

public class Putinar
{
    public static int countD = 0;
    int startDIndex;
    int startNode, endNode;
    public PolynomialPredicate constraints;

    public Polynomial objective;

    Putinar(int startNode, int endNode)
    {
        this.startNode = startNode;
        this.endNode = endNode;
        constraints = new PolynomialPredicate();
        Polynomial lc = new Polynomial();
        lc.add(Monomial.one, Rational.one);
        constraints.add(lc); // 1>=0 is always true

//        nodeConstraint = new PolynomialPredicate();

        startDIndex = countD;
        countD++; // for 1>=0
    }


    public Putinar deepCopy()
    {
        Putinar ret = new Putinar(startNode, endNode);
        countD--; // for 1>=0 which is added in new
        ret.startDIndex = startDIndex;
//        ret.nodeConstraint.exprs.addAll(nodeConstraint.exprs);

        //countD+=invConstraint.size();

        ret.constraints = constraints.deepCopy();
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


    void addPredicate(PolynomialPredicate pp)
    {
        constraints.add(pp);
        countD+=pp.exprs.size();
    }


    void setObjective(Polynomial obj)
    {
        objective = obj.deepCopy();
    }

    void replaceVarWithPoly(String var, Polynomial lc)
    {
        constraints.replaceVarWithPoly(var,lc);
        objective.replaceVarWithPoly(var,lc);
//        nodeConstraint.replaceVarWithPoly(var,lc);
    }

    public Vector<Polynomial> generateEqualities()
    {
        Vector<Polynomial> equalities = new Vector<>();  // poly = 0
        Polynomial right = new Polynomial();
        for(Polynomial poly:constraints.exprs)
        {
            Polynomial h = generateHPolynomial(Parser.allVars,Main.mu);
            Polynomial sos = generateSOSPolynomial(Parser.allVars,Main.mu);
//            System.err.println("h: "+h.toNormalString());
//            System.err.println("sos: "+sos.toNormalString());
//            System.err.println("-------------------------");
//            System.exit(0);
            equalities.addAll(makeEqualities(h,sos));
            h.multiplyByPolynomial(poly);
            right.add(h);
        }
        equalities.addAll(makeEqualities(objective,right));
        return equalities;
    }

    private Polynomial generateHPolynomial(Set<String> vars,int degree)
    {
        Set<Monomial> monomials= Monomial.getAllMonomials(vars,degree);
        Polynomial h=new Polynomial();
        for(Monomial m:monomials)
        {
            Monomial mp=m.deepCopy();
            mp.addVar("t_"+InvariantGeneration.tCount,1);
            InvariantGeneration.tCount++;
            h.add(mp,Rational.one);
        }
        return h;
    }

    private Polynomial generateSOSPolynomial(Set<String> vars, int degree) // ret = y LL^T y^t
    {
//        if(degree==0) //NOTE: if not comment it will be Farkas when mu=0
//        {
//            String var = "l_"+InvariantGeneration.lCount;
//            InvariantGeneration.lCount++;
//            InvariantGeneration.nonNegativeLvars.add(var);
//            return new Polynomial(var);
//        }
        Vector<Monomial> tmp = new Vector<>(Monomial.getAllMonomials(vars,degree/2));

        Vector<Polynomial> y=new Vector<>();
        int dim = tmp.size();
        Polynomial[][] L = new Polynomial[dim][dim],Lt=new Polynomial[dim][dim],yt=new Polynomial[dim][1];

        for(int i=0;i<dim;i++)
        {
            y.add(new Polynomial(tmp.elementAt(i)));
            yt[i][0]=new Polynomial(tmp.elementAt(i));
        }

        for(int i=0;i<dim;i++)
            for (int j=0;j<dim;j++)
            {
                if (j <= i)
                {
                    String var = "l_" + InvariantGeneration.lCount;
                    InvariantGeneration.lCount++;
                    L[i][j] = new Polynomial(var);
                    Lt[j][i] = L[i][j].deepCopy();
                    if (i == j)
                        InvariantGeneration.nonNegativeLvars.add(var);
                }
                else
                {
                    L[i][j] = new Polynomial();
                    Lt[j][i] = L[i][j].deepCopy();
                }
            }

        Vector<Polynomial> yL = mulVecMat(y,L);
        Vector<Polynomial> yLLt = mulVecMat(yL,Lt);
        Polynomial ret= new Polynomial();
        for(int i=0;i<dim;i++)
            ret.add(Polynomial.mul(yLLt.elementAt(i), yt[i][0]));

//        System.err.println("SOS: "+ret.toNormalString());
        return ret;
    }

    private Vector<Polynomial> mulVecMat(Vector<Polynomial> y,Polynomial[][] L)
    {
        Vector<Polynomial> ret= new Vector<>();
        int sz=y.size();
        for(int col=0;col<sz;col++)
        {
            Polynomial p=new Polynomial();
            for(int i=0;i<sz;i++)
                p.add(Polynomial.mul(y.elementAt(i),L[i][col]));
            ret.add(p);
        }
        return ret;
    }

    Vector<Polynomial> makeEqualities(Polynomial left, Polynomial right)
    {
        Set<Monomial> allMonomials= new TreeSet<>();
        allMonomials.addAll(left.getProgramVariableMonomials());
        allMonomials.addAll(right.getProgramVariableMonomials());

        Vector<Polynomial> ret=new Vector<>();
        for(Monomial m:allMonomials)
        {
            Polynomial leftm=left.getCoef(m),rightm=right.getCoef(m);
            rightm.add(leftm.negate());
            ret.add(rightm);
        }

        return ret;
    }

    public Set<Monomial> getAllVars()
    {
        Set<Monomial> ret=new TreeSet<>();
        for(Polynomial lc: constraints.exprs)
            ret.addAll(lc.terms.keySet());
//        for(Polynomial qc: nodeConstraint.exprs)
//            ret.addAll(qc.terms.keySet());
        ret.addAll(objective.terms.keySet());
        return ret;
    }

//    public QuadraticCombination makeEquality(String var)
//    {
//        QuadraticCombination qc = new QuadraticCombination();
//        int dIndex = startDIndex;
//        if (!invConstraint.exprs.isEmpty())
//        {
//            //for(int i=0;i<invConstraint.exprs.size();i++)
//            for (QuadraticCombination invc : invConstraint.exprs)
//            {
//                if (invc.coef.containsKey(var))
//                {
//                    String invMultiplier = "d_" + dIndex;
//                    //InvariantGeneration.addUnknownVar("d_" + dIndex);
//
//
//                    LinearCombination lc = invc.coef.get(var);
//                    qc.add(invMultiplier, lc);
//                }
//                dIndex++;
//            }
//        }
//
//        for (LinearCombination lp : linearConstraints.exprs) // lp>=0
//        {
//            String multiplier = "d_" + dIndex;
//            if (lp.coef.containsKey(var))
//            {
//                Rational coef = lp.coef.get(var);
//                qc.add(multiplier, new LinearCombination(coef));
//                //InvariantGeneration.addUnknownVar("d_" + dIndex);
//            }
//            dIndex++;
//        }
//
//        LinearCombination coef = objective.getCoef(var);
//        //qc=coef  <=>  qc-coef=0
//        if (coef != null)
//        {
//            LinearCombination lc = coef.negate();
//            qc.add(lc);
//        }
////        System.err.println("var: "+var+" => "+qc.toNormalString());
//        return qc;
//    }

    public String toString()
    {
        String ret = "";
        ret += "\n---------------------------------------------\n";
        ret += "from: " + startNode + " to: " + endNode + "\n";
        int dIndex = startDIndex;
//        for (int i = 0; i < nodeConstraint.exprs.size(); i++)
//        {
//            ret += "d_" + dIndex + ": " + nodeConstraint.exprs.elementAt(i).toNormalString() + "\n";
//            dIndex++;
//        }
        for (Polynomial lc : constraints.exprs)
        {
            ret += "\nd_" + dIndex + ": " + lc.toNormalString();
            dIndex++;
        }
        ret += "\n---------------------------------------------\n";

        ret += objective.toNormalString();
        return ret;
    }
}
