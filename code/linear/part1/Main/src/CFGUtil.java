import java.util.Vector;

public class CFGUtil
{
    public static Vector<CFGNode> findCutpoints()
    {
        Vector<CFGNode> ret=new Vector<>();
        ret.add(Main.startNode);
        Main.startNode.isCutPoint=true;
        ret.add(Main.termNode);
        Main.termNode.isCutPoint=true;
        dfs(Main.startNode,ret,new Vector<CFGNode>());
        return ret;
    }

    private static void dfs(CFGNode v,Vector<CFGNode> res,Vector<CFGNode> currentBranch)
    {
        v.visited=true;
        currentBranch.add(v);
        for(Transition t:v.out)
        {
            if(!t.u.visited)
                dfs(t.u, res, currentBranch);
            else if(!res.contains(t.u) && currentBranch.contains(t.u))
            {
                t.u.isCutPoint=true;
                res.add(t.u);
            }
        }
        currentBranch.removeElementAt(currentBranch.size()-1);
    }


    public static void weakestPreCondition(Vector<Transition> path,Farkas farkas)  //NOTE: for C-Integer programs this is completely fine but for general T2 transition systems it might have problems
    {
        for(int i=path.size()-1;i>=0;i--)
        {
            Transition t=path.elementAt(i);
            for(int j=0;j<t.varName.size();j++)
            {
                String var=t.varName.elementAt(j);
                LinearCombination upd=t.update.elementAt(j);
                farkas.replaceVarWithLinear(var,upd);
            }
            farkas.addPredicate(t.detGuard.deepCopy());
            farkas.addInvConstraint(t.nondetGuard.deepCopy());
        }
    }
}
