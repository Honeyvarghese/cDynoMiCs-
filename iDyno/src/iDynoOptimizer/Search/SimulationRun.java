package iDynoOptimizer.Search;

import java.io.File;

import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.ZipUtils;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.ProtocolFile.ParamSelector;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.FeatureFactory;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;
import iDynoOptimizer.Results.Feature.Names.SpatialNumericFeatureNameIteration;
import iDynoOptimizer.Results.Feature.SpatialNumericIterationFeature;
import iDynoOptimizer.Results.IterationResult;
import utils.ZipArchive;

import java.io.IOException;
import java.util.*;

public class SimulationRun {


    private SearchResultType searchResultType;
    private boolean          closeToBest;

    private String                    resultPath;
    private List<ParamNum>            paramSet;
    private List<SimulationIteration> simulationIterations;
    List<Integer> extracted = new ArrayList<>(); //keep track of the iterations extracted
    private TreeMap<FeatureNameSimulation, Feature> wholeSimulationFeatures;
    private int                                     iterationCount;
    private boolean                                 isValid;
    private int                                     lastIteration;
    //this will be the result path if just 1 repeat
    //it will be the list of repeats folders if more than 1
    private List<File>                              simulationFolders;
    private double wrinklingIndex = 0;

    public SimulationRun(String resultPath) {
        this.resultPath = resultPath;
        getSimFolders(resultPath);
        if (isValid) {

            simulationIterations = new ArrayList<>();
            wholeSimulationFeatures = new TreeMap<>();

            //getSimFolder now does this
            // iterationCount = IterationResult.getIterationCount(resultPath);
            lastIteration = iterationCount - 1;

            searchResultType = SearchResultType.other;

            //This will load in the current values (min, max, step, value used in this run)
            //It will not load in the protocol values (absmin, absmax, pointcont, range, epsilon...)
            paramSet = ParamSelector.readParams(resultPath + File.separator + Global.getGlobal().paramsFile);
        }

    }

    //output data and/or delete it
    public void outputDeleteZip(boolean doOutput, boolean doDelete, boolean doZip, boolean deleteAfterZip) {

        if (isValid) {
            if (doOutput) {
                String outputPath = resultPath + File.separator + "features";
                int iteration;
                for (SimulationIteration si : simulationIterations) {
                    iteration = si.getIteration();

                    if (iteration == lastIteration)
                        iteration = lastIteration;

                    si.output(outputPath + File.separator + iteration);
                }

                for (Feature f : wholeSimulationFeatures.values()) {
                    f.output(outputPath);

                }
            }

            if (doDelete) deleteAfterOutput();

        }
        if (doZip) zip(deleteAfterZip);

    }

    private void zip(boolean deleteAfterZip) {

        ZipUtils appZip = new ZipUtils(resultPath);
        appZip.zipIt(resultPath, deleteAfterZip);


    }

    //Can be called after final extraction
    private void deleteRawData() {
        for (SimulationIteration si : simulationIterations) {
            si.deleteRawData();
        }
    }

    //Can be called after final extraction
    private void deleteIntermediateIterationData() {
        for (SimulationIteration si : simulationIterations) {
            si.deleteIntermediate();
        }

    }

    //can be called after output
    private void deleteIntermediateSimulationData() {
        for (Feature f : wholeSimulationFeatures.values()) {

            f.deleteIntermediateData();
        }
    }

    private void deleteAfterOutput() {
        deleteRawData();
        deleteIntermediateIterationData();
        deleteIntermediateSimulationData();
    }

    public void deleteAfterErrorGenerated() {
        deleteIterationsAndFeatures();
    }


    //can be called after errors generated
    private void deleteIterationsAndFeatures() {
        for (SimulationIteration si : simulationIterations) {
            si.deleteFeatures();
        }
        simulationIterations = null;
        for (Feature f : wholeSimulationFeatures.values()) {
            f.deleteFeatures();
        }

        wholeSimulationFeatures = null;
    }


    public void calculateWrinklingIndex() {

        for (SimulationIteration si : getSimulationIterations()) {
            SpatialNumericIterationFeature snif = (SpatialNumericIterationFeature) si.getFeature(SpatialNumericFeatureNameIteration.contour);

            wrinklingIndex += snif.getRatioAboveAvg();
        }

        if (simulationIterations.size() == 0) wrinklingIndex = 0;
        else wrinklingIndex /= (double) simulationIterations.size();

    }


    public Feature extractSimFeatAll(FeatureNameSimulation featureNameSimulation) {
        List<FeatureNameSimulation> listWrapper = new ArrayList<>();
        listWrapper.add(featureNameSimulation);

        return extractSimFeatAll(listWrapper).firstEntry().getValue();
    }


