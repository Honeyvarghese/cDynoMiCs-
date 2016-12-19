package iDynoOptimizer;

import iDynoOptimizer.Global.ExtraMath;
import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.Executor;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.NondominatedPopulation;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Solution;
import iDynoOptimizer.Protocol.Parameters.ParamResetType;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolFile;
import iDynoOptimizer.Results.Feature.Error.SquaredDifference;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;
import iDynoOptimizer.Results.Feature.Names.NumericFeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.SpatialNumericFeatureNameIteration;
import iDynoOptimizer.Search.*;
import iDynoOptimizer.Search.ExitCondition.ExitCondition;
import iDynoOptimizer.Search.ExitCondition.Operator;
import iDynoOptimizer.Search.GeneticExtensions.GeneticParallelAlgName;
import iDynoOptimizer.Search.GeneticExtensions.ParseGeneticAlgProp;
import iDynoOptimizer.Search.GeneticExtensions.SimulationProblem;
import iDynoOptimizer.Search.GeneticExtensions.SimulationSolution;


import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.AbstractMap.SimpleEntry;


/**
 * Created by Chris on 9/2/2015.
 */
public class Sweep {

    private static final String mainMode        = "mm";
    private static final String master          = "master";
    private static final String slave           = "slave";
    private static final String extractExisting = "extract";

    private static final String roster             = "ro"; //(required) roster path (will be created. if it exists it will be recreated)
    private static final String rosterMaster       = "rom"; //(required) master roster path (will be created. if it exists it will be recreated)
    private static final String lock               = "l"; //(required) full lock path (full path and file name)
    private static final String input              = "i"; //(required) input path (must already exist)
    private static final String output             = "o"; //(required) output path (must already exist)
    private static final String folder             = "fo"; //name of folder containing protocol files to run. This folder needs to be located at the input path location
    private static final String file               = "fi"; //name of target protocol file. Either this or the in vitro file path must be specified
    private static final String inVitroData        = "ivd"; //path to the in vitro data. if this is not specified, comparisons are done to the target simulation.
    // If both are specified the in vitro data will be used
    private static final String repeats            = "rs"; //number of random seed repeats
    private static final String sweepName          = "snm"; //name of the file located in output path that all the sweep data will be written to. Will be created if it doesn't exist
    private static final String geneticAlg         = "ga"; //name of the genetic algorithm to use.
    private static final String geneticAlgProps    = "gap"; //string of name value pairs containing the names and values of the genetic algorithm properties
    private static final String features           = "fs"; //path to the folder containing the features to extract (and possibly output and use to the fitness function)
    private static final String milliSecWait       = "mw"; //milliseconds to wait in between checks to see if any new simulations are done
    private static final String mode               = "m"; //the mode(sweep, hill climber, or genetic algorithm and whether or not to output the extracted features
    private static final String exitError          = "ee"; //the threshold error for ending the a hill climber search
    private static final String maximumSteps       = "ms"; //the maximum number of steps(generations) that the hill climber or genetic algorithm can take
    private static final String sweepLocationCount = "lc";
    private static final String maximumEvaluations = "me"; //the maximum number of fitness evaluations genetic algorithm can do


    private static final String xyzPadding = "xyzp"; //in grid units how much of the biofilm boundary do we ignore when comparing against real data?
    //expects 3 (comma separated) integers, which are the number of grid units to pad each side of the the x, y, and z dimensions, respectively
    //(2, 6, 5) adds 4 units total to x, 12 to y, and 10 to z (4, 6, and 5 to each side of x, y, and z, respectively)
    //particles within this padded area will be ignored.


    private static final String miscFilesToResults = "mfr"; //a list of full file paths (comma separated) to copy to the results folder


    //a genetic algorithm will terminate when the evaluation or step/generate limit has been reached

    private static final String                 defaultFoldername         = "Sweep";
    private static final GeneticParallelAlgName defaultGeneticAlgorithm   = GeneticParallelAlgName.NSGAII;
    private static final int                    defaultPopulationSize     = 30;
    private static final String                 defaultGAProperties       = "populationSiz" + ParseGeneticAlgProp.getAssignDelimiter() + defaultPopulationSize;
    private static       int                    defaultNumRepeats         = 1;
    private static final int                    defaultMillSecToWait      = 6000;
    private static final HCMode                 defaultMode               = HCMode.sweepWithOutput;
    private static final double                 defaultExitError          = .0000001;
    private static final int                    defaultMaximumSteps       = 30;
    private static final int                    defaultSweepLocationCount = 2;
    private static final int                    defaultMaximumEvaluations = 10000;

