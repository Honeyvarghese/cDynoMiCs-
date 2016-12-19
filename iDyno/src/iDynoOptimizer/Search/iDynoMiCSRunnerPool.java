package iDynoOptimizer.Search;

import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Chris on 5/13/2015.
 */
public class iDynoMiCSRunnerPool {


    private static final String splitChar = "%";

    private static List<Runner> runs;

    private static final String rosterFilePath       = Global.getGlobal().getRosterFilePath();
    private static final String rosterMasterFilePath = Global.getGlobal().getRosterMasterFilePath();
    private static final String lockFilePath         = Global.getGlobal().getLockFilePath();
    private static final File   roster               = new File(rosterFilePath);
    private static final File   rosterMaster         = new File(rosterMasterFilePath);
    private static File rosterMasterBackUp;

    private static final File lockFile                       = new File(lockFilePath);
    private static final int  millWaitForIOException         = 1000;
    private static       int  numRetryAttemptsForIOException = 2500;

    //the number of lines in the regular roster at any given time will be numRepeats * maxRunsInRoster  + numRepeats
    private static final int maxRunsInRoster = 100;


    private static boolean haveLock;

    private static RandomAccessFile raf;
    private static RandomAccessFile rafRosterMaster;
    private static RandomAccessFile rafRosterMasterBackup;


