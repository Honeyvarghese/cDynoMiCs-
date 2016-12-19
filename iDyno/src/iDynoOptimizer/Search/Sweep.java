package iDynoOptimizer.Search;

import iDynoOptimizer.Global.ExtraMath;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.Parameters.ParamNumValue;
import iDynoOptimizer.Search.ExitCondition.*;
import iDynoOptimizer.Search.Operators.StepParam;
import iDynoOptimizer.Search.Operators.StepParamIteratively;

import java.io.File;
import java.util.*;


public class Sweep {


    private List<ParamNameValues> toSweep;
    private List<List<ParamNum>> combinations;
    private StepParam stepParam;
    private IExitCondition exitCondition;
    private File protocolFile;
    private int numOfRepeats;

    private int runNumber;
    private int stepNumber;

    private List<Runner> resultPaths;


    private static Map<String, Map<List<ParamNum>, Runner>> history;

    private  Map<List<ParamNum>, Runner> subHistory;

    public List<Runner> getResultPaths() {
        return resultPaths;
    }

/*
         * Initializes a new Sweep object with a mapping of each parameter to sweep to an initial value for the parameter
         */



    public Sweep(List<ParamNum> initialParams, int stepNumber, int runNumberStart, int numOfRepeats, File protocolFile) {

        resultPaths = new ArrayList<>();
        toSweep = new LinkedList<ParamNameValues>();
        combinations = new ArrayList<List<ParamNum>>();
        history = new HashMap<>();

        for (ParamNum pn : initialParams) {
            ParamNameValues pnv = new ParamNameValues(pn);
            toSweep.add(pnv);
        }

        this.protocolFile = protocolFile;
        this.numOfRepeats = numOfRepeats;
        this.runNumber = runNumberStart;
        this.stepNumber = stepNumber;

        if(!history.containsKey(protocolFile.getPath()))
        {
            history.put(protocolFile.getPath(), new HashMap<>());
        }

        subHistory = history.get(protocolFile.getPath());

    }

    private void sweep() {
        preComputeToSweep();
        preComputeCombinations(new LinkedList<ParamNum>(), 0);

        //contemplating a new run
        for (List<ParamNum> params : combinations) {


            Runner r = searchHistory(params);
            if(r == null)
            {
                r = Runner.Run(numOfRepeats, params, runNumber, stepNumber, protocolFile);
                //add to the history
                subHistory.put(params, r);
                runNumber++;
            }

            resultPaths.add(r);

        }
    }



    public Runner searchHistory(List<ParamNum> newParams)
    {
        Runner r = null;
        boolean breakOut = false;

        //go through the history of runs that used the same protocol file
        for(List<ParamNum> paramsInHistory : subHistory.keySet())
        {
            //find two params that match
            boolean equivalent = true;
            for(ParamNum pn : newParams)
            {
                for(ParamNum pnH : paramsInHistory)
                {
                    if(pn.getName().equalsIgnoreCase(pnH.getName()))
                    {
                        equivalent = ExtraMath.approxEqual(pnH.getValue().getValue(), pn.getValue().getValue(), pn.getEpsilon());
                        breakOut = !equivalent;
                    }
                    if(breakOut) break;
                }
                if(breakOut) break;
            }

            if(equivalent) {
                r = subHistory.get(paramsInHistory);
                break;
            }

        }

        return r;
    }
    /*
     * Performs a sweep of parameters, running the simulation on many parameter combinations. The combinations considered depends on the operator used
     * to alter each parameter. All possible combinations for all of the swept parameter values are considered
     */
    public void sweep(boolean preCompute) {
        if (preCompute) sweep();
        //else sweep(new LinkedList<ParamNum>(), 0, toSweep.size() - 1, false);
    }


    //generates the list of values to sweep for each parameter
    private void preComputeToSweep() {

        for (ParamNameValues pnv : toSweep) {
            boolean exit = false;

            stepParam = new StepParamIteratively(pnv);
            //generate the exit conditions
            double max = pnv.getParamNum().getCurrentMax();
            double min = pnv.getParamNum().getCurrentMin();
            ExitCondition ecMax = new ExitCondition(max, Operator.greater);
            ExitCondition ecMin = new ExitCondition(min, Operator.less);


            //step the parameter until the value is no longer in the range
            while (!exit) {

                //step the parameter
                ParamNumValue pi = stepParam.step();


                exitCondition = new CompositeExitCondition(ecMax, ecMin, Operand.or);
                exit = exitCondition.met(pi.getValue(), pnv.getParamNum().getEpsilon());

                if (!exit) pnv.addParamValue(pi);

            }
        }
    }


