package iDynoOptimizer.Protocol.Parameters;


import iDynoOptimizer.Global.ExtraMath;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Protocol.ProtocolFile.ParamSelector;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolFile;
import iDynoOptimizer.Search.SimulationRun;

import java.util.List;
import java.util.Random;





public class ParamNum {

    //START protocol file values : These values are loaded each sweep from the protocol file. They do not change during a sweep

    //Used to uniquely identify this parameter. It's up to the protocol file to ensure each changing parameter has a unique name
    private String name;

    //the absolute biggest value to use
    private double absMax = Double.MAX_VALUE;

    //the absolute smallest value to use
    private double absMin = Double.MIN_VALUE;

    //A small value used to determine equality between different values of this parameter
    //If two values differ by epsilon or less, they are considered equal
    //Also, a single step (used to go from one value to the next for a parameter) must be bigger than epsilon by the "epsilonStepFactor"
    private double epsilon;

    //Determines how many values of this parameter to use in a sweep
    private int pointCount;
    //Determines the range of values to sweep over in a sweep
    private double range;

    //name of the prtocolfile to which this param belongs
    private String protocolFileName;

    //END protocol file values



    private static final int epsilonStepFactor = 2;




    /*
    These values are set each sweep and are the actual values used in a sweep.
    These are written to a file inside the sweep directory
     */

    private double currentMin;
    private double currentMax;
    private double currentStep;
    private ParamNumValue value;


    //a reference to the parameter that will be written to the protocol file whenever ProtocolFile.write(String path) is called
    private ParamNumValue masterRef;
    private String        selectorString;




    /*
    Used for the random seed repeats, to get a reference to that value in the protocol file
     */
    public ParamNum(String name, String selectorString) {

        this.selectorString = selectorString;
        this.name = name;

        if (selectorString != null)
            masterRef = ParamSelector.selectParam(selectorString, ProtocolFile.getClassRep());
        value = masterRef.clone();
    }


    /*
    Used for populating the current values from the params file, which is written to the sweep directory
    This method will not populate the protocol values (abs min and max, epsilon, range, and pointCount
     */
    public ParamNum(String name, String selectorString, double currentValue, double currentMin, double currentMax, double currentStep) {
        paramNumHelper(currentMin, currentMax, currentStep);
        this.selectorString = selectorString;
        this.name = name;
        if (selectorString != null)
            masterRef = ParamSelector.selectParam(selectorString, ProtocolFile.getClassRep());
        this.value = new ParamNumValue(currentValue);
    }

    /*
    Used to create a copy of this parameter
     */
    private ParamNum(String name, String protocolFileName, ParamNumValue masterRef, String selectorString, ParamNumValue currentValue, double currentMin, double currentMax, double currentStep, double absMin, double absMax, double epsilon, double range, int pointCount) {
        this.name = name;
        this.protocolFileName = protocolFileName;
        this.value = currentValue;
        this.absMin = absMin;
        this.absMax = absMax;
        this.epsilon = epsilon;
        this.range = range;
        this.pointCount = pointCount;
        //populate these for a normal sweep
        paramNumHelper(currentMin, currentMax, currentStep);
        this.selectorString = selectorString;
        this.masterRef = masterRef;

    }


    /*
    Used when populating this param from a protocol file
     */
    public ParamNum(String name, String protocolFileName, ParamNumValue masterRef, double absMin, double absMax, double epsilon, double range, int pointCount) {
        this.name = name;
        this.protocolFileName = protocolFileName;

        this.masterRef = masterRef;
        this.value = new ParamNumValue(masterRef.getValue());
        this.absMin = absMin;
        this.absMax = absMax;

        //epsilon and range not used in a normal sweep
        this.epsilon = epsilon;
        this.range = range;
        this.pointCount = pointCount;



        //populate these for a normal sweep
        paramNumHelper(absMin, absMax, divideRange(absMax - absMin, pointCount));
    }




    public void writeMasterRef() {
        masterRef.setValue(value.getValue());
    }


    private void paramNumHelper(double currentMin, double currentMax, double currentStep) {
        setCurrentMax(currentMax);
        setCurrentMin(currentMin, false);
        setCurrentStep(currentStep);

    }

    public ParamNum copy() {
        return new ParamNum(name, protocolFileName, masterRef, selectorString, value.clone(), currentMin, currentMax, currentStep, absMin, absMax, epsilon, range, pointCount);
    }


    public void setCurrentMin(double min) {

        setCurrentMin(min, true);
    }

