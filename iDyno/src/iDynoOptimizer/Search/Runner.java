package iDynoOptimizer.Search;

import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Protocol.Parameters.ParamNumValue;
import iDynoOptimizer.Protocol.ProtocolFile.ParamSelector;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolFile;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolReader;
import iDynoOptimizer.Protocol.ProtocolFile.ProtocolWriter;

import java.io.File;
import java.util.*;

/**
 * Created by Chris Johnson on 1/14/2015.
 */
public class Runner {


   public enum RunnerState
    {
        pending,  // for pending (need to be added to the regular roster)
        inProgress, //for in progress (have been added to the regular roster to be run)
        finished ,  // for finished (need to be added back to the master roster because they are finished)
        recorded // finished and recorded in the master roster




    }


    private final String               resultPath;
    private final String               protocolFileName;

    //the values are

    private final Map<String, RunnerState> restulPathFull;
    private final List<String>         protocolFilePathNameFull;
    private       int                  expectedItrCount;

    private boolean isTarget;
    private boolean isExtracted;

    public int getExpectedItrCount() {
        return expectedItrCount;
    }

    public boolean isTarget() {
        return isTarget;
    }


    public boolean isExtracted() {
        return isExtracted;
    }

    public void extracted() {
        isExtracted = true;
    }

    public Runner(String resultPath, String protocolFileName, List<String> restulPathFull, List<String> protocolFilePathNameFull, boolean target, int expectedItrCount) {
        this.resultPath = resultPath;
        this.protocolFileName = protocolFileName;
        this.restulPathFull = new TreeMap<>();

        for(String s :restulPathFull)
        {
            this.restulPathFull.put(s + "\\", RunnerState.pending);
        }
        this.protocolFilePathNameFull = protocolFilePathNameFull;
        this.isTarget = target;
        this.expectedItrCount = expectedItrCount + 1;
    }

    public String getResultPath() {
        return resultPath;
    }

    public String getProtocolFileName() {
        return protocolFileName;
    }

    public Map<String, RunnerState> getRestulPathFull() {
        return restulPathFull;
    }

    public List<String> getProtocolFilePathNameFull() {

       Collections.sort(protocolFilePathNameFull);
        return protocolFilePathNameFull;
    }

    private static ParamNum randomSeedParam;


    //runs the simulation
    public static Runner Run(int runXTimes, List<ParamNum> paramSet, int runNumber, int stepNumber, File protocolFile) {

        writeMasterParamRefs(paramSet);


        String protocolFileName = protocolFile.getName().replace(".xml", "");
        String resultPath       = Global.getGlobal().getTestResultPathPartial(runNumber, stepNumber, protocolFileName);


        Runner r = run(runXTimes, resultPath, protocolFile.getPath(), protocolFile.getName(), false);

        ParamSelector.writeParams(resultPath + File.separator + Global.getGlobal().paramsFile, paramSet);

        return r;
    }


    public static Runner RunTarget(int runXTimes) {

        String protocolFileNamePathFull = Global.getGlobal().getTargetProtocolFileNamePathFull();
        String resultPath       = Global.getGlobal().getTargetResultPath();
        ProtocolReader.read(protocolFileNamePathFull);

       return run(runXTimes, resultPath, protocolFileNamePathFull, Global.getGlobal().getTargetProtocolFileName(), true);

    }

    private static Runner run(int runXTimes, String resultPath, String protocolFileNamePathFull, String protocolFileName, boolean isTarget) {
        String       resultPathFull;
        String       protocolFilePathName;
        List<String> fullResultPaths       = new ArrayList<>();
        List<String> protocolFilePathNames = new ArrayList<>();



        randomSeedParam = new ParamNum("random seed", "simulator/param;name=randomSeed@innerValue");
        ParamNumValue endOfSimulation =  ParamSelector.selectParam("simulator/timeStep/param;name=endOfSimulation@innerValue", ProtocolFile.getClassRep());



        for (int x = 1; x <= runXTimes; x++) {

            randomizeSeed();
            if (runXTimes > 1) resultPathFull = resultPath + File.separator + x;
            else resultPathFull = resultPath;



            protocolFilePathName = ProtocolFile.copyProtocolToResults(resultPathFull, protocolFileName, protocolFileNamePathFull);

            String[] miscFilesFullPaths = Global.getGlobal().getMiscFilesFullPaths();

            if(miscFilesFullPaths != null && miscFilesFullPaths.length > 0)
                ProtocolFile.copyMiscFilesToResults(Global.getGlobal().getMiscFilesResultPath(resultPathFull),miscFilesFullPaths);

            ProtocolWriter.write((protocolFilePathName));

            if(Search.getRunSimulationsInline())
                iDynoMiCSRunner.runSimulation(resultPathFull, protocolFilePathName, false, true);

            fullResultPaths.add(resultPathFull);
            protocolFilePathNames.add(protocolFilePathName);


        }
        return new Runner(resultPath, protocolFileName, fullResultPaths, protocolFilePathNames, isTarget, (int)endOfSimulation.getValue());
    }


    private static void randomizeSeed() {
        Random r = new Random();
        int rInt = r.nextInt(100);

        ParamNumValue pnv = new ParamNumValue(rInt);

        randomSeedParam.setValue(pnv);
        randomSeedParam.writeMasterRef();


    }

    private static void writeMasterParamRefs(List<ParamNum> paramSet) {

        if (paramSet != null) {
            for (ParamNum pn : paramSet) {
                pn.writeMasterRef();

            }
        }
    }

}
