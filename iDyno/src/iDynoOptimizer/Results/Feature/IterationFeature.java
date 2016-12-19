package iDynoOptimizer.Results.Feature;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.IterationResult;
import iDynoOptimizer.Search.SimulationIteration;

import java.util.List;

/**
 * Created by Chris Johnson on 1/12/2015.
 */
// There are multiple IterationResult to allow for random seed repeats. calculateAverage will average the number associated with each
public abstract class IterationFeature extends Feature {


    private SimulationIteration mySimulationIteration;



    public IterationFeature(SimulationIteration mySimulationIteration, FeatureNameIteration name)
    {
        super(mySimulationIteration, name);
        this.mySimulationIteration = mySimulationIteration;
    }

    public IterationFeature(FeatureNameIteration name)
    {
     super(name);
    }





    protected List<IterationResult> getSimResults() {
        return mySimulationIteration.getResults();
    }

    public SimulationIteration getMySimulationIteration() {
        return mySimulationIteration;
    }






}
