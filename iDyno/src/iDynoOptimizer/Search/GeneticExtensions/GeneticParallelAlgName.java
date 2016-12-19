package iDynoOptimizer.Search.GeneticExtensions;

/**
 * Created by chris on 10/30/2015.
 */
public enum GeneticParallelAlgName {

    NSGAII, //populationSize, sbx.rate, sbx.distributionIndex, pm.rate, pm.distributionIndex
    eNSGAII, //populationSize, epsilon, sbx.rate, sbx.distributionIndex, pm.rate, pm.distributionIndex, injectionRate, windowSize, maxWindowSize, minimumPopulationSize, maximumPopulationSize
    NSGAIII, //populationSize, divisions, sbx.rate, sbx.distributionIndex, pm.rate, pm.distributionIndex (for the two-layer approach, replace divisions by divisionsOuter and divisionsInner)
    GDE3     //populationSize, de.crossoverRate, de.stepSize

}