    private static final int    defaultPadding       = 0;
    private static final String defaultPaddingString = defaultPadding + "," + defaultPadding + "," + defaultPadding;


    /// String protocolFile, String protocolFolder, int numRepeats, int millSecToWait, double exitError, double maximumSteps, int sweepLocationCount,
    // Map<FeatureNameSimulation, int[]> simWhichIterations, Map<FeatureNameIteration, int[]> itrWhichIterations, boolean output

    private static String protocolFile;
    private static String protocolFolder;
    private static int    numRepeats;
    private static int    millSecToWait;
    private static int    maxGenerations; //holds the maximum number of steps/generations (used by genetic and hill climber)
    private static int paddingX = 0;
    private static int paddingY = 0;
    private static int paddingZ = 0;


    private static Map<FeatureNameSimulation, int[]> simWhichIterations;
    private static Map<FeatureNameIteration, int[]>  itrWhichIterations;
    private static boolean                           doOutput;


    /*

    Sees if the first argument is named mainMode

    If it's not or the value is "slave" it runs a SimRunnerPool, passing the rest of the arguments to that class's main method

    If it is and the value is "master", it runs a Sweep, passing the rest of the arguments
    If it is and the value is something else, it runs ExtractAndOutputExisting


     */
    public static void main(String[] args) throws IOException {


        MyPrinter.Printer().setTier1On(true);
        MyPrinter.Printer().setTier2On(true);

        if (args == null || args.length < 1) {
            //let this class handle the fact that there aren't enough arguments
            SimRunnerPool.main(args);
        } else {
            String name;
            String value;
            String[] parts;
            parts = args[0].split("=");
            name = parts[0].trim();
            value = parts[1].trim();

            if (name.equalsIgnoreCase(mainMode)) {
                //remove the "mors" arguement
                String[] newArgs = new String[args.length - 1];
                for (int i = 0; i < newArgs.length; i++) {
                    newArgs[i] = args[i + 1];
                }

                if (value.equalsIgnoreCase(master)) Sweep.sweepAndHC(newArgs);
                else if (value.equalsIgnoreCase(slave)) SimRunnerPool.main(newArgs);
                else ExtractAndOutputExisting.main(newArgs);
            }
        }


    }

