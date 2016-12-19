package iDynoOptimizer;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;
import iDynoOptimizer.Results.Feature.Names.NumericFeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.SpatialNumericFeatureNameIteration;
import iDynoOptimizer.Results.IterationResult;
import iDynoOptimizer.Search.SimulationRun;
import iDynoOptimizer.Search.SimulationRunComparator;
import java.io.File;
import java.util.*;

/**
 * Created by chris on 9/21/2015.
 */
public class ExtractAndOutputExisting {


    private static final String ef = "ef";
    private static final String fs = "fs";
    private static final String rw = "rw";

    public static void main(String[] args) {

        String experimentFolderString = "";
        String featureFileString = "";
        boolean rankWrinkling = false;

        if (args == null || args.length < 2)
            throw new IllegalArgumentException("You must specify both the experiment folder path (full path to the sweeps) and the features file path");

        String   name;
        String   value;
        String[] parts;

        for (String s : args) {
            parts = s.split("=");
            name = parts[0].trim();
            value = parts[1].trim();

            if(name.equalsIgnoreCase(ef)) experimentFolderString = value;
            else if(name.equalsIgnoreCase(fs)) featureFileString = value;

            else if(name.equalsIgnoreCase(rw))  rankWrinkling = Boolean.parseBoolean(value);
        }

        if(!(new File(featureFileString).exists()))
        {
            throw new IllegalArgumentException("The features file doesn't exist!");
        }

        List<String> resultPaths = new ArrayList<>();

        //get the result paths by drilling into the experiment folder until and agent state folder is found, then go up one
        recursivelyFindASimulation(new File(experimentFolderString), resultPaths);

        Collections.sort(resultPaths);

        if(resultPaths.isEmpty())
        {
            throw new IllegalArgumentException("The experiment folder doesn't exist or doesn't contain any simulations!");
        }


        Global g = Global.getGlobal();
        g.setExperimentFolder(experimentFolderString);

        Map<FeatureNameSimulation, int[]>              simWhichIterations      = Feature.simFeatureNamesFromFile(featureFileString);
        Map<NumericFeatureNameIteration, int[]>        itrNumWhichIterations   = Feature.numItrFeatureNamesFromFile(featureFileString);
        Map<SpatialNumericFeatureNameIteration, int[]> itrSpNumWhichIterations = Feature.spatialFeatureNamesFromFile(featureFileString);


        Map<FeatureNameIteration, int[]> itrWhichIterations = new HashMap<>();
        itrWhichIterations.putAll(itrNumWhichIterations);
        itrWhichIterations.putAll(itrSpNumWhichIterations);


        List<SimulationRun> runs = new ArrayList<>();
        int successAdds = 0;
        for(String r : resultPaths) {

            SimulationRun sr = SimulationRun.extractSimulationAndSomeIter(simWhichIterations, itrWhichIterations, r);
            sr.outputDeleteZip(true, true, false, false);
            if(sr.isValid()) {
                sr.calculateWrinklingIndex();
                sr.deleteAfterErrorGenerated();
                runs.add(sr);

              if(++successAdds % 100 == 0) MyPrinter.Printer().printTier1ln(successAdds);
            }
            else
            {

                MyPrinter.Printer().printTier1ln("------" + r + " is INVALID------");
            }
        }

        if(rankWrinkling)
        {
            runs.sort(new SimulationRunComparator());
            resultPaths = new ArrayList<>();
            int i = 0;
            for(SimulationRun sr : runs) {
                resultPaths.add(sr.getWrinklingIndex() + " " + ++i + " "  + sr.getResultPath());

            }

            FileReaderWriter.writeLines(experimentFolderString + File.separator+ "wrinklingRanking.txt", resultPaths);

        }

    }

    private static void recursivelyFindASimulation(File parent, List<String> resultPaths)
    {

        if(parent.exists() && parent.isDirectory())
        {
            if(parent.getName().equalsIgnoreCase(IterationResult.getAgentFolder())) resultPaths.add(parent.getParent());
            else
            {
               for(File child : parent.listFiles())
               {
                   recursivelyFindASimulation(child, resultPaths);
               }

            }
        }
    }
}
