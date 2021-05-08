package Pack;

/**
 * @program: MySootScript
 * @description:
 * @author: 0range
 * @create: 2021-05-08 10:32
 **/

import soot.Local;
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.AssignStmt;
import soot.jimple.Expr;
import soot.jimple.internal.AbstractBinopExpr;
import soot.toolkits.graph.DirectedGraph;
import soot.toolkits.scalar.ArraySparseSet;
import soot.toolkits.scalar.BackwardFlowAnalysis;
import soot.toolkits.scalar.FlowSet;

import java.util.Iterator;

/**
 * An expression e is very busy at point p if
 * On every path from p, expression e is evaluated before
 * the value of e is changed
 * BackWard & MUST
 */


public class VeryBusyExpressionsAnalysis extends BackwardFlowAnalysis<Unit, FlowSet<AbstractBinopExpr>>{

    //AbstractBinopExpr 二元操作表达式
    private FlowSet<AbstractBinopExpr> emptySet;

    public VeryBusyExpressionsAnalysis(DirectedGraph g){
        super(g);
        //fixed-point
        doAnalysis();
    }

    @Override
    protected FlowSet<AbstractBinopExpr> newInitialFlow() {
        return emptySet.clone();
    }

    @Override
    protected FlowSet<AbstractBinopExpr> entryInitialFlow() {
        return emptySet.clone();
    }



    @Override
    protected void flowThrough(FlowSet<AbstractBinopExpr> in, Unit node,
                               FlowSet<AbstractBinopExpr> out) {
        // out <- (in - expr containing locals defined in d) union out
        kill(in, node, out);
        // out <- out union expr used in d
        gen(out, node);
    }
    @Override
    protected void merge(FlowSet<AbstractBinopExpr> in1,
                         FlowSet<AbstractBinopExpr> in2,
                         FlowSet<AbstractBinopExpr> out) {
        in1.intersection(in2, out);
    }

    @Override
    protected void copy(FlowSet<AbstractBinopExpr> source,
                        FlowSet<AbstractBinopExpr> dest) {
        source.copy(dest);
    }

    private void kill(FlowSet<AbstractBinopExpr> inSet, Unit u,
                      FlowSet<AbstractBinopExpr> outSet) {
        FlowSet<AbstractBinopExpr> kills = emptySet.clone();
        for (ValueBox defBox : u.getDefBoxes()) {
            if(defBox.getValue() instanceof Local){
                //delete all relate exprs in inSet
                Iterator<AbstractBinopExpr> inIt = inSet.iterator();
                while(inIt.hasNext()){
                    AbstractBinopExpr e = inIt.next();
                    Iterator<ValueBox> eIt = e.getUseBoxes().iterator();
                    while(eIt.hasNext()){
                        ValueBox useBox = eIt.next();
                        if(useBox.getValue() instanceof Local &&
                            useBox.getValue().equivTo(defBox.getValue())){
                            kills.add(e);
                        }
                    }
                }
            }
        }
        // IN - kills
        inSet.intersection(kills, outSet);
    }

    private void gen(FlowSet<AbstractBinopExpr> outSet, Unit u) {
        for (ValueBox useBox : u.getUseBoxes()) {
            if (useBox.getValue() instanceof AbstractBinopExpr)
                outSet.add((AbstractBinopExpr) useBox.getValue());
        }
    }

}