    public static void sweepAndHC(String[] args) throws IOException {

        Search.setRunSimulationsInline(false);


        SimpleEntry<String, String> rosterPath       = new SimpleEntry(Sweep.roster, "");
        SimpleEntry<String, String> rosterMasterPath = new SimpleEntry(Sweep.rosterMaster, "");
        SimpleEntry<String, String> lockPath         = new SimpleEntry(Sweep.lock, "");
        SimpleEntry<String, String> inputPath        = new SimpleEntry(Sweep.input, "");
        SimpleEntry<String, String> outputPath       = new SimpleEntry(Sweep.output, "");
        SimpleEntry<String, String> protocolFolder   = new SimpleEntry(Sweep.folder, "");
        SimpleEntry<String, String> protocolFile     = new SimpleEntry(Sweep.file, "");
        SimpleEntry<String, Number> numRepeats       = new SimpleEntry(Sweep.repeats, defaultNumRepeats);
        SimpleEntry<String, String> sweepFolderName  = new SimpleEntry(Sweep.sweepName, defaultFoldername);

        SimpleEntry<String, String>                 inVitroDataFolderPath = new SimpleEntry(Sweep.inVitroData, "");
        SimpleEntry<String, GeneticParallelAlgName> geneticAlgName        = new SimpleEntry(Sweep.geneticAlg, defaultGeneticAlgorithm);
        SimpleEntry<String, String>                 geneticAlgProperties  = new SimpleEntry<String, String>(Sweep.geneticAlgProps, defaultGAProperties);

        SimpleEntry<String, String> padding = new SimpleEntry<String, String>(Sweep.xyzPadding, defaultPaddingString);

        SimpleEntry<String, String> miscFiles = new SimpleEntry<String, String>(Sweep.miscFilesToResults, "");

        SimpleEntry<String, Number> millSecToWait = new SimpleEntry(Sweep.milliSecWait, defaultMillSecToWait);

        SimpleEntry<String, String> features = new SimpleEntry(Sweep.features, "");

        SimpleEntry<String, HCMode> mode         = new SimpleEntry(Sweep.mode, defaultMode);
        SimpleEntry<String, Number> exitError    = new SimpleEntry(Sweep.exitError, defaultExitError);
        SimpleEntry<String, Number> maximumSteps = new SimpleEntry(Sweep.maximumSteps, defaultMaximumSteps);

        SimpleEntry<String, Number> maximumEvaluations = new SimpleEntry(Sweep.maximumEvaluations, defaultMaximumEvaluations);

        SimpleEntry<String, Number> sweepLocationCount = new SimpleEntry<String, Number>(Sweep.sweepLocationCount, defaultSweepLocationCount);

        List<SimpleEntry<String, String>> stringPairs = new ArrayList<>();


        stringPairs.add(rosterPath);
        stringPairs.add(rosterMasterPath);
        stringPairs.add(inputPath);
        stringPairs.add(outputPath);
        stringPairs.add(protocolFolder);
        stringPairs.add(protocolFile);
        stringPairs.add(sweepFolderName);
        stringPairs.add(inVitroDataFolderPath);
        stringPairs.add(features);
        stringPairs.add(lockPath);
        stringPairs.add(geneticAlgProperties);
        stringPairs.add(padding);
        stringPairs.add(miscFiles);

        if (args == null || args.length < 5)
            throw new IllegalArgumentException("You must specify the path to the roster, the path to the master roster, the path to the lock file, " +
                    "the path where input is located, " +
                    "and the path where the output is to be stored.");

        if (args.length < 6) {
            throw new IllegalArgumentException("You must specify either the name of the folder containing the protocol files to be run, the name of a protocol file to run, or both. " +
                    "These must be located in the input folder");
        }

        String   name;
        String   value;
        String[] parts;

        for (String s : args) {
            parts = s.split("=");
            name = parts[0].trim();
            value = parts[1].trim();

            for (SimpleEntry p : stringPairs) {
                if (((String) p.getKey()).equalsIgnoreCase(name))
                    p.setValue(value);
            }


            if (geneticAlgName.getKey().equalsIgnoreCase(name)) {
                try {
                    geneticAlgName.setValue(GeneticParallelAlgName.valueOf(value));
                } catch (IllegalArgumentException ie) {
                    MyPrinter.Printer().printErrorln("The genetic algorithm you specified is invalid. Make sure it is one of the parallelizable ones implemented in MOEA. " + defaultGeneticAlgorithm.toString() + " will be used if this run is a genetic algorithm");
                }
            }

            convertNumber(name, value, maximumEvaluations, "Maximum evaluations not an integer. " + defaultMaximumEvaluations + " will be used. Currently this parameter is only used for the genetic algorithms");

            convertNumber(name, value, numRepeats, "Number of random seed repeats not an integer. Only " + defaultNumRepeats + " of each simulation will be run.");

            convertNumber(name, value, millSecToWait, "Milliseconds to wait not an integer. " + defaultMillSecToWait + " will be used.");


            if (mode.getKey().equalsIgnoreCase(name)) {
                try {
                    mode.setValue(HCMode.valueOf(value));
                } catch (IllegalArgumentException e) {
                    MyPrinter.Printer().printErrorln("The mode you specified is invalid. The default mode (" + defaultMode.toString() + ") will be used.");
                }

            }

            convertNumber(name, value, exitError, "The exit error threshold you specified is not value. It should be a positive number. " + defaultExitError + " will be used (if this run is a hill climber).");
            convertNumber(name, value, maximumSteps, "The maximum number of hill climber steps you specified is not value. It should be a positive integer. " + defaultMaximumSteps + " will be used (if this run is a hill climber).");


            convertNumber(name, value, sweepLocationCount, "Sweep location count not an integer. " + defaultSweepLocationCount + " will be used (if this run is a hill climber).");


        }

        parsePadding(padding.getValue());


        File pathChecks = new File(rosterPath.getValue());

        if (pathChecks.exists()) {
            pathChecks.delete();
        }

        try {

            if (!pathChecks.createNewFile()) throw new IllegalArgumentException();
        } catch (IOException e) {

            throw new IllegalArgumentException("Unable to create roster file");
        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException("Unable to create roster file");
        }

        pathChecks = new File(rosterMasterPath.getValue());

        if (pathChecks.exists()) {
            pathChecks.delete();
        }

        try {

            if (!pathChecks.createNewFile()) throw new IllegalArgumentException();
        } catch (IOException e) {

            throw new IllegalArgumentException("Unable to create master roster file");
        } catch (IllegalArgumentException e) {

            throw new IllegalArgumentException("Unable to create master roster file");
        }


        pathChecks = new File(inputPath.getValue());

        if (!pathChecks.exists()) {
            throw new IllegalArgumentException("Invalid input path");
        }

        pathChecks = new File(outputPath.getValue());
        if (!pathChecks.exists()) {
            throw new IllegalArgumentException("Invalid output path");
        }


        pathChecks = new File(lockPath.getValue());

        if (!pathChecks.exists()) {
            try {
                if (pathChecks.createNewFile()) pathChecks.delete();
                else throw new IllegalArgumentException();
            } catch (IOException e) {

                throw new IllegalArgumentException("Invalid lock file");
            } catch (IllegalArgumentException e) {

                throw new IllegalArgumentException("Invalid lock file");
            }
        } else {
            pathChecks.delete();
        }

        boolean protocolFolderExist = !protocolFolder.getValue().isEmpty() && (new File(inputPath.getValue() + File.separator + protocolFolder.getValue()).exists());
        boolean protocolFileExist   = !protocolFile.getValue().isEmpty() && (new File(inputPath.getValue() + File.separator + protocolFile.getValue()).exists());
        boolean invitroDataPath     = !inVitroDataFolderPath.getValue().isEmpty() && (new File(inVitroDataFolderPath.getValue())).exists();

        if (mode.getValue() == HCMode.hillClimber || mode.getValue() == HCMode.hillClimberWithOutput || mode.getValue() == HCMode.genetic || mode.getValue() == HCMode.geneticWithOutput) {

            if ((!protocolFileExist && !invitroDataPath) || !protocolFolderExist) {
                throw new IllegalArgumentException("Since this run is a hill climb or genetic run (the test protocol folder (at the input path location) must exist) AND (the target protocol file (at the input path location) must exist OR the invitro data path must exist. They either don't exist or you didn't specify their locations correctly");
            }

        } else {
            if (!protocolFolderExist && !protocolFileExist) {
                throw new IllegalArgumentException("Either the protocol file, folder, or both do not exist at the input file location. You forgot to make them or enter them");
            }
        }


        Global.getGlobal(inputPath.getValue(), outputPath.getValue(), protocolFolder.getValue(), protocolFile.getValue(), sweepFolderName.getValue());
        Global.getGlobal().setRosterFilePath(rosterPath.getValue());
        Global.getGlobal().setRosterMasterFilePath(rosterMasterPath.getValue());
        Global.getGlobal().setLockFilePath(lockPath.getValue());

        Global.getGlobal().setMiscFilesFullPaths(parseMiscFiles(miscFiles.getValue()));


        simWhichIterations = Feature.simFeatureNamesFromFile(features.getValue());
        Map<NumericFeatureNameIteration, int[]>        itrNumWhichIterations   = Feature.numItrFeatureNamesFromFile(features.getValue());
        Map<SpatialNumericFeatureNameIteration, int[]> itrSpNumWhichIterations = Feature.spatialFeatureNamesFromFile(features.getValue());


        itrWhichIterations = new HashMap<>();
        itrWhichIterations.putAll(itrNumWhichIterations);
        itrWhichIterations.putAll(itrSpNumWhichIterations);


        Sweep.protocolFile = protocolFile.getValue();
        Sweep.protocolFolder = protocolFolder.getValue();
        Sweep.numRepeats = (int) numRepeats.getValue();
        Sweep.millSecToWait = (int) millSecToWait.getValue();
        Sweep.maxGenerations = (int) maximumSteps.getValue();
        if (mode.getValue() == HCMode.hillClimber || mode.getValue() == HCMode.hillClimberWithOutput) {

            Sweep.doOutput = mode.getValue() == HCMode.hillClimberWithOutput;
            hillClimb((double) exitError.getValue(), (int) sweepLocationCount.getValue());
        } else if (mode.getValue() == HCMode.genetic || mode.getValue() == HCMode.geneticWithOutput) {
            Sweep.doOutput = mode.getValue() == HCMode.geneticWithOutput;
            genetic(geneticAlgName.getValue(), geneticAlgProperties.getValue(), (int) maximumEvaluations.getValue(), inVitroDataFolderPath.getValue());
        } else {
            Sweep.doOutput = mode.getValue() == HCMode.sweepWithOutput;
            sweep();
        }

    }


