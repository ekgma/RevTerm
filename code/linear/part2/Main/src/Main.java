import java.util.Vector;


public class Main
{
    public static Rational eps=Rational.one;
    public static CFGNode startNode,cutPoint,termNode;
    public static String fileName="",solver="",workingdir="",solversDir="",cpaDir="";
    public static int con=0,dis=0;

    public static void main(String[] args) throws Exception
    {
        con=Integer.parseInt(args[0]);
        dis=Integer.parseInt(args[1]);
        solver=args[2];
        fileName=args[3];
        workingdir=args[4];
        solversDir=args[5];
        cpaDir=args[6];

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
            Transition r=t.reverse();
            RevTransitions.add(r);
            r.v.rout.add(r);
        }

        Vector<CFGNode> cutPoints=CFGUtil.findCutpoints();

        fileName=fileName.replace("/","_");

//        for(Transition t:Transition.allTransitions)
//            System.err.println(t);


        InvariantGeneration.MakeTemplate(1,1,startNode,0);
//        System.err.println("-----------------------Invariant---------------------");
//        for(CFGNode n:CFGNode.allCFGNodes)
//            if(!n.inv.isEmpty())
//                System.err.println(n.id+": "+n.inv.firstElement().toNormalString());
//        System.err.println("-----------------------Invariant---------------------");

        Vector <Farkas> InvFarkas=new Vector<>();
        InvariantGeneration.generate(cutPoints,InvFarkas,0);
        //System.err.println("number of Farkas for CFG= "+InvFarkas.size());

        InvariantGeneration.MakeTemplate(con,dis,termNode,1);
        termNode.preCondition=termNode.inv.firstElement();
//        System.err.println("-----------------------Backward Invariant---------------------");
//        for(CFGNode n:CFGNode.allCFGNodes)
//            for(QuadraticPredicate qp:n.inv)
//                System.err.println(n.id+": "+qp.toNormalString());
//        System.err.println("-----------------------Backward Invariant---------------------");
        InvariantGeneration.generate(cutPoints,InvFarkas,1);

        Vector<Farkas> inductiveness=new Vector<>();
        int Dcount=Farkas.countD;
        InvariantGeneration.generate(cutPoints,inductiveness,0);
        Farkas.countD=Dcount;

//        for(Farkas farkas:InvFarkas)
//            System.err.println(farkas);
        boolean result=InvUtil.checkNonTermination(InvFarkas,inductiveness,startNode);
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