    //generates all the parameter combinations
    private void preComputeCombinations(LinkedList<ParamNum> paramSet, int depth) {

        if (depth == toSweep.size()) {
            combinations.add(paramSet);

            return;
        }


        for (int i = 0; i < toSweep.get(depth).getParamNumValues().size(); i++) {


            //get the parameter to change
            ParamNameValues pnv = toSweep.get(depth);

            //get the next value
            ParamNumValue pi = pnv.getParamValue(i);


            //create a new parameter with the next value
            ParamNum pnClone = pnv.getParamNum().copy();
            pnClone.setValue(pi);


            //make a copy of the parameter set to send to the next level of recursion
            //the parameter set at this level will remain unchanged
            LinkedList<ParamNum> clone = copyParamSet(paramSet);


            clone.add(pnClone);


            //continue down the rabbit hole, sending the updated parameter set along
            preComputeCombinations(clone, depth + 1);
        }
    }


       

        /*
        maxDepth = parameter being enumerated to generate the paramSet
        activeLevel = the parameter being changed
         */

//    private void sweep(LinkedList<ParamNum> paramSet, int depth, int activeLevel, boolean comboComplete) {
//
//        if (depth == toSweep.size()) {
//
//            LinkedList<ParamNum> clone = copyParamSet(paramSet);
//
//
//            Runner r = searchHistory(clone);
//
//            if(r == null) {
//                subHistory.put(paramSet, r);
//                resultPaths.add(Runner.Run(numOfRepeats, clone, runNumber, stepNumber, protocolFile));
//            }
//
//            resultPaths.add(r);
//            runNumber++;
//
//            if (comboComplete || activeLevel == toSweep.size() - 1) {
//
//                ParamNameValues activeParam = toSweep.get(activeLevel);
//
//                stepParam(activeParam, activeLevel, paramSet);
//            }
//
//            return;
//        }
//
//
//        for (int i = 0; i < toSweep.get(depth).getParamNumValues().size(); i++) {
//
//
//            //get the parameter to change
//            ParamNameValues pnv = toSweep.get(depth);
//            //get the next value
//            ParamNumValue pi = pnv.getParamValue(i);
//
//            //create a new parameter with the next value
//            ParamNum pnClone = pnv.getParamNum().copy();
//            pnClone.setValue(pi);
//
//
//            //make a copy of the parameter set to send to the next level of recursion
//            //the parameter set at this level will remain unchanged
//            LinkedList<ParamNum> clone = copyParamSet(paramSet);
//
//
//            clone.add(pnClone);
//
//
//            comboComplete = (depth == (activeLevel + 1) && i == toSweep.get(depth).getParamNumValues().size() - 1);
//
//            //continue down the rabbit hole, sending the updated parameter set along
//            sweep(paramSet, depth + 1, activeLevel, comboComplete);
//
//        }
//    }


//    private int stepParam(ParamNameValues activeParam, int activeLevel, LinkedList<ParamNum> paramSet) {
//
//        boolean exit;
//
//        stepParam = new StepParamIteratively(activeParam);
//
//        //generate new parameter value for the active parameter based on the error or iteratively
//
//        ParamNumValue pi = stepParam.step();
//
//
//        double max = activeParam.getParamNum().getCurrentMax();
//        double min = activeParam.getParamNum().getCurrentMin();
//
//        ExitCondition ecMax = new ExitCondition(max, Operator.greater);
//        ExitCondition ecMin = new ExitCondition(min, Operator.less);
//
//        exitCondition = new CompositeExitCondition(ecMax, ecMin, Operand.or);
//
//        exit = exitCondition.met(pi.getValue());
//
//
//        if (exit) {
//            activeLevel--;
//
//            if (activeLevel >= 0) {
//
//                return stepParam(toSweep.get(activeLevel), activeLevel, paramSet);
//            } else {
//
//                return activeLevel;
//            }
//
//
//        }
//        //add the new param to the parameter we are currently changing
//        else {
//            activeParam.addParamValue(pi);
//            return activeLevel;
//        }
//    }

    private LinkedList<ParamNum> copyParamSet(List<ParamNum> paramSet) {
        LinkedList<ParamNum> copy = new LinkedList<ParamNum>();

        for (ParamNum pn : paramSet) {
            copy.add(pn.copy());
        }
        return copy;
    }


}
