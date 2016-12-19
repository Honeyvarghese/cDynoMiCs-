package iDynoOptimizer.Results.Feature.Names;

/**
 * Created by Chris on 9/1/2015.
 */
public enum NumericFeatureNameIteration implements FeatureNameIteration {

    agentCount,
    totalMass,
    maxHeight, //maximum distance from the X = 0 boundary
    maxLength, //maximum distance from the Y = 0 boundary
    maxDepth, //maximum distance from the Z = 0 boundary. Will always be 0 for 2D simulations

    //minimum distances from the boundaries
    minHeight,
    minLength,
    minDepth

}