    private static void convertNumber(String name, String value, SimpleEntry<String, Number> se, String message) {
        if (se.getKey().equalsIgnoreCase(name)) {
            try {
                se.setValue(Integer.parseInt(value));
            } catch (NumberFormatException e1) {

                try {
                    se.setValue(Double.parseDouble(value));
                } catch (NumberFormatException e2) {
                    MyPrinter.Printer().printErrorln(message);
                }


            }
        }
    }

    private static void parsePadding(String paddingString) {
        String[] parts = paddingString.trim().split(",");

        try {
            if (parts.length > 0)
                Sweep.paddingX = Integer.parseInt(parts[0]);
            if (parts.length > 1)
                Sweep.paddingY = Integer.parseInt(parts[1]);
            if (parts.length > 2)
                Sweep.paddingZ = Integer.parseInt(parts[2]);
        } catch (Exception e) {
            MyPrinter.Printer().printErrorln("One or more of the x,y,z padding values is not an integer. Using the default value of 0 for each of them");
            Sweep.paddingX = Sweep.defaultPadding;
            Sweep.paddingY = Sweep.defaultPadding;
            Sweep.paddingZ = Sweep.defaultPadding;
        }

    }

    private static String[] parseMiscFiles(String miscResultsString) {
        if (miscResultsString == null || miscResultsString.isEmpty()) return null;

        return miscResultsString.trim().split(",");
    }


