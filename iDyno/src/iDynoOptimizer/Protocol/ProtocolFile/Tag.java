package iDynoOptimizer.Protocol.ProtocolFile;

import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.Parameters.ParamNumValue;
import iDynoOptimizer.Protocol.Parameters.ParamStringValue;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Chris on 11/20/2014.
 */
public class Tag {

    private String        stringValue;
    private boolean       booleanValue;
    private boolean       isBooleanValue;
    private ParamNumValue paramNumValue;
    //matches strings like these:
    //$(stiffness, .005, 0.2,.1)
    //$( k1,1,10,0.5)
    //$( k3,1,10,0.5E-10)
    //$(1param1, 1, 10e25, 0.5)
    private static final String  paramSelectPatternString = "^\\$\\([\\s]*([A-Za-z\\d]+)[\\s]*,[\\s]*([-]?[0-9]*\\.?[0-9]+[eE]*[-]?[0-9]*)[\\s]*,[\\s]*([-]?[0-9]*\\.?[0-9]+[eE]*[-]?[0-9]*)[\\s]*,[\\s]*([-]?[0-9]*\\.?[0-9]+[eE]*[-]?[0-9]*)[\\s]*,[\\s]*([-]?[0-9]*\\.?[0-9]+[eE]*[-]?[0-9]*)[\\s]*,[\\s]*([-]?[0-9]*\\.?[0-9]+[eE]*[-]?[0-9]*)[\\s]*\\)$";
    private static final Pattern paramSelectPattern       = Pattern.compile(paramSelectPatternString);
    private List<ParamNum> changingParams;

    private String    tagName;
    private List<Tag> allChildren;

    private Map<String, ParamStringValue> stringAttributes;
    private Map<String, ParamNumValue>    doubleAttributes;


    public Tag(String tagName) {

        this.tagName = tagName;
        changingParams = new ArrayList<ParamNum>();
        stringAttributes = new HashMap<String, ParamStringValue>();
        doubleAttributes = new HashMap<String, ParamNumValue>();
        allChildren = new LinkedList<Tag>();


    }


    public void addChild(Tag child) {

        allChildren.add(child);
    }


    public String getTagName() {
        return tagName;
    }

    public Map<String, ParamStringValue> getStringAttributes() {
        return stringAttributes;
    }

    public Map<String, ParamNumValue> getDoubleAttributes() {
        return doubleAttributes;
    }

    public void addAttrs(String name, String value) {

        try {

            double d = Double.parseDouble(value);
            doubleAttributes.put(name, new ParamNumValue(d));
        } catch (NumberFormatException e) {
            ParamNumValue pnv = tryCreateParamNum(value);
            if (pnv == null) {
                stringAttributes.put(name, new ParamStringValue(value));
            } else {
                doubleAttributes.put(name, pnv);
            }
        }


    }

    public List<Tag> getAllChildren() {
        return allChildren;
    }


    public boolean hasChildren() {
        return allChildren.size() > 0;
    }

    public String getStringValue() {
        if (this.isBooleanValue) return String.valueOf(this.booleanValue);
        else if (this.paramNumValue != null) return paramNumValue.toString();
        else return stringValue;
    }


    public void setValue(String value) {
        stringValue = value;
        if (value != null) {
            ParamNumValue pnv = tryCreateParamNum(value);
            if (pnv == null) {
                try {
                    double d = Double.parseDouble(value);
                    pnv = new ParamNumValue(d);
                    setParamNumValue(pnv);

                } catch (NumberFormatException e) {
                    if (value.equals("true")) setBooleanValue(true);
                    if (value.equals("false")) setBooleanValue(false);
                }
            } else setParamNumValue(pnv);
        }


    }


    public boolean getBooleanValue() {
        return booleanValue;
    }

    public void setBooleanValue(boolean value) {

        isBooleanValue = true;
        this.booleanValue = value;
    }

    public ParamNumValue getParamNumValue() {
        return paramNumValue;
    }

    public void setParamNumValue(ParamNumValue value) {
        this.paramNumValue = value;


    }

    public List<ParamNum> getChangingParams() {
        return changingParams;
    }

    private ParamNumValue tryCreateParamNum(String value) {
        Matcher m = paramSelectPattern.matcher(value);
        if (m.matches()) {
            String name = m.group(1);
            double absMin = Double.parseDouble(m.group(2));
            double absMax = Double.parseDouble(m.group(3));
            int pointCount = Integer.parseInt(m.group(4));
            double range = Double.parseDouble(m.group(5));
            double epsilon = Double.parseDouble(m.group(6));

            ParamNumValue pnv = new ParamNumValue(absMin);

            changingParams.add(new ParamNum(name, ProtocolFile.getClassRep().getProtocolFileName(), pnv, absMin, absMax, epsilon, range, pointCount));
            return pnv;
        }
        return null;
    }

}
