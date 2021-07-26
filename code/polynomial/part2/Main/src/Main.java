import java.util.Vector;


public class Main
{
    public static Rational eps=Rational.one;
    public static CFGNode startNode,cutPoint,termNode;
    public static String fileName="",solver="",workingdir="",solversDir="",cpaDir="";
    public static int con=0,dis=0,degree=0,mu=0;

    public static void main(String[] args) throws Exception
    {
        con=Integer.parseInt(args[0]);
        dis=Integer.parseInt(args[1]);
        degree = Integer.parseInt(args[2]);
        mu = Integer.parseInt(args[3]);
        solver=args[4];
        fileName=args[5];
        workingdir=args[6];
        solversDir=args[7];
        cpaDir=args[8];

        termNode=CFGNode.addNode(-2);
        startNode= CFGNode.addNode(-1);

        long startTime=System.currentTimeMillis();
        Parser.readFile(fileName);
        Parser.parseProg(0,Parser.getTokenCount()-1);


        for(CFGNode n:CFGNode.allCFGNodes)
        {
            if(n.id==-1 || n.id==-2)
                continue;
            n.addTerminalTransitions();
        }

        int curTotalCFGNodes=CFGNode.allCFGNodes.size();
        for(int i=0;i<curTotalCFGNodes;i++)
        {
            CFGNode n=CFGNode.allCFGNodes.elementAt(i);
            if(n.id==-2 || n.id==-1)
                continue;
            n.addNecessaryNondet();
        }

        //this is done after parsing because we need to have the list of all variables.
        for(Transition t:Transition.allTransitions)
            t.addNondetTemplate();

        Vector<Transition> RevTransitions=new Vector<>();
        for(Transition t:Transition.allTransitions)
        {
            Vector<Transition> r=t.reverse();
            RevTransitions.addAll(r);
            for(Transition tau:r)
                tau.v.rout.add(tau);

        }

        Vector<CFGNode> cutPoints=CFGUtil.findCutpoints();

        fileName=fileName.replace("/","_");

//        for(Transition t:RevTransitions)
//            System.err.println(t);
//        System.exit(0);

        InvariantGeneration.MakeTemplate(1,1,startNode,0);
//        System.err.println("-----------------------Invariant---------------------");
//        for(CFGNode n:CFGNode.allCFGNodes)
//            if(!n.inv.isEmpty())
//                System.err.println(n.id+": "+n.inv.firstElement().toNormalString());
//        System.err.println("-----------------------Invariant---------------------");

        Vector <Putinar> InvPutinar=new Vector<>();
        InvariantGeneration.generate(cutPoints,InvPutinar,0);

        InvariantGeneration.MakeTemplate(con,dis,termNode,1);
        termNode.preCondition=termNode.inv.firstElement();
//        System.err.println("-----------------------Backward Invariant---------------------");
//        for(CFGNode n:CFGNode.allCFGNodes)
//            for(PolynomialPredicate qp:n.inv)
//                System.err.println(n.id+": "+qp.toNormalString());
//        System.err.println("-----------------------Backward Invariant---------------------");
        InvariantGeneration.generate(cutPoints,InvPutinar,1);
//        for(Putinar p:InvPutinar)
//            System.err.println(p.toString());

        Vector<Putinar> inductiveness=new Vector<>();
        InvariantGeneration.generate(cutPoints,inductiveness,0);

        Vector<Polynomial> invEqualities=new Vector<>();
        for(Putinar putinar: InvPutinar)
            invEqualities.addAll(putinar.generateEqualities());
        boolean result=InvUtil.checkNonTermination(invEqualities,inductiveness,startNode);
        if(result) //does not terminate
            System.out.println("Non-Terminating");
        else
            System.out.println("Could Not Prove Non-Termination");
        long endTime=System.currentTimeMillis();
        System.out.println("total time used: "+ (endTime-startTime));
        int val=(result) ? 3 : 0;
        System.exit(val);

    }


}