    public static void sweep() throws IOException {


        boolean runTarget = !protocolFile.isEmpty();
        boolean runTests  = !protocolFolder.isEmpty();


        if (runTarget) {

            Runner target = Search.runJustTarget(numRepeats);
            iDynoMiCSRunnerPool.addRun(target, millSecToWait);

        }
        if (runTests) {
            List<Runner> tests = Search.doJustSweep(numRepeats);
            iDynoMiCSRunnerPool.addRun(tests, millSecToWait);

        }


        getSimRunsAndExtractAsDone();


        MyPrinter.Printer().printTier1ln("SWEEP/MASTER IS ALL DONE!");
    }


    public static void hillClimb(double exitError, int sweepLocationCount) throws IOException {


        ExitCondition errorMin = new ExitCondition(exitError, Operator.lessThanOrEqual);
        ExitCondition stepMax  = new ExitCondition(maxGenerations, Operator.greaterThanOrEqual);


        Runner targetPath = Search.runJustTarget(numRepeats);


        iDynoMiCSRunnerPool.addRun(targetPath, millSecToWait);

//        while (iDynoMiCSRunnerPool.addRunToRoster(targetPath) == null) {
//            iDynoMiCSRunnerPool.waitWithCatch(millSecToWait);
//        }
//        iDynoMiCSRunnerPool.addRun(targetPath);
        ProtocolFile.copyTestProtocols();

        int step = 1;

        Random                     r             = new Random();
        SimulationRun              best          = null;
        SimulationRun              toCheckAround = null;
        SimulationRun              bestThisRound = null;
        Map<SimulationRun, Double> better        = new LinkedHashMap<>();

        Map<SimulationRun, Double>               all         = new LinkedHashMap<>();
        Map<Integer, Map<SimulationRun, Double>> allByStep   = new TreeMap();
        Map<SimulationRun, Double>               allOneStep;
        double                                   lowestError = Double.MAX_VALUE;
        double                                   lowestErrorThisRound;

        List<Double> allErrors = new ArrayList<>();

        double[] allErrorsAvgSTD = new double[2];
        boolean  foundBetter;
        boolean  foundPromising;

        ParamResetType paramResetType = ParamResetType.Random;

        SimulationRun target = null;
        while (!errorMin.met(lowestError) && !stepMax.met(step - 1)) {


            allOneStep = new LinkedHashMap<>();
            allByStep.put(step, allOneStep);

            List<Runner> testPaths = Search.doSweepHelper(step++, numRepeats, toCheckAround, paramResetType, sweepLocationCount);


            iDynoMiCSRunnerPool.addRun(testPaths, millSecToWait);

//            while (iDynoMiCSRunnerPool.addRunsToRoster(testPaths) == null) {
//                iDynoMiCSRunnerPool.waitWithCatch(millSecToWait);
//            }
//            iDynoMiCSRunnerPool.addRuns(testPaths);

            List<SimulationRun> srs = new ArrayList<>();

            boolean stopChecking = false;

//            while (!stopChecking) {
//                if (stopChecking = iDynoMiCSRunnerPool.checkRostersIfAllDone()) {
//
//                    for (Runner run : iDynoMiCSRunnerPool.getRunnerList()) {
//                        SimulationRun sr = SimulationRun.extractSimulationAndSomeIter(simWhichIterations, itrWhichIterations, run.getResultPath());
//                        if (!run.isTarget()) srs.add(sr);
//                        else target = sr;
//                        if (output) sr.output();
//                    }
//                }
//
//                List<Runner> finishedRuns = iDynoMiCSRunnerPool.handleFinishedSimsInRosters(millSecToWait);
//
//                iDynoMiCSRunnerPool.waitWithCatch(millSecToWait);
//            }


            srs = getSimRunsAndExtractAsDone();


            iDynoMiCSRunnerPool.getRunnerList().clear();
            List<CompareRuns> crs = new ArrayList<CompareRuns>();

            for (SimulationRun sr : srs) {

                CompareRuns cr = new CompareRuns(target, sr);
                SquaredDifference sd = new SquaredDifference();
                cr.compareSimulation(sd, false);
                cr.compare(sd, false);
                cr.deleteErrorMaps();
                crs.add(cr);

                sr.deleteAfterErrorGenerated();


            }


            foundBetter = false;
            foundPromising = false;

            lowestErrorThisRound = Double.MAX_VALUE;


            for (CompareRuns cr : crs) {

                if (cr.getTotalError() < lowestErrorThisRound) {
                    lowestErrorThisRound = cr.getTotalError();
                    bestThisRound = cr.getTest();
                }
                all.put(cr.getTest(), cr.getTotalError());
                allOneStep.put(cr.getTest(), cr.getTotalError());
                allErrors.add(cr.getTotalError());

                MyPrinter.Printer().printTier1ln("Swept p1: " + cr.getTest().getParamSet().get(0).getValue() + "\t\t\t" + cr.getTotalError() + "\t\t\t" + cr.getTest().getResultPath());

            }

            if (lowestErrorThisRound < lowestError) {
                foundBetter = true;
                lowestError = lowestErrorThisRound;

                if (best != null) {
                    best.setSearchResultType(SearchResultType.better);
                }

                toCheckAround = best = bestThisRound;
                best.setSearchResultType(SearchResultType.best);
                better.put(best, lowestError);

                MyPrinter.Printer().printTier1ln("Best So Far p1: " + best.getParamSet().get(0).getValue() + "\t\t\t" + lowestError + "\t\t\t" + best.getResultPath());

            }
            //only do this at most 70% of the time to avoid going over the same values again and again by refinding the same good (but not best) values)
            else if (ExtraMath.approxEqual(lowestErrorThisRound, lowestError, allErrorsAvgSTD[1]) && r.nextDouble() < .7) {
                foundPromising = true;
                toCheckAround = bestThisRound;
                MyPrinter.Printer().printTier1ln("OTHER PROMISING p1: " + toCheckAround.getParamSet().get(0).getValue() + "\t\t\t" + lowestErrorThisRound + "\t\t\t" + toCheckAround.getResultPath());

            }


            allErrorsAvgSTD = ExtraMath.avgStd(allErrors);


            boolean betterByWideMargin = ExtraMath.approxEqualorLess(lowestError, allErrorsAvgSTD[0], allErrorsAvgSTD[1] * 1.1) && (foundBetter || foundPromising);


            if (betterByWideMargin) paramResetType = ParamResetType.NormalResetMethod1;
            else paramResetType = ParamResetType.Random;


        }

        System.out.print("\n\nBEST p1: \n" + best.getResultPath() + "\t\t ERROR:" + lowestError + "\t\t Value:" + best.getParamSet().get(0).getValue());


        System.out.print("\n\nHISTORY: \n");
        for (SimulationRun sr : better.keySet()) {
            MyPrinter.Printer().printTier1ln("P1" + sr.getResultPath() + "\t\t ERROR:" + better.get(sr) + "\t\t Value:" + sr.getParamSet().get(0).getValue());

        }

        System.out.print("\n\n Other Close:\n");
        for (SimulationRun sr : all.keySet()) {
            if (ExtraMath.approxEqual(all.get(sr), lowestError, allErrorsAvgSTD[1])) {
                sr.setCloseToBest(true);
                MyPrinter.Printer().printTier1ln("P1" + sr.getResultPath() + "\t\t ERROR: " + all.get(sr) + "\t\t Value:" + sr.getParamSet().get(0).getValue());

            }
        }


        Search.outputClimber(allByStep);

        Search.compilePovraysFromSims(new ArrayList<>(better.keySet()));

    }


