package iDynoOptimizer.Protocol.Parameters;

import iDynoOptimizer.Global.ExtraMath;

/**
 * Created by Chris on 11/15/2014.
 */
public class ParamNumValue {


    private double value;

    public ParamNumValue(double value) {
        this.value = value;
    }


    public double getValue() {
        return this.value;
    }

    public void setValue(double value) {
        this.value = value;
    }

    public ParamNumValue clone() {
        return new ParamNumValue(this.value);
    }

    public String toString() {

        String stringValue = String.valueOf(value);

        if (stringValue.endsWith((".0"))) stringValue = stringValue.replace(".0", "");

        return stringValue;
    }
}
