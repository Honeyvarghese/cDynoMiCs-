package iDynoOptimizer.Results.Feature;

import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.SpatialNumericFeatureNameIteration;
import iDynoOptimizer.Search.SimulationIteration;

import java.io.File;
import java.util.DoubleSummaryStatistics;

/**
 * Created by Chris on 8/31/2015.
 */
public class SpatialNumericIterationFeature extends IterationFeature {


    private double[][][][] repeats;

    private double[][][] average;

    private double overallAvg;
    private double sum;
    private double sumAboveOverallAvg;
    private double threshold = 1.1;
    private double sumSlope;


    private boolean normalizeToBinary;

    private int    nI;
    private int    nJ;
    private int    nK;
    private int    nR;
    private double resolution;


    public SpatialNumericIterationFeature(int nR, int nI, int nJ, int nK, double resolution, SimulationIteration mySimulationIteration, SpatialNumericFeatureNameIteration myName, boolean normalizeToBinary) {

        super(mySimulationIteration, myName);

        this.nI = nI;
        this.nJ = nJ;
        this.nK = nK;
        this.nR = nR;
        this.resolution = resolution;
        repeats = new double[this.nR][this.nI][this.nJ][this.nK];
        this.normalizeToBinary = normalizeToBinary;
    }


    /*
    instantiates a partial version of this class that holds in vitro data
     */
    public SpatialNumericIterationFeature(String fileNameFull, FeatureNameIteration name) {
        super(name);
        average[0] = SpatialIterationFeature.load(fileNameFull);
    }


    public void setValueAt(int nR, int nI, int nJ, int nK, double value, boolean accumulate) {
        if (accumulate) repeats[nR][nI][nJ][nK] += value;
        else repeats[nR][nI][nJ][nK] = value;

    }


    public double getValueAt(int nR, int nI, int nJ, int nK) {
        return repeats[nR][nI][nJ][nK];
    }


    public int findColumnIndex(int nR, int nJ, int nK, double value) {
        for (int i = 0; i < repeats[0].length; i++) {

            if (repeats[nR][i][nJ][nK] == value) return i;

        }
        return -1;
    }


    @Override
    public double calulateError(Feature f, IErrorCalculation errorCalculation,int paddingX, int paddingY, int paddingZ) {

        SpatialNumericIterationFeature compareTo = (SpatialNumericIterationFeature) f;
        setErrorCalculation(errorCalculation);
        //depending on the type of feature it is, it may make sense to average or sum, for now just average everything
        setError(SpatialIterationFeature.calculateSpatialError(getAverage(), compareTo.getAverage(), errorCalculation, true, paddingX, paddingY, paddingZ));
        return getError();
    }


    @Override
    public void calculateAverage() {

        average = new double[repeats[0].length][repeats[0][0].length][repeats[0][0][0].length];

        double avg;
        for (int i = 0; i < repeats[0].length; i++) {
            for (int j = 0; j < repeats[0][0].length; j++) {
                for (int k = 0; k < repeats[0][0][0].length; k++) {
                    avg = 0;

                    for (int c = 0; c < repeats.length; c++) {

                        double value = repeats[c][i][j][k];
                        avg += value;
                    }
                    avg /= (double) repeats.length;

                    overallAvg += avg;


                    average[i][j][k] = avg;
                }
            }
        }


        //before dividing by the number of elements to make it the average, record it as the sum
        sum = overallAvg;
        double numElements = 0;

        if (getMyName() != SpatialNumericFeatureNameIteration.contour)
            numElements = (double) (average.length * average[0].length * average[0][0].length);
        else numElements = (double) (average[0].length * average[0][0].length);

        overallAvg /= numElements;

        for (int i = 0; i < average.length; i++) {
            for (int j = 0; j < average[0].length; j++) {
                for (int k = 0; k < average[0][0].length; k++) {

                    double v = average[i][j][k];
                    if (v > overallAvg * threshold) {

                        sumAboveOverallAvg += v;

                    }
                }
            }
        }


        // sumSlope = SpatialIterationFeature.sumSlope(this, 0, 1);


//        if(getMyName() == SpatialNumericFeatureNameIteration.contour)
//        {
//            maxValue = Double.MIN_VALUE;
//            minValue = Double.MAX_VALUE;
//            int count;
//            for (int j= 0; j < average[0].length; j++) {
//                for (int k = 0; k < average[0][0].length; k++) {
//                    avg = 0;
//                    count = 0;
//                    for (int i = 0; i < average.length; i++) {
//
//                        double value =average[i][j][k];
//                        if(getMyName() != SpatialNumericFeatureNameIteration.contour || value > 0) count++;
//
//                        avg += value;
//                    }
//                    avg /= (double)count;
//
//
//                    if(avg > maxValue) maxValue = avg;
//                    if(avg < minValue) minValue = avg;
//                }
//            }
//        }


        repeats = null;

        if (normalizeToBinary) normalizeToBinary();
    }

    @Override
    public void extract() {

    }

    @Override
    public void deleteIntermediateData() {

    }

    @Override
    public void deleteFeatures() {

        repeats = null;
        average = null;
    }


    public double getRatioAboveAvg() {
        double v = sumAboveOverallAvg / sum;
        if (Double.isNaN(v)) v = 0;
        return v;
    }

//    public double getSumSlop()
//    {
//        return sumSlope;
//    }

    private void normalizeToBinary() {

        double[] minmax = SpatialIterationFeature.calcMinandMax(average);

        for (int i = 0; i < average.length; i++) {
            for (int j = 0; j < average.length; j++) {
                for (int k = 0; k < average[0].length; k++) {

                    average[i][j][k] = (float) Math.round(SpatialIterationFeature.scale(average[i][j][k], minmax[0], minmax[1]));

                }
            }
        }


    }

    public double[][][] getAverage() {
        return average;
    }


    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(SpatialIterationFeature.rangeLine(0, nI, 0, nJ, 0, nK, resolution));
        for (int i = 0; i < average.length; i++) {
            for (int j = 0; j < average[0].length; j++) {

                sb.append('(');
                for (int k = 0; k < average[0][0].length; k++) {
                    sb.append(average[i][j][k]);
                    if (k != average[0][0].length - 1) sb.append(',');
                }
                sb.append(')');
            }
            if (i < average.length - 1) sb.append('\n');

        }


        return sb.toString();
    }


    public int getnI() {
        return nI;
    }

    public int getnJ() {
        return nJ;
    }

    public int getnK() {
        return nK;
    }

    public int getnR() {
        return nR;
    }

    public double getResolution() {
        return resolution;
    }
}
