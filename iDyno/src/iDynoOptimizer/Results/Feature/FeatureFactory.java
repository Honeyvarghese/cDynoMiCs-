package iDynoOptimizer.Results.Feature;

import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;
import iDynoOptimizer.Results.Feature.Temporal.VelocitySimulationFeature;
import iDynoOptimizer.Search.SimulationIteration;

import java.util.*;

/**
 * Created by Chris Johnson on 11/18/2014.
 */

/*
This factory is essentially a mapping of FeatureNameIteration to the correct feature instance, e.g., HeightFeature
 */
public class FeatureFactory {




    /*
        Returns one or more features of type specified by "name"
        If more than one file is found at pathToFeatureData, tries to return a feature instance for each (each one will be the same type of feature)
     */
//    public static List<Feature> makeFeatures(FeatureNameIteration name, String pathToFeatureData)
//    {
//        List<Feature>features = new ArrayList<Feature>();
//        if (name == FeatureNameIteration.contour)
//        {
//            for(File f : FileReaderWriter.getFilesInDir(pathToFeatureData))
//            {
//                features.add(new ContourIterationFeature(f));
//            }
//            return features;
//        }
//        else return null;
//    }
//
//    public static Feature makeFeature(FeatureNameIteration name, String pathToFeatureData)
//    {
//        List<Feature> features = new ArrayList<Feature>();
//        if(features.size() > 0) return features.get(0);
//        else return null;
//    }
//    /*
//           Returns one or more features for each of the feature types specified by names
//        */
//    public static LinkedHashMap<FeatureNameIteration, List<Feature>> makeFeatures(HashMap<FeatureNameIteration, String> featureNamesAndTheirPathLocations)
//    {
//        LinkedHashMap<FeatureNameIteration, List<Feature>>  features = new LinkedHashMap<FeatureNameIteration, List<Feature>>();
//
//        for(FeatureNameIteration fni : featureNamesAndTheirPathLocations.keySet())
//        {
//            features.put(fni, makeFeatures(fni, featureNamesAndTheirPathLocations.get(fni)));
//        }
//        return features;
//    }





    public static Feature makeFeature(FeatureNameSimulation name, List<SimulationIteration> iterations)
    {
        if(name == FeatureNameSimulation.velocitySimulation)
            return new VelocitySimulationFeature(iterations);



        else return null;
    }



}
