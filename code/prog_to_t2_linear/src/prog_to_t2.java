import java.io.FileWriter;

public class prog_to_t2
{
    public static Rational eps=Rational.one;
    public static void main(String[] args) throws Exception
    {
        String input=args[0];
        String output=args[1];

        int con=1,dis=2;
        long startTime=System.currentTimeMillis();
        //System.exit(0);
        Parser.readFile(input);

        //CFG.startNode=new Node(null,-1,-1,"startNode","",false);

        Node root=Parser.parseStmtList(null,0,Parser.getTokenCount()-1,false);
        //CFG.startNode.preCondition=root.preCondition;

//        for(Node n:Node.allNodes)
//        {
//            System.err.println("---------------------------");
//            System.err.println(n.toString());
//            System.err.println("---------------------------");
//        }

        CFG.endNode=new Node(null,-1,-1,"endNode","",false);
        PairNode p=CFG.make(root);
        CFG.startNode=p.first;


        CFG.edges.add(CFG.getTransition(p.second,CFG.endNode,"to end"));


        FileWriter fw=new FileWriter(output);
        fw.write("START: "+CFG.startNode.id+";\n\n");
        for(Transition t:CFG.edges)
            fw.write(t.toString()+"\n\n");
        fw.close();
    }
}
