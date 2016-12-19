package iDynoOptimizer.Search;

import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;

import java.util.*;

/**
 * Created by Chris Johnson on 11/18/2014.
 */
public class CompareRuns {


    //takes a target simulation run and a test simulation run and computes errors


    private double totalError;
    private SimulationRun target;
    private SimulationRun test;
    private LinkedHashMap<SimulationIteration, Map<FeatureNameIteration, Error>> errorsByIteration;
    private Map<FeatureNameIteration, LinkedHashMap<SimulationIteration, Error>> errorsByFeatureName;
    private Map<FeatureNameIteration, Double> accumulatedErrorsByFeatureName;

    private Map<FeatureNameSimulation, Error> errorsBySimulationFeatureName;



    /*
    The target run should already have its features extracted, but the test should not. The features extracted will be determined by the features already extracted
    from the target
     */
    public CompareRuns(SimulationRun target, SimulationRun test) {


        this.target = target;
        this.test = test;
        errorsByIteration = new LinkedHashMap<SimulationIteration, Map<FeatureNameIteration, Error>>();
        errorsByFeatureName = new HashMap<FeatureNameIteration, LinkedHashMap<SimulationIteration, Error>>();
        accumulatedErrorsByFeatureName = new HashMap<>();
        errorsBySimulationFeatureName= new HashMap<>();


    }


    public SimulationRun getTarget() {
        return target;
    }

    /*
    The error for each iteration by iteration feature:
    E.G.: Feature 1, List of iteration/error pairs for feature 1
          Feature 2, List of iteration/error pairs for feature 2
     */
    public Map<FeatureNameIteration, LinkedHashMap<SimulationIteration, Error>> getErrorsByFeatureName() {
        return errorsByFeatureName;
    }

    /*
    The summed error for each iteration feature over all the iterations
    E.G.: Feature 1, sum of the errors for feature 1 over every iteration
          Feature 2, sum of the errors for feature 2 over every iteration

    To get the average error for each iteration feature over the iteations, need to divide by the number of iterations
     */
    public Map<FeatureNameIteration, Double> getAccumulatedErrorsByFeatureName() {
        return accumulatedErrorsByFeatureName;
    }



    /*
    The error for each iteration feature by iteration
    Same values as in errorsByFeatureName, but different organization
    E.G: Iteration 1, List of iteration feature/error pairs for iteration 1
         Iteration 2, List of iteration feature/error pairs for iteration 2
     */
    public LinkedHashMap<SimulationIteration, Map<FeatureNameIteration, Error>> getErrorsByIteration() {
        return errorsByIteration;
    }

    public Map<FeatureNameSimulation, Error> getErrorsBySimulationFeatureName() {
        return errorsBySimulationFeatureName;
    }

    /*
        Sum of:
        1) average of the values in accumulated errors by feature (average over both the features and the iterations)
        2) average of the values in  errors by simulation feature
         */
    public double getTotalError() {
        return totalError;
    }


    public Map<FeatureNameIteration, Error> getLastIterErrors() {
        return errorsByIteration.get(test.getLastSimulationIteration());
    }

    public SimulationRun getTest() {
        return test;
    }


    public void compareSimulation(IErrorCalculation errorCalculation, boolean extractTests) {

        if (extractTests) {
            Map<FeatureNameSimulation, List<SimulationIteration>> toExtract = new HashMap<>();

            for (FeatureNameSimulation fns : target.getWholeSimulationFeatures().keySet()) {
                toExtract.put(fns, target.getWholeSimulationFeatures().get(fns).getIterationsUsed());
            }
            test.extractSimFeatAllMatchGivenItrNumbers(toExtract);

            test.finalExtract();
        }


        double tmpTotalError = 0;


        //simulation features are features over the whole simulation, so don't need to divide by the number of iterations to get the average
        for (FeatureNameSimulation fns : target.getWholeSimulationFeatures().keySet()) {
            Error e = new Error(target.getWholeSimulationFeatures().get(fns), test.getWholeSimulationFeatures().get(fns), errorCalculation);
            errorsBySimulationFeatureName.put(fns, e);
            tmpTotalError += e.getError();
        }


        totalError += tmpTotalError / test.getWholeSimulationFeatures().size();

    }

    public void compare(IErrorCalculation errorCalculation, boolean extractTests) {


        SimulationIteration targetSi;
        SimulationIteration si;

        List<SimulationIteration> targetSis = target.getSimulationIterations();
        List<SimulationIteration> testSiS = null;

        if (!extractTests) testSiS = test.getSimulationIterations();

        for (int i = 0; i < targetSis.size(); i++) {

            targetSi = targetSis.get(i);

            if (extractTests) {
                si = test.addSimulationIteration(targetSi.getIteration(), targetSi.getFeatures().keySet());
                if(si != null)
                    si.finalExtract();
            } else si = testSiS.get(i);

            Map<FeatureNameIteration, Error> itErrors = new HashMap<FeatureNameIteration, Error>();

            for (FeatureNameIteration featurename : targetSi.getFeatures().keySet()) {


                Error e = new Error(targetSi.getFeature(featurename), si.getFeature(featurename), errorCalculation);
                itErrors.put(featurename, e);

                double current = 0;
                LinkedHashMap<SimulationIteration, Error> sitErrors;
                if (errorsByFeatureName.containsKey(featurename)) {
                    sitErrors = errorsByFeatureName.get(featurename);
                    current = accumulatedErrorsByFeatureName.get(featurename);
                } else sitErrors = new LinkedHashMap<>();

                accumulatedErrorsByFeatureName.put(featurename, e.getError() + current);
                sitErrors.put(si, e);


            }
            errorsByIteration.put(si, itErrors);
        }

        for (FeatureNameIteration fni : accumulatedErrorsByFeatureName.keySet()) {

            totalError += accumulatedErrorsByFeatureName.get(fni) / errorsByIteration.size();
        }
        totalError /= accumulatedErrorsByFeatureName.size();


    }


    public void deleteErrorMaps() {
        errorsByFeatureName = null;
        accumulatedErrorsByFeatureName = null;
        errorsByIteration = null;
        errorsBySimulationFeatureName = null;
    }


}
