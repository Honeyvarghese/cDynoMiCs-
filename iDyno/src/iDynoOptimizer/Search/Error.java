package iDynoOptimizer.Search;

import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.NumericIterationFeature;

/**
 * Created by Chris Johnson on 11/18/2014.
 */
public class Error {


    private Feature target;
    private Feature test;
    private double error;


    /*
    IErrorCalculation is only used for a numeric feature
    set to null for other feature types

     */
    public Error(Feature target, Feature test, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ) {
        this.target = target;
        this.test = test;
       error = target.calulateError(test, errorCalculation, paddingX, paddingY, paddingZ);

    }

    public Error(Feature target, Feature test, IErrorCalculation errorCalculation)
    {
        this(target, test, errorCalculation, 0, 0, 0);
    }



    public Feature getTarget() {
        return target;
    }

    public Feature getTest() {
        return test;
    }

    public double getError() {
        return error;
    }

}
