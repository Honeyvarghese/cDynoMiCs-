package iDynoOptimizer.Search;

import iDynoOptimizer.Results.Feature.SpatialIterationFeature;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.IterationFeature;
import iDynoOptimizer.Results.IterationResult;

import java.io.File;
import iDynoOptimizer.Results.Feature.SpatialIterationFeature.AgentTypeChoice;
import java.util.*;

/**
 * Created by Chris Johnson on 11/18/2014.
 */





public class SimulationIteration implements Comparable {





    private SpatialIterationFeature mySpatialIterationFeature;
    private SimulationRun mySimulationRun;

    private List<IterationResult> results;

    //iterations in IdynoMiCS start at 1
    //a value less than refers to the last iteration, which is put in a special folder by iDynoMiCS
    private int iteration;
    private int lastIteration;


    public SimulationIteration(SimulationRun mySimulationRun, int iteration, Set<FeatureNameIteration> features, int lastIteration) {
        this.iteration = iteration;
        this.mySimulationRun = mySimulationRun;
        results = new ArrayList<IterationResult>();
        this.lastIteration = lastIteration;



        mySpatialIterationFeature = new SpatialIterationFeature(this,1, AgentTypeChoice.Alive, features);




    }




    public void output(String filePath)
    {
        mySpatialIterationFeature.output(filePath);
    }


    public void deleteFeatures()
    {
        mySpatialIterationFeature.deleteFeatures();
        mySpatialIterationFeature = null;
    }


    public void deleteIntermediate()
    {
        mySpatialIterationFeature.deleteIntermediateData();
    }

    public void deleteRawData()
    {
        for(IterationResult ir : getResults())
        {
            ir.delete();
            results = null;
        }
    }


    public void finalExtract()
    {
        mySpatialIterationFeature.setup();

    }


    public Map<FeatureNameIteration, IterationFeature> getFeatures() {
        return mySpatialIterationFeature.getFeaturesUsed();
    }


    public IterationFeature getFeature(FeatureNameIteration name) {
        return  mySpatialIterationFeature.getFeaturesUsed().get(name);

    }


    public int getIteration() {
        return iteration;
    }


    public void readResults(AgentTypeChoice agentTypeChoice) {

//        String resultPath = mySimulationRun.getResultPath();
//
//        File resultDir = new File(resultPath);
//        IterationResult result;
//
//        List<File> simulationFolders = new ArrayList<>();
//
//        for (File f : resultDir.listFiles()) {
//            if (f.isDirectory() && f.getName().matches("[\\d]+")) simulationFolders.add(f);
//        }
//
//        if (simulationFolders.isEmpty()) {
//            result = new IterationResult(resultPath, iteration, agentTypeChoice, lastIteration);
//            results.add(result);
//        } else {
//            for (File f : simulationFolders) {
//                result = new IterationResult(f.getPath(), iteration, agentTypeChoice, lastIteration);
//                results.add(result);
//            }
//        }
//
//        for (File f : simulationFolders) {
//            result = new IterationResult(f.getPath(), iteration, agentTypeChoice, lastIteration);
//            results.add(result);
//        }


        for(File simFolder : mySimulationRun.getSimulationFolders())
        {
            results.add(new IterationResult(simFolder.getPath(), iteration, agentTypeChoice, lastIteration));


        }


    }




    public List<IterationResult> getResults() {
        return results;
    }

    public SpatialIterationFeature getMySpatialIterationFeature() {
        return mySpatialIterationFeature;
    }



    @Override
    public int compareTo(Object o) {

        SimulationIteration si = (SimulationIteration)o;

        return getIteration() - si.getIteration();

    }

    public int getLastIteration() {
        return lastIteration;
    }

    public void addFeaturesToExtract(Set<FeatureNameIteration> newFeatureNames)
    {
        mySpatialIterationFeature.addFeatureNamesToUse(newFeatureNames);
    }

    public SimulationRun getMySimulationRun() {
        return mySimulationRun;
    }
}