    public static void genetic(GeneticParallelAlgName algorithmName, String propertiesString, int maximumEvaluations, String inVitroDataFilePath) {


        MyPrinter.Printer().printTier1ln("Starting genetic algorithm");

        ProtocolFile.copyTestProtocols();


        // run separately for each protocol file
        // results for each can be compared outside the MOEA algorithm later
        File[] protocolFiles = Global.getGlobal().getTestProtocolFiles();


        //load the in vitro data to be compared to later
        int numObjs = CompareRealData.load(inVitroDataFilePath);


        for (int i = 0; i < protocolFiles.length; i++) {


            SimulationProblem.init(i, protocolFiles[i], numObjs, Sweep.paddingX, Sweep.paddingY, Sweep.paddingZ);

            //call executor
            //need to parse and add properties

            MyPrinter.Printer().printTier1ln("Handing control over to the Executor");
            MyPrinter.Printer().printTier1ln("With algorithm: " + algorithmName.toString());
            MyPrinter.Printer().printTier1ln("With max evaluations: " + maximumEvaluations);
            MyPrinter.Printer().printTier1ln("With max generations: " + Sweep.maxGenerations);
            MyPrinter.Printer().printTier1ln("With properties: " + propertiesString);
            NondominatedPopulation results = new Executor().withProblemClass(SimulationProblem.class).withAlgorithm(algorithmName.toString()).withMaxEvaluations(maximumEvaluations).withMaxGenerations(Sweep.maxGenerations).withProperties(ParseGeneticAlgProp.parse(algorithmName, propertiesString), false).run();


            MyPrinter.Printer().printTier1ln("Executor returned results. GA is finished. Printing results:");
            //output results from MOEA
            //add better output later
            for (Solution sol : results) {
                int objectiveCount = sol.getNumberOfObjectives();

                SimulationSolution ss = (SimulationSolution) sol;
                MyPrinter.Printer().printTier1ln(ss.getResultPath());
                MyPrinter.Printer().printTier1ln("");
                for (int o = 0; o < objectiveCount; o++) {
                    MyPrinter.Printer().printTier1ln(sol.getObjective(o));
                }
                MyPrinter.Printer().printTier1ln("");

            }
        }


        //possibly output climber results and povrays


    }


