package iDynoOptimizer.Results.Feature;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Names.*;
import iDynoOptimizer.Search.SimulationIteration;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Chris on 11/15/2014.
 */
public abstract class Feature {



    private IErrorCalculation errorCalculation;

    private double                    error;
    private List<SimulationIteration> iterationsUsed;

    public Feature() {
        iterationsUsed = new ArrayList<>();
    }

    private FeatureName myName;


    public Feature(FeatureName name)
    {
        myName = name;
    }

    public Feature(List<SimulationIteration> iterationsUsed, FeatureName name) {
        this.iterationsUsed = iterationsUsed;
        myName = name;
    }

    public Feature(SimulationIteration iterationUsed, FeatureName name) {
        iterationsUsed = new ArrayList<>();
        iterationsUsed.add(iterationUsed);
        myName = name;
    }


    public Feature(SimulationIteration iterationUsed1, SimulationIteration iterationUsed2, FeatureName name) {
        iterationsUsed = new ArrayList<>();
        iterationsUsed.add(iterationUsed1);
        iterationsUsed.add(iterationUsed2);
        myName = name;
    }


    public IErrorCalculation getErrorCalculation() {
        if (errorCalculation == null) {

            NullPointerException e = new NullPointerException();
            e.printStackTrace();
            throw e;


        }
        return errorCalculation;
    }

    protected void setErrorCalculation(IErrorCalculation errorCalculation) {
        this.errorCalculation = errorCalculation;
    }

    public abstract double calulateError(Feature f, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ);


    public abstract void calculateAverage();

    public abstract void extract();

    public double getError() {
        return error;
    }



    protected void setError(double error) {
        this.error = error;
    }


    public void setup() {
        extract();
        calculateAverage();
    }

    public FeatureName getMyName() {
        return myName;
    }

    public void output(String filePath) {
        FileReaderWriter.writeSingleFile(toString(), filePath, getMyName().toString(), false);
    }

    public abstract void deleteIntermediateData();

    public abstract  void deleteFeatures();


    protected static int[] makeIterationsArray(int itrStart, int itrEnd, int step) {
        int[] arry = new int[itrEnd - itrStart / step + 1];

        int incrmental = itrStart;
        for (int i = 0; i < arry.length; i++) {
            arry[i] = incrmental;
            incrmental += step;
        }
        return arry;
    }

    public List<SimulationIteration> getIterationsUsed() {
        return iterationsUsed;
    }


    /*
    Every two lines is: a feature name on the first line and space delimited iteration numbers on the second
     */
    private static Map<FeatureName, int[]> featureNamesFromFile(String fullFilePath) {


        if (fullFilePath.isEmpty())
            return new HashMap<>();

        List<String> lines = FileReaderWriter.readLines(fullFilePath);

        int[]                   iterations;
        String[]                iterationsLine;
        Map<FeatureName, int[]> fnsItrs = new HashMap<>();

        for (int i = 0; i < lines.size(); i += 2) {

            FeatureName fns = null;
            try {
                fns = FeatureNameSimulation.valueOf(lines.get(i).trim());
            } catch (IllegalArgumentException e) {
             //   e.printStackTrace();
            }
            if (fns == null) {
                try {
                    fns = SpatialNumericFeatureNameIteration.valueOf(lines.get(i).trim());
                } catch (IllegalArgumentException e) {
                //    e.printStackTrace();
                }
            }
            if (fns == null) {
                try {
                    fns = NumericFeatureNameIteration.valueOf(lines.get(i).trim());
                } catch (IllegalArgumentException e) {
                  //  e.printStackTrace();
                }
            }


            iterationsLine = lines.get(i + 1).trim().split(" ");


            iterations = new int[iterationsLine.length];

            for (int x = 0; x < iterationsLine.length; x++) {
                iterations[x] = Integer.parseInt(iterationsLine[x].trim());
            }
            fnsItrs.put(fns, iterations);
        }


        return fnsItrs;

    }

    public static Map<FeatureNameSimulation, int[]> simFeatureNamesFromFile(String fullFilePath) {


        Map<FeatureName, int[]> fnsItrs = featureNamesFromFile(fullFilePath);

        Map<FeatureNameSimulation, int[]> fnsItrsSim = new HashMap<>();

        for (Map.Entry e : fnsItrs.entrySet()) {

            if (e.getKey().getClass().getName().equalsIgnoreCase(FeatureNameSimulation.class.getName()))
                fnsItrsSim.put((FeatureNameSimulation) e.getKey(), (int[]) e.getValue());
        }

        return fnsItrsSim;

    }

    public static Map<NumericFeatureNameIteration, int[]> numItrFeatureNamesFromFile(String fullFilePath) {
        Map<FeatureName, int[]> fnsItrs = featureNamesFromFile(fullFilePath);

        Map<NumericFeatureNameIteration, int[]> fnsItrsNum = new HashMap<>();

        for (Map.Entry e : fnsItrs.entrySet()) {

            if (e.getKey().getClass().getName().equalsIgnoreCase(NumericFeatureNameIteration.class.getName()))
                fnsItrsNum.put((NumericFeatureNameIteration) e.getKey(), (int[]) e.getValue());
        }

        return fnsItrsNum;
    }

    public static Map<SpatialNumericFeatureNameIteration, int[]> spatialFeatureNamesFromFile(String fullFilePath) {
        Map<FeatureName, int[]> fnsItrs = featureNamesFromFile(fullFilePath);

        Map<SpatialNumericFeatureNameIteration, int[]> fnsItrsSp = new HashMap<>();

        for (Map.Entry e : fnsItrs.entrySet()) {

            if (e.getKey().getClass().getName().equalsIgnoreCase(SpatialNumericFeatureNameIteration.class.getName()))
                fnsItrsSp.put((SpatialNumericFeatureNameIteration) e.getKey(), (int[]) e.getValue());
        }

        return fnsItrsSp;


    }


}
