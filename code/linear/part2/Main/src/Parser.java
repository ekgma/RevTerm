import java.io.File;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Vector;

public class Parser
{
    public static Set<String> allVars = new HashSet<>();
    public static Vector<String> tokens = new Vector<>();
    public static int nondetCount = 0;


    static
    {
        allVars.add("1");
    }

    public static Node parseProg(int beginIndex, int endIndex) throws Exception
    {
        if (!getToken(beginIndex).equals("START"))
            throw new Exception("program should start with START");
        Node cur = new Node(null, beginIndex, endIndex, "prog");

        CFGNode start = CFGNode.addNode(Integer.parseInt(getToken(beginIndex + 2)));
        Transition fromStart = new Transition(Main.startNode, start);
        fromStart.addToGraph();

        if (getToken(beginIndex + 4).equals("CUTPOINT"))
            Main.cutPoint = CFGNode.addNode(Integer.parseInt(getToken(beginIndex + 6)));
        int lastFROM = -1;
        for (int i = beginIndex; i <= endIndex; i++)
            if (getToken(i).equals("FROM"))
            {
                if (lastFROM != -1)
                    throw new Exception(" \"TO: index\" expected before @" + i);
                lastFROM = i;
            }
            else if (getToken(i).equals("TO"))
            {
                parseTransition(cur, lastFROM, i + 3);
                lastFROM = -1;
            }
        return cur;
    }

    public static Node parseTransition(Node par, int beginIndex, int endIndex) throws Exception
    {
        if (!getToken(endIndex).equals(";"))
            throw new Exception("Transition must end with ; @" + beginIndex + "-" + endIndex);
        Node cur = new Node(par, beginIndex, endIndex, "Transition");
        int vIndex = Integer.parseInt(getToken(beginIndex + 2)), uIndex = Integer.parseInt(getToken(endIndex - 1));
        CFGNode vNode = CFGNode.addNode(vIndex);
        CFGNode uNode = CFGNode.addNode(uIndex);

        Vector<Transition> transitionVector = new Vector<>();
        transitionVector.add(new Transition(vNode, uNode));
        int lastColon = beginIndex + 3;
        for (int i = beginIndex + 4; i <= endIndex - 4; i++)
        {
            if (getToken(i).equals(";"))
            {
                Node ch = parseStmt(cur, lastColon + 1, i - 1);
                if (ch.type.equals("assume"))
                {
                    if (ch.guard.size() == 1)
                        for (Transition t : transitionVector)
                            t.detGuard.add(ch.guard.elementAt(0));
                    else if (ch.guard.size() > 1)
                    {
                        Vector<Transition> tmp = new Vector<>();
                        for (int j = 0; j < ch.guard.size(); j++)
                        {
                            for (Transition t : transitionVector)
                            {
                                Transition tp = t.deepCopy();
                                tp.detGuard.add(ch.guard.elementAt(j));
                                tmp.add(tp);
                            }
                        }
                        transitionVector = tmp;
                    }
                }
                else
                {
                    for (Transition t : transitionVector)
                    {
                        t.varName.add(ch.varName);
                        t.update.add(ch.expr);
//                        if (ch.expr.containsNondetVar())
//                        {
//                            LinearCombination lowerBound = ch.expr.deepCopy();
//                            lowerBound.minus(new LinearCombination(ch.nondetLow, Rational.one));  //var - low >= 0
//                            t.nondetGuard.add(lowerBound);
//
//                            LinearCombination upperBound = new LinearCombination(ch.nondetUp, Rational.one);
//                            upperBound.minus(ch.expr.deepCopy());   //up - var >= 0
//                            t.nondetGuard.add(upperBound);
//
//                        }
                    }
                }
                lastColon = i;
            }
        }
        for (Transition t : transitionVector)
            t.addToGraph();
        return cur;
    }

    public static Node parseStmt(Node par, int beginIndex, int endIndex) throws Exception
    {
        if (getToken(beginIndex).equals("assume"))
        {
            Node cur = new Node(par, beginIndex, endIndex, "assume");
            Node ch = parseBexpr(cur, beginIndex + 2, endIndex - 1);
            cur.guard = ch.guard;
            return cur;
        }
        else
        {
            Node cur = new Node(par, beginIndex, endIndex, "assignment");
            if (!getToken(beginIndex + 1).equals(":="))
                throw new Exception("assignment without := @" + beginIndex + "-" + endIndex);
            int sgn = beginIndex + 1;

            String varName = getToken(beginIndex);
            allVars.add(varName);

            boolean isNondet = false;
            for (int i = sgn + 1; i <= endIndex; i++)
                if (getToken(i).equals("nondet"))
                    isNondet = true;
            if (isNondet)
            {
                cur.varName = varName;
                cur.expr = new LinearCombination("_r_"+nondetCount);
                nondetCount++;
            }
            else
            {
                LinearCombination update = parseExpr(cur, sgn + 1, endIndex).expr;
                cur.varName = varName;
                cur.expr = update;
            }
            return cur;
        }
    }