    public void setCurrentMin(double min, boolean alsoSetValue)
    {
        if(min < absMin && absMin != Double.MAX_VALUE) min = absMin;
        if(alsoSetValue) value.setValue(min);

        currentMin = min;
    }

    public void setCurrentMax(double max) {

        if(max> absMax && absMax != Double.MIN_VALUE) max = absMax;
        currentMax = max;
    }

    public void setCurrentStep(double step) {

        if(step < epsilon) step = epsilon * epsilonStepFactor;
        if(step > currentMax - currentMin) step = currentMax - currentMin;
        currentStep = step;
    }

    public void setValue(ParamNumValue pnv) {
        value = pnv;
    }
    public void setValue(double newValue)
    {
        ParamNumValue pnv = new ParamNumValue(newValue);
        this.value = pnv;
    }

    public double getCurrentMin() {
        return currentMin;
    }

    public double getCurrentMax() {
        return currentMax;
    }


    public double getAbsMin() {
        return absMin;
    }

    public double getAbsMax() {
        return absMax;
    }

    public double getCurrentStep() {
        return currentStep;
    }

    public ParamNumValue getValue() {
        return value;
    }



    public String getName() {
        return name;
    }

    public String getProtocolFileName() { return protocolFileName;}


    public double getEpsilon() {
        return epsilon;
    }

    public String getSelectorString() {
        return selectorString;
    }

    public double getPointCount() {
        return pointCount;
    }

    public double getRange() {
        return range;
    }



    public void resetMinMaxStepMethod1(ParamNum best) {
        double bestValue = best.getValue().getValue();
        double bestMax = best.getCurrentMax();
        double bestMin = best.getCurrentMin();
        double bestStep = best.getCurrentStep();
        double bestDiff = bestMax - bestMin;
        if (ExtraMath.approxEqual(bestValue, bestMin, epsilon)) //search below the minimum because it was the best value found
        {
            setCurrentMax(bestMin + bestStep / 2);
            setCurrentMin(currentMax - bestDiff);



        } else if (ExtraMath.approxEqual(bestValue, bestMax, epsilon)) //search above the maximum beecause it was the best valuef ound
        {
            setCurrentMin(bestMax - bestStep / 2);
            setCurrentMax(currentMin + bestDiff);

        } else //the best value found was somewhere in between, so narrow the search to there
        {
            setCurrentMin(bestValue - bestStep / 3);
            setCurrentMax(bestValue + bestStep / 3);


        }

        setCurrentStep(divideRange(currentMax - currentMin, pointCount));
    }

    public void randomMinMaxStepReset() {



        Random r = new Random();
        double upOrDown = r.nextDouble();
        double newMin;
        double newMax;

        if (upOrDown < 0.5) {
            newMin = absMin + (absMax - absMin) * r.nextDouble();
            newMax = newMin +range;
        } else {
            newMax = absMin + (absMax - absMin) * r.nextDouble();
            newMin = newMax - range;
        }

        setCurrentMin(newMin);
        setCurrentMax(newMax);

        setCurrentStep(divideRange(currentMax - currentMin, pointCount));

    }







    public static void reset(List<ParamNum> initialParams, ParamResetType paramResetType, SimulationRun best) {


        boolean printValues = paramResetType == ParamResetType.NormalResetMethod1 || paramResetType == ParamResetType.Random;

        if(paramResetType == ParamResetType.Random) {
            for (ParamNum pnI : initialParams) {

                pnI.randomMinMaxStepReset();

                if (printValues)
                    MyPrinter.Printer().printTier1ln(paramResetType.toString() + " " + pnI.getCurrentMin() + "\t\t\t" + pnI.getCurrentMax() + "\t\t\t" + pnI.getCurrentStep());
            }
        }

        else if(paramResetType == ParamResetType.NormalResetMethod1 && best != null) {

            for (ParamNum pnBest : best.getParamSet()) {
                for (ParamNum pnI : initialParams) {
                    if (pnI.getName().equalsIgnoreCase(pnBest.getName()))
                    {
                        pnI.resetMinMaxStepMethod1(pnBest);
                        if(printValues)
                        MyPrinter.Printer().printTier1ln(paramResetType.toString() + " " + pnI.getCurrentMin() + "\t\t\t" + pnI.getCurrentMax() + "\t\t\t" + pnI.getCurrentStep());
                    }



                }



            }
        }






    }

    public static double divideRange(double range, double points)
    {
        return range / (points - 1);
    }


}