    public TreeMap<FeatureNameSimulation, Feature> extractSimFeatAll(List<FeatureNameSimulation> featureNameSimulations) {
        //extract all the iterations
        int itrStart = 0;
        int itrEnd   = lastIteration;
        int step     = 1;

        return extractSimFeatSome(featureNameSimulations, itrStart, itrEnd, step);

    }

    public TreeMap<FeatureNameSimulation, Feature> extractSimFeatSome(List<FeatureNameSimulation> featureNameSimulations, int itrStart, int itrEnd, int step) {
        //only extract the iterations specified
        for (int i = itrStart; i < itrEnd; i += step) {
            addSimulationIteration(i, null);
        }

        for (FeatureNameSimulation fns : featureNameSimulations) {
            //make the features using all of the iterations specified
            wholeSimulationFeatures.put(fns, FeatureFactory.makeFeature(fns, getSimulationIterations()));

        }
        return wholeSimulationFeatures;

    }

    public Feature extractSimFeatSome(FeatureNameSimulation featureNameSimulation, int[] iterations) {


        List<FeatureNameSimulation> listWrapper = new ArrayList<>();
        listWrapper.add(featureNameSimulation);

        return extractSimFeatSome(listWrapper, iterations).firstEntry().getValue();

    }

    public TreeMap<FeatureNameSimulation, Feature> extractSimFeatSome(List<FeatureNameSimulation> featureNameSimulations, int[] iterations) {


        //only extract the iterations specified
        for (int i : iterations) {
            addSimulationIteration(i, null);
        }

        for (FeatureNameSimulation fns : featureNameSimulations) {
            //make the features using all of the iterations specified

            wholeSimulationFeatures.put(fns, FeatureFactory.makeFeature(fns, getSimulationIterations()));

        }

        return wholeSimulationFeatures;
    }


    /*
    this method allows associating a list of iterations with a simulation feature so that only those iterations are used in that feature
     */
    public TreeMap<FeatureNameSimulation, Feature> extractSimFeatSome(Map<FeatureNameSimulation, int[]> featureNameSimulationandIterationsMap) {

        Map<FeatureNameSimulation, List<SimulationIteration>> itrsToUseBySimFeat = new HashMap<>();
        List<SimulationIteration>                             tmpList;
        //extract all the iterations contained in the map
        for (FeatureNameSimulation fns : featureNameSimulationandIterationsMap.keySet()) {
            tmpList = new ArrayList<>();
            int[] itrs = featureNameSimulationandIterationsMap.get(fns);
            for (int i = 0; i < itrs.length; i++) {
                SimulationIteration si = addSimulationIteration(itrs[i], null);
                if (si != null)
                    tmpList.add(si);
            }
            itrsToUseBySimFeat.put(fns, tmpList);
        }
        for (FeatureNameSimulation fns : featureNameSimulationandIterationsMap.keySet()) {

            //make the feature using all of the iterations specified for this feature
            wholeSimulationFeatures.put(fns, FeatureFactory.makeFeature(fns, itrsToUseBySimFeat.get(fns)));

        }
        return wholeSimulationFeatures;
    }


    public void extract(Map<FeatureNameIteration, int[]> featureNameIterationandIterationMap) {

        Map<Integer, Set<FeatureNameIteration>> iterationsToFeatureMap = new HashMap<>();

        for (FeatureNameIteration fni : featureNameIterationandIterationMap.keySet()) {
            for (Integer itr : featureNameIterationandIterationMap.get(fni)) {
                Set<FeatureNameIteration> tmp;
                if (iterationsToFeatureMap.containsKey(itr)) tmp = iterationsToFeatureMap.get(itr);
                else {
                    tmp = new HashSet<>();
                    iterationsToFeatureMap.put(itr, tmp);
                }
                tmp.add(fni);
            }
        }

        for (Integer itr : iterationsToFeatureMap.keySet()) {

            addSimulationIteration(itr, iterationsToFeatureMap.get(itr));

        }


    }


    public TreeMap<FeatureNameSimulation, Feature> extractSimFeatAllMatchGivenItrNumbers(Map<FeatureNameSimulation, List<SimulationIteration>> targetItrsToMatch) {
        Map<FeatureNameSimulation, List<SimulationIteration>> itrsToUseBySimFeat = new HashMap<>();
        List<SimulationIteration>                             tmpTargetItrList;
        List<SimulationIteration>                             tmpMatchItrList;
        //extract all the iterations contained in the map

        for (FeatureNameSimulation fns : targetItrsToMatch.keySet()) {
            tmpTargetItrList = targetItrsToMatch.get(fns);
            tmpMatchItrList = new ArrayList<>();

            for (SimulationIteration si : tmpTargetItrList) {
                tmpMatchItrList.add(addSimulationIteration(si.getIteration(), null));
            }
            itrsToUseBySimFeat.put(fns, tmpMatchItrList);
        }
        for (FeatureNameSimulation fns : targetItrsToMatch.keySet()) {

            //make the feature using all of the iterations specified for this feature
            wholeSimulationFeatures.put(fns, FeatureFactory.makeFeature(fns, itrsToUseBySimFeat.get(fns)));

        }
        return wholeSimulationFeatures;

    }


