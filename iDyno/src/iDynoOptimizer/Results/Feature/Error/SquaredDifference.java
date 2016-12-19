package iDynoOptimizer.Results.Feature.Error;

import iDynoOptimizer.Results.Feature.NumericIterationFeature;

import java.util.List;

/**
 * Created by Chris on 12/6/2014.
 */
public class SquaredDifference implements IErrorCalculation {
    @Override
    public double calculateError(double experimental, double actual) {



        return Math.pow(experimental - actual, 2);
    }



    @Override
    public double calculateError(double[] experimental, double[] actual)
    {

        if(experimental.length != actual.length) {
            IllegalArgumentException iae = new IllegalArgumentException();
            iae.printStackTrace();
            System.exit(-5);
        }

        double error = 0;
        for(int a = 0; a < experimental.length;a++)
        {
            error += calculateError(experimental[a], actual[a]);
        }

        error /= experimental.length;
        return (error);
    }
}
