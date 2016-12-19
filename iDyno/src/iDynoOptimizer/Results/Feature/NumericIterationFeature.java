package iDynoOptimizer.Results.Feature;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.NumericFeatureNameIteration;
import iDynoOptimizer.Search.SimulationIteration;

import java.io.File;

/**
 * Created by Chris on 4/16/2015.
 */
public class NumericIterationFeature extends IterationFeature {

    private double[] featureResults;
    private double   average;


    /*
instantiates a partial version of this class that holds in vitro data
*/
    public NumericIterationFeature(String fileName, FeatureNameIteration name) {
        super(name);
        average = load(fileName);

    }

    public NumericIterationFeature(int nR, SimulationIteration mySimulationIteration, NumericFeatureNameIteration myName) {
        super(mySimulationIteration, myName);
        featureResults = new double[nR];

        if (myName.toString().contains("min")) {
            for (int i = 0; i < featureResults.length; i++) {
                featureResults[i] = Double.MAX_VALUE;
            }
        }

    }


    private double load(String fileName) {
        return Double.parseDouble(FileReaderWriter.readLines(fileName).get(0));
    }

    private void setValueAt(int nR, double value, boolean accumulate) {
        if (accumulate) featureResults[nR] += value;
        else featureResults[nR] = value;
    }

    public void setValueAt(int nR, double value) {
        setValueAt(nR, value, false);
    }

    public void addValueAt(int nR, double value) {
        setValueAt(nR, value, true);
    }

    public double getValueAt(int nR) {
        return featureResults[nR];
    }


    @Override
    public void calculateAverage() {
        double sum = 0;

        for (double d : featureResults) {
            sum += d;
        }
        average = sum / featureResults.length;

        featureResults = null;
    }


    @Override
    public double calulateError(Feature f, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ) {

        NumericIterationFeature compareTo = (NumericIterationFeature) f;
        setErrorCalculation(errorCalculation);
        setError(getErrorCalculation().calculateError(this.getAverage(), compareTo.getAverage()));

        return getError();
    }


    public double getAverage() {
        return average;
    }


    public void extract() {

    }

    @Override
    public void deleteIntermediateData() {

    }

    @Override
    public void deleteFeatures() {
        featureResults = null;

    }



    @Override
    public String toString() {

        return String.valueOf(average);
    }

}
