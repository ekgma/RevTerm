import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Parser
{
    public static Set<String> allVars=new HashSet<>();
    public static Vector <String> tokens=new Vector<>();


    static{
        allVars.add("1");
    }

    public static Node parseStmtList(Node p,int beginIndex,int endIndex,boolean inLoop) throws Exception
    {

        if(getToken(beginIndex).equals(";"))
            throw new Exception("stmtList cannot start with ; @"+beginIndex+"-"+endIndex);
        if(getToken(endIndex).equals(";"))
            throw new Exception("stmtList cannot end with ; @"+beginIndex+"-"+endIndex);

        Node cur=new Node(p,beginIndex,endIndex,"stmtlist","",inLoop);
        int afterPre=beginIndex-1;
        if(getToken(beginIndex).equals("#"))
        {
            for(int i=beginIndex+1;i<=endIndex;i++)
                if(getToken(i).equals("#"))
                    afterPre=i;
            if(afterPre==beginIndex)
                throw new Exception("Pre-condition has no end");
            Node pre=parseBexpr(null,beginIndex+1,afterPre-1);
            cur.preCondition=pre.guard.elementAt(0);
        }

        Vector<Integer> colons=new Vector<>();
        colons.add(afterPre);

        int openIf=0,openWhile=0;
        for(int i=afterPre+1;i<=endIndex;i++)
        {
            if(getToken(i).equals("if"))
                openIf++;
            else if(getToken(i).equals("while"))
                openWhile++;
            else if(getToken(i).equals("fi"))
                openIf--;
            else if(getToken(i).equals("od"))
                openWhile--;
            else if(getToken(i).equals(";") && openIf==0 && openWhile==0)
                colons.add(i);

            if(openIf<0)
                throw new Exception("more fi's than if's @"+beginIndex+"-"+endIndex);
            if(openWhile<0)
                throw new Exception("more od's than while's @"+beginIndex+"-"+endIndex);
        }
        colons.add(endIndex+1);

        for(int i=1;i<colons.size();i++)
            parseStmt(cur,colons.elementAt(i-1)+1,colons.elementAt(i)-1,inLoop);
        return cur;
    }

    public static Node parseStmt(Node par,int beginIndex,int endIndex,boolean inLoop) throws Exception
    {
//        System.err.println(beginIndex+"---------"+endIndex);
        if(getToken(beginIndex).equals("skip"))
        {
            if(beginIndex!=endIndex)
                throw new Exception("skip node with beginIndex!=endIndex @"+beginIndex+"-"+endIndex);
            Node cur=new Node(par,beginIndex,endIndex,"stmt","skip",inLoop);
            return cur;
        }
        else if(getToken(beginIndex).equals("if")) //branching
        {
            if(!getToken(endIndex).equals("fi"))
                throw new Exception("if must end with fi @"+beginIndex+"-"+endIndex);
//            if(getToken(beginIndex+1).equals("*") || getToken(beginIndex+1).equals("_NONDET_")) //non-deterministic branching
//            {
//                int thenIndex=-1;
//                for(int i=beginIndex;i<=endIndex;i++)
//                    if(getToken(i).equals("then"))
//                    {
//                        thenIndex=i;
//                        break;
//                    }
//                if(thenIndex==-1)
//                    throw new Exception("if* without then @"+beginIndex+"-"+endIndex);
//                Node cur=new Node(par,beginIndex,endIndex,"stmt","if*",inLoop);
//
//                int elseIndex=getElseOfIf(beginIndex+1,endIndex-1),fiIndex=endIndex;
//                Node thenNode=parseStmtList(cur,thenIndex+1,elseIndex-1,inLoop);
//                Node elseNode=parseStmtList(cur,elseIndex+1,fiIndex-1,inLoop);
//                Node fiNode=new Node(cur,fiIndex,fiIndex,"stmt","fi",inLoop);
//
//                return cur;
//            }
            else    //affine branching
            {
                int thenIndex=-1;
                for(int i=beginIndex;i<=endIndex;i++)
                    if(getToken(i).equals("then"))
                    {
                        thenIndex=i;
                        break;
                    }
                if(thenIndex==-1)
                    throw new Exception("if<bexpr> without then @"+beginIndex+"-"+endIndex);
                Node cur=new Node(par,beginIndex,endIndex,"stmt","affif",inLoop);
                int elseIndex=getElseOfIf(beginIndex+1,endIndex-1),fiIndex=endIndex,ifIndex=beginIndex;
                Node bexprNode= parseBexpr(cur,ifIndex+1,thenIndex-1);
                Node thenNode=parseStmtList(cur,thenIndex+1,elseIndex-1,inLoop);
                Node elseNode=parseStmtList(cur,elseIndex+1,fiIndex-1,inLoop);
                Node fiNode=new Node(cur,endIndex,endIndex,"stmt","fi",inLoop);

                return cur;
            }
        }
        else if(getToken(beginIndex).equals("while")) //while loop
        {
            if(!getToken(endIndex).equals("od"))
                throw new Exception("while does not end with od @"+beginIndex+"-"+endIndex);
            int whileIndex=beginIndex,doIndex=-1,odIndex=endIndex;
            for(int i=beginIndex;i<=endIndex;i++)
                if(getToken(i).equals("do"))
                {
                    doIndex = i;
                    break;
                }
            if(doIndex==-1)
                throw new Exception("while does not have do @"+beginIndex+"-"+endIndex);
            Node cur=new Node(par,beginIndex,endIndex,"stmt","while",true);
            Node bexprNode= parseBexpr(cur,whileIndex+1,doIndex-1);
            Node doNode=parseStmtList(cur,doIndex+1,odIndex-1,true);
            Node odNode=new Node(cur,endIndex,endIndex,"stmt","od",false);
            return cur;
        }
        else    //assignment
        {

            int signIndex=beginIndex+1;
            if(!getToken(signIndex).equals(":="))
                throw new Exception("assignment without ':=' @"+beginIndex+"-"+endIndex);

            String varName=getToken(beginIndex);
            allVars.add(varName);

            if(endIndex==beginIndex+2 && getToken(beginIndex+2).equals("_NONDET_"))
            {
                Node cur=new Node(par,beginIndex,endIndex,"stmt","assignment",inLoop);
                cur.expr=null;
                cur.varName=varName;
                return cur;
            }

            Node cur=new Node(par,beginIndex,endIndex,"stmt","assignment",inLoop);
            Node ch=parseExpr(cur,signIndex+1,endIndex);
            cur.expr=ch.expr;
            cur.varName=varName;
            return cur;
        }
    }
    public static Node parseBexpr(Node par, int beginIndex, int endIndex) throws Exception
    {
//        System.err.println("parseBexpr: "+beginIndex+"---"+endIndex);

        Node cur = new Node(par, beginIndex, endIndex, "Bexpr","Bexpr",false);


        if(cur.guard==null)
            cur.guard=new Vector<>();

        Vector<Integer> ors = new Vector<>();
        Vector<Integer> ands = new Vector<>();


        ors.add(beginIndex - 1);
        ands.add(beginIndex - 1);

        int openPar = 0;
        for (int i = beginIndex; i <= endIndex; i++)
            if (getToken(i).equals("("))
                openPar++;
            else if (getToken(i).equals(")"))
                openPar--;
            else if (openPar == 0 && getToken(i).equals("or"))
            {
                ors.add(i);
            }
            else if (openPar == 0 && getToken(i).equals("and"))
            {
                ands.add(i);
            }
        ors.add(endIndex + 1);
        ands.add(endIndex + 1);
        if (ors.size() > 2)
        {
            for (int i = 1; i < ors.size(); i++)
            {
                Node ch = parseBexpr(cur, ors.elementAt(i - 1) + 1, ors.elementAt(i) - 1);
                cur.guard = LinearPredicate.disjunct(cur.guard, ch.guard);
            }
            return cur;
        }
        if (ands.size() > 2)
        {
            for (int i = 1; i < ands.size(); i++)
            {
                Node ch = parseBexpr(cur, ands.elementAt(i - 1) + 1, ands.elementAt(i) - 1);
                cur.guard = LinearPredicate.conjunct(cur.guard, ch.guard);
            }
            return cur;
        }

        boolean isCompletlyInsidePar = true;
        openPar = 0;
        for (int i = beginIndex; i <= endIndex; i++)
        {
            if (getToken(i).equals("("))
                openPar++;
            else if (getToken(i).equals(")"))
                openPar--;
            if (openPar == 0 && i != endIndex)
            {
                isCompletlyInsidePar = false;
                break;
            }
        }
        if (isCompletlyInsidePar)
        {
            Node ch = parseBexpr(cur, beginIndex + 1, endIndex - 1);
            cur.guard = LinearPredicate.conjunct(cur.guard, ch.guard);
            return cur;
        }
        if (getToken(beginIndex).equals("!"))
        {
            Node ch = parseBexpr(cur, beginIndex + 1, endIndex);
            cur.guard = LinearPredicate.negate(ch.guard);
            return cur;
        }
        Node ch = parseLiteral(cur, beginIndex, endIndex);
        cur.guard = LinearPredicate.conjunct(cur.guard, ch.guard);
        return cur;
    }

    public static Node parseLiteral(Node par, int beginIndex, int endIndex) throws Exception
    {
//        System.err.println("parseLiteral:"+ beginIndex+"---"+endIndex);
        int sgn = -1, type = -1; //types: 0: "<="  1: ">="   2: ">"   3: "<"   4: "=="    5: "!="
        for (int i = beginIndex; i <= endIndex; i++)
            if (getToken(i).equals("<="))
            {
                sgn = i;
                type = 0;
            }
            else if (getToken(i).equals(">="))
            {
                sgn = i;
                type = 1;
            }
            else if (getToken(i).equals(">"))
            {
                sgn = i;
                type = 2;
            }
            else if (getToken(i).equals("<"))
            {
                sgn = i;
                type = 3;
            }
            else if (getToken(i).equals("=="))
            {
                sgn = i;
                type = 4;
            }
            else if (getToken(i).equals("!="))
            {
                sgn = i;
                type = 5;
            }
        if (sgn == beginIndex || sgn == endIndex)
            throw new Exception("literal starts or ends with sign @" + beginIndex + "-" + endIndex);
        Node cur = new Node(par, beginIndex, endIndex, "literal","literal",false);
        for(int i=beginIndex;i<=endIndex;i++)
            if(getToken(i).equals("_NONDET_"))
            {
                LinearPredicate lp=new LinearPredicate();
                lp.exprs.add(null);
                cur.guard.add(lp);
                return cur;
            }
        Node left = null;
        Node right = null;
        if (sgn == -1)
        {
            type = 5;
            left = parseExpr(cur, beginIndex, endIndex);
            right = new Node(cur, endIndex, endIndex, "0","0",false);
            right.expr = new LinearCombination(Rational.zero);
        }
        else
        {
            left = parseExpr(cur, beginIndex, sgn - 1);
            right = parseExpr(cur, sgn + 1, endIndex);
        }
        if (type == 0)   //left<=right   -->    right-left>=0
        {
            LinearCombination lc = right.expr.deepCopy();
            lc.minus(left.expr);
            LinearPredicate lp = new LinearPredicate();
            lp.add(lc);
            cur.guard.add(lp);
        }
        else if (type == 1)  //left>=right    -->    left-right>=0
        {
            LinearCombination lc = left.expr.deepCopy();
            lc.minus(right.expr);
            LinearPredicate lp = new LinearPredicate();
            lp.add(lc);
            cur.guard.add(lp);
        }
        else if (type == 2) // left > right   ->   left -right >=eps   ->   left - right -eps >=0
        {
            LinearCombination lc = left.expr.deepCopy();
            lc.minus(right.expr); // left - right
            lc.minus(new LinearCombination(prog_to_t2.eps));  // left - right - eps
            LinearPredicate lp = new LinearPredicate();
            lp.add(lc);
            cur.guard.add(lp);
        }
        else if (type == 3) //left < right  -->   right - left > eps   -->   right - left -eps >=0
        {
            LinearCombination lc = right.expr.deepCopy();
            lc.minus(left.expr); // right - left
            lc.minus(new LinearCombination(prog_to_t2.eps));  // right - left - eps
            LinearPredicate lp = new LinearPredicate();
            lp.add(lc);
            cur.guard.add(lp);
        }
        else if (type == 4)  //left==right  -->  left-right>=0 and right-left>=0
        {
            LinearCombination lc = right.expr.deepCopy();
            lc.minus(left.expr);

            LinearCombination lc2 = left.expr.deepCopy();
            lc2.minus(right.expr);

            LinearPredicate lp = new LinearPredicate();
            lp.add(lc);
            lp.add(lc2);
            cur.guard.add(lp);
        }
        else if (type == 5)  // left != right  -->  left - right -1 >=0  or right - left -1 >=0
        {
            LinearCombination lc = right.expr.deepCopy();
            lc.minus(left.expr);
            lc.minus(new LinearCombination(prog_to_t2.eps));

            LinearCombination lc2 = left.expr.deepCopy();
            lc2.minus(right.expr);
            lc2.minus(new LinearCombination(prog_to_t2.eps));

            LinearPredicate lp1 = new LinearPredicate(), lp2 = new LinearPredicate();
            lp1.add(lc);
            lp2.add(lc2);
            cur.guard.add(lp1);
            cur.guard.add(lp2);
        }

        return cur;
    }
//    public static Node parseOrBexpr(Node par, int beginIndex, int endIndex) throws Exception
//    {
//
//        Vector<Integer> ors=new Vector<>();
//        ors.add(beginIndex-1);
//        for(int i=beginIndex;i<=endIndex;i++)
//            if(getToken(i).equals("or"))
//                ors.add(i);
//        ors.add(endIndex+1);
//
//        Node cur=new Node(par,beginIndex,endIndex,"bexpr","or",false);
//        for (int i = 1; i < ors.size(); i++)
//        {
//            Node ch=parseAndBexpr(cur, ors.elementAt(i - 1) + 1, ors.elementAt(i) - 1);
//            cur.guard.add(ch.guard.firstElement());
//        }
//
//        return cur;
//    }

//    public static Node parseAndBexpr(Node par, int beginIndex, int endIndex) throws Exception
//    {
//        Vector<Integer> ands=new Vector<>();
//        ands.add(beginIndex-1);
//        for(int i=beginIndex;i<=endIndex;i++)
//            if(getToken(i).equals("and"))
//                ands.add(i);
//        ands.add(endIndex+1);
//        Node cur=new Node(par,beginIndex,endIndex,"bexpr","and",false);
//        LinearPredicate lp=new LinearPredicate();
//        for(int i=1;i<ands.size();i++)
//        {
//            Node ch=parseLiteral(cur, ands.elementAt(i - 1) + 1, ands.elementAt(i) - 1);
//            lp.add(ch.guard.firstElement());
//        }
//        cur.guard.add(lp);
//        return cur;
//    }

//    public static Node parseLiteral(Node par,int beginIndex,int endIndex) throws Exception
//    {
//        int sgn=-1,type=-1; //types: 0-> "<="  1->">="
//        for(int i=beginIndex;i<=endIndex;i++)
//            if(getToken(i).equals("<="))
//            {
//                sgn=i;
//                type=0;
//            }
//            else if(getToken(i).equals(">="))
//            {
//                sgn=i;
//                type=1;
//            }
//            else if(getToken(i).equals(">"))
//            {
//                sgn=i;
//                type=2;
//            }
//            else if(getToken(i).equals("<"))
//            {
//                sgn=i;
//                type=3;
//            }
//            else if(getToken(i).equals("=="))
//            {
//                sgn=i;
//                type=4;
//            }
//            else if(getToken(i).equals("!="))
//            {
//                sgn=i;
//                type=5;
//            }
//        if(sgn==beginIndex || sgn==endIndex)
//            throw new Exception("literal starts or ends with sign @"+beginIndex+"-"+endIndex);
//        if(sgn==-1)
//            throw new Exception("no sign found for literal @"+beginIndex+"-"+endIndex);
//        Node cur=new Node(par,beginIndex,endIndex,"literal",getToken(sgn),false);
//        Node left=parseExpr(cur,beginIndex,sgn-1);
//        Node right=parseExpr(cur,sgn+1,endIndex);
//        if(type==0)   //left<=right   -> right-left>=0
//        {
//            LinearCombination lc=right.expr.deepCopy();
//            lc.minus(left.expr);
//            LinearPredicate lp=new LinearPredicate();
//            lp.add(lc);
//            cur.guard.add(lp);
//        }
//        else if(type==1)  //left>=right    -> left-right>=0
//        {
//            LinearCombination lc=left.expr.deepCopy();
//            lc.minus(right.expr);
//            LinearPredicate lp=new LinearPredicate();
//            lp.add(lc);
//            cur.guard.add(lp);
//        }
//        else if(type==2) // left > right  -> left -right >=eps -> left - right -eps >=0
//        {
//            LinearCombination lc=left.expr.deepCopy();
//            lc.minus(right.expr); // left - right
//            lc.minus(new LinearCombination(prog_to_t2.eps));  // left - right - eps
//            LinearPredicate lp=new LinearPredicate();
//            lp.add(lc);
//            cur.guard.add(lp);
//        }
//        else if(type==3) //left < right  -> right - left > eps -> right - left -eps >=0
//        {
//            LinearCombination lc=right.expr.deepCopy();
//            lc.minus(left.expr); // right - left
//            lc.minus(new LinearCombination(prog_to_t2.eps));  // right - left - eps
//            LinearPredicate lp=new LinearPredicate();
//            lp.add(lc);
//            cur.guard.add(lp);
//        }
//        else if(type==4) //left==right -> left-right>=0 and left - right <=0
//        {
//            LinearCombination lc=left.expr.deepCopy(),lc2=right.expr.deepCopy();
//            lc.minus(right.expr);
//            lc2.minus(left.expr);
//            LinearPredicate lp=new LinearPredicate();
//            lp.add(lc);
//            lp.add(lc2);
//            cur.guard.add(lp);
//        }
//        else if(type==5) //left != right  -> left > right || right > left  -> left - right -eps >=0 || right - left - eps >=0
//        {
//            LinearCombination lc=left.expr.deepCopy(),lc2=right.expr.deepCopy();
//            lc.minus(right.expr);
//            lc2.minus(left.expr);
//            lc.minus(new LinearCombination(prog_to_t2.eps));
//            lc2.minus(new LinearCombination(prog_to_t2.eps));
//            LinearPredicate lp=new LinearPredicate(),lp2=new LinearPredicate();
//            lp.add(lc);
//            lp2.add(lc2);
//            cur.guard.add(lp);
//            cur.guard.add(lp2);
//        }
//        return cur;
//    }

    public static Node parseExpr(Node par, int beginIndex, int endIndex) throws Exception
    {
        //System.err.println("parseExpr: "+beginIndex+"----"+endIndex);
        Vector<Integer> signIndex = new Vector<>();
        Vector<String> signType = new Vector<>();
        if (!getToken(beginIndex).equals("-"))
        {
            signIndex.add(beginIndex - 1);
            signType.add("+");
        }
        int openPar = 0;
        for (int i = beginIndex; i <= endIndex; i++)
        {
            if (getToken(i).equals("("))
                openPar++;
            else if (getToken(i).equals(")"))
                openPar--;
            if (openPar == 0 && (getToken(i).equals("+")
                    || (getToken(i).equals("-") && (i - 1 < beginIndex || (i - 1 >= beginIndex && !getToken(i - 1).equals("*") && !getToken(i - 1).equals("+"))))))
            {
                signIndex.add(i);
                signType.add(getToken(i));
            }
        }
        signIndex.add(endIndex + 1);
        signType.add("+");

        Node cur = new Node(par, beginIndex, endIndex, "expr","expr",false);
        cur.expr = new LinearCombination();
        for (int i = 0; i + 1 < signIndex.size(); i++)
        {
            Node ch = parseTerm(cur, signIndex.elementAt(i) + 1, signIndex.elementAt(i + 1) - 1);
            if (signType.elementAt(i).equals("+"))
                cur.expr.add(ch.expr);
            else
                cur.expr.minus(ch.expr);
        }
        return cur;
    }


    public static Node parseTerm(Node par, int beginIndex, int endIndex) throws Exception
    {
        //System.err.println("parseTerm: "+beginIndex+"---"+endIndex);
        if ((beginIndex == endIndex && isNumeric(getToken(beginIndex)))) //constant
        {
            Node cur = new Node(par, beginIndex, endIndex, "term","constant",false);
            int val = Integer.parseInt(getToken(beginIndex));
            cur.expr = new LinearCombination();
            cur.expr.add("1", new Rational(val, 1));
            return cur;
        }
        else if (beginIndex == endIndex - 1 && isNumeric(getToken(endIndex))) //negative constant
        {
            Node cur = new Node(par, beginIndex, endIndex,"term", "constant",false);
            int val = -Integer.parseInt(getToken(endIndex));
            cur.expr = new LinearCombination();
            cur.expr.add("1", new Rational(val, 1));
            return cur;
        }
        else if (beginIndex == endIndex)    //var
        {
            Node cur = new Node(par, beginIndex, endIndex,"term", "var",false);
            String var = getToken(beginIndex);
            allVars.add(var);
            if (Character.isDigit(var.charAt(0)))
                throw new Exception("Incorrect var name @" + beginIndex);
            cur.expr = new LinearCombination();
            cur.expr.add(var, new Rational(1, 1));
            return cur;
        }
        else  // (...) or [] * []
        {
            Node cur = new Node(par, beginIndex, endIndex,"term", "term mul",false);
            cur.expr = new LinearCombination();
            Vector<Integer> sgnIndex = new Vector<>();
            Vector<String> sgnType = new Vector<>();
            sgnIndex.add(beginIndex - 1);
            sgnType.add("*");
            int openPar = 0;
            for (int i = beginIndex; i <= endIndex; i++)
                if (getToken(i).equals("("))
                    openPar++;
                else if (getToken(i).equals(")"))
                    openPar--;
                else if (openPar == 0 && (getToken(i).equals("*") || getToken(i).equals("/")))
                {
                    sgnIndex.add(i);
                    sgnType.add(getToken(i));
                }
                else if (getToken(i).equals("%"))
                {
                    throw new Exception("% is not supported. @" + beginIndex + "-" + endIndex);
                }
            sgnIndex.add(endIndex + 1);
            sgnType.add("*");
            if (sgnIndex.size() == 2) // (...)
            {
                Node ch = parseExpr(cur, beginIndex + 1, endIndex - 1);
                cur.expr = ch.expr;
                return cur;
            }
            else
            {
                cur.expr.add("1", Rational.one);
                for (int i = 1; i < sgnIndex.size(); i++)
                {
                    Node ch = parseExpr(cur, sgnIndex.elementAt(i - 1) + 1, sgnIndex.elementAt(i) - 1);
                    if (sgnType.elementAt(i - 1).equals("*"))
                        cur.expr.multiplyByLin(ch.expr);
                    else if (ch.expr.isConstant() && ch.expr.coef.containsKey("1"))
                        cur.expr.multiplyByValue(Rational.inverse(ch.expr.coef.get("1")));
                    else
                        throw new Exception("Divison by variable is not possible @" + beginIndex + "-" + endIndex);
                }
                return cur;
            }
        }
    }


    public static int getElseOfIf(int beginIndex,int endIndex) throws Exception
    {
        int openIf=0,openWhile=0;
        for(int i=beginIndex;i<=endIndex;i++)
        {
            if(getToken(i).equals("if"))
                openIf++;
            else if(getToken(i).equals("fi"))
                openIf--;
            else if(getToken(i).equals("while"))
                openWhile++;
            else if(getToken(i).equals("od"))
                openWhile--;
            else if(openIf==0 && openWhile==0 && getToken(i).equals("else"))
                return i;
        }
        throw new Exception("no else found for if @"+beginIndex+"-"+endIndex);
    }


    public static boolean isNumeric(String s)
    {
        for(int i=0;i<s.length();i++)
            if(!Character.isDigit(s.charAt(i)) && s.charAt(i)!='.')
                return false;
        return true;
    }


    public static int getTokenCount()
    {
        return tokens.size();
    }

    public static String getToken(int x)
    {
        return tokens.elementAt(x);
    }

    public static void readTokens(String program) throws Exception
    {
        String extraSpace="";
        for(int i=0;i<program.length();i++)
        {
            char c=program.charAt(i);
            if(c=='.' || Character.isAlphabetic(c) || Character.isDigit(c) || c=='_')
                extraSpace+=c;
            else
            {
                extraSpace+=" ";
                extraSpace+=c;
                extraSpace+=" ";
            }
        }

        Scanner scanner=new Scanner(extraSpace);
        while(scanner.hasNext())
        {
            String s=scanner.next();
            if(s.equals("="))
            {
                if(tokens.size()==0)
                    throw new Exception("program cannot start with =");
                String last=tokens.lastElement();
                if(last.equals(":") || last.equals(">") || last.equals("<") || last.equals("=") || last.equals("!"))
                {
                    tokens.removeElementAt(getTokenCount() - 1);
                    last += s;
                    tokens.add(last);
                }
                else
                    tokens.add(s);
            }
            else
                tokens.add(s);
        }
    }

    public static void readFile(String fileName) throws Exception
    {
        File file=new File(fileName);
        Scanner in=new Scanner(file);

        String program="";
        boolean mainPartHasStarted=false;
        while(in.hasNextLine())
        {
            String s=in.nextLine();
            if(s.contains("if") || s.contains("while"))
                mainPartHasStarted=true;
            if(!mainPartHasStarted && s.contains("_NONDET_"))
                continue;
            program += s + " ";
        }
        readTokens(program);

//        System.err.println("tokens are:");
//        for(int i=0;i<tokens.size();i++)
//            System.err.println(i+": "+getToken(i));
    }
}
