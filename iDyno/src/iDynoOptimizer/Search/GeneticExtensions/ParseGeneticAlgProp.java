package iDynoOptimizer.Search.GeneticExtensions;

import iDynoOptimizer.Global.MyPrinter;

import java.util.*;

/**
 * Created by chris on 11/8/2015.
 */
public class ParseGeneticAlgProp {


    private static Map<GeneticParallelAlgName, List<String>> algNameToProp = new HashMap();


    private static String assign = "#";
    private static String sep    = ",";


    public static String getAssignDelimiter()
    {
        return assign;
    }

    //Returns a set of properties parsed from the propString
    //If a property doesn't belong to the named algorithm, prints an error message and doesn't use it
    public static Properties parse(GeneticParallelAlgName algName, String propString) {

        loadAlgProps();

        String[] propNameValues = propString.split(sep);

        Properties ps = new Properties();

        for (String pnv : propNameValues) {
            String[] nv = pnv.split(assign);
            if (nv.length != 2) continue;
            String name = nv[0];
            String value = nv[1];



            //if the specified algorithm doesn't contain the property, skip it and print an error message
            if(!algNameToProp.get(algName).contains(name))
            {
                MyPrinter.Printer().printErrorln("Property " + name + " doesn't belong to algorithm " + algName.toString());
                continue;
            }

            MyPrinter.Printer().printTier2ln("Parsed property " + name + " with value " + value);
            ps.put(name, value);
        }
        return ps;
    }






    private static void loadAlgProps()
    {
        List<String> props = new ArrayList<>();
        props.add("populationSize");
        props.add("sbx.rate");
        props.add("sbx.distributionIndex");
        props.add("pm.rate");
        props.add("pm.distributionIndex");
        algNameToProp.put(GeneticParallelAlgName.NSGAII, props);


        props = new ArrayList<>();
        props.add("populationSize");
        props.add("epsilon");
        props.add("sbx.rate");
        props.add("sbx.distributionIndex");
        props.add("pm.rate");
        props.add("pm.distributionIndex");
        props.add("injectionRate");
        props.add("windowSize");
        props.add("maxWindowSize");
        props.add("minimumPopulationSize");
        props.add("maximumPopulationSize");
        algNameToProp.put(GeneticParallelAlgName.eNSGAII, props);


        props = new ArrayList<>();
        props.add("populationSize");
        props.add("divisions");
        props.add("sbx.rate");
        props.add("sbx.distributionIndex");
        props.add("pm.rate");
        props.add("pm.distributionIndex");
        props.add("divisionsOuter");
        props.add("divisionsInner");
        algNameToProp.put(GeneticParallelAlgName.NSGAIII, props);


        props = new ArrayList<>();
        props.add("populationSize");
        props.add("de.crossoverRate");
        props.add("de.stepSize");
        algNameToProp.put(GeneticParallelAlgName.GDE3, props);



    }

}
