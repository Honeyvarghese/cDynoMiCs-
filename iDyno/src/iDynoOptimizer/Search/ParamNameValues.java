package iDynoOptimizer.Search;

import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.Parameters.ParamNumValue;

import java.util.LinkedList;
import java.util.List;

/**
 * Created by Chris on 11/15/2014.
 */
public class ParamNameValues {


    private ParamNum paramNum;

    private List<ParamNumValue> paramNumValues;


    public ParamNameValues(ParamNum pn) {
        this.paramNum = pn;
        paramNumValues = new LinkedList<ParamNumValue>();
        paramNumValues.add(pn.getValue());
    }


    public void addParamValue(ParamNumValue pi) {
        paramNumValues.add(pi);
    }


    public ParamNum getParamNum() {
        return paramNum;
    }


    public List<ParamNumValue> getParamNumValues() {
        return paramNumValues;
    }


    public ParamNumValue getParamValue(int i) {
        try {
            return paramNumValues.get(i);
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
            return null;
        }
    }


    public ParamNumValue getLastParamValue() {
        int lastIndex = paramNumValues.size() - 1;
        return getParamValue(lastIndex);
    }

}