    public void extractAll(Set<FeatureNameIteration> featureNameIterations) {
        int iterationStart = 0;
        int iterationEnd   = lastIteration;


        extractRange(featureNameIterations, iterationStart, iterationEnd);
    }

    public void extractAll(FeatureNameIteration featureNameIteration) {
        Set<FeatureNameIteration> fis = new HashSet<>();
        fis.add(featureNameIteration);
        extractAll(fis);
    }


    public List<File> extractAllfilesruns() {
        List<File> NestedResultFiles = new ArrayList<File>();


        File   folder2                 = new File(resultPath);
        File[] listOfFilesNestedResult = (File[]) folder2.listFiles();


        for (File f2 : folder2.listFiles()) {
            if (f2.isDirectory()) {
                String fileNameNestedResult = f2.getName();
                //	System.out.println(fileNameNestedResult);
                NestedResultFiles.add((File) f2);
            }
        }
        return NestedResultFiles;
    }


    public void extract(FeatureNameIteration featureNameIteration, int iteration) {
        Set<FeatureNameIteration> fis = new HashSet<FeatureNameIteration>();
        fis.add(featureNameIteration);
        addSimulationIteration(iteration, fis);
    }

    public void extract(Set<FeatureNameIteration> names, int[] iterations) {
        for (int i = 0; i < iterations.length; i++) {

            addSimulationIteration(iterations[i], names);
        }
    }

    public void extract(FeatureNameIteration featureNameIteration, int[] iterations) {


        for (int i = 0; i < iterations.length; i++) {
            extract(featureNameIteration, iterations[i]);
        }
    }


    public void extractLast(Set<FeatureNameIteration> names) {

        addSimulationIteration(lastIteration, names);

    }

    public void extractLast(FeatureNameIteration name) {
        Set<FeatureNameIteration> fis = new HashSet<>();
        fis.add(name);
        extractLast(fis);
    }

    private void finalExtractIterationFeatures() {
        for (SimulationIteration si : simulationIterations) {
            si.finalExtract();
        }
    }

    private void finalExtractSimulationFeatures() {

        for (Feature f : wholeSimulationFeatures.values()) {
            f.setup();
        }
    }

    public void finalExtract() {
        finalExtractIterationFeatures();
        finalExtractSimulationFeatures();
    }


    public List<SimulationIteration> getSimulationIterations() {
        Collections.sort(simulationIterations);
        return simulationIterations;
    }

    public SimulationIteration getSimulationIterationX(int x) {

        Collections.sort(simulationIterations);

        for (SimulationIteration si : simulationIterations) {
            if (si.getIteration() == x) return si;
        }

        return null;
    }

    public SimulationIteration getLastSimulationIteration() {
        return getSimulationIterationX(lastIteration);
    }

    public void addSimulationIteration(SimulationIteration si) {
        extracted.add(si.getIteration());
        simulationIterations.add(si);
    }

    public SimulationIteration addSimulationIteration(int iteration, Set<FeatureNameIteration> features) {

        if (iteration == IterationResult.getAllIterationPH()) {
            extractAll(features);
            return null;
        }


        if (iteration == IterationResult.getLastIterationPH()) {
            iteration = lastIteration;
        }

        if (iteration <= lastIteration) {
            SimulationIteration si;
            if (!extracted.contains(iteration))  //only extract iterations not already extracted because there will likely be duplicates
            {
                si = new SimulationIteration(this, iteration, features, lastIteration);
                addSimulationIteration(si);
                return si;
            } else {
                si = getSimulationIterationX(iteration);
            }
            if (features != null) si.addFeaturesToExtract(features);
            return si;
        }
        return null;

    }

    public String getResultPath() {
        return resultPath;
    }

    public List<ParamNum> getParamSet() {
        return paramSet;
    }


    public TreeMap<FeatureNameSimulation, Feature> getWholeSimulationFeatures() {
        return wholeSimulationFeatures;
    }

    public void extractRange(Set<FeatureNameIteration> targetFeatureNameIterations, int iterationStart, int iterationEnd) {
        for (int i = iterationStart; i <= iterationEnd; i++) {
            addSimulationIteration(i, targetFeatureNameIterations);
        }

    }


    public double getWrinklingIndex() {
        return wrinklingIndex;
    }

