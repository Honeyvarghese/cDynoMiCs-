package iDynoOptimizer.Search;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Solution;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.Parameters.ParamResetType;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolFile;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolReader;
import iDynoOptimizer.Results.Feature.Error.SquaredDifference;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;
import iDynoOptimizer.Results.IterationResult;
import iDynoOptimizer.Search.ExitCondition.ExitCondition;
import iDynoOptimizer.Search.ExitCondition.Operator;
import iDynoOptimizer.Search.GeneticExtensions.SimulationSolution;
import org.apache.commons.io.FileUtils;
import iDynoOptimizer.Global.ExtraMath;

import java.io.*;
import java.nio.file.Files;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class Search {

    private static List<ParamNum> initialParams = new LinkedList<ParamNum>();

    private static boolean runSimulationsInline = true;


    public static boolean getRunSimulationsInline() {
        return runSimulationsInline;
    }

    public static void setRunSimulationsInline(boolean value) {
        runSimulationsInline = value;
    }


    private static int    control   = 0;
    private static double min_error = 0;
    private static int    runXTimes = 1;


    public static List<Runner> doSweep(int numOfRepeats) {

        List<Runner> runLocations = new ArrayList<>();
        ProtocolFile.copyProtocols();//this only to copy the orginal files in tp the results folder
        runLocations.add(runTarget(numOfRepeats));               // to run target one time
        runLocations.addAll(doSweepHelper(1, numOfRepeats));

        return runLocations;
    }


    public static List<Runner> doJustSweep(int numOfRepeats) {
        ProtocolFile.copyTestProtocols();
        return doSweepHelper(1, numOfRepeats);
    }


    private static List<Runner> doSweepHelper(int step, int numOfRepeats) {

        return doSweepHelper(step, numOfRepeats, null, ParamResetType.None, 1);
    }


    /*
    read in the protocol file (get references to the parameters)
    For each MOEA solutions
        set the parameter to the values from the MOEA solution
        setup a simulation with these parameter values(calls Runner.Run)

     */
    public static List<Runner> doRunHelper(int step, int numOfRepeats, Iterable<Solution> sss) {


        List<Runner> runners      = new ArrayList<>();
        int          runNumber    = 0;
        File         protocolFile = null;

        for (Solution s : sss) {

            SimulationSolution ss = (SimulationSolution) s;
            MyPrinter.Printer().printTier1ln("Generating a run for a MOEA solution");

            if (runNumber == 0) {
                protocolFile = Global.getGlobal().getTestProtocolFiles()[ss.getProtocolFileIndex()];

                ProtocolReader.read(protocolFile.getPath());
                initialParams = ProtocolFile.getClassRep().getChangingParams();
            }

            MyPrinter.Printer().printTier1ln("Using protocol file " + protocolFile.getPath());


            for (ParamNum pn : initialParams) {

                double varValue = ss.getVariable(pn.getName(), pn.getProtocolFileName());
                MyPrinter.Printer().printTier1ln("Setting parameter " + pn.getName() + " to value " + varValue);
                pn.setValue(varValue);


            }
            Runner r = Runner.Run(numOfRepeats, initialParams, ++runNumber, step, protocolFile);
            MyPrinter.Printer().printTier1ln("Generated run " + r.getResultPath() + " with run number" + runNumber + " and " + numOfRepeats + " reapeats");
            runners.add(r);
            ss.setResultPath(r.getResultPath());
        }

        return runners;


    }

    public static List<Runner> doSweepHelper(int step, int numOfRepeats, SimulationRun best, ParamResetType paramResetType, int sweepLocationCount) {


        File[] protocolFiles = Global.getGlobal().getTestProtocolFiles();

        List<Runner>            allResultPaths    = new ArrayList<>();
        Map<File, List<Runner>> oneSetResultPaths = new HashMap<>();
        List<Runner>            tmp1;
        List<Runner>            tmp2              = null;
        ParamResetType          currentParamResetType;
        int                     prevRunCounts     = 0;

        for (int i = 1; i <= sweepLocationCount; i++) {

            if (i == 1) currentParamResetType = paramResetType;
            else currentParamResetType = ParamResetType.Random;

            for (File protocolFile : protocolFiles) {
                initialParams = ProtocolFile.getClassRep().getChangingParams();
                ProtocolReader.read(protocolFile.getPath());


                MyPrinter.Printer().printTier2ln(i + ":");
                ParamNum.reset(initialParams, currentParamResetType, best);


                prevRunCounts = 0;

                if (oneSetResultPaths.containsKey(protocolFile)) {
                    tmp2 = oneSetResultPaths.get(protocolFile);
                    prevRunCounts = tmp2.size();
                }

                tmp1 = startSweep(true, step, 1 + prevRunCounts, numOfRepeats, protocolFile);


                if (tmp2 != null)
                    tmp2.addAll(tmp1);
                else oneSetResultPaths.put(protocolFile, tmp1);


                allResultPaths.addAll(tmp1);
            }
        }

        return allResultPaths;


    }


    private static Runner runTarget(int numRepeats) {

        return Runner.RunTarget(numRepeats);

    }

    public static Runner runJustTarget(int numRepeats) {
        ProtocolFile.copyTargetProtocol();
        return Runner.RunTarget(numRepeats);
    }


//    public static void doHillSomeItersMultFeatToTarget(Map<FeatureNameSimulation, int[]> simWhichIterations, Map<FeatureNameIteration, int[]> itrWhichIterations, double exitError, double maximumSteps) {
//        ExitCondition errorMin = new ExitCondition(exitError, Operator.lessThanOrEqual);
//        ExitCondition stepMax  = new ExitCondition(maximumSteps, Operator.greaterThanOrEqual);
//
//
//        runJustTarget(runXTimes);
//
//
//        SimulationRun target = SimulationRun.extractSimulationAndSomeIter(simWhichIterations, itrWhichIterations, Global.getGlobal().getTargetResultPath());
//
//        if (!target.isValid()) return;
//
//        ProtocolFile.copyTestProtocols();
//
//        int step = 1;
//
//        Random                     r             = new Random();
//        SimulationRun              best          = null;
//        SimulationRun              toCheckAround = null;
//        SimulationRun              bestThisRound = null;
//        Map<SimulationRun, Double> better        = new LinkedHashMap<>();
//
//        Map<SimulationRun, Double>               all         = new LinkedHashMap<>();
//        Map<Integer, Map<SimulationRun, Double>> allByStep   = new TreeMap();
//        Map<SimulationRun, Double>               allOneStep;
//        double                                   lowestError = Double.MAX_VALUE;
//        double                                   lowestErrorThisRound;
//
//        List<Double> allErrors = new ArrayList<>();
//
//        double[] allErrorsAvgSTD = new double[2];
//        boolean  foundBetter;
//        boolean  foundPromising;
//
//        ParamResetType paramResetType = ParamResetType.Random;
//
//        while (!errorMin.met(lowestError) && !stepMax.met(step - 1)) {
//
//            allOneStep = new LinkedHashMap<>();
//            allByStep.put(step, allOneStep);
//
//
//            List<SimulationRun> srs = generateSimRunsToExtract(doSweepHelper(step++, runXTimes, toCheckAround, paramResetType, 1));  //contains all of the runs for this step, including all of them from each initial condition(protocol file)
//
//            List<CompareRuns> crs = new ArrayList<CompareRuns>();
//
//            for (SimulationRun sr : srs) {
//
//                CompareRuns cr = new CompareRuns(target, sr);
//                SquaredDifference sd = new SquaredDifference();
//                cr.compareSimulation(sd, true);
//                cr.compare(sd, true);
//                crs.add(cr);
//            }
//
//
//            foundBetter = false;
//            foundPromising = false;
//
//            lowestErrorThisRound = Double.MAX_VALUE;
//
//
//            for (CompareRuns cr : crs) {
//
//                if (cr.getTotalError() < lowestErrorThisRound) {
//                    lowestErrorThisRound = cr.getTotalError();
//                    bestThisRound = cr.getTest();
//                }
//                all.put(cr.getTest(), cr.getTotalError());
//                allOneStep.put(cr.getTest(), cr.getTotalError());
//                allErrors.add(cr.getTotalError());
//
//                System.out.println("Swept p1: " + cr.getTest().getParamSet().get(0).getValue() + "\t\t\t" + cr.getTotalError() + "\t\t\t" + cr.getTest().getResultPath());
//                // System.out.println("Swept p2: " + cr.getTest().getParamSet().get(1).getValue() + "\t\t\t" + cr.getTotalError() + "\t\t\t" + cr.getTest().getResultPath());
//            }
//
//            if (lowestErrorThisRound < lowestError) {
//                foundBetter = true;
//                lowestError = lowestErrorThisRound;
//
//                if (best != null) {
//                    best.setSearchResultType(SearchResultType.better);
//                }
//
//                toCheckAround = best = bestThisRound;
//                best.setSearchResultType(SearchResultType.best);
//                better.put(best, lowestError);
//
//                System.out.println("Best So Far p1: " + best.getParamSet().get(0).getValue() + "\t\t\t" + lowestError + "\t\t\t" + best.getResultPath());
//                // System.out.println("Best So Far p2: " + best.getParamSet().get(1).getValue() + "\t\t\t" + lowestError + "\t\t\t" + best.getResultPath());
//            }
//            //only do this at most 70% of the time to avoid going over the same values again and again by refinding the same good (but not best) values)
//            else if (ExtraMath.approxEqual(lowestErrorThisRound, lowestError, allErrorsAvgSTD[1]) && r.nextDouble() < .7) {
//                foundPromising = true;
//                toCheckAround = bestThisRound;
//                System.out.println("OTHER PROMISING p1: " + toCheckAround.getParamSet().get(0).getValue() + "\t\t\t" + lowestErrorThisRound + "\t\t\t" + toCheckAround.getResultPath());
//                //  System.out.println("OTHER PROMISING p2: " + toCheckAround.getParamSet().get(1).getValue() + "\t\t\t" + lowestErrorThisRound + "\t\t\t" + toCheckAround.getResultPath());
//            }
//
//
//            allErrorsAvgSTD = ExtraMath.avgStd(allErrors);
//
//
//            boolean betterByWideMargin = ExtraMath.approxEqualorLess(lowestError, allErrorsAvgSTD[0], allErrorsAvgSTD[1] * 1.1) && (foundBetter || foundPromising);
//
//
//            if (betterByWideMargin) paramResetType = ParamResetType.NormalResetMethod1;
//            else paramResetType = ParamResetType.Random;
//
//
//        }
//
//        System.out.print("\n\nBEST p1: \n" + best.getResultPath() + "\t\t ERROR:" + lowestError + "\t\t Value:" + best.getParamSet().get(0).getValue());
//        // System.out.print("\n\nBEST p2: \n" + best.getResultPath() + "\t\t ERROR:" + lowestError + "\t\t Value:" + best.getParamSet().get(1).getValue());
//
//        System.out.print("\n\nHISTORY: \n");
//        for (SimulationRun sr : better.keySet()) {
//            System.out.println("P1" + sr.getResultPath() + "\t\t ERROR:" + better.get(sr) + "\t\t Value:" + sr.getParamSet().get(0).getValue());
//            //   System.out.println("P2" + sr.getResultPath() + "\t\t ERROR:" + better.get(sr) + "\t\t Value:" + sr.getParamSet().get(1).getValue());
//        }
//
//        System.out.print("\n\n Other Close:\n");
//        for (SimulationRun sr : all.keySet()) {
//            if (ExtraMath.approxEqual(all.get(sr), lowestError, allErrorsAvgSTD[1])) {
//                sr.setCloseToBest(true);
//                System.out.println("P1" + sr.getResultPath() + "\t\t ERROR: " + all.get(sr) + "\t\t Value:" + sr.getParamSet().get(0).getValue());
//                //     System.out.println("P2" + sr.getResultPath() + "\t\t ERROR: " + all.get(sr) + "\t\t Value:" + sr.getParamSet().get(1).getValue());
//            }
//        }
//
//
//        outputClimber(allByStep);
//
//        compilePovraysFromSims(new ArrayList<>(better.keySet()));
//
//    }


    public static void outputClimber(Map<Integer, Map<SimulationRun, Double>> climberResultsByStep) {

        List<String>  linesAll        = new ArrayList<>();
        List<String>  linesJustBetter = new ArrayList<>();
        StringBuilder lineAll;
        StringBuilder lineJustBetter;
        String        delimeter       = "\t";

        for (Integer step : climberResultsByStep.keySet()) {
            Map<SimulationRun, Double> tmp = climberResultsByStep.get(step);
            for (SimulationRun sr : tmp.keySet()) {
                lineAll = new StringBuilder();
                lineJustBetter = new StringBuilder();

                //step \t error \t
                lineAll.append(step).append(delimeter).append(tmp.get(sr)).append(delimeter);


                //searchResultType
                lineAll.append(sr.getSearchResultType().ordinal()).append(delimeter);

                //closeToBest
                lineAll.append(sr.isCloseToBest() ? 1 : 0);

                for (ParamNum param : sr.getParamSet()) {
                    // \t param1value \t param2value ....
                    lineAll.append(delimeter).append(param.getValue().getValue());

                }
                if (sr.getSearchResultType() == SearchResultType.better || sr.getSearchResultType() == SearchResultType.best || sr.isCloseToBest())
                    lineJustBetter.append(lineAll.toString());

                linesAll.add(lineAll.toString());
                if (!lineJustBetter.toString().isEmpty()) linesJustBetter.add(lineJustBetter.toString());
            }

        }

        File dir = new File(Global.getGlobal().getClimberOutPath());
        if (!dir.exists()) dir.mkdirs();

        FileReaderWriter.writeLines(Global.getGlobal().getClimberOutPath() + File.separator + "climberAll.txt", linesAll);
        FileReaderWriter.writeLines(Global.getGlobal().getClimberOutPath() + File.separator + "climberJustBetter.txt", linesJustBetter);


    }


    public static void compilePovraysFromSims(List<SimulationRun> runs) {
        List<String> paths = new ArrayList<>();

        for (SimulationRun sr : runs) {
            paths.add(sr.getResultPath());
        }
        compilePovrays(paths);
    }

    private static void compilePovrays(List<String> resultPaths) {

        String destinationFolderString = Global.getGlobal().getClimberOutPath() + File.separator + "povrayHistory";
        String flatLoc                 = "all";
        File   destFolder              = new File(destinationFolderString + File.separator + flatLoc);

        if (!destFolder.exists()) destFolder.mkdirs();


        String sourceFolder;
        File   dir;
        File   destFlat;
        File   destSep;
        int    i      = 1;
        int    runNum = 1;

        for (String resultPath : resultPaths) {
            sourceFolder = resultPath + File.separator + IterationResult.getPovRayFolder();
            dir = new File(sourceFolder);

            if (!dir.exists()) {
                sourceFolder = resultPath + File.separator + "1" + File.separator + IterationResult.getPovRayFolder();
                dir = new File(sourceFolder);
            }

            for (File f : dir.listFiles()) {


                destFolder = new File(destinationFolderString + File.separator + runNum);

                if (!destFolder.exists()) destFolder.mkdirs();


                destSep = new File(destinationFolderString + File.separator + runNum + File.separator + f.getName());

                if (f.getName().endsWith(IterationResult.getPovExt()))
                    destFlat = new File(destinationFolderString + File.separator + flatLoc + File.separator + i++ + "." + IterationResult.getPovExt());
                    //copy the header and footers
                else
                    destFlat = new File(destinationFolderString + File.separator + flatLoc + File.separator + f.getName());

                //only copy the header and footer once
                if (!destFlat.exists()) {
                    try {
                        Files.copy(f.toPath(), destFlat.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                if (!destSep.exists()) {
                    try {
                        Files.copy(f.toPath(), destSep.toPath());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }


            }


            runNum++;

        }


    }


//    public static void doHillSomeItersMultFeatRealData(HashMap<FeatureNameIteration, String> featureNamesAndTheirPathLocations, int numOfRepeats, ExitCondition errorMin, ExitCondition stepMax, int[] whichIterations)
//    {
//        ProtocolFile.copyTestProtocols();
//
//        //each list is a set of features of the same type, where each feature will be compared to a feature from a single iteration
//        //there are multiple lists so multipe types of features can be compared
//        //E.g., we could do the maxHeight and the contour at each iteration
//        LinkedHashMap<FeatureNameIteration, List<Feature>> realDatas = FeatureFactory.makeFeatures(featureNamesAndTheirPathLocations);
//
//        int step = 1;
//        SimulationRun best = null;
//        double lowestError = Double.MAX_VALUE;
//        while (!errorMin.met(lowestError) && !stepMax.met(step)) {
//
//            //extract feature(s) for each initial condition run && generate error
//            List<SimulationRun> srs = generateSimRunsToExtract(doSweepHelper(step++, numOfRepeats, best));  //contains all of the runs for this step, including all of them from each initial condition(protocol file)
//
//            double tmpErrorOneFeature = 0; //average error for a single feature in a single simulation run
//            double tmpErrorOverall = 0; //average error for all the features in a single simulation run
//
//            for (SimulationRun sr : srs) {
//
//                List<FeatureNameIteration> featureNameList = new ArrayList<FeatureNameIteration>();
//                featureNameList.addAll(featureNamesAndTheirPathLocations.keySet());
//                //sr.extractAll(featureNameList);
//                sr.extract(featureNameList, whichIterations);
//
//                List<SimulationIteration> iterations = sr.getSimulationIterations();
//
//                int featureCount = 0;
//                for(FeatureNameIteration fni : realDatas.keySet())
//                {
//                    int i  = 0;
//                    for(Feature f : realDatas.get(fni))
//                    {
//                        if(i == 0) featureCount++; //only count a feature type if it actually contains data, but only count it once
//                        tmpErrorOneFeature += iterations.get(i++).getFeature(fni).calulateError(f, new SquaredDifference());
//                    }
//                    if(i > 0) tmpErrorOneFeature /= i;
//                    tmpErrorOverall += tmpErrorOneFeature;
//                    tmpErrorOneFeature = 0;
//                }
//                if(featureCount >0)  tmpErrorOverall /= featureCount;
//
//                if (tmpErrorOverall < lowestError) {
//                    lowestError = tmpErrorOverall;
//                    best = sr;
//                }
//                tmpErrorOverall = 0;
//
//            }
//        }
//    }


//    public void doHillLastIterRealData(FeatureNameIteration featureNameIteration, String pathToFeatureData, int numOfRepeats, ExitCondition errorMin, ExitCondition stepMax) {
//
//
//
//
//        // ProtocolFile.copyProtocols();
//
//        //run target if we're comparing against a target simulation
//        // runTarget();
//        ProtocolFile.copyTestProtocols();
//
//        Feature realData = FeatureFactory.makeFeature(featureNameIteration, pathToFeatureData);
//
//        int step = 1;
//        SimulationRun best = null;
//         double lowestError = Double.MAX_VALUE;
//        while (!errorMin.met(lowestError) && !stepMax.met(step)) {
//            if (step == 1) {
//               // initialParams = ProtocolFile.getClassRep().getChangingParams();
//            } else {
//
//                //generate a new range (min and max) for the sweeping parameters
//                if (best != null) {
//                    for (ParamNum pn : initialParams) {
//
//                        pn.resetMinMaxStepAroundMidPoint();
//                    }
//                }
//
//            }
//            doSweepHelper(step++, numOfRepeats);
//            //extract feature(s) for each initial condition run && generate error
//            List<SimulationRun> srs = generateSimRunsToExtract(step);  //contains all of the runs for this step, including all of them from each initial condition(protocol file)
//
//
//            double tmpError;
//            for (SimulationRun sr : srs) {
//                sr.extractLast(featureNameIteration);
//                tmpError = sr.getLastSimulationIteration().getFeature(featureNameIteration).calulateError(realData, new SquaredDifference());
//                if (tmpError < lowestError) {
//                    lowestError = tmpError;
//                    best = sr;
//                }
//
//            }
//        }
//
//    }


    public static List<SimulationRun> generateSimRunsToExtract(List<Runner> resultPaths) {
        List<SimulationRun> runs = new ArrayList<SimulationRun>();
        for (Runner resultpath : resultPaths) {
            SimulationRun sr = new SimulationRun(resultpath.getResultPath());
            if (sr.isValid()) {
                runs.add(sr);
            }
        }
        return runs;
    }

    private static List<SimulationRun> generateTestSimRunsToExtract(int stepNumber) {

        List<SimulationRun> runs = new ArrayList<SimulationRun>();

        Global g = Global.getGlobal();

        TreeMap<Integer, String> runResultMap = new TreeMap();

        File testDir = new File(g.getTestStepPathPartial(stepNumber));

        Pattern p = Pattern.compile(g.resultPathFull.replace("(", "\\(").replace(")", "\\)").replace(g.runNumPH, "(\\d+)").replace(g.initCondPH, "(.*)"));

        int id = 0;
        for (File f : testDir.listFiles()) {
            if (f.isDirectory()) {

                String fileName = f.getName();
                Matcher m = p.matcher(fileName);


                if (m.matches()) {

                    int runNumber = Integer.parseInt(m.group(1));
                    String initCond = m.group(2);
                    String resultPath = g.getTestResultPathPartial(runNumber, stepNumber, initCond);
                    runResultMap.put(runNumber, resultPath);


                }
            }
        }

        for (Integer i : runResultMap.keySet()) {

            SimulationRun sr = new SimulationRun(runResultMap.get(i));
            if (sr.isValid()) {
                runs.add(sr);
            }
        }

        return runs;
    }


    private static List<Runner> startSweep(boolean preCompute, int stepNumber, int runNumberStart, int numOfRepeats, File protocolFile) {

        Sweep sweep = new Sweep(initialParams, stepNumber, runNumberStart, numOfRepeats, protocolFile);
        sweep.sweep(preCompute);
        return sweep.getResultPaths();
    }


    public static void dohill(int numOfRepeats) throws IOException {


        ArrayList<Double> minvlaue  = new ArrayList<Double>();
        ArrayList<Double> maxvalue  = new ArrayList<Double>();
        ArrayList<Double> setpvalue = new ArrayList<Double>();

        ProtocolFile.copyProtocols();//this only to copy the orginal files in tp the results folder
        runTarget(1);               // to run target one time


        int    numberofDeppest = 15;
        String EXpfolder       = Global.getGlobal().getExperimentFolder();
        for (int Steps = 1; Steps <= numberofDeppest; Steps++)//number of deep search
        {

            //@@if step 1 intialize it from file
            if (Steps == 1) {

                initialParams = ProtocolFile.getClassRep().getChangingParams();

            } else {
                for (int m = 0; m < initialParams.size(); m++) {
                    double min = minvlaue.get(m);
                    double max = maxvalue.get(m);
                    double steps = setpvalue.get(m);
                    MyPrinter.Printer().printTier1ln("maxddd" + max);
                    MyPrinter.Printer().printTier1ln("mindddd" + steps);
                    MyPrinter.Printer().printTier2ln("stepsdddd" + min);
                    initialParams.get(m).setCurrentMax(max);
                    initialParams.get(m).setCurrentMin(min);
                    initialParams.get(m).setCurrentStep(steps);


                }
            }


            doSweepHelper(Steps, numOfRepeats);


            plotData(EXpfolder, Steps);
            //choose the the less feture value
            String exppath = Global.getGlobal().getExperimentFolder();
            String AllerrorPath = exppath + File.separator + "allerror-" + Steps;
            File ErrorFile = new File(AllerrorPath);


            int NumberOfLine = NumberofLine(ErrorFile.getAbsolutePath());
            //read the first line

            String linej = (String) FileUtils.readLines(ErrorFile).get(1);

            String delims = "\t";
            String[] tokens = linej.split(delims); //tokens each line
            List<String> operater = new ArrayList<String>();
            List<String> CurrentValue = new ArrayList<String>();
            min_error = Double.parseDouble(tokens[tokens.length - 1]);//sice the last tken for each line is the error for these paramters value
            //initlize operater //initlize CurrentValue
            for (int k = 0; k < tokens.length - 1; k++) {
                CurrentValue.add(tokens[k]);
                operater.add("+");
                operater.add(k, "+");
            }


            int minline = 0;
            for (int j = 1; j < NumberOfLine; j++)//start from the second line so j have to be 1
            {
                linej = (String) FileUtils.readLines(ErrorFile).get(j);

                tokens = linej.split(delims); //tokens each line

                for (int i = 0; i < tokens.length; i++) {

                    if (min_error >= Double.parseDouble(tokens[tokens.length - 1])) {
                        minline = j;
                        min_error = Double.parseDouble(tokens[tokens.length - 1]);
                        //System.out.println("min_error"+min_error);


                    }    //end if min_error

                }


            }


            linej = (String) FileUtils.readLines(ErrorFile).get(minline);

            tokens = linej.split(delims); //tokens the min error line


            maxvalue.clear();
            minvlaue.clear();
            setpvalue.clear();
            for (int k = 0; k < tokens.length - 1; k++) {


                if (initialParams.get(k).getCurrentMin() < Double.parseDouble(tokens[k])) {
                    // we find the less error in the error file then we will set the paermter based on the min error parmater
                    // if current min less
                    minvlaue.add(Double.parseDouble(tokens[k]));
                    maxvalue.add((initialParams.get(k).getCurrentMax()) + (Double.parseDouble(tokens[k])));
                    setpvalue.add(initialParams.get(k).getCurrentStep());


                } else if (initialParams.get(k).getCurrentMin() >= Double.parseDouble(tokens[k]))

                {
                    maxvalue.add(Double.parseDouble(tokens[k]));
                    minvlaue.add((initialParams.get(k).getCurrentMin() - ((Double.parseDouble(tokens[k]) / 2)) + 10e-100));
                    setpvalue.add(((Double.parseDouble(tokens[k])) - ((initialParams.get(k).getCurrentMin() - ((Double.parseDouble(tokens[k]) / 2)) + 10e-100))) / 5);


                }


            }
            //move the folders
            if (Steps < numberofDeppest) {
                String S = move_folders(EXpfolder, Steps);
                MyPrinter.Printer().printTier1ln("::::" + S);
            }

        }     // do for step

        Find_the_best(EXpfolder);

    }//end the function dohill


    public static String move_folders(String EXpFolder, int step) throws IOException {
        Global g         = Global.getGlobal();
        String EXpfolder = g.getFileOutputPath() + File.separator + EXpFolder;

        String TestPath = EXpfolder + File.separator + "test";
        File   folder   = new File(TestPath);
        for (File test : folder.listFiles()) {
            if (test.isDirectory() && test.getName().startsWith("results")) {
                MyPrinter.Printer().printTier2ln(test.getAbsolutePath());
                File S1 = new File(EXpfolder + File.separator + "R" + File.separator + "r" + step + test.getName());


                delete(test);//42
                FileUtils.waitFor(test, 2);

            }
        }

        return "Done !";
    }


    public static void cfolder(File s) throws IOException {

        FileUtils.forceMkdir(s);

    }


    public static void delete(File srcDir) throws IOException {


        try {


            FileUtils.forceDelete(srcDir);


        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void move(File srcDir, File destDir) {

        try {

            FileUtils.moveDirectory(srcDir, destDir);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public static void Find_the_best(String EXpFolder) throws IOException {
        String bestError = "";
        String header    = "";
        Global g         = Global.getGlobal();
        String EXpfolder = g.getFileOutputPath() + File.separator + EXpFolder;

        double   min_error;
        File     folder = new File(EXpfolder);
        String   delims = "\t";
        String[] tokens;


        String linej0 = (String) FileUtils.readLines(new File(EXpfolder + File.separator + "allerror-1")).get(1);

        tokens = linej0.split(delims);
        min_error = Double.parseDouble(tokens[tokens.length - 1]);
        for (File test : folder.listFiles()) {
            if (test.isFile() && test.getName().startsWith("allerror")) {


                int NumberOfLine = NumberofLine(test.getAbsolutePath());

                header = (String) FileUtils.readLines(test).get(0);
                for (int j = 1; j < NumberOfLine; j++)//start from the second line so j have to be 1
                {
                    String linej = (String) FileUtils.readLines(test).get(j);

                    tokens = linej.split(delims); //tokens each line
                    for (int i = 0; i < tokens.length; i++) {


                        if (min_error > Double.parseDouble(tokens[tokens.length - 1])) {
                            min_error = Double.parseDouble(tokens[tokens.length - 1]);
                            bestError = linej;
                            MyPrinter.Printer().printTier1ln("********" + linej);

                        }

                    }

                }
            }
        }

        FileWriter fs = new FileWriter(EXpfolder + File.separator + "Best");
        fs.append(header);
        fs.append("\n");
        fs.append(bestError);
        fs.close();
    }


//    private List<SimulationRun> generateTestSimRunsToExtract2() {
//
//        List<SimulationRun> runs = new ArrayList<SimulationRun>();
//
//
//        Global g = Global.getGlobal();
//
//        TreeMap<Integer, String> runResultMap = new TreeMap();
//
//        File testDir = new File(g.getTestPath());
//
//
//        //create a regular expression based off of the result path
//        //( and ) are escaped
//
//        Pattern p = Pattern.compile(g.resultPath2.replace("(", "\\(").replace(")", "\\)").replace(g.runNumPH, "(\\d+)"));
//
//        for (File f : testDir.listFiles()) {
//            if (f.isDirectory()) {
//
//                String fileName = f.getName();
//                System.out.println(fileName);
//                Matcher m = p.matcher(fileName);
//                if (m.matches()) {
//
//                    for (File f2 : f.listFiles()) {
//                        if (f2.isDirectory()) {
//                            //       int runNumber = Integer.parseInt(m.group(1));
//                            int runNumber = Integer.parseInt(f2.getName());
//                            //    System.out.println(Integer.parseInt(m.group(1))+"Doen!!!!!");
//                            String resultPathFull = g.getTestResultPathPartial(runNumber);
//                            //     System.out.println(resultPathFull+"MMM");
//                            runResultMap.put(runNumber, resultPathFull);
//                        }
//                    }
//
//                }
//            }
//        }
//
//
//        for (Integer i : runResultMap.keySet()) {
//            runs.add(new SimulationRun(runResultMap.get(i), i));
//        }
//
//        return runs;
//    }


    public static void CreateAvgFile(List<File> gFile, String SavePath) throws IOException {
        String OutputAvg = "";

        for (int j = 1; j < 73; j++) {
            double Total = 0;
            double avg = 0;

            for (int i = 0; i < gFile.size(); i++) {
                File element = gFile.get(i);
                String linej = (String) FileUtils.readLines(element).get(j);
                Total += Double.parseDouble(linej);

            }

            avg = Total / gFile.size();
            OutputAvg += Double.toString(avg);

            OutputAvg += '\n';

        }
        // funct
        FileWriter fs = new FileWriter(SavePath + File.separator + "Averg" + ".txt");
        fs.append(OutputAvg);
        fs.close();


    }

    //CreateoneErrorFile
    public static void CreateoneErrorFile(List<File> gFile, String SavePath) throws IOException {
        for (int i = 0; i < gFile.size(); i++) {

        }

        double error       = 0;
        String Outputerror = "";

        for (int i = 0; i < gFile.size(); i++) {
            //	Double error;
            File element = gFile.get(i);

            for (int j = 1; j < 70; j++) {
                if (gFile.get(i).getAbsolutePath().toLowerCase().contains("100")) {
                    if (j == 4) {
                        error += Math.pow((0.24e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 24) {
                        error += Math.pow((0.62e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 30) {
                        error += Math.pow((0.76e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 48) {
                        error += Math.pow((1.2e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 54) {
                        error += Math.pow((1.54e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 72) {
                        error += Math.pow((1.885e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }

                } else if (gFile.get(i).getAbsolutePath().toLowerCase().contains("200")) {

                    if (j == 4) {
                        error += Math.pow((0.19e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 24) {
                        error += Math.pow((0.51e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 30) {
                        error += Math.pow((0.68e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }

                    if (j == 48) {
                        error += Math.pow((0.999e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 54) {
                        error += Math.pow((1.23e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 72) {
                        error += Math.pow((1.47e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }

                } else if (gFile.get(i).getAbsolutePath().toLowerCase().contains("300")) {

                    if (j == 4) {
                        error += Math.pow((0.17e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 24) {
                        error += Math.pow((0.429e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 30) {
                        error += Math.pow((0.57e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 48) {
                        error += Math.pow((0.894e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 54) {
                        error += Math.pow((0.964e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 72) {
                        error += Math.pow((1.19e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }

                } else if (gFile.get(i).getAbsolutePath().toLowerCase().contains("400")) {

                    if (j == 4) {
                        error += Math.pow((0.14e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 24) {
                        error += Math.pow((0.33e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 30) {
                        error += Math.pow((0.41e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 48) {
                        error += Math.pow((0.5931e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 54) {
                        error += Math.pow((0.721e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }
                    if (j == 72) {
                        error += Math.pow((0.865e-9 - Double.parseDouble((String) FileUtils.readLines(element).get(j))), 2);
                    }

                } else {
                    MyPrinter.Printer().printTier1ln("NOOOOOOOOOOOOOOOOO Error File is deducted !");
                }

            }
        }

        //  	System.out.println(error+"fhdhgkjeghkjwe");

        Outputerror += Double.toString(error);


        // funct
        FileWriter fs = new FileWriter(SavePath + File.separator + "error" + ".txt");
        fs.append(Outputerror);
        fs.close();


    }

    public static int NumberofLine(String Path) throws IOException {
        BufferedReader reader = new BufferedReader(new FileReader(Path));
        int            lines  = 0;
        while (reader.readLine() != null) {
            lines++;
        }
        reader.close();


        return lines;

    }


    public static void The_Main_Error(String expermintesPath) throws IOException {
        control = 0;


        List<String> Header = new ArrayList<String>();
        List<String> values = new ArrayList<String>();
        List<String> errors = new ArrayList<String>();

        File   ExpFolder   = new File(expermintesPath);
        String OutAllError = "";
        for (File test : ExpFolder.listFiles()) {
            if (test.isDirectory() && test.getName().endsWith("test")) {
                for (File Insidetest : test.listFiles()) {


                    if (Insidetest.isDirectory()) {
                        for (File InsideResult : Insidetest.listFiles()) {


                            if (InsideResult.isFile() && InsideResult.getName().startsWith("paramsFile")) {
                                control++;
                                int NumberOfLine = NumberofLine(InsideResult.getAbsolutePath());
                                for (int j = 0; j < NumberOfLine; j++) {

                                    String linej = (String) FileUtils.readLines(InsideResult).get(j);

                                    String delims = ",";
                                    String[] tokens = linej.split(delims);
                                    if (control == 1) {

                                        Header.add(tokens[0]);


                                    }
                                    int tokenlength = tokens.length;
                                    values.add(tokens[tokenlength - 1]);

                                }

                            } else if (InsideResult.isFile() && InsideResult.getName().endsWith("error.txt")) {
                                String linej = (String) FileUtils.readLines(InsideResult).get(0);
                                errors.add(linej);

                            }


                        }

                    }
                }
            }
        }

        int numberfeature = Header.size();
        for (int i = 0; i < Header.size(); i++) {
            OutAllError += Header.get(i);
            OutAllError += '\t';
        }
        OutAllError += "Error";

        OutAllError += '\n';
        for (int m = 0; m < errors.size(); m++) {


        }

        for (int i = 1, m = 0; i <= values.size(); i++) {
            int j = 0;
            OutAllError += values.get(i - 1);
            OutAllError += '\t';
            if (i % numberfeature == 0) {
                if (m < errors.size()) {
                    OutAllError += errors.get(m);
                    m++;
                }

                OutAllError += '\n';
                j++;
            }
        }


        FileWriter fs = new FileWriter(errorfile(expermintesPath));

        fs.append(OutAllError);
        fs.close();


    }


    private static String errorfile(String ss) {


        String s = ss + File.separator + "allerror-";

        int    i    = 1;
        String temp = s + i;
        while (new File(temp).exists()) {
            i++;
            temp = s + i;
        }
        return temp;

    }


    public static void runAverge(File recived) throws IOException {
        List<File> GFile = new ArrayList();
        for (java.io.File f2 : recived.listFiles()) {
            if (f2.isDirectory()) {


                for (java.io.File f3 : f2.listFiles()) {
                    if (f3.isDirectory()) {

                        if (f3.getName().endsWith("Concentration_Per_cell")) {


                            for (java.io.File f4 : f3.listFiles()) {
                                if (f4.isFile() && f4.getName().endsWith("VEGFAllsolute_per_cell.txt")) {


                                    GFile.add((de.schlichtherle.io.File) f4);
                                    CreateAvgFile(GFile, recived.getAbsolutePath());

                                    // send array to function that find Total of each cell
                                    // and by then end we get the totla for each cell and we bulid new file and save
                                    // it in f2

                                }
                            }

                        }
                    }


                }
            }

        }

        //   System.out.println();


    }

    // FindError


    public static void FindError(String ResultPath) throws IOException {
        //System.out.println(recived.getAbsolutePath()+"KNKNKNK");
        File       ExtractErrFromR = new File(ResultPath);
        List<File> GFile2          = new ArrayList<File>();

        for (java.io.File f2 : ExtractErrFromR.listFiles()) {
            if (f2.isDirectory()) {


                for (java.io.File f3 : f2.listFiles()) {

                    if (f3.isFile() && f3.getName().endsWith("Averg.txt")) {

//Error     Calculation
                        GFile2.add(f3);
                        //    GFile.add( (de.schlichtherle.io.File) f3);


                    }

                }
                //  CreateoneErrorFile(GFile2,ResultPath);


            }


        }

        CreateoneErrorFile(GFile2, ResultPath);

    }


    public static void plotData(String folder, int step) throws IOException {

        Global g = Global.getGlobal();
        //set the experiment path
        g.setExperimentFolder(folder);
        List<SimulationRun> testRuns = generateTestSimRunsToExtract(step);
        MyPrinter.Printer().printTier1ln(testRuns.size() + "::::");
        //  SimulationRun target = extractTargetLast(fns, 1);
        List<File> Fileinsideresult;


        for (SimulationRun sr : testRuns) {
            Fileinsideresult = sr.extractAllfilesruns();
            for (int i = 0; i < Fileinsideresult.size(); i++) {

                File element = Fileinsideresult.get(i);

                // function to send elment and print the avrage of runs inside that.

                runAverge(element);
                // now calculate the error
                // one error per one result
                //then make the relation between Paramter list and error function

            }

            FindError(sr.getResultPath());


        }
        The_Main_Error(g.getExperimentFolder());

    }// end of plotData


    public static void showFiles(File[] files) {
        for (File file : files) {
            if (file.isDirectory()) {

                showFiles(file.listFiles()); // Calls same method again.
            } else {

            }
        }
    }


}
