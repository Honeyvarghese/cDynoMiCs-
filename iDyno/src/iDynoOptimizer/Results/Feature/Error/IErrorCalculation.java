package iDynoOptimizer.Results.Feature.Error;

import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.NumericIterationFeature;

import java.util.List;

/**
 * Created by Chris on 12/6/2014.
 */
public interface IErrorCalculation {

    double calculateError(double experimental, double actual);
    double calculateError(double[] experimental, double[] actual);


}