    public static void farmOutRuns(Iterable<Solution> moeaSolutions, int stepNumber) {


        MyPrinter.Printer().printTier2ln("Farming out solutions in step/generation " + stepNumber);

        int count = 0;
        //count how many solutions there are
        for (Solution s : moeaSolutions) {
            count++;
        }

        MyPrinter.Printer().printTier2ln("Number of MOEA solutions received to farm out: " + count);
        if (count > 0) {


            List<SimulationRun> srs = new ArrayList<>();

            List<Runner> runs = Search.doRunHelper(stepNumber, Sweep.numRepeats, moeaSolutions);

            MyPrinter.Printer().printTier2ln("Number of runs generated from MOEA solutions " + runs.size());

            try {
                //add the sim runs to the roster to be run by the workers
                if (runs != null && !runs.isEmpty()) {
                    iDynoMiCSRunnerPool.addRun(runs, Sweep.millSecToWait);
                    //extract each sim run as it gets done

                    MyPrinter.Printer().printTier1ln("Started extracting simulations");
                    srs = Sweep.getSimRunsAndExtractAsDone();

                    MyPrinter.Printer().printTier1ln("Finished extracting simulations");

                } else MyPrinter.Printer().printErrorln("No runs were generated to farm out.");


            } catch (IOException e) {
                e.printStackTrace();
            }


            for (SimulationRun sr : srs) {

                for (Solution s : moeaSolutions) {

                    SimulationSolution ss = (SimulationSolution) s;
                    if (ss.getResultPath().equalsIgnoreCase(sr.getResultPath())) {
                        MyPrinter.Printer().printTier2ln("Adding a simulation run to MOEA solution: " + ss.getResultPath());

                        ss.setSimulationRun(sr);
                    }
                }

            }
        } else MyPrinter.Printer().printErrorln("The GA didn't return any MOEA solutions to farm out");

        iDynoMiCSRunnerPool.getRunnerList().clear();
    }


