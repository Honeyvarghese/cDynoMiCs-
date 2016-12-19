package iDynoOptimizer.Results.Feature.Temporal;

import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.Names.FeatureNameSimulation;
import iDynoOptimizer.Search.SimulationIteration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 4/16/2015.
 * <p>
 * Represents the convergence over an entire simulation (or just a few iterations) using a list of VelocityFeature
 */
public class VelocitySimulationFeature extends Feature {


    /*
    The list of convergences at each iteration pair
    If each iteration of the simulation is used: Given C iterations, there will be C - 1 VelocityFeature because each one takes two iterations to generate a convergence grid
    That is, [it1, it2]; [it2, it3]; [it3, it4]; ...
     */
    private List<VelocityFeature> vectorFeatureAtIterations;


    /*
    The average convergence at each grid point over the iterations used (a temporal average)
     */
    private InOut[][][] vectors;

    /*
    The average convergence over each grid point (a spatial average)
    This is generated using "vectors", so it is the overall average convergence using each grid point of each iteration
     */
    private InOut overallAvg;


    public VelocitySimulationFeature(List<SimulationIteration> iterations) {

        super(iterations, FeatureNameSimulation.velocitySimulation);
        vectorFeatureAtIterations = new ArrayList<VelocityFeature>();


        for (int i = 0; i < iterations.size(); i++) {
            if (i < iterations.size() - 1)
                vectorFeatureAtIterations.add(new VelocityFeature(iterations.get(i), iterations.get(i + 1)));
        }

    }

    public VelocitySimulationFeature(List<VelocityFeature> velocities, boolean dummy) {

        super(FeatureNameSimulation.velocitySimulation);
        vectorFeatureAtIterations = velocities;
    }

    public void setup() {
        for (VelocityFeature vfi : vectorFeatureAtIterations) {
            vfi.setup();
        }

        super.setup();

//        for(VelocityFeature vif: vectorFeatureAtIterations) {
//            vif.removeNoise(overallAvg);
//        }
        // overallAvg = null;
    }

    @Override
    public void output(String filePath) {

        for (VelocityFeature vf : vectorFeatureAtIterations) {
            vf.output(filePath + File.separator + getMyName());
        }
    }

    @Override
    public void deleteIntermediateData() {
        for (VelocityFeature vf : vectorFeatureAtIterations) {
            vf.deleteIntermediateData();
        }


    }


    public void deleteFeatures()
    {
        for (VelocityFeature vf : vectorFeatureAtIterations) {
            vf.deleteFeatures();
        }

        vectorFeatureAtIterations = null;
        vectors = null;
        overallAvg = null;
    }


    @Override
    public double calulateError(Feature f, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ) {

        VelocitySimulationFeature compareTo = (VelocitySimulationFeature) f;
        setErrorCalculation(errorCalculation);
        double errorSum = 0;
        for (int i = 0; i < vectorFeatureAtIterations.size(); i++) {

            //the target
            VelocityFeature vifThis = vectorFeatureAtIterations.get(i);
            //the test
            VelocityFeature vifThat = compareTo.getVectorFeatureAtIterations().get(i);


            errorSum += vifThis.calulateError(vifThat, errorCalculation,paddingX, paddingY,paddingZ);

        }

        errorSum /= vectorFeatureAtIterations.size();
        setError(errorSum);
        return getError();


    }


    /*
    Requires "extract()" to be called first - so first the average convergence grid is calculated using each iteration ( this is stored in "vectors"),
    then the convergence grid itself is used to calculate the average convergence over each grid point
    Calculates "overallAvg"
     */
    @Override
    public void calculateAverage() {
        overallAvg = new InOut();

        int count = vectors.length;
        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < vectors[0].length; j++) {
                for (int k = 0; k < vectors[0][0].length; k++) {
                    overallAvg.add(vectors[i][i][k]);
                }
            }
        }

        // vectors = null;
        overallAvg.divide(count);
    }

    @Override
    /*
        Calculates "vectors" by calculating the average convergence grid over the iterations represented - see "vectors"
     */
    public void extract() {
        int count = vectorFeatureAtIterations.size();
        for (VelocityFeature vtf : vectorFeatureAtIterations) {

            InOut[][][] vectorsAtIteration = vtf.getAvgVectorSums();

            if (vectors == null) {
                vectors = new InOut[vectorsAtIteration.length][vectorsAtIteration[0].length][vectorsAtIteration[0][0].length];
            }

            for (int i = 0; i < vectorsAtIteration.length; i++) {
                for (int j = 0; j < vectorsAtIteration[0].length; j++) {
                    for (int k = 0; k < vectorsAtIteration[0][0].length; k++) {

                        InOut current = vectors[i][j][k];
                        if (current == null) {
                            current = new InOut();
                            vectors[i][j][k] = current;
                        }
                        //add
                        InOut next = vectorsAtIteration[i][j][k];
                        current.add(next);
                    }
                }
            }

        }

        for (int i = 0; i < vectors.length; i++) {
            for (int j = 0; j < vectors[0].length; j++) {
                for (int k = 0; k < vectors[0][0].length; k++) {
                    vectors[i][j][k].divide((double) count);
                }
            }
        }


    }

    public List<VelocityFeature> getVectorFeatureAtIterations() {
        return vectorFeatureAtIterations;
    }

    public InOut[][][] getVectors() {
        return vectors;
    }

    public InOut getOverallAvg() {
        return overallAvg;
    }


    public int getnI() {
        return vectorFeatureAtIterations.get(0).getnI();
    }

    public int getnJ() {
        return vectorFeatureAtIterations.get(0).getnJ();
    }

    public int getnK() {
        return vectorFeatureAtIterations.get(0).getnK();
    }
}
