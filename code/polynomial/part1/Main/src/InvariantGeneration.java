import java.util.*;

public class InvariantGeneration
{
    public static Map<String, String> nondetVarsC = new HashMap<>();
    public static String negativeVar;
    public static int totalUnknownVars = 0;
    public static int cCount = 0,lCount=0,tCount=0;
    public static Vector<String> nonNegativeLvars=new Vector<>();


    public static void MakeTemplate(int con,int dis)//conjunctions, disjunctions
    {
        for (CFGNode n : CFGNode.allCFGNodes)
        {
            if (n.id == Main.startNode.id)
            {
                PolynomialPredicate pp = new PolynomialPredicate();
                for (String var : Parser.allVars) // var = c_j
                {
                    Polynomial p = new Polynomial();
                    Rational minusOne = Rational.negate(Rational.one);
                    p.add(new Monomial(var),minusOne); // p = -var

                    p.add(new Monomial("c_"+cCount),Rational.one); // qc = -var + c_cCount
                    nondetVarsC.put(var,"c_"+cCount);
                    cCount++;

                    pp.add(p); // p>=0
                    pp.add(p.deepCopy().negate()); // -p>=0

                }
                n.inv.add(pp);
            }
            else if(n.id == Main.termNode.id) // -1 >=0
            {
                PolynomialPredicate pp = new PolynomialPredicate();
                Polynomial p = new Polynomial();

                negativeVar="c_"+cCount;
                cCount++;
                p.add(new Monomial(negativeVar),Rational.one);
                pp.add(p);
                n.inv.add(pp);
            }
            else if(n.isCutPoint)
            {
                for(int k=0;k<dis;k++)
                {
                    PolynomialPredicate pp = new PolynomialPredicate();
                    for (int j = 0; j < con; j++)
                    {
                        Polynomial p = new Polynomial(); // c_0 * 1 + c_1 * var_1 + c_2 * var2 .... + c_n * var_n >=0
                        Set<Monomial> allMonomials= Monomial.getAllMonomials(Parser.allVars,Main.degree);
                        for (Monomial m : allMonomials)
                        {
                            m.addVar("c_"+cCount,1);
                            p.add(m,Rational.one); // p+= var * c_cCount
                            cCount++;
                        }
                        pp.add(p); // p>=0
                    }
                    n.inv.add(pp);
                }
            }
        }
    }


    public static void generate(Vector<CFGNode> cutPoints,Vector<Putinar> putinarVector)
    {
        for(CFGNode v:cutPoints)
            processPaths(v,v,new Vector<Transition>(),putinarVector);
    }

    private static void processPaths(CFGNode st,CFGNode v,Vector<Transition> path,Vector<Putinar> putinarVector)
    {
        Vector<Transition> tran=v.out;

        for(Transition t:tran)
        {
            CFGNode u=t.u;
            path.add(t);

            if(u.isCutPoint)
            {
                PolynomialPredicate objPred = u.inv.lastElement();
                Vector<PolynomialPredicate> tmp = new Vector<>();
                for(PolynomialPredicate pp:u.inv)
                    if(pp!=objPred)
                        tmp.add(pp.deepCopy());

                Vector<PolynomialPredicate> uinvNegate = PolynomialPredicate.negate(tmp,0);
                if(uinvNegate.isEmpty())
                    uinvNegate.add(PolynomialPredicate.TRUE);
                for(PolynomialPredicate vinv:st.inv)
                    for(PolynomialPredicate uinv:uinvNegate)
                        for(Polynomial obj:objPred.exprs)
                        {
                            //vinv & uinv & path => obj
                            Putinar putinar=new Putinar(st.id,u.id);
                            if(uinv!=PolynomialPredicate.TRUE)
                                putinar.addPredicate(uinv.deepCopy());
                            putinar.setObjective(obj.deepCopy());
                            CFGUtil.weakestPreCondition(path,putinar);

                            putinar.addPredicate(vinv);
                            putinarVector.add(putinar);
                        }

            }
            else
                processPaths(st,u,path,putinarVector);

            path.removeElementAt(path.size()-1);
        }
    }
}