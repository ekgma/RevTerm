import java.util.Vector;


public class Main
{
    public static Rational eps = Rational.one;
    public static CFGNode startNode, cutPoint, termNode;
    public static String fileName = "", solver = "",workingdir="",solversDir="";
    public static int con = 0, dis = 0,degree=0,mu=0;
    //con = number of conjunctions, dis = number of disjunctions, degree = degree of invariants, mu = degree of SOS polynomials
    public static void main(String[] args) throws Exception
    {
//        Polynomial temp = new Polynomial("x",Rational.one);
//        temp.add(new Monomial("x",2),Rational.one);
//        System.err.println(temp.toString());
//        System.exit(0);
//        Polynomial upd = temp.getCoef(new Monomial("x",2));
//        System.err.println("coef: " + upd.toNormalString());
//
////        temp.replaceVarWithPoly("x",upd);
////        System.err.println(temp.toNormalString());
//        System.exit(0);

        con = Integer.parseInt(args[0]);
        dis = Integer.parseInt(args[1]);
        degree = Integer.parseInt(args[2]);
        mu = Integer.parseInt(args[3]);
        solver = args[4];
        fileName = args[5];
        workingdir = args[6];
        solversDir = args[7];

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

        /////////////////////////////OK//////////////////////////////////

        InvariantGeneration.MakeTemplate(con,dis);


//        for(CFGNode n:cutPoints)
//            for(int i=0;i<n.inv.size();i++)
//                System.err.println("id: "+n.id+" inv: "+n.inv.elementAt(i).toNormalString());



        Vector<Putinar> invPutinar = new Vector<>();
        InvariantGeneration.generate(cutPoints,invPutinar);


//        for(Putinar putinar:invPutinar)
//            System.err.println(putinar.toString());
        boolean result = InvUtil.checkNonTermination(invPutinar, startNode);
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