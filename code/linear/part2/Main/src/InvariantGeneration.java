import java.util.*;

public class InvariantGeneration
{
    public static Map<String,String> nondetVarsC=new HashMap<>();
    public static int totalUnknownVars=0;
    public static int cCount=0;


    public static void MakeTemplate(int con,int dis,CFGNode startNode,int counter)//conjunctions, disjunctions
    {
        for(CFGNode n:CFGNode.allCFGNodes)
        {
            if(n.id==Main.termNode.id && counter==1)
            {
                //n.inv does not change. the previous computed invariant is used as the BI of the start location of BCFG.
                for(String var:Parser.allVars)
                    if(var.startsWith("_a_") || var.startsWith("_b_"))
                    {
                        String cVar="c_"+cCount;
                        cCount++;
                        nondetVarsC.put(var,cVar);
                    }
            }
            else if(n.isCutPoint)
            {
                n.inv.clear();
                for(int i=0;i<dis;i++)
                {
                    QuadraticPredicate qp = new QuadraticPredicate();
                    for (int j = 0; j < con; j++)
                    {
                        QuadraticCombination qc = new QuadraticCombination();
                        for (String var : Parser.allVars)
                        {
                            if(var.startsWith("_a_") || var.startsWith("_b_"))
                                continue;
                            qc.add(var, new LinearCombination("c_" + cCount));

                            cCount++;
                        }
                        qp.add(qc);
                    }
                    n.inv.add(qp);
                }
            }
        }
    }

    public static void generate(Vector<CFGNode> cutPoints,Vector<Farkas> farkasVector,int counter)
    {
        if(counter==0)  //for startNode
        {
            CFGNode u = Main.startNode;
            QuadraticPredicate objPred=u.inv.lastElement();
            Vector<QuadraticPredicate> tmp = new Vector<>();
            for(QuadraticPredicate qc:u.inv)
                if(qc!=objPred)
                    tmp.add(qc.deepCopy());

            Vector<QuadraticPredicate> uinvNegate=QuadraticPredicate.negate(tmp);
            if(uinvNegate.isEmpty())
                uinvNegate.add(QuadraticPredicate.TRUE);
            for(QuadraticCombination qc:objPred.exprs)
                for(QuadraticPredicate qp:uinvNegate)
                {
                    Farkas farkas=new Farkas(-1,-1);
                    if(qp!=QuadraticPredicate.TRUE)
                        farkas.addInvConstraint(qp.deepCopy());
                    farkas.setObjective(qc);
                    farkasVector.add(farkas);
                }
        }
        for(CFGNode v:cutPoints)
            processPaths(v,v,new Vector<Transition>(),farkasVector,counter);

    }

    private static void processPaths(CFGNode st,CFGNode v,Vector<Transition> path,Vector<Farkas> farkasVector,int counter)
    {
        //System.err.println("processPaths: st:"+st.id+" v:"+v.id+" v.out="+v.out.size()+" v.rout="+v.rout.size()+" counter="+counter);
        Vector<Transition> tran;
        if(counter==0)
            tran=v.out;
        else
            tran=v.rout;
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
                            CFGUtil.weakestPreCondition(path,farkas,counter);

                            farkas.addInvConstraint(vinv);

                            farkasVector.add(farkas);
                        }
            }
            else
                processPaths(st,u,path,farkasVector,counter);
            path.removeElementAt(path.size()-1);
        }
    }

}