    public static Node parseBexpr(Node par, int beginIndex, int endIndex) throws Exception
    {
//        System.err.println("parseBexpr: "+beginIndex+"---"+endIndex);

        Node cur = new Node(par, beginIndex, endIndex, "Bexpr");

        for (int i = beginIndex; i <= endIndex; i++)
            if (getToken(i).equals("nondet"))
                return cur;

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
            else if (openPar == 0 && getToken(i).equals("|") && getToken(i + 1).equals("|"))
            {
                ors.add(i + 1);
                i++;
            }
            else if (openPar == 0 && getToken(i).equals("&") && getToken(i + 1).equals("&"))
            {
                ands.add(i + 1);
                i++;
            }
        ors.add(endIndex + 2);
        ands.add(endIndex + 2);
        if (ors.size() > 2)
        {
            for (int i = 1; i < ors.size(); i++)
            {
                Node ch = parseBexpr(cur, ors.elementAt(i - 1) + 1, ors.elementAt(i) - 2);
                cur.guard = LinearPredicate.disjunct(cur.guard, ch.guard);
            }
            return cur;
        }
        if (ands.size() > 2)
        {
            for (int i = 1; i < ands.size(); i++)
            {
                Node ch = parseBexpr(cur, ands.elementAt(i - 1) + 1, ands.elementAt(i) - 2);
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
        Node cur = new Node(par, beginIndex, endIndex, "literal");
        Node left = null;
        Node right=null;
        if (sgn == -1)
        {
            type = 5;
            left = parseExpr(cur, beginIndex, endIndex);
            right = new Node(cur, endIndex, endIndex, "0");
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
            lc.minus(new LinearCombination(Main.eps));  // left - right - eps
            LinearPredicate lp = new LinearPredicate();
            lp.add(lc);
            cur.guard.add(lp);
        }
        else if (type == 3) //left < right  -->   right - left > eps   -->   right - left -eps >=0
        {
            LinearCombination lc = right.expr.deepCopy();
            lc.minus(left.expr); // right - left
            lc.minus(new LinearCombination(Main.eps));  // right - left - eps
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
        else
        {
            LinearCombination lc = right.expr.deepCopy();
            lc.minus(left.expr);
            lc.minus(new LinearCombination(Main.eps));

            LinearCombination lc2 = left.expr.deepCopy();
            lc2.minus(right.expr);
            lc2.minus(new LinearCombination(Main.eps));

            LinearPredicate lp1 = new LinearPredicate(), lp2 = new LinearPredicate();
            lp1.add(lc);
            lp2.add(lc2);
            cur.guard.add(lp1);
            cur.guard.add(lp2);
        }

        return cur;
    }

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

        Node cur = new Node(par, beginIndex, endIndex, "expr");
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
            Node cur = new Node(par, beginIndex, endIndex, "constant");
            int val = Integer.parseInt(getToken(beginIndex));
            cur.expr = new LinearCombination();
            cur.expr.add("1", new Rational(val, 1));
            return cur;
        }
        else if (beginIndex == endIndex - 1 && isNumeric(getToken(endIndex))) //negative constant
        {
            Node cur = new Node(par, beginIndex, endIndex, "constant");
            int val = -Integer.parseInt(getToken(endIndex));
            cur.expr = new LinearCombination();
            cur.expr.add("1", new Rational(val, 1));
            return cur;
        }
        else if (beginIndex == endIndex)    //var
        {
            Node cur = new Node(par, beginIndex, endIndex, "var");
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
            Node cur = new Node(par, beginIndex, endIndex, "term mul");
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


    public static boolean isNumeric(String s)
    {
        for (int i = 0; i < s.length(); i++)
            if (!Character.isDigit(s.charAt(i)) && s.charAt(i) != '.')
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
        String extraSpace = "";
        for (int i = 0; i < program.length(); i++)
        {
            char c = program.charAt(i);
            if (c == '.' || Character.isAlphabetic(c) || Character.isDigit(c) || c == '_')
                extraSpace += c;
            else
            {
                extraSpace += " ";
                extraSpace += c;
                extraSpace += " ";
            }
        }

        Scanner scanner = new Scanner(extraSpace);
        while (scanner.hasNext())
        {
            String s = scanner.next();
            if (s.equals("="))
            {
                if (tokens.size() == 0)
                    throw new Exception("program cannot start with =");
                String last = tokens.lastElement();
                if (last.equals(":") || last.equals(">") || last.equals("<") || last.equals("=") || last.equals("!"))
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
        File file = new File(fileName);
        Scanner in = new Scanner(file);

        String program = "";
        while (in.hasNextLine())
        {
            String s = in.nextLine();
            if (s.contains("//"))
                s = s.substring(0, s.indexOf("//"));
            if (s.contains("AT("))
            {
                int ind = s.indexOf("AT(");
                int openPar = 0, endOfAT = -1;
                for (int i = 0; i < s.length(); i++)
                {
                    if (s.charAt(i) == '(')
                        openPar++;
                    else if (s.charAt(i) == ')')
                    {
                        openPar--;
                        if (openPar == 0)
                        {
                            endOfAT = i;
                            break;
                        }
                    }
                }
                s = s.substring(0, ind) + s.substring(endOfAT + 1, s.length());
            }

            program += s + " ";
        }
        readTokens(program);
    }
}
