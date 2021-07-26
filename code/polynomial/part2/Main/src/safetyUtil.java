import java.io.File;
import java.io.FileWriter;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeMap;

public class safetyUtil
{

    public static boolean check(String fileName) throws Exception
    {
        Map<String,Integer> dict = getSMTResults(fileName);
        replaceAllVariables(dict);
        convertToC(fileName,dict);
        return hasPath(fileName);
    }

    public static Map<String,Integer> getSMTResults(String fileName) throws Exception
    {
        Map<String,Integer> res=new TreeMap<>();
        File f=new File(fileName+".result");
        Scanner in=new Scanner(f);
        in.nextLine(); //skip the "sat"
        while(in.hasNextLine())
        {
            String s=in.nextLine();
            s=takePars(s);

            //System.err.println("s = " + s);
            Scanner tmp=new Scanner(s);
            if(!tmp.hasNext())
                continue;
            String var=tmp.next();

            int val=1;
            String p=tmp.next();
            if(p.equals("-"))
            {
                val *= -1;
                p=tmp.next();
            }

            val*=Integer.parseInt(p);
            if(!Parser.allVars.contains(var))
                res.put(var,val);
        }
//        for(String var:res.keySet())
//            System.err.println(var+" = "+res.get(var));
        return res;
    }
    public static String takePars(String s)
    {
        //System.err.println(s);
        String ret=s.replace("("," ");
        ret=ret.replace(")"," ");
        return ret;
    }

    public static void replaceAllVariables(Map<String,Integer> dict) throws Exception
    {
        for(CFGNode n:CFGNode.allCFGNodes)
        {
            for(PolynomialPredicate qc:n.inv)
            {
                PolynomialPredicate lc=qc.replaceVarsWithValue(dict);
                //System.err.println(n.id+": "+lc);
                n.computedInv.add(lc);
            }
        }

    }

    public static void convertToC(String fileName, Map<String,Integer> dict) throws Exception
    {
        int nondetCnt=0;
        FileWriter fw=new FileWriter(fileName+".c");
        fw.write("int main()\n{\n");
        for(String var:Parser.allVars)
            if(!var.equals("1") && !var.startsWith("_a_") && !var.startsWith("_b_"))
                fw.write("int "+var+";\n");
        fw.write("if(");
        boolean isFirst=true;
        for(String var:Parser.allVars)
            if(!var.equals("1") && !var.startsWith("_a_") && !var.startsWith("_b_"))
            {
                if(!isFirst)
                    fw.write(" || ");
                else
                    isFirst=false;
                fw.write(var + ">100000 || " + var + "<-100000 ");
            }
        fw.write(")\nreturn 0;\n");
        fw.write("\ngoto START;\n");
        for(CFGNode n:CFGNode.allCFGNodes)
        {
            fw.write(getlocName(n)+":\n");

            //if(!BI) goto ERROR
            if(!n.computedInv.isEmpty())
            {
//                System.err.println("computed: ");
//                for(LinearPredicate lp:n.computedInv)
//                    System.err.println(lp.toString());
//                System.err.println("actual: ");
//                for(QuadraticPredicate qp:n.inv)
//                    System.err.println(qp.toNormalString());

                fw.write("if(!(");
                for (int i = 0; i < n.computedInv.size(); i++)
                {
                    PolynomialPredicate lp = n.computedInv.elementAt(i);
                    if (i != 0)
                        fw.write(" || ");
                    fw.write("("+lp.toString()+")");
                }
                fw.write("))\n");
                fw.write("goto ERROR;\n");
            }
            //

            for(Transition t:n.out)
            {
                if(t.detGuard.exprs.size()!=0)
                    fw.write("if( "+t.detGuard+" )\n{\n");
                for(int i=0;i<t.varName.size();i++)
                {
                    String var=t.varName.elementAt(i);
                    Polynomial upd=t.update.elementAt(i);
                    if(upd.toNormalString().contains("_r_"))
                    {
                        fw.write(var + " = nondet" + nondetCnt + "();\n");
                        nondetCnt++;
                    }
                    else
                        fw.write(var+" = "+upd.toNormalString()+";\n");
                }
                fw.write("goto "+getlocName(t.u)+";\n");
                if(t.detGuard.exprs.size()!=0)
                    fw.write("}\n");
            }
            fw.write("return 0;\n");
        }
        fw.write("ERROR:\nreturn (-1);\n");
        fw.write("}");
        fw.close();
    }

    public static String getlocName(CFGNode n) throws Exception
    {
        if(n.id>=0)
            return "location"+n.id;
        else if(n.id==-1)
            return "START";
        else if(n.id==-2)
            return "TERM";
        else
            throw new Exception("node without known location id "+n.id);
    }

    public static boolean hasPath(String fileName) throws Exception
    {
        String []configurations={"predicateAnalysis.properties","bmc-incremental.properties","valueAnalysis-NoCegar.properties","valueAnalysis-Cegar.properties","kInduction-linear.properties"};
        String config=configurations[4];
//        System.err.println("Checker Started");
        ProcessBuilder processBuilder = new ProcessBuilder("./"+Main.cpaDir+"/scripts/cpa.sh","-noout", "-config","cpachecker/config/"+config,fileName+".c")
                .redirectOutput(new File(fileName+"_cpa_out.txt"));
        Process process=processBuilder.start();
        process.waitFor();
        File output=new File(fileName+"_cpa_out.txt");
        Scanner in=new Scanner(output);
        while(in.hasNextLine())
        {
            String s=in.nextLine();
            if (s.contains("FALSE."))
                return true;
        }
        return false;
    }

}
