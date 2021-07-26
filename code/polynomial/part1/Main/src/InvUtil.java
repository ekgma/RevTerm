import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.util.Vector;


public class InvUtil
{
    public static boolean checkNonTermination(Vector<Putinar> I, CFGNode startNode) throws Exception
    {

        Vector<Polynomial> equalities = new Vector<>();
        for(Putinar putinar: I)
            equalities.addAll(putinar.generateEqualities());

        String Template = "";//InvariantGeneration.cCount+" "+Farkas.countD+" "+Parser.allVars.size()+"\n";
        Template += "(set-option :print-success false)    \n";

        if (Main.solver.equals("bclt"))
        {
            Template +="(set-option :produce-models true)\n"+
                    "(set-option :produce-assertions true)\n" +
                    "(set-logic QF_NIA)\n";
        }
        for (int i = 0; i < InvariantGeneration.cCount; i++)
            Template += "(declare-const c_" + i + " Int)\n";
        Template+="(assert (< "+InvariantGeneration.negativeVar+" 0))\n"; //invariant at l_term

//        for (int i = 0; i < Putinar.countD; i++)
//        {
//            Template += "(declare-const d_" + i + " Int)\n";
//            Template += "(assert (>= d_" + i + " 0))\n";            // d_i>=0
//        }
        for(int i=0;i<InvariantGeneration.lCount;i++)
            Template += "(declare-const l_"+i + " Int)\n";
        for(String lvar:InvariantGeneration.nonNegativeLvars)
            Template += "(assert (>= " + lvar + " 0))\n";            // lvar>=0

        for(int i=0;i<InvariantGeneration.tCount;i++)
            Template += "(declare-const t_"+i+" Int)\n";


        for (String var : Parser.allVars)
            if (!var.equals("1"))
                Template += "(declare-const " + var + " Int)\n";

        for (Polynomial qc : startNode.inv.firstElement().exprs)
            Template += "(assert (>= " + qc.toString() + " 0))\n";

        FileWriter fw = new FileWriter(Main.workingdir+"/"+Main.solver + Main.con+"-"+Main.dis+Main.fileName + ".smt2");
        fw.write(Template);
        for (Polynomial e : equalities)
        {
            //System.err.println(qc.toNormalString());
            fw.write("(assert (= 0 " + e.toString() + "))\n");
            //System.err.println(qc.toNormalString());
        }



        fw.write("(check-sat)\n");
        fw.write("(get-value (");
        for (int i = 0; i < InvariantGeneration.cCount; i++)
            fw.write("c_" + i + " ");
        for (String var : Parser.allVars)
            if (!var.equals("1"))
                fw.write(var + " ");

        fw.write("))");

        fw.close();
        return check();
    }

    public static boolean check() throws Exception
    {
//        System.err.println("Solver Started");
        String[] configs= {"bclt --file", "z3 -smt2", "mathsat "};
        int solverInd = -1;
        if (Main.solver.equals("bclt"))
            solverInd = 0;
        else if(Main.solver.equals("z3"))
            solverInd=1;
        else if(Main.solver.equals("mathsat"))
            solverInd=2;
        Process process = Runtime.getRuntime().exec("./"+Main.solversDir+"/"+configs[solverInd] + " " + Main.workingdir+"/"+Main.solver + Main.con+"-"+Main.dis+Main.fileName+".smt2");
        process.waitFor();
//        if(!process.waitFor(10, TimeUnit.SECONDS))
//        {
//            process.destroy();
//            return false;
//        }
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while (bufferedReader.ready())
        {
            String s = bufferedReader.readLine();
            if (s.equals("sat"))
            {
//                System.err.println("SAT!");
                return true;
            }
            else if (s.equals("unsat"))
            {
//                System.err.println("UNSAT!");
                return false;
            }
        }
        return false;
    }
}