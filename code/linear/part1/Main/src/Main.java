import java.util.Vector;


public class Main
{
    public static Rational eps = Rational.one;
    public static CFGNode startNode, cutPoint, termNode;
    public static String fileName = "", solver = "",workingdir="",solversDir="";
    public static int con = 0, dis = 0;

    public static void main(String[] args) throws Exception
    {
        con = Integer.parseInt(args[0]);
        dis = Integer.parseInt(args[1]);
        solver = args[2];
        fileName = args[3];
        workingdir = args[4];
        solversDir = args[5];

        termNode = CFGNode.addNode(-2);
        startNode = CFGNode.addNode(-1);

        long startTime = System.currentTimeMillis();
        Parser.readFile(fileName);
        Parser.parseProg(0, Parser.getTokenCount() - 1);




        for (CFGNode n : CFGNode.allCFGNodes)
        {
            if (n.id == -1 || n.id == -2)
                continue;
            n.addTerminalTransitions();
        }

        int curTotalCFGNodes = CFGNode.allCFGNodes.size();
        for (int i = 0; i < curTotalCFGNodes; i++)
        {
            CFGNode n = CFGNode.allCFGNodes.elementAt(i);
            if (n.id == -2 || n.id == -1)
                continue;
            n.addNecessaryNondet();
        }

        //this is done after parsing because we need to have the list of all variables.
        for(Transition t:Transition.allTransitions)
            t.addNondetTemplate();

//        System.err.println("Parsing Finished");

        Vector<CFGNode> cutPoints=CFGUtil.findCutpoints();
//        for(CFGNode x:cutPoints)
//            System.err.println("cutPoint: "+x.id);



        fileName=fileName.replace("/","_");

//        for (Transition t : Transition.allTransitions)
//            System.err.println(t);

        InvariantGeneration.MakeTemplate(con,dis);

//        for(CFGNode n:cutPoints)
//            for(int i=0;i<n.inv.size();i++)
//                System.err.println("id: "+n.id+" inv: "+n.inv.elementAt(i).toNormalString());

        Vector<Farkas> InvFarkas = new Vector<>();
        InvariantGeneration.generate(cutPoints,InvFarkas);

//        for(Farkas farkas:InvFarkas)
//            System.err.println(farkas.toString());
        boolean result = InvUtil.checkNonTermination(InvFarkas, startNode);
        if(result) //does not terminate
            System.out.println("Non-Terminating");
        else
            System.out.println("Could Not Prove Non-Termination");

        long endTime = System.currentTimeMillis();
        System.out.println("total time used: " + (endTime - startTime));
        int val = (result) ? 3 : 0;
        System.exit(val);
    }
}