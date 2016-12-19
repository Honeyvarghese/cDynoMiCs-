package iDynoOptimizer.Search;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.IterationFeature;
import iDynoOptimizer.Results.Feature.Names.*;
import iDynoOptimizer.Results.Feature.NumericIterationFeature;
import iDynoOptimizer.Results.Feature.SpatialNumericIterationFeature;
import iDynoOptimizer.Results.Feature.Temporal.VelocityFeature;
import iDynoOptimizer.Results.Feature.Temporal.VelocitySimulationFeature;


import java.io.File;
import java.util.*;

/**
 * Created by chris on 11/1/2015.
 */
public class CompareRealData {


    private static Map<FeatureName, Double> accumulatedErrorsByFeatureName;


    //These are the real (in vitro) data
    //Map from Iteration -> Map from FeatureName -> Feature
    private static Map<Integer, Map<FeatureNameIteration, IterationFeature>> itrRealFeatures = new HashMap<>();
    private static Map<FeatureNameSimulation, Feature>                       simRealFeatures = new HashMap<>();


    /*
    Iteration features file organization:
        A folder for each iteration (named after the iteration number)
            A file for each feature (named after the feature name) with the data
                Either a 2D matrix or a single number


    Simulation features file organization
        A folder for each one named after the Sim feature name
            Folder contains a separate file for each time slice of the sim feature

    If anything else is found, it ignores it, but does print a line indicating

    returns the number of features loaded
     */
    public static int load(String dataFilePath) {


        MyPrinter.Printer().printTier1ln("Loading in vitro data from " + dataFilePath);


        List<File> files = FileReaderWriter.getFilesInDir(dataFilePath);

        List<String> simFiles = new ArrayList<>();

        int count = 0;
        for (File featFolder : files) {

            boolean isDir = featFolder.isDirectory();
            boolean isSimFeatFolder = isSimFeatFolder(featFolder.getName());

            if (isDir && !isSimFeatFolder) {

                int iteration;
                //make sure its named after a number
                try {
                    iteration = Integer.parseInt(featFolder.getName().trim());

                } catch (IllegalArgumentException ie) {
                    MyPrinter.Printer().printErrorln("A directory in the in vitro data directory is not named after a simulation feature nor an integer. Ignoring this directory");
                    continue;
                }


                MyPrinter.Printer().printTier2ln("Loading iteration feature from " + featFolder.getName());

                count += loadItrFeatures(featFolder, iteration);
            } else if (isDir && isSimFeatFolder) {

                MyPrinter.Printer().printTier2ln("Loading simulation feature from " + featFolder.getName());
                loadSimFeatures(featFolder);
                count++;
            }
        }


        MyPrinter.Printer().printTier1ln(count + " objectives loaded");
        return count;

    }

    //returns true if the comparison was successful
    //false if not
    public static boolean compare(SimulationRun sr, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ) {

        accumulatedErrorsByFeatureName = new TreeMap<>();


        List<SimulationIteration> sis = sr.getSimulationIterations();

        //sum up the error fore each feature over the iterations
        if (itrRealFeatures != null && !itrRealFeatures.isEmpty()) {
            for (SimulationIteration si : sis) {
                Map<FeatureNameIteration, IterationFeature> realFeatures = itrRealFeatures.get(si.getIteration());

                for (FeatureNameIteration fni : realFeatures.keySet()) {

                    double current;
                    IterationFeature realFeat = realFeatures.get(fni);
                    IterationFeature testFeat = si.getFeature(fni);

                    if (realFeat == null || testFeat == null) return false;

                    Error e = new Error(realFeat, testFeat, errorCalculation, paddingX, paddingY, paddingZ);


                    if (accumulatedErrorsByFeatureName.containsKey(fni))
                        current = accumulatedErrorsByFeatureName.get(fni);
                    else current = 0;

                    accumulatedErrorsByFeatureName.put(fni, e.getError() + current);
                }

            }

            //calculate the average error over the iterations (temporal average)
            for (FeatureName fi : accumulatedErrorsByFeatureName.keySet()) {
                double temporalAvg = accumulatedErrorsByFeatureName.get(fi) / sis.size();
                accumulatedErrorsByFeatureName.put(fi, temporalAvg);
            }
        }


        if (simRealFeatures != null && !simRealFeatures.isEmpty()) {
            //the temporal average is already computed for simulation features, so just record the error (don't need to take an average)
            for (FeatureNameSimulation fns : sr.getWholeSimulationFeatures().keySet()) {
                Feature realFeat = simRealFeatures.get(fns);
                Feature testFeat = sr.getWholeSimulationFeatures().get(fns);

                if (realFeat == null || testFeat == null) return false;

                Error e = new Error(realFeat, testFeat, errorCalculation, paddingX, paddingY, paddingZ);

                accumulatedErrorsByFeatureName.put(fns, e.getError());

            }
        }


        return true;

    }


    private static void loadSimFeatures(File simFolder) {

        List<VelocityFeature> velocities = new ArrayList<>();
        for (File featureFile : simFolder.listFiles()) {

            if (isTemporalFeatFile(featureFile.getName().split("-")[0])) {

                velocities.add(new VelocityFeature(featureFile.getPath()));

            }

        }

        VelocitySimulationFeature velocitySimulationFeature = new VelocitySimulationFeature(velocities, true);
        simRealFeatures.put(FeatureNameSimulation.velocitySimulation, velocitySimulationFeature);

    }

    /*
    Returns the number of features loaded
     */
    private static int loadItrFeatures(File itrFolder, int iteration) {


        Map innerMap = new HashMap<>();
        int count    = 0;


        //load the feature data from the iteration folder
        for (File featureFile : itrFolder.listFiles()) {
            if (!featureFile.isDirectory()) {
                FeatureNameIteration fni;
                IterationFeature feat;

                try {
                    fni = NumericFeatureNameIteration.valueOf(featureFile.getName());

                    feat = new NumericIterationFeature(featureFile.getPath(), fni);

                } catch (IllegalArgumentException ie) {
                    try {
                        fni = SpatialNumericFeatureNameIteration.valueOf(featureFile.getName());

                        feat = new SpatialNumericIterationFeature(featureFile.getName(), fni);

                    } catch (IllegalArgumentException ie2) {
                        MyPrinter.Printer().printErrorln("A file in an in vitro iteration feature folder is not named after an iteration feature. Ignoring this file");
                        continue;
                    }

                }

                count++;
                innerMap.put(fni, feat);
                itrRealFeatures.put(iteration, innerMap);
            } else {
                MyPrinter.Printer().printErrorln("A directory found in an in vitro iteration feature folder. Ignoring this directory");
            }

        }
        return count;
    }


    public static Map<FeatureName, Double> getAccumulatedErrorsByFeatureName() {
        return accumulatedErrorsByFeatureName;
    }


    private static boolean isSimFeatFolder(String folderName) {
        try {
            FeatureNameSimulation.valueOf(folderName);
            return true;
        } catch (IllegalArgumentException ie) {
            return false;
        }
    }

    private static boolean isTemporalFeatFile(String fileName) {
        try {
            TemporalNumericFeatureName.valueOf(fileName);
            return true;
        } catch (IllegalArgumentException ie) {
            return false;
        }
    }

}
