package iDynoOptimizer.Search.GeneticExtensions;

import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Solution;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Variable;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.variable.RealVariable;
import iDynoOptimizer.Search.SimulationRun;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by chris on 10/30/2015.
 */
public class SimulationSolution extends Solution {

    private String resultPath;
    private SimulationRun simulationRun;
    private Map<String, Integer> paramVariableIndexMap;
    private int protocolFileIndex;

    public SimulationSolution(int protocolFileIndex, int numberOfVariables, int numberOfObjectives) {
        super(numberOfVariables, numberOfObjectives);
        this.paramVariableIndexMap = new HashMap<>();
        this.protocolFileIndex = protocolFileIndex;
    }


    public SimulationSolution(SimulationSolution ss)
    {
        super(ss);
        this.paramVariableIndexMap = ss.paramVariableIndexMap;
        this.protocolFileIndex = ss.protocolFileIndex;
    }



    @Override
    public Solution copy(){

        /*
        Copying the solution is part of the genetic algorithms
        We are overriding so that the copy produces a simulation solution instead of just a solution
         */
        return new SimulationSolution(this);

    }

    public void setSimulationRun(SimulationRun sr)
    {
        this.simulationRun = sr;
    }

    public SimulationRun getSimulationRun()
    {
        return this.simulationRun;
    }


    public void setVariable(int index, Variable variable, String paramName, String protocolName)
    {
        super.setVariable(index, variable);
        paramVariableIndexMap.put(protocolName+paramName, index);

    }


    public double getVariable(String paramName, String protocolName)
    {
        return ((RealVariable)getVariable(paramVariableIndexMap.get(protocolName+paramName))).getValue();
    }

    public int getProtocolFileIndex()
    {
        return protocolFileIndex;
    }

    public void setResultPath(String resultPath)
    {
        this.resultPath = resultPath;
    }
    public String getResultPath()
    {
        return resultPath;
    }

}
