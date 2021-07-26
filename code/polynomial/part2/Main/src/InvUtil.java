import java.io.*;
import java.nio.channels.FileLockInterruptionException;
import java.util.Scanner;
import java.util.Vector;
//TODO: update checkNonTermination

public class InvUtil
{
    public static boolean checkNonTermination(Vector<Polynomial> I,Vector<Putinar> Inductive,CFGNode startNode) throws Exception
    {
        String Template="";//InvariantGeneration.cCount+" "+Farkas.countD+" "+Parser.allVars.size()+"\n";
        Template+="(set-option :print-success false)    \n" +
                "(set-option :produce-models true)\n" ;
        if(Main.solver.equals("bclt"))
        {
            Template+="(set-option :produce-assertions true)\n" +
                    "(set-logic QF_NIA)\n";
        }
        for(int i=0;i<InvariantGeneration.cCount;i++)
            Template+="(declare-const c_"+i+" Int)\n";
        for(int i=0;i<InvariantGeneration.lCount;i++)
            Template+="(declare-const l_"+i+" Int)\n";
        for(String lvar: InvariantGeneration.nonNegativeLvars)
            Template+="(assert (>= "+lvar+ " 0))\n";
        for(int i=0;i<InvariantGeneration.tCount;i++)
            Template+="(declare-const t_"+i+" Int)\n";


        for(String var: Parser.allVars)
            Template += "(declare-const " + var + " Int)\n";
        for(int i=0;i<Parser.nondetCount;i++)
            Template+="(declare-const "+"_r_"+i+" Int)\n";

        FileWriter fw = new FileWriter(Main.workingdir+"/"+Main.solver+Main.con+"-"+Main.dis+Main.fileName+".smt2");
        fw.write(Template);

        //Inductive Backward Invariant:
        for(Polynomial f:I)
            fw.write("(assert (= 0 "+f.toString()+"))\n");

        //Not Inductive Invariant:
        fw.write("(assert (or ");


        for(Putinar putinar: Inductive)
        {
            fw.write("(and ");
            for(Polynomial q:putinar.constraints.exprs)
                fw.write("(>= "+ q.toString()+" 0) ");

            fw.write("(< "+putinar.objective+" 0)");
            fw.write(")\n");
        }
        fw.write("))\n");

        fw.write("(check-sat)\n");
        fw.write("(get-value (");
        for(int i=0;i<InvariantGeneration.cCount;i++)
            fw.write("c_"+i+" ");
        for(String var:Parser.allVars)
            if(!var.equals("1") && !var.startsWith("_a_") && !var.startsWith("_b_"))
                fw.write(var + " ");

        fw.write("))\n");

        fw.close();

//        System.exit(0);
        if(check())
            return true;
        return false;
    }

    public static boolean check() throws Exception
    {
        String smtFile=Main.workingdir+"/"+Main.solver+Main.con+"-"+Main.dis+Main.fileName;
        String[] configs = {"bclt --file", "mathsat","z3 -smt2 "};
        int solverInd = -1;
        if (Main.solver.equals("bclt"))
            solverInd = 0;
        else if(Main.solver.equals("mathsat"))
            solverInd=1;
        else if(Main.solver.equals("z3"))
            solverInd=2;
//        System.err.println(smtFile);
//        System.err.println("Solver Started");
        Process process = Runtime.getRuntime().exec("./"+Main.solversDir+"/"+configs[solverInd] + " " + smtFile + ".smt2");
        process.waitFor();

        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(process.getInputStream()));
        FileWriter fw=new FileWriter(smtFile+".result");

        boolean isSAT=false;

        while (bufferedReader.ready())
        {
            String s = bufferedReader.readLine();
//            System.err.println(s);
            fw.write(s+"\n");
            if (s.equals("sat"))
            {
//                System.err.println("SAT!");
                isSAT=true;
            }
        }
        fw.close();
//        System.err.println("Solver Finished");
        if(isSAT==false)
            return false;
        else
            return safetyUtil.check(smtFile);
    }
}
