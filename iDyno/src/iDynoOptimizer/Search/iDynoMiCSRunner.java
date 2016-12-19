package iDynoOptimizer.Search;

/**
 * Created by Chris on 11/19/2014.
 */

import simulator.Simulator;
import utils.ExtraMath;
import utils.LogFile;

import java.io.File;


/*
This code is based on the idyno.Idynomics class of iDynoMiCS
 */
public class iDynoMiCSRunner {


    private static Simulator aSimulator;


    /**
     * runs a simulation using
     * protocolFile as the location and name of the protocol file
     * and
     * resultPathFull as the result directory
     */
    public static void runSimulation(String resultPath, String protocolFile, boolean writeEnvOutput, boolean writePOV) {


        if (initSimulation(resultPath, protocolFile, writeEnvOutput, writePOV)) {

            launchSimulation();
            LogFile.writeLog("========> " + resultPath + " <========");

            LogFile.closeFile();

        }


    }


    public static boolean initSimulation(String resultPath, String protocolFile, boolean writeEnvOutput, boolean writePOV) {


        // Create the simulator
        try {


            LogFile.openFile(resultPath);
            new File(resultPath + File.separator + "lastIter").mkdirs();
            new File(resultPath + File.separator + "SoluteConcentration").mkdirs();
            new File(resultPath + File.separator + "Molecules").mkdirs();
            new File(resultPath + File.separator + "MovedCells").mkdirs();
            new File(resultPath + File.separator + "povray").mkdirs();
            new File(resultPath + File.separator + "TightJunctionCount").mkdirs();


            //takes protocol file and results path as parameters
            aSimulator = new Simulator(protocolFile, resultPath, writeEnvOutput, writePOV);

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }


    public static void launchSimulation() {
        try {
            double begin = System.currentTimeMillis();
            aSimulator.run();
            begin = Math.round(System.currentTimeMillis() - begin);

            String time = ExtraMath.toString(begin / 1e3 / 60, false);

        } catch (Exception e) {
            e.printStackTrace();

        }
    }


}