    public static SimulationRun extractRange(Set<FeatureNameIteration> featureNameIteration, int iterationStart, int iterationEnd, String resultPath) {
        SimulationRun sr = new SimulationRun(resultPath);
        if (sr.isValid) {
            sr.extractRange(featureNameIteration, iterationStart, iterationEnd);
            sr.finalExtract();
        }
        return sr;

    }

    public static SimulationRun extractSomeIter(Set<FeatureNameIteration> featureNameIterations, int[] iterations, String resultPath) {
        SimulationRun sr = new SimulationRun(resultPath);
        if (sr.isValid) {
            sr.extract(featureNameIterations, iterations);

            sr.finalExtract();
        }

        return sr;
    }

    public static SimulationRun extractAll(Set<FeatureNameIteration> featureNameIterations, String resultPath) {
        SimulationRun sr = new SimulationRun(resultPath);
        if (sr.isValid) {
            sr.extractAll(featureNameIterations);
            sr.finalExtract();
        }
        return sr;

    }


    public static SimulationRun extractSimulationAndSomeIter(Map<FeatureNameSimulation, int[]> simWhichIterations, Map<FeatureNameIteration, int[]> itrWhichIterations, String resultPath) {
        SimulationRun sr = new SimulationRun(resultPath);
        if (sr.isValid) {
            if (simWhichIterations != null && !simWhichIterations.isEmpty()) sr.extractSimFeatSome(simWhichIterations);
            if (itrWhichIterations != null && !itrWhichIterations.isEmpty()) sr.extract(itrWhichIterations);
            sr.finalExtract();
        }
        return sr;
    }


    public static SimulationRun extractLast(String resultPath, FeatureNameIteration... featureNameIterations) {
        SimulationRun sr = new SimulationRun(resultPath);

        if (sr.isValid) {
            Set<FeatureNameIteration> fnis = new HashSet<>();

            for (FeatureNameIteration fni : featureNameIterations) {
                fnis.add(fni);
            }
            sr.extractLast(fnis);
            sr.finalExtract();
        }

        return sr;

    }

    public static SimulationRun extractAt(Set<FeatureNameIteration> featureNameIterations, int iterationNumber, String resultPath) {
        SimulationRun sr = new SimulationRun(resultPath);

        if (sr.isValid) {
            sr.addSimulationIteration(iterationNumber, featureNameIterations);

            sr.finalExtract();
        }
        return sr;

    }


    public void setWholeSimulationFeatures(TreeMap<FeatureNameSimulation, Feature> wholeSimulationFeatures) {
        this.wholeSimulationFeatures = wholeSimulationFeatures;
    }

    public SearchResultType getSearchResultType() {
        return searchResultType;
    }

    public void setSearchResultType(SearchResultType searchResultType) {
        this.searchResultType = searchResultType;
    }

    public boolean isCloseToBest() {
        return closeToBest;
    }

    public void setCloseToBest(boolean closeToBest) {
        this.closeToBest = closeToBest;
    }


    public boolean isValid() {
        return isValid;
    }


    public List<File> getSimulationFolders() {
        return simulationFolders;
    }

    public List<File> getSimFolders(String resultPath) {

        File resultDir = new File(resultPath);

        simulationFolders = new ArrayList<>();

        if (!resultDir.exists() || !resultDir.isDirectory()) {
            isValid = false;
            return simulationFolders;
        }

        for (File repeats : resultDir.listFiles()) {
            if (repeats.isDirectory() && repeats.getName().matches("[\\d]+")) simulationFolders.add(repeats);
        }

        if (simulationFolders.isEmpty()) simulationFolders.add(resultDir);

        File representativeAgentFolder = null;

        for (File simFolder : simulationFolders) {

            File[] simChildFolders = simFolder.listFiles();
            File agentFolder = null;

            for (File simChildFolder : simChildFolders) {
                if (simChildFolder.isDirectory() && simChildFolder.getName().matches(IterationResult.getAgentFolder())) {
                    agentFolder = simChildFolder;
                    break;
                }
            }
            //if any of the agent folders meet these criteria or don't exist
            if (agentFolder == null || !agentFolder.exists() || !agentFolder.isDirectory() || agentFolder.listFiles().length <= 0) {
                isValid = false;
                return simulationFolders;
            }

            if (representativeAgentFolder == null) representativeAgentFolder = agentFolder;


        }

        //count the number of agent files in one of the agent folders to get the number of iterations
        //this assumes the one we check has the same number of iterations as the rest of them
        int    count      = 0;
        File[] agentFiles = representativeAgentFolder.listFiles();

        //we know it will have at least one file because we've already checked in this method
        for (File agentFile : agentFiles) {
            String fileName = agentFile.getName();
            if (fileName.endsWith(IterationResult.getAgentStateExt()) || fileName.endsWith(IterationResult.getAgentStateExt2()))
                count++;
        }


        iterationCount = count;

        isValid = true;
        return simulationFolders;

    }

}
