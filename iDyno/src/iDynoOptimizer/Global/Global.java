package iDynoOptimizer.Global;

import java.io.File;
import java.util.List;

/**
 * Created by Chris on 11/15/2014.
 */


/*
This is a singleton that contains paths for the input and output of the simulations
 */
public class Global {


    private static Global global;
    private static final String miscFilesFolderName = "miscFiles";
    public static final  String runNumPH            = "?R";
    public static final  String stepNumPH           = "?S";
    public static final  String initCondPH          = "?IC";
    public static final  String stepPath            = "step-" + stepNumPH;
    private static final String resultPath          = "results(" + runNumPH + ")";
    public static final  String resultPathFull      = initCondPH + File.separator + resultPath;
    public static final  String paramsFile          = "paramsFile";


    public String rosterFilePath;
    public String rosterMasterFilePath;
    public String lockFilePath;


    private Global() {

        setExperimentFolder(null);
    }

    private Global(String fileInputPath, String fileOutputPath, String testProtocolFolder, String targetProtocolFileName, String sweepName) {


        setTestProtocolFolder(testProtocolFolder);
        setTargetProtocolFileName(targetProtocolFileName);

        setFileInputPath(fileInputPath);
        setFileOutputPath(fileOutputPath);

        setSweepName(sweepName);
        setExperimentFolder(null);
    }


    public static Global getGlobal() {
        if (global == null) global = new Global();
        return global;
    }


    public static Global getGlobal(String fileInputPath, String fileOutputPath) {
        getGlobal(fileInputPath, fileOutputPath, "", "", "");
        return global;
    }


    public static Global getGlobal(String fileInputPath, String fileOutputPath, String testProtocolFolder, String targetProtocolFileName, String sweepName) {
        if (global == null)
            global = new Global(fileInputPath, fileOutputPath, testProtocolFolder, targetProtocolFileName, sweepName);
        else {


            global.setTestProtocolFolder(testProtocolFolder);
            global.setTargetProtocolFileName(targetProtocolFileName);

            global.setFileOutputPath(fileOutputPath);
            global.setFileInputPath(fileInputPath);

            global.setSweepName(sweepName);
        }

        return global;
    }


    /**
     * ******************************BEGIN INPUTS********************************
     */


    private String protocolFilesSourcePath;
    private String testProtocolFolder     = "testProtocol";
    private String targetProtocolFileName = "targetProtocol.xml";
    private String sweepName;

    private String   sourceTargetProtocolFileName;
    private String   sourceTestProtocolFolder;
    private String[] miscFilesFullPaths;

    public String getInputPath() {
        return protocolFilesSourcePath;
    }

    private void setFileInputPath(String fileInputPath) {

        protocolFilesSourcePath = fileInputPath;
        sourceTargetProtocolFileName = protocolFilesSourcePath + File.separator + targetProtocolFileName;
        sourceTestProtocolFolder = protocolFilesSourcePath + File.separator + testProtocolFolder;
    }

    private void setTestProtocolFolder(String f) {
        if (!f.isEmpty()) testProtocolFolder = f;
    }

    private void setTargetProtocolFileName(String f) {
        if (!f.isEmpty()) targetProtocolFileName = f;
    }


    public void setMiscFilesFullPaths(String[] paths) {
        miscFilesFullPaths = paths;
    }

    public String getSourceTargetProtocolFileName() {
        return sourceTargetProtocolFileName;
    }

    public String getSourceTestProtocolFolder() {
        return sourceTestProtocolFolder;
    }

    public String getTestProtocolFolder() {
        return getTestPath() + File.separator + testProtocolFolder;
    }

    public File[] getTestProtocolFiles() {
        File folder = new File(getTestProtocolFolder());
        return folder.listFiles();
    }

    public String getTargetProtocolFileNamePathFull() {
        return getTargetPath() + File.separator + targetProtocolFileName;
    }

    public String getTargetProtocolFileName() {
        return targetProtocolFileName;
    }

    public String[] getMiscFilesFullPaths() {
        return miscFilesFullPaths;
    }


/*********************************END INPUTS*********************************/


    /**
     * ******************************BEGIN OUTPUTS********************************
     */
    private String fileOutputPath;
    private String experimentFolder;


    public String getFileOutputPath() {
        return fileOutputPath;

    }

    private void setFileOutputPath(String path) {

        fileOutputPath = path;
    }


    private String determineExpFolder() {
        String s = fileOutputPath + File.separator + sweepName + "-";

        int    i    = 1;
        String temp = s + i;
        while (new File(temp).exists()) {
            i++;
            temp = s + i;
        }
        return temp;

    }


    public void setExperimentFolder(String expPath) {

        if (expPath == null || expPath.isEmpty()) {
            experimentFolder = determineExpFolder();
        } else if (new File(expPath).exists()) experimentFolder = expPath;
        else experimentFolder = fileOutputPath + File.separator + expPath;


    }

    private void setSweepName(String sweepname) {
        if (!sweepname.isEmpty()) this.sweepName = sweepname;
        else if (!targetProtocolFileName.isEmpty()) this.sweepName = targetProtocolFileName;
        else this.sweepName = testProtocolFolder;
    }


    public String getMainOutput() {
        return experimentFolder + File.separator + "mainOutput";

    }

    public String getClimberOutPath() {
        return getMainOutput() + File.separator + "climber";
    }

    public String getExperimentFolder() {
        return experimentFolder;
    }


    public String getTargetPath() {
        return experimentFolder + File.separator + "target";

    }

    public String getTestPath() {
        return experimentFolder + File.separator + "test";

    }

    public String getRosterFilePath() {
        return rosterFilePath;
    }

    public String getRosterMasterFilePath() {
        return rosterMasterFilePath;
    }

    public String getLockFilePath() {
        return lockFilePath;
    }

    public void setRosterFilePath(String rosterFilePath) {
        this.rosterFilePath = rosterFilePath;
    }

    public void setRosterMasterFilePath(String rosterMasterFilePath) {
        this.rosterMasterFilePath = rosterMasterFilePath;
    }

    public void setLockFilePath(String lockFilePath) {
        this.lockFilePath = lockFilePath;
    }

    public String getTestStepPathPartial(int stepNumber) {

        return getTestPath() + File.separator + stepPath.replace(Global.getGlobal().stepNumPH, String.valueOf(stepNumber));
    }

    public String getTestResultPathPartial(int runNum, int stepNumber, String initCond) {

        return getTestStepPathPartial(stepNumber) + File.separator + resultPathFull.replace(Global.getGlobal().initCondPH, initCond).replace(Global.getGlobal().runNumPH, String.valueOf(runNum));
    }

    public String getMiscFilesResultPath(String resultPath)
    {
        return resultPath + File.separator + miscFilesFolderName;
    }

    public String getTargetResultPath() {
        return getTargetPath() + File.separator + "results";

    }


    /**
     * ******************************END OUTPUTS********************************
     */


}
