import java.io.File;
import java.io.FileWriter;
import java.util.Scanner;
import java.util.Vector;

public class C_to_prog
{
    public static void main(String[] args) throws Exception
    {
        String input=args[0];
        String output=args[1];
        Convert(input,output);
    }
    public static void Convert(String input,String output) throws Exception
    {
        File file=new File(input);
        Scanner in=new Scanner(file);
        String program="";
        boolean comment=false;
        Vector <String> scope=new Vector<>();
        int cnt=0;
        scope.add("main");
        while(in.hasNext())
        {
            String s = in.nextLine();
            for(int i=0;i<s.length();i++)
                if(s.charAt(i)!=' ' && s.charAt(i)!='\t')
                {
                    s=s.substring(i,s.length());
                    break;
                }
            s = s.replace("true","1>=0");
            s = s.replace("false","1<=0");
            s = s.replace("&&"," and ");
            s = s.replace("||"," or ");
            s = s.replace("(", " ");
            s = s.replace(")", " ");
            s = s.replace(";", ";\n");

            s = s.replace("__VERIFIER_nondet_int", "_NONDET_");

            //System.err.println(s);
            for (int i = 0; i < s.length(); i++)
            {
                if (i + 2 <= s.length() && s.substring(i, i + 2).equals("/*"))
                {
                    comment = true;
                    continue;
                }
                else if (i + 2 <= s.length() && s.substring(i, i + 2).equals("*/"))
                {
                    comment = false;
                    i++;
                    continue;
                }
                else if (s.contains("typedef") || s.contains("extern") || s.contains("int") || s.contains("return") || (i + 2 <= s.length() && s.substring(i, i + 2).equals("//")))
                    break;
                if (comment)
                    continue;
                if(s.charAt(i)==';')
                    cnt++;
                if (i + 2 <= s.length() && s.substring(i, i + 2).equals("if") && !Character.isAlphabetic(s.charAt(i+2)) && (i==0 || !Character.isAlphabetic(s.charAt(i-1))))
                {
                    scope.add("if");
                    program += "if ";
                    i++;
                    continue;
                }
                else if (i + 4 <= s.length() && s.substring(i, i + 4).equals("else"))
                {
                    program = program.substring(0, program.length() - 15) + "\nelse";
                    scope.add("else");
                    i += 3;
                }
                else if (i + 5 <= s.length() && s.substring(i, i + 5).equals("while"))
                {
                    scope.add("while");
                    program += "while ";
                    i += 4;
                }
                else if (s.charAt(i) == '{')
                {
                    //System.err.println(program);
                    if (scope.lastElement().equals("while"))
                        program += " do ";
                    else if (scope.lastElement().equals("if"))
                        program += " then ";
                    cnt=0;
                    continue;
                }
                else if (s.charAt(i) == '}')
                {
                    while(program.endsWith(" ") || program.endsWith("\n") || program.endsWith("\t") || program.endsWith(";"))
                        program = program.substring(0, program.length() - 1);
                    program+="\n";
//                    System.err.println("------------------");
//                    System.err.println(program);
//                    System.err.println("------------------");
                    String last = scope.lastElement();
                    scope.removeElementAt(scope.size() - 1);
                    if(cnt==0)
                        program+="skip\n";
                    if (last.equals("if"))
                        program += "else\nskip\nfi;\n";
                    else if (last.equals("else"))
                        program += "fi;\n";
                    else if (last.equals("while"))
                        program += "od;\n";
                    cnt=1;
                }
                else if (s.charAt(i) == '=')
                {
                    char c = s.charAt(i - 1),t=s.charAt(i+1);
                    if (c != '!' && c != '<' && c != '>' && c != '=' && t!='=')
                        program += ":=";
                    else
                        program += "=";
                }
                else
                    program += s.charAt(i);

            }
            if (!program.equals("") && program.charAt(program.length() - 1) != '\n')
                program += "\n";

        }
        while(program.endsWith("\n") || program.endsWith(" ") || program.endsWith("\t") || program.endsWith(";"))
            program=program.substring(0,program.length()-1);
        FileWriter fw=new FileWriter(output);
        fw.write(program);
        fw.close();
    }
}
