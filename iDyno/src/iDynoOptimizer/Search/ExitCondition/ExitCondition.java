package iDynoOptimizer.Search.ExitCondition;

import iDynoOptimizer.Global.ExtraMath;
import iDynoOptimizer.Protocol.Parameters.ParamNum;

/**
 * Created by Chris on 11/15/2014.
 */
public class ExitCondition implements IExitCondition {

    private double threshold;
    private Operator operator;

    public ExitCondition(double threshold, Operator operator) {
        this.threshold = threshold;
        this.operator = operator;
    }


    @Override
    public boolean met(double value) {

       return met(value, 0);

    }

    @Override
    public boolean met(double value, double epsilon) {
        return ExtraMath.approxComp(value, threshold, epsilon, operator);
    }
}
