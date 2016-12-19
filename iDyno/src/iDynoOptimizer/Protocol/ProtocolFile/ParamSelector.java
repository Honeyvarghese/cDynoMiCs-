package iDynoOptimizer.Protocol.ProtocolFile;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.Parameters.ParamNumValue;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris Johnson on 11/24/2014.
 */
public class ParamSelector {


    private static final String tagSplitter          = "/";
    private static final String tagAttSplitter       = ";";
    private static final String attSplitter          = "&";
    private static final String nameAttSplitter      = "=";
    private static final String desiredParamSplitter = "@";
    private static final String innerValue           = "innerValue";

    //example selector
    //the .. just indicate that more tags can exist
    //  "idynomics.name=value;name=value/../../

    public static ParamNumValue selectParam(String selector, ProtocolFile file) {

        //start at the root
        Tag match = file.getRoot();


        String tags        = selector.split(desiredParamSplitter)[0];
        String paramString = selector.split(desiredParamSplitter)[1];

        //loop through each tag in the selector
        for (String tag : tags.split(tagSplitter)) {

            if (match == null) return null;

            String tagName = "";
            String[][] nameAttributePairs = null;

            if (tag.contains(tagAttSplitter)) {
                //separate the tag name from the list of name/attribute pairs
                String[] tagAttributes = tag.split(tagAttSplitter);
                tagName = tagAttributes[0];
                String nameAttributesString = tagAttributes[1];

                //separate the name/attribute pairs
                String[] nameAttributes = nameAttributesString.split(attSplitter);


                //each row is a name/attribute pair
                //first column is name
                //second column is attribute
                nameAttributePairs = new String[nameAttributes.length][2];


                //loop through the name/attribute pairs and put them into the matrix
                for (int i = 0; i < nameAttributes.length; i++) {

                    //checking for name/attribute pairs with just a name allows the selector to be indifferent to the attribute

                    //the name/attribute pair has both a name and an attribute because it contains the name/attribute splitter
                    if (nameAttributes[i].contains(nameAttSplitter)) {
                        String name = nameAttributes[i].split(nameAttSplitter)[0];
                        String att = nameAttributes[i].split(nameAttSplitter)[1];
                        nameAttributePairs[i][0] = name;
                        nameAttributePairs[i][1] = att;
                    }
                    //only contains the name
                    else {
                        String name = nameAttributes[i];
                        nameAttributePairs[i][0] = name;
                    }

                }
            } else tagName = tag;
            match = check(match, tagName, nameAttributePairs);

        }

        ParamNumValue pnv = null;

        //its the tag's inner value we want
        if (paramString.equalsIgnoreCase(innerValue)) {
            pnv = match.getParamNumValue();


        } else {
            for (String pn : match.getDoubleAttributes().keySet()) {
                if (pn.equalsIgnoreCase(paramString)) {
                    pnv = match.getDoubleAttributes().get(pn);
                }
            }
        }

        if (pnv == null) {
            Exception e = new Exception("Param selector is not valid. The syntax could be invalid or one of the tag names or attribute names is invalid. The selected value must be a number");
            e.printStackTrace();
            System.exit(-5);
        }


        return pnv;


    }

    //this method returns the matching child tag found or null
    //match is done by tag name and name/attribute pairs
    //the tag name and name/attribute pairs of the parent tag are ignored
    private static Tag check(Tag t, String tagName, String[][] nameAttributePairs) {
        Tag match = null;

        //check all the children of the tag
        for (Tag child : t.getAllChildren()) {

            //name matches
            //so search for name/attribute matches
            if (child.getTagName().toString().equals(tagName)) {


                //if there are no name/attribute pairs
                //then just a matching tag name signifies a match
                boolean allFound = true;

                if (nameAttributePairs != null) {
                    //loop over each of the name/attribute pairs to find a matching name/attribute pair
                    for (String[] namevalue : nameAttributePairs) {

                        String name = namevalue[0];
                        String value = namevalue[1];

                        boolean found = false;

                        //first search the string attributes
                        //then search the double attributes

                        //for each selector name/attribute pair, see if there's a match
                        for (String pn : child.getStringAttributes().keySet()) {

                            boolean nameMatches = pn.equalsIgnoreCase(name);
                            boolean valueMatches = child.getStringAttributes().get(pn).getValue().equals(value);
                            found = nameMatches && (value.isEmpty() || valueMatches);

                            if (found) break;

                        }

                        if (!found) {
                            //for each selector name/attribute pair, see if there's a match
                            for (String pn : child.getDoubleAttributes().keySet()) {
                                boolean nameMatches = pn.equalsIgnoreCase(name);
                                boolean valueMatches = String.valueOf(child.getDoubleAttributes().get(pn).getValue()).equals(value);
                                found = nameMatches && (value.isEmpty() || valueMatches);

                                if (found) break;

                            }
                        }


                        if (!found) {
                            allFound = found;
                            break;
                        }
                    }
                }

                if (allFound) {
                    match = child;
                    break;
                }


            }
        }

        return match;
    }


    private static final String paramFileSep = ",";

    public static void writeParams(String selectorFile, List<ParamNum> paramNums) {

        //"name selector min max step value"


        List<String> paramLines = new ArrayList<String>();

        for (ParamNum paramNum : paramNums) {

            paramLines.add(paramNum.getName().toString() + paramFileSep +
                            paramNum.getSelectorString() + paramFileSep +
                            paramNum.getCurrentMin() + paramFileSep +
                            paramNum.getCurrentMax() + paramFileSep +
                            paramNum.getCurrentStep() + paramFileSep +
                            paramNum.getValue().toString() + paramFileSep
            );

            FileReaderWriter.writeLines(selectorFile, paramLines);


        }

    }


    public static List<ParamNum> readParams(String selectorFile) {

        List<ParamNum> paramNums = new ArrayList<ParamNum>();
        File           f         = new File(selectorFile);
        if (f.exists()) {
            List<String> paramLines = FileReaderWriter.readLines(selectorFile);


            for (String paramLine : paramLines) {

                String[] parts = paramLine.split(paramFileSep);
                String name = parts[0];
                String selector = parts[1];
                if (selector.equalsIgnoreCase("null")) selector = null;
                double min = Double.parseDouble(parts[2]);
                double max = Double.parseDouble(parts[3]);
                double step = Double.parseDouble(parts[4]);
                double value = Double.parseDouble(parts[5]);
                //  String name, String selectorString, double value, double min, double max, double step
                ParamNum pn = new ParamNum(name, selector, value, min, max, step);

                paramNums.add(pn);
            }


        }
        return paramNums;
    }

}
