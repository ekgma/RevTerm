import java.util.*;

public class InvariantGeneration
{
    public static Map<String, String> nondetVarsC = new HashMap<>();
    public static String negativeVar;
    public static int totalUnknownVars = 0;
    public static int cCount = 0;


    public static void MakeTemplate(int con,int dis)//conjunctions, disjunctions
    {
        for (CFGNode n : CFGNode.allCFGNodes)
        {
            if (n.id == Main.startNode.id)
            {
                QuadraticPredicate qp = new QuadraticPredicate();
                for (String var : Parser.allVars) // var = c_j
                {
                    if (var.equals("1"))
                        continue;
                    QuadraticCombination qc = new QuadraticCombination();
                    Rational minusOne = Rational.negate(Rational.one);
                    qc.add(var, new LinearCombination(Rational.one)); // qc = -var

                    qc.add("1", new LinearCombination("c_" + cCount, minusOne));  // qc = -var + c_cCount
                    nondetVarsC.put(var,"c_"+cCount);

                    cCount++;


                    qp.add(qc);    //qc>=0
                    qp.add(qc.deepCopy().negate());  // -qc>=0
                }
                n.inv.add(qp);
            }
            else if(n.id == Main.termNode.id) // -1 >=0
            {
                QuadraticPredicate qp = new QuadraticPredicate();
                QuadraticCombination qc = new QuadraticCombination();
                negativeVar="c_"+cCount;
                cCount++;
                qc.add("1",new LinearCombination(negativeVar,Rational.one));

                qp.add(qc);
                n.inv.add(qp);
            }
            else if(n.isCutPoint)
            {
                for(int k=0;k<dis;k++)
                {
                    QuadraticPredicate qp = new QuadraticPredicate();
                    for (int j = 0; j < con; j++)
                    {
                        QuadraticCombination qc = new QuadraticCombination();  // c_0 * 1 + c_1 * var_1 + c_2 * var2 .... + c_n * var_n >=0
                        for (String var : Parser.allVars)
                        {
                            qc.add(var, new LinearCombination("c_" + cCount));   //qc += var * c_cCount
                            cCount++;
                        }
                        //
                        qp.add(qc);  //qc >=0
                    }
                    n.inv.add(qp);
                }
            }
        }
    }


    public static void generate(Vector<CFGNode> cutPoints,Vector<Farkas> farkasVector)
    {
        for(CFGNode v:cutPoints)
            processPaths(v,v,new Vector<Transition>(),farkasVector);
    }

    private static void processPaths(CFGNode st,CFGNode v,Vector<Transition> path,Vector<Farkas> farkasVector)
    {
        Vector<Transition> tran=v.out;

        for(Transition t:tran)
        {
            CFGNode u=t.u;
            path.add(t);


            if(u.isCutPoint)
            {
                QuadraticPredicate objPred=u.inv.lastElement();
                Vector<QuadraticPredicate> tmp = new Vector<>();
                for(QuadraticPredicate qc:u.inv)
                    if(qc!=objPred)
                        tmp.add(qc.deepCopy());

                Vector<QuadraticPredicate> uinvNegate=QuadraticPredicate.negate(tmp);
                if(uinvNegate.isEmpty())
                    uinvNegate.add(QuadraticPredicate.TRUE);

                for(QuadraticPredicate vinv:st.inv)
                    for(QuadraticPredicate uinv:uinvNegate)
                        for(QuadraticCombination obj:objPred.exprs)
                        {
                            //vinv & uinv & path => obj
                            Farkas farkas=new Farkas(st.id,u.id);
                            if(uinv!=QuadraticPredicate.TRUE)
                                farkas.addInvConstraint(uinv.deepCopy());
                            farkas.setObjective(obj.deepCopy());
                            CFGUtil.weakestPreCondition(path,farkas);

                            Set<String> vars=farkas.getAllVars();

                            farkas.addInvConstraint(vinv);

                            farkasVector.add(farkas);
                        }
            }
            else
                processPaths(st,u,path,farkasVector);
            path.removeElementAt(path.size()-1);
        }
    }



}