package iDynoOptimizer;

import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Search.iDynoMiCSRunnerPool;

import java.io.File;
import java.io.IOException;

/**
 * Created by Chris on 9/2/2015.
 */
public class SimRunnerPool {


    private static final String r   = "r";
    private static final String rm  = "rm";
    private static final String l   = "l";
    private static final String mw  = "mw";
    private static final String ra  = "ra";
    private static final String weo = "weo";
    private static final String wp  = "wp";

    /*
 inputs are

     @@STRING - r - full roster path (full path and file name)
     @@STRING - rm - full master roster path
     @@STRING - l - full lock path (full path and file name)

     @@INTEGER - mw (optional) - number of milliseconds to wait between attempts to look for another simulation to run
         default: 1000
     @@INTEGER - ra - (optional) number of retry attempts before shutting down
         default: 500

     @@BOOLEAN - weo - (optional) should simulations run by this slave write environmental output?
         default: false
     @@BOOLEAN - wp - (optional) should simulations run by this slave write pov output
        default: true
  */
    public static void main(String[] args) {
        String rosterPath       = "";
        String rosterMasterPath = "";
        String lockPath         = "";

        boolean writeEnvOutput = false;
        boolean writePOV       = true;

        int milliSecToWait = 1000;
        int retryAttempts  = 500;
        if (args == null || args.length < 3)
            throw new IllegalArgumentException("You must specify the path to the roster file, the master roster file, and  the lock file");


        String   name;
        String   value;
        String[] parts;

        for (String s : args) {
            parts = s.split("=");
            name = parts[0].trim();
            value = parts[1].trim();

            if (name.equalsIgnoreCase(r)) {
                rosterPath = value;


            } else if (name.equalsIgnoreCase(rm)) {
                rosterMasterPath = value;
            } else if (name.equalsIgnoreCase(l)) {
                lockPath = value;


            } else if (name.equalsIgnoreCase(weo)) {
                writeEnvOutput = Boolean.parseBoolean(value);
            } else if (name.equalsIgnoreCase(wp)) {
                writePOV = Boolean.parseBoolean(value);
            } else if (name.equalsIgnoreCase(mw)) {
                try {
                    milliSecToWait = Integer.parseInt(value);
                } catch (NumberFormatException e) {

                    MyPrinter.Printer().printTier1ln("Millisecond wait not an integer. 1000 millisecond wait will be used.");
                }
            } else if (name.equalsIgnoreCase(ra)) {
                try {
                    retryAttempts = Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    MyPrinter.Printer().printTier1ln("Retry attempts not an integer. 500 retry attempts will be used.");
                }
            } else {
                MyPrinter.Printer().printTier1ln("Argument " + s + " not recognized. It will be ignored.");
            }


        }


        File roster = new File(rosterPath);

        if (!roster.exists()) {
            throw new IllegalArgumentException("Invalid roster file path");
        }

        File rosterMaster = new File(rosterMasterPath);

        if (!rosterMaster.exists()) {
            throw new IllegalArgumentException("Invalid master roster path");
        }


        if (lockPath.isEmpty()) {
            throw new IllegalArgumentException("Invalid/Empty lock file path");
        }


        Global.getGlobal().setRosterFilePath(rosterPath);
        Global.getGlobal().setRosterMasterFilePath(rosterMasterPath);
        Global.getGlobal().setLockFilePath(lockPath);


        iDynoMiCSRunnerPool.RunSimulations(milliSecToWait, retryAttempts, writeEnvOutput, writePOV);

        MyPrinter.Printer().printTier1ln("RUNNER POOL/SLAVE IS ALL DONE!");


    }
}
