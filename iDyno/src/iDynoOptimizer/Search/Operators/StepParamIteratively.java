package iDynoOptimizer.Search.Operators;

import iDynoOptimizer.Protocol.Parameters.ParamNumValue;
import iDynoOptimizer.Search.ParamNameValues;

/**
 * Created by Chris on 11/19/2014.
 */
public class StepParamIteratively implements StepParam {


    private ParamNameValues paramNameValues;

    public StepParamIteratively(ParamNameValues paramNameValues) {
        this.paramNameValues = paramNameValues;
    }


    @Override
    public ParamNumValue step() {

        double oldValue = paramNameValues.getLastParamValue().getValue();
        return new ParamNumValue(oldValue + paramNameValues.getParamNum().getCurrentStep());
    }
}