    private static List<SimulationRun> getSimRunsAndExtractAsDone() throws IOException {

        MyPrinter.Printer().printTier2ln("Entering getSimRunsAndExtractAsDone");

        boolean             stopChecking       = false;
        boolean             getDoneOneLastTime = true;
        List<SimulationRun> srs                = new ArrayList<>();
        while (!stopChecking) {
            List<Runner> finishedRuns = iDynoMiCSRunnerPool.handleFinishedSimsInRosters(millSecToWait);


            //IO exceptions for reading the roster or acquiring/releasing the lock have occurred too many times. Just exit loop so process can terminate
            if (finishedRuns == null) {
                getDoneOneLastTime = false;
                break;
            }
            if (!finishedRuns.isEmpty()) {
                extractTheFinishedRuns(finishedRuns, srs);
                stopChecking = iDynoMiCSRunnerPool.checkRostersIfAllDone(Sweep.millSecToWait);
            }

            if (!stopChecking) {

                MyPrinter.Printer().printTier2ln("Not all the simulations are done yet.");
                iDynoMiCSRunnerPool.waitWithCatch(millSecToWait, "getSimRunsAndExtractAsDone");
            }

        }
        if (getDoneOneLastTime) {


            MyPrinter.Printer().printTier2ln("All the simulations are done. Removing remaining from roster and extracting them");
            //all of the simulations have finished at this point
            //check for any remaining in roster and move them to the master roster
            //then extract them
            //It may be that one or more runs have finished between checking which ones to move from the roster to the master (handleFinishedSimsInRosters)
            //So checkRostersIfAllDone() returns true, but they were not moved over in the call to handleFinishedSimsInRosters
            //this method will also check to see if they are all done. If it returns false this time(it returned true previously), then something is wrong.

            extractTheFinishedRuns(iDynoMiCSRunnerPool.handleFinishedSimsInRosters(millSecToWait), srs);
            boolean theyAllBetterBeDoneStill = iDynoMiCSRunnerPool.checkRostersIfAllDone(Sweep.millSecToWait);

            if (!theyAllBetterBeDoneStill) {
                Exception e = new Exception("In the one last check to remove sims from the roster to the master(and extract them), it returned saying not all of them are done. Sometimes wrong.");
                e.printStackTrace();
                System.exit(-5);
            }

        }

        return srs;
    }


    private static void extractTheFinishedRuns(List<Runner> finishedRuns, List<SimulationRun> srs) throws IOException {

        MyPrinter.Printer().printTier2ln("Entering extractTheFinishedRuns");
        MyPrinter.Printer().printTier2ln("Starting extraction for " + finishedRuns.size() + " simulations");

        for (Runner run : finishedRuns) {
            if (!run.isExtracted()) {
                SimulationRun sr = SimulationRun.extractSimulationAndSomeIter(simWhichIterations, itrWhichIterations, run.getResultPath());
                srs.add(sr);
                run.extracted();
                sr.outputDeleteZip(Sweep.doOutput, true, true, true);
            } else {
                MyPrinter.Printer().printTier2ln("Skipped a simulation because it was already extracted");
            }
        }

    }
}