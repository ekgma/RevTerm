import java.io.*;
import java.nio.channels.FileLockInterruptionException;
import java.util.Scanner;
import java.util.Vector;
//TODO: update checkNonTermination

public class InvUtil
{
    public static boolean checkNonTermination(Vector<Farkas> I,Vector<Farkas> Inductive,CFGNode startNode) throws Exception
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
        for(int i=0;i<Farkas.countD;i++)
        {
            Template += "(declare-const d_" + i + " Int)\n";
            Template+="(assert (>= d_"+i+" 0))\n";            // d_i>=0
        }

        for(String var: Parser.allVars)
            if(!var.equals("1"))
                Template += "(declare-const " + var + " Int)\n";
        for(int i=0;i<Parser.nondetCount;i++)
            Template+="(declare-const "+"_r_"+i+" Int)\n";

        FileWriter fw = new FileWriter(Main.workingdir+"/"+Main.solver+Main.con+"-"+Main.dis+Main.fileName+".smt2");
        fw.write(Template);

        //Inductive Backward Invariant:
        for(Farkas f:I)
        {
            Vector<QuadraticCombination> vqc=f.generateEqualities();
            for(QuadraticCombination qc:vqc)
                fw.write("(assert (= 0 "+qc.toString()+"))\n");
        }

        //Not Inductive Invariant:
        fw.write("(assert (or ");


        for(Farkas farkas: Inductive)
        {
            fw.write("(and ");
            for(QuadraticCombination q:farkas.invConstraint.exprs)
                fw.write("(>= "+ q.toString()+" 0) ");
            for(LinearCombination lc: farkas.linearConstraints.exprs)
                if(lc.coef.size()==1 && lc.coef.containsKey("1"))  //1>=0 is not necessary
                    continue;
                else
                    fw.write("(>= "+ lc.toString()+" 0) ");

            fw.write("(< "+farkas.objective+" 0)");
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

        if(isSAT==false)
        {
            return false;
        }
        else
            return safetyUtil.check(smtFile);
    }
}
