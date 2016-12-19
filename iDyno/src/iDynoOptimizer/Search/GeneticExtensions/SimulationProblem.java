package iDynoOptimizer.Search.GeneticExtensions;

import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.Solution;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.core.variable.RealVariable;
import iDynoOptimizer.MOEAFramework26.src.org.moeaframework.problem.AbstractProblem;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolFile;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolReader;
import iDynoOptimizer.Results.Feature.Error.SquaredDifference;
import iDynoOptimizer.Results.Feature.Names.FeatureName;
import iDynoOptimizer.Search.CompareRealData;
import iDynoOptimizer.Search.SimulationRun;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by chris on 10/30/2015.
 */
public class SimulationProblem extends AbstractProblem {

    private int protocolFileIndex;

    private static int paddingX, paddingY, paddingZ;


    private static int            numVariables;
    private static int            numObjs;
    private static List<ParamNum> params;
    private static int            staticProtocolFileIndex;

    public SimulationProblem() {
        super(SimulationProblem.numVariables, SimulationProblem.numObjs);

        this.protocolFileIndex = SimulationProblem.staticProtocolFileIndex;


    }

    /*
    must be called before an instant of this class is created
     */
    public static void init(int protocolFileIndex, File protocolFile, int numObjs, int paddingX, int paddingY, int paddingZ) {


        MyPrinter.Printer().printTier1ln("Initializing GA using protocol file: " + protocolFile.getName());

        ProtocolReader.read(protocolFile.getPath());
        SimulationProblem.params = ProtocolFile.getClassRep().getChangingParams();
        SimulationProblem.numVariables = params.size();

        SimulationProblem.numObjs = numObjs;

        SimulationProblem.staticProtocolFileIndex = protocolFileIndex;

        SimulationProblem.paddingX = paddingX;
        SimulationProblem.paddingY = paddingY;
        SimulationProblem.paddingZ = paddingZ;

        MyPrinter.Printer().printTier1ln("Initialized GA with " + SimulationProblem.numVariables + " parameters/variables and " + SimulationProblem.numObjs + " objectives");

    }

    @Override
    public Solution newSolution() {
        SimulationSolution solution = new SimulationSolution(protocolFileIndex, numberOfVariables, numberOfObjectives);

        int i = 0;
        for (ParamNum pn : params) {
            solution.setVariable(i++, new RealVariable(pn.getAbsMin(), pn.getAbsMax()), pn.getName(), pn.getProtocolFileName());
        }


        return solution;

    }


    @Override


    /*

    Future plans:

    Have a total number ff sims / step
        Have a GA population size less than this
        The difference will be randomly generated and injected at each evaluation(this method)
        The GA population size will slowly increase to the total as we get more and more simulations with a wrinkle identified
            concept similar to simulated annealing

    In this method, check if the solution has a  wrinkle
    If it does NOT:
        Compare only non-wrinkle features and use these values as objectives
        Set the wrinkle objectives to a very high value
    If it DOES:
        Compare all features and use all of these as objectives

    This will artificially make wrinkled simulations much better on the wrinkle objectives



    This method will need to know the run number of each simulation(can be stored in the SimulationRun object)
    and the stpe number of this generration so it can name the files correctly of the randomly injected solutions


     */
    public void evaluate(Solution solution) {


        //at this point the sims are extracted
        //now need to compare, generate errors, and add them as objectives

        MyPrinter.Printer().printTier2ln("Entering evaluation");

        SimulationSolution ss = (SimulationSolution) solution;

        SimulationRun sr = ss.getSimulationRun();


        MyPrinter.Printer().printTier1ln("Evaluating solution: " + sr.getResultPath());


        boolean success = CompareRealData.compare(sr, new SquaredDifference(), paddingX, paddingY, paddingZ);

        int i = 0;
        if(success)
        {
            Map<FeatureName, Double> errors = CompareRealData.getAccumulatedErrorsByFeatureName();

            for(FeatureName fn : errors.keySet())
            {
                double error = errors.get(fn);
                MyPrinter.Printer().printTier1ln("Error on " + fn.toString() + " is " + error);
                solution.setObjective(i++, error);
            }
        }
        else
        {
            int exitCode = -5;

            Exception iae = new Exception("Master terminated with exit code " +exitCode + " There was a problem comparing simulation results with in vitro data. " +
                    "Make sure all of the features in the in vitro folder are also in the features file so they can be extracted");

            iae.printStackTrace();

            //stop the virtual machine
            System.exit(exitCode);


        }

        sr.deleteAfterErrorGenerated();


    }


}