    public static void waitWithCatch(int millSecToWait, String callerMethodName) {
        try {

            MyPrinter.Printer().printTier2ln("Waiting for " + millSecToWait + ". Called from " + callerMethodName);

            Thread.sleep(millSecToWait);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public static void waitForIOException() {
        numRetryAttemptsForIOException--;

        if (numRetryAttemptsForIOException <= 0) {
            MyPrinter.Printer().printTier1ln("The number of retry attempts for an IO exception was JUST EXCEEDED");
        }

        iDynoMiCSRunnerPool.waitWithCatch(millWaitForIOException, "waitForIOException");
    }


    private static boolean getRosterLock() {
        return getRosterLock(0);
    }

    private static boolean getRosterLock(int milliSecWait) {

        boolean firstTry = true;
        while ((firstTry || milliSecWait > 0) && !haveLock) {

            MyPrinter.Printer().printTier2ln("Attempting to acquire roster lock");

            firstTry = false;
            if (!lockFile.exists()) {

                MyPrinter.Printer().printTier2ln("The lock file doesn't exist");

                if (haveLock) {
                    haveLock = false;
                    try {
                        throw new Exception("The lock file doesn't exist, but I think I have a lock. Something is wrong");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                while (numRetryAttemptsForIOException > 0) {
                    try {

                        MyPrinter.Printer().printTier2ln("Attempting to create lock file");
                        haveLock = lockFile.createNewFile();
                        break;
                    } catch (IOException e) {
                        MyPrinter.Printer().printErrorln("Couldn't create lock file. Remaining retry attempts: " + numRetryAttemptsForIOException + ". Waiting for " + millWaitForIOException);
                        // e.printStackTrace();
                        haveLock = false;
                        raf = null;
                        waitForIOException();
                    } catch (SecurityException e) {
                        haveLock = false;
                        raf = null;
                        throw new SecurityException("Unable to create lock file because of a security exception");
                    }
                }
            } else {
                MyPrinter.Printer().printTier2ln("The lock file DOES exist. So we can't write the roster right now");

                if (milliSecWait > 0) waitWithCatch(milliSecWait, "getRosterLock");
            }

        }
        if (haveLock) {

            MyPrinter.Printer().printTier2ln("Roster lock acquired");

            while (numRetryAttemptsForIOException > 0) {
                try {
                    MyPrinter.Printer().printTier2ln("Creating random access file of roster");
                    raf = new RandomAccessFile(roster, "rw");
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    raf = null;
                    waitForIOException();
                }
            }
        }
        return haveLock;
    }

    private static boolean releaseRosterLock(boolean definitelyHaveLock) {
        if (lockFile.exists()) {
            try {
                while (!lockFile.delete()) {
                    iDynoMiCSRunnerPool.waitWithCatch(500, "releaseRosterLock");
                }
            } catch (SecurityException se) {
                throw new SecurityException("Unable to delete lock file because of a security exception");
            }

            haveLock = false;
            while (numRetryAttemptsForIOException > 0) {
                try {
                    raf.close();
                    break;
                } catch (IOException e) {
                    e.printStackTrace();
                    waitForIOException();
                }
            }

        } else if (definitelyHaveLock) {
            try {
                throw new Exception("The lock file doesn't exist, but it should because I'm supposed to have a lock on the roster!");
            } catch (Exception e) {
                e.printStackTrace();
            }


        }
        haveLock = false;

        return !haveLock;
    }


    private static void createRosterMaster() {
        //  if (rafRosterMaster == null) {
        try {

            MyPrinter.Printer().printTier2ln("Creating the random access file of the roster master");
            rafRosterMaster = new RandomAccessFile(rosterMaster, "rw");
        } catch (IOException e) {
            MyPrinter.Printer().printErrorln("Couldn't create the master roster file and/or the random access file to access it");
        }
        //   }
    }


    static class iDynoMiCSRunnerThread {
        private String  resultPath;
        private String  protocolFileName;
        private boolean writeEnvOutput;
        private boolean writePOV;

        public iDynoMiCSRunnerThread(String resultPath, String protocolFileName, boolean writeEnvOutput, boolean writePOV) {


            this.resultPath = resultPath;
            this.protocolFileName = protocolFileName;
            this.writeEnvOutput = writeEnvOutput;
            this.writePOV = writePOV;
            updateRoster(resultPath, Runner.RunnerState.inProgress);
        }

        public void run() {

            MyPrinter.Printer().printTier1ln("STARTING SIMULATION - " + resultPath);
            iDynoMiCSRunner.runSimulation(resultPath, protocolFileName, writeEnvOutput, writePOV);
            MyPrinter.Printer().printTier1ln("FINISHED SIMULATION - " + resultPath);

            updateRoster(resultPath, Runner.RunnerState.finished);
        }


    }


    private static List<String> getDonePaths() {


        MyPrinter.Printer().printTier2ln("Entering getDonePaths");


        List<String> done;
        while (numRetryAttemptsForIOException > 0) {
            done = new ArrayList<>();
            if (!getRosterLock()) {
                MyPrinter.Printer().printTier2ln("Couldn't get roster lock at getDonePaths. Returning empty list");
                return done;
            }

            StringBuilder sb = new StringBuilder();

            try {
                for (String line; (line = raf.readLine()) != null; ) {

                    String[] parts = line.split(splitChar);
                    if (parts[3].equalsIgnoreCase(String.valueOf(Runner.RunnerState.finished.ordinal()))) {

//                        if (parts[0].endsWith("\\"))
//                            parts[0] = parts[0].substring(0, parts[0].length() - 1);

                        MyPrinter.Printer().printTier2ln("Adding " + parts[0] + " as done");
                        done.add(parts[0]);
                    }
                    //store the lines not yet done and re-write them to the file so that the finished ones get removed
                    else {
                        sb.append(line).append("\n");
                    }

                }
                raf.setLength(0);
                raf.write(sb.toString().getBytes());

                releaseRosterLock(true);
                return done;


            } catch (IOException e) {
                MyPrinter.Printer().printErrorln("Unable to get the finished simulations and/or delete them from the roster");
                e.printStackTrace();
                releaseRosterLock(false);
                waitForIOException();

            }

        }
        MyPrinter.Printer().printTier2ln("The number of retry attempts for an IOException was exceeded when calling checkRosterIfAllDone. Returning null.");
        return null;
    }


    private static boolean checkRosterMasterIfALlDone() throws IOException {

        MyPrinter.Printer().printTier2ln("Entering checkRosterMasterIfALlDone");

        try {

            for (String line; (line = rafRosterMaster.readLine()) != null; ) {
                if (!line.split(splitChar)[3].equalsIgnoreCase(String.valueOf(Runner.RunnerState.finished.ordinal()))) {

                    return false;
                }
            }
        } catch (IOException e) {
            throw new IOException("Unable to check the master roster to see if all simulations have finished");
        }

        return true;
    }


    private static boolean checkRosterIfAllDone(int milliSecWait) {

        MyPrinter.Printer().printTier2ln("Entering checkRosterIfAllDone");

        while (numRetryAttemptsForIOException > 0) {

            if (!getRosterLock(milliSecWait)) {
                MyPrinter.Printer().printTier2ln("Couldn't get roster lock at checkRosterIfAllDone. Returning false");
                return false;
            }

            try {

                for (String line; (line = raf.readLine()) != null; ) {
                    if (!line.split(splitChar)[3].equalsIgnoreCase(String.valueOf(Runner.RunnerState.finished.ordinal()))) {

                        releaseRosterLock(true);
                        MyPrinter.Printer().printTier2ln("Exiting checkRosterIfAllDone. Returning false");
                        return false;
                    }

                }
                releaseRosterLock(true);


                MyPrinter.Printer().printTier2ln("Exiting checkRosterIfAllDone. Returning true");
                return true;

            } catch (IOException e) {

                MyPrinter.Printer().printErrorln("Unable to check the roster to see if all the simulations have finished");

                e.printStackTrace();
                releaseRosterLock(false);
                waitForIOException();
            }
        }

        MyPrinter.Printer().printTier2ln("The number of retry attempts for an IOException was exceeded when calling checkRosterIfAllDone. Returning null.");
        return false;
    }

    //input is the amount of time in milliseconds to wait between attempts to get a lock on the roster
    public static boolean checkRostersIfAllDone(int milliSecWaitForRosterLock) throws IOException {
        return checkRosterMasterIfALlDone() && checkRosterIfAllDone( milliSecWaitForRosterLock);
    }


    private static void appendRoster(String toAppend, boolean assumeLockAcquired) throws IOException {

        MyPrinter.Printer().printTier2ln("Entering appendRoster");

        boolean firstTry = true;
        while ((firstTry || !assumeLockAcquired) && numRetryAttemptsForIOException > 0) {

            firstTry = false;
            if (!assumeLockAcquired && !getRosterLock()) {
                MyPrinter.Printer().printTier2ln("We're not assuming the lock is required, so we tried to get it, but couldn't. Exiting method.");
                return;
            }

            if (!assumeLockAcquired) {

                MyPrinter.Printer().printTier2ln("We're not assuming the lock is required and we acquired it, so we're now appending the roster");

                try {
                    raf.seek(raf.length());
                    raf.write(toAppend.getBytes());
                    releaseRosterLock(true);
                    return;
                } catch (IOException e) {

                    MyPrinter.Printer().printErrorln("Unable to append the roster");
                    e.printStackTrace();
                    releaseRosterLock(false);
                    waitForIOException();
                }
            } else {

                MyPrinter.Printer().printTier2ln("We're assuming the lock is acquired, so we're not appending the roster");

                raf.seek(raf.length());
                raf.write(toAppend.getBytes());
            }
        }
    }

    // X is number of pending simulations the roster should have at any given time
    // 1) append all to master roster
    // 2) check roster for how many sims it has finished - F. Store the lines of these F simulations (exact string) and delete them from the roster
    // 3) Move F number of pending simulations (0) from the master roster to the roster (and delete them from the master)
    // 4) Add the F stored simulation strings to the master


    private static void appendRosterMaster(String toAppend) throws IOException {
        createRosterMaster();

        try {
            rafRosterMaster.seek(rafRosterMaster.length());
            rafRosterMaster.write(toAppend.getBytes());
        } catch (IOException e) {
            throw new IOException("Unable to append to the master roster");
        }
    }


    private static List<String> getFPendingFromMasterRoster(int F) {


        MyPrinter.Printer().printTier2ln("Entering getFPendingFromMasterRoster");

        createRosterMaster();
        List<String> pendingSims = new ArrayList<>();

        try {


            StringBuilder sb = new StringBuilder();

            for (String line; (line = rafRosterMaster.readLine()) != null; ) {
                if (line.split(splitChar)[3].equalsIgnoreCase(String.valueOf(Runner.RunnerState.pending.ordinal())) && F-- > 0) {

                    String[] parts = line.split(splitChar);

//                    if (parts[0].endsWith("\\"))
//                        parts[0] = parts[0].substring(0, parts[0].length() - 1);

                    MyPrinter.Printer().printTier2ln("Found " + parts[0] + " pending simulation in the master");


                    pendingSims.add(parts[0]);


                } else {
                    sb.append(line).append("\n");
                }


            }

            rafRosterMaster.setLength(0);
            rafRosterMaster.write(sb.toString().getBytes());
        } catch (IOException e) {
            MyPrinter.Printer().printErrorln("Unable to get F pending simulations from the master roster");
            e.printStackTrace();
        }

        return pendingSims;
    }

    public static List<Runner> handleFinishedSimsInRosters(int miliSecWait) throws IOException {


        MyPrinter.Printer().printTier2ln("Entering handleFinishedSimsInRosters");


        //gets the simulations in the roster that have finished and deletes them from the roster
        List<String> finishedStrings = getDonePaths();

        MyPrinter.Printer().printTier2ln("Found " + finishedStrings.size() + " new finished simulations");


        //runs that have at least one random repeat finished
        List<Runner> atLeastPartiallyFinished = new ArrayList<>();

        //runs that have been completely finished
        List<Runner> fullyFinished = new ArrayList<>();

        if (finishedStrings == null) return null;
        if (finishedStrings.isEmpty()) return fullyFinished;


        int F = finishedStrings.size();

        //gets F pending simulations from the master roster and deletes them from the master roster

        List<String> pendingStrings = getFPendingFromMasterRoster(F);


        MyPrinter.Printer().printTier2ln("Removed " + pendingStrings.size() + " pending simulations from the master roster");


        List<Runner> pendingRuns = new ArrayList<>();


        //go over each run and update its random repeats as finished or pending (if they are)
        for (Runner run : getRunnerList()) {


            //updates the repeats to finished
            for (String fin : finishedStrings) {
                for (String finishedResultPathFull : run.getRestulPathFull().keySet()) {
                    if (finishedResultPathFull.equalsIgnoreCase(fin)) {

                        if (!atLeastPartiallyFinished.contains(run)) atLeastPartiallyFinished.add(run);

                        run.getRestulPathFull().put(finishedResultPathFull, Runner.RunnerState.finished);

                    }
                }
            }

            boolean allFinished = false;
            for (String finishedResultPathFull : run.getRestulPathFull().keySet()) {

                Runner.RunnerState state = run.getRestulPathFull().get(finishedResultPathFull);
                allFinished = state == Runner.RunnerState.recorded || state == Runner.RunnerState.finished;
                if (!allFinished) break;
            }

            if (allFinished) fullyFinished.add(run);


            //updates the repeats to pending
            //if any of them are pending, adds them to a pending list so they can be added to the roster
            for (String pen : pendingStrings) {
                for (String pendingResultPathFull : run.getRestulPathFull().keySet()) {
                    if (pendingResultPathFull.equalsIgnoreCase(pen)) {

                        //we don't want to add it for each random repeat (getRestulPathFull() represents the random repeats for the given run)
                        if (!pendingRuns.contains(run)) pendingRuns.add(run);

                        //  to indicate that it needs to be added to the roster
                        run.getRestulPathFull().put(pendingResultPathFull, Runner.RunnerState.pending);

                    }
                }
            }
        }

        //adds F pending runs to the roster
        if (pendingRuns != null && !pendingRuns.isEmpty()) {
            MyPrinter.Printer().printTier2ln("Attempting to add " + pendingRuns.size() + " pending sims to the roster");

            addRunsToRosterWait(pendingRuns, miliSecWait);
        }

        //adds the finished runs (will be F of them) to the master
        if (atLeastPartiallyFinished != null && !atLeastPartiallyFinished.isEmpty()) {

            MyPrinter.Printer().printTier2ln("Attempting to add " + atLeastPartiallyFinished.size() + " partially finished runs to the master roster");
            addRunsToRosterMaster(atLeastPartiallyFinished);
        }


        return fullyFinished;


    }


    private static void updateRoster(String resultPath, Runner.RunnerState state) {


        while (numRetryAttemptsForIOException > 0) {

            if (!getRosterLock(50)) {
                MyPrinter.Printer().printTier2ln("Couldn't get roster lock at updateRoster. Exiting method");
                return;
            }

            try {
                long start = System.currentTimeMillis();
                StringBuilder sb = new StringBuilder();

                raf.seek(0);

                for (String line; (line = raf.readLine()) != null; ) {
                    if (line.split(splitChar)[0].equalsIgnoreCase(resultPath)) {
                        line = line.substring(0, line.length() - 1) + state.ordinal();
                    }
                    sb.append(line).append("\n");

                }

                raf.setLength(0);
                raf.write(sb.toString().getBytes());
                releaseRosterLock(true);

                long end = System.currentTimeMillis();
                long diff = end - start;
                MyPrinter.Printer().printTier2ln("Updating roster took " + diff);
                return;

            } catch (IOException e) {
                MyPrinter.Printer().printErrorln("Unable to update the roster");
                e.printStackTrace();
                releaseRosterLock(false);
                waitForIOException();

            }

        }


    }


    public static void RunSimulations(int millSecToWait, int retryAttempts, boolean writeEnvOutput, boolean writePOV) {

        while (retryAttempts > 0) {
            iDynoMiCSRunnerThread worker = load(writeEnvOutput, writePOV);
            if (worker != null) worker.run();

            else {
                retryAttempts--;
                //wait a specified amount of time and then start looking for more simulations to run
                waitWithCatch(millSecToWait, "RunSimulations");
            }

        }

    }

    private static iDynoMiCSRunnerThread load(boolean writeEnvOutput, boolean writePOV) {


        while (numRetryAttemptsForIOException > 0) {
            if (!getRosterLock()) {
                MyPrinter.Printer().printTier2ln("Couldn't get roster lock at load. Returning null");
                return null;
            }

            try {
                String line;
                String[] split;


                while ((line = raf.readLine()) != null) {
                    if (line.trim().isEmpty()) continue;
                    split = line.split(splitChar);
                    if (split[3].equalsIgnoreCase(String.valueOf(Runner.RunnerState.pending.ordinal()))) {
                        return new iDynoMiCSRunnerThread(split[0].trim(), split[1].trim(), writeEnvOutput, writePOV);
                    }
                }

                releaseRosterLock(true);
                return null;
            } catch (IOException e) {
                MyPrinter.Printer().printErrorln("Unable to load a simulation from the roster to run");
                e.printStackTrace();
                releaseRosterLock(false);
                waitForIOException();
            }


        }
        return null;
    }


    public static List<Runner> getRunnerList() {
        if (runs == null) runs = new ArrayList<>();

        return runs;
    }


    public static void addRun(Runner r, int millSecToWait) throws IOException {

//
//        for (String resultPathFull : r.getRestulPathFull().keySet()) {
//            r.getRestulPathFull().put(resultPathFull, Runner.RunnerState.pending);
//        }


        addRunToRosterWait(r, millSecToWait);

        //  addRunToRosterMaster(r);

        getRunnerList().add(r);
    }

    public static void addRun(List<Runner> rs, int millSecToWait) throws IOException {


        int numToAddToRosterNow = rs.size() >= maxRunsInRoster ? maxRunsInRoster : rs.size();

        int numToAddToMasterRoster = rs.size() - numToAddToRosterNow;
        MyPrinter.Printer().printTier2ln("Adding " + numToAddToRosterNow + " runs to the roster and " + numToAddToMasterRoster + " to the roster master");

        List<Runner> toAddToRosterNow = rs.subList(0, numToAddToRosterNow);


        addRunsToRosterWait(toAddToRosterNow, millSecToWait);

        if (numToAddToMasterRoster > 0) {
            List<Runner> toAddToMasterNow = rs.subList(numToAddToRosterNow, rs.size());

            addRunsToRosterMaster(toAddToMasterNow);
        }

        getRunnerList().addAll(rs);
    }

    private static void addRunsToRosterWait(List<Runner> rs, int millSecToWait) {

        MyPrinter.Printer().printTier2ln("Entered addRunsToRosterWait");

        if (rs == null || rs.isEmpty()) {
            MyPrinter.Printer().printTier2ln("The list of runs received at addRunsToRosterWait was null or empty. Exiting method.");
            return;
        }

        List<Runner> tmp = null;
        while (tmp == null) {
            tmp = addRunsToRoster(rs);
            if (tmp == null) {

                MyPrinter.Printer().printTier2ln("addRunsToRoster returned null, indicating it couldn't add the runs.");
                iDynoMiCSRunnerPool.waitWithCatch(millSecToWait, "addRunsToRosterWait");
            }
        }
    }

    private static void addRunToRosterWait(Runner r, int millSecToWait) {
        Runner tmp = null;
        while (tmp == null) {
            tmp = addRunToRoster(r);

            if (tmp == null) {
                iDynoMiCSRunnerPool.waitWithCatch(millSecToWait, "addRunToRosterWait");
            }
        }

    }

    private static List<Runner> addRunsToRoster(List<Runner> rs) {


        MyPrinter.Printer().printTier2ln("Entering addRunsToRoster");

        if (rs == null || rs.isEmpty()) {
            MyPrinter.Printer().printTier2ln("The list of runs received at addRunsToRoster was null or empty. Returning null");
            return null;
        }

        while (numRetryAttemptsForIOException > 0) {
            if (!getRosterLock()) {
                MyPrinter.Printer().printTier2ln("Couldn't get roster lock at addRunsToRoster. Returning null");
                return null;
            }

            StringBuilder sb = new StringBuilder();

            for (Runner r : rs) {
                addRunToRoster(r, sb);
            }
            try {
                appendRoster(sb.toString(), true);
                releaseRosterLock(true);
                return rs;
            } catch (IOException e) {

                MyPrinter.Printer().printErrorln("Unable to add one or more runs to the roster");
                e.printStackTrace();
                releaseRosterLock(false);
                waitForIOException();
            }


        }

        MyPrinter.Printer().printTier2ln("The number of retry attempts for an IOException was exceeded when calling addRunsToRoster. Returning null.");
        return null;
    }

    private static Runner addRunToRoster(Runner r) {
        return addRunToRoster(r, null);
    }


    private static Runner addRunToRoster(Runner r, StringBuilder sb) {

        MyPrinter.Printer().printTier2ln("Entering addRunToRoster");

        if (sb == null) {

            MyPrinter.Printer().printTier2ln("The string builder is null in addRunToRoster");

            while (numRetryAttemptsForIOException > 0) {
                try {

                    if (!getRosterLock()) {
                        MyPrinter.Printer().printTier2ln("Couldn't get roster lock at addRunToRoster. Returning null");
                        return null;
                    }
                    appendRoster(buildRosterToAppend(r, new StringBuilder()).toString(), true);
                    releaseRosterLock(true);
                    return r;
                } catch (IOException e) {
                    MyPrinter.Printer().printErrorln("Unable to add a run to the roster");
                    e.printStackTrace();
                    releaseRosterLock(false);
                    waitForIOException();
                }
            }

            MyPrinter.Printer().printTier2ln("The number of retry attempts for an IOException was exceeded when calling addRunToRoster. Returning null.");
            return null;


        } else {
            buildRosterToAppend(r, sb);
            return r;
        }

    }


    private static StringBuilder buildRosterToAppend(Runner r, StringBuilder sb) {


        MyPrinter.Printer().printTier2ln("Entering buildRosterToAppend");


        List<String> fullProtocolPaths = r.getProtocolFilePathNameFull();


        int i = 0;
        //we can rely upon getRestulPathFull() and getProtocolFilePathNameFull() being in the same order because getRestulPathFull() is a TreeMap and getProtocolFilePathNameFull() is sorted when accessed
        for (String resultPathFull : r.getRestulPathFull().keySet()) {

            Runner.RunnerState rosterState = r.getRestulPathFull().get(resultPathFull);

            if (rosterState == Runner.RunnerState.pending || rosterState == Runner.RunnerState.finished) {
                MyPrinter.Printer().printTier2ln("Adding stuff to append to roster");
                sb.append(resultPathFull).append(splitChar).append(fullProtocolPaths.get(i)).append(splitChar).append(r.getExpectedItrCount()).append(splitChar).append(rosterState.ordinal()).append("\n");

            }

            //update the state
            if (rosterState == Runner.RunnerState.pending) {
                MyPrinter.Printer().printTier2ln("Changing state of " + resultPathFull + " from pending to inProgress");
                r.getRestulPathFull().put(resultPathFull, Runner.RunnerState.inProgress);
            } else if (rosterState == Runner.RunnerState.finished) {
                MyPrinter.Printer().printTier2ln("Changing state of " + resultPathFull + " from finished to recorded");
                r.getRestulPathFull().put(resultPathFull, Runner.RunnerState.recorded);
            }

            i++;

        }
        return sb;

    }


    private static void addRunsToRosterMaster(List<Runner> runs) throws IOException {
        StringBuilder sb = new StringBuilder();

        for (Runner run : runs) {
            buildRosterToAppend(run, sb);

        }
        appendRosterMaster(sb.toString());

    }


}