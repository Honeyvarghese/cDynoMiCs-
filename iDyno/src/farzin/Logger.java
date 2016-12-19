package farzin;

import simulator.agent.LocatedAgent;
import utils.LogFile;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import idyno.SimTimer;

public class Logger {
    public static final boolean            writeMovementsMagnitudeFlag = false;
    public static final boolean            writeNumMovementsFlag       = false;
    public static final int                size                        = 33;
    public static final int                resolution                  = 2;
    public static final int                actualResolution            = 8;
    public static double[][]         forces                      = new double[size * resolution][size * resolution];
    //public static double [][]  tightJunctions;
    public static List<List<Double>> tightJunctions              = new ArrayList<List<Double>>();


    //NOTE, the visualizations don't work well with shoving factors < ~2.5
    public static final boolean drawingTightJunctions  = false;
    public static final boolean coloringTightJunctions = false;
    public static double[][] CDPMap;
    public static final boolean writeTightJunctionCount = false;


    public static final List<Integer> tightJunctionCounts = new ArrayList<Integer>();

    public static void addtightJunctionCount(int i, int d) {
        if (tightJunctionCounts.size() > i) tightJunctionCounts.set(i, d);
        else tightJunctionCounts.add(d);
    }


    public static Integer tightJunctionCount() {
        Integer tightJunctionCountsCount = 0;
        for (Integer d : tightJunctionCounts) {
            tightJunctionCountsCount += d;
        }
        return tightJunctionCountsCount;
    }

    public static void writeTightJunctionCount(simulator.Simulator sim, int allAgentsCount) {
        int currentIter = SimTimer.getCurrentIter();
        int countTj     = tightJunctionCount();
        int ratio       = countTj / allAgentsCount;
        try {
            FileOutputStream fs = new FileOutputStream(sim.getResultPath() + File.separator + "TightJunctionCount" + File.separator + "count.txt", true);
            String output = currentIter + "\n";
            output += "Total Tight Junctions:" + tightJunctionCount() + "\n";
            output += "Total Particles: " + allAgentsCount + "\n";
            output += "Ratio : " + ratio + "\n\n";
            fs.write(output.getBytes());
            fs.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }


    //record of tight junctions used for visualizing particle forces changed from an array to List<List<Double>> so it can record new tight junctions formed from growth
    //Chris Johnson 4/8/2014

    public static void initTightJunctionsArray(int agentNums) {
        tightJunctions = new ArrayList<List<Double>>();
        reSizeTightJunctionsArray(agentNums);

//        if(!tightJunctionsInitilized) reSizeTightJunctionsArray(agentNums);
//        tightJunctionsInitilized = true;
    }

    private static void reSizeTightJunctionsArray(int agentNums) {
        int temp = agentNums;
        while (temp-- > 0) {
            tightJunctions.add(new ArrayList<Double>(agentNums));
        }

        reSizeInnerTightJunctionsInnerLists(tightJunctions.size());

        //  tightJunctions=new double [agentNums][agentNums];
    }

    private static void reSizeInnerTightJunctionsInnerLists(int agentNums) {
        for (List l : tightJunctions) {
            while (l.size() < agentNums) {
                l.add(0.0);
            }
        }
    }

    private static void reSizeTightJunctionsArray(int id1, int id2) {
        int bigger = Math.max(id1, id2);
        if (tightJunctions.size() < bigger + 1) reSizeTightJunctionsArray(tightJunctions.size());
    }

    public static void addModifyTightJunctionRecord(int id1, int id2, double magnitude) {
        reSizeTightJunctionsArray(id1, id2);
        double currentValue = tightJunctions.get(id1).get(id2);
        tightJunctions.get(id1).set(id2, currentValue + magnitude);
    }

    public static void setTightJunctionRecord(int id1, int id2, double magnitude) {
        reSizeTightJunctionsArray(id1, id2);
        tightJunctions.get(id1).set(id2, magnitude);
    }

    //	public static void recordTightMovements(int srcId,int destId, double magnitude)
//	{
//		tightJunctions[srcId][destId]+=magnitude;
//	}
//
    public static void detailedNumberofMovements(double[] moves, simulator.Simulator sim) {
        int currentIter = SimTimer.getCurrentIter();
        try {
            FileOutputStream fs = new FileOutputStream(sim.getResultPath() + File.separator + "MovedCells" + File.separator + "MovedCells" + currentIter + ".txt", true);
            String output = "";//= Integer.toString(currentIter);
            //output+="\n";

            for (int i = 0; i < moves.length; i++)
                if (moves[i] > 1)
                    output += Double.toString(moves[i]) + " ";
            fs.write(output.getBytes());
            fs.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }

    public static void iterationNumberWriter(simulator.Simulator sim) {
        int currentIter = SimTimer.getCurrentIter();
        try {
            FileOutputStream fs = new FileOutputStream(sim.getResultPath() + File.separator + "lastIter" + File.separator + "iteration.txt", false);
            String output = "";//= Integer.toString(currentIter);
            //output+="\n";


            output = Integer.toString(currentIter);
            fs.write(output.getBytes());
            fs.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }


    /**
     * @param me
     * @param neighbor
     * @param magnitude
     *
     */
    public static void addMovementMagnitudes(LocatedAgent me, LocatedAgent neighbor, double magnitude) {
        int xMe = (int) Math.round(me._location.x) * resolution / actualResolution;
        int yMe = (int) Math.round(me._location.y) * resolution / actualResolution;

        int xNeighbor = (int) Math.round(neighbor._location.x) * resolution / actualResolution;
        int yNeighbor = (int) Math.round(neighbor._location.y) * resolution / actualResolution;


        int fullSize = size * resolution;
        if (xMe == fullSize) xMe--;
        if (yMe == fullSize) yMe--;
        if (xNeighbor == fullSize) xNeighbor--;
        if (yNeighbor == fullSize) yNeighbor--;

        forces[xMe][yMe] += magnitude;
        forces[xNeighbor][yNeighbor] += magnitude;
    }


    public static void writeMovementMagnitudes(simulator.Simulator sim) {
        int currentIter = SimTimer.getCurrentIter();
        try {

            FileOutputStream fs = new FileOutputStream(sim.getResultPath() + File.separator + "SoluteConcentration" + File.separator + "Forces solute " + LogFile.numberToString(currentIter) + ".txt", true);
            StringBuilder output = new StringBuilder();//= Integer.toString(currentIter);
            //output+="\n";

            int end = size * resolution;

            for (int i = 0; i < end; i++) {
                for (int j = 0; j < end; j++)
                    output.append(Double.toString(forces[i][j])).append("\t");
                output.append("\n");
            }
            fs.write(output.toString().getBytes());
            fs.close();
            forces = new double[end][end];
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public static void detailedTuringValue(double[] values, simulator.Simulator sim, double dt) {
        int currentIter = SimTimer.getCurrentIter();
        try {
            FileOutputStream fs = new FileOutputStream(sim.getResultPath() + File.separator + "TuringValues" + currentIter + "_" + Double.toString(dt) + ".txt", true);
            String output = "";//= Integer.toString(currentIter);
            //output+="\n";

            for (int i = 0; i < values.length; i++)
                output += Double.toString(values[i]) + "\n";
            fs.write(output.getBytes());
            fs.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


    public static void meanOfTuringValue(double mean, simulator.Simulator sim) {
//		int currentIter = SimTimer.getCurrentIter();
        try {
            FileOutputStream fs = new FileOutputStream(sim.getResultPath() + File.separator + "meanOfTuring.txt", true);
            String output = Double.toString(mean) + "\n";

            fs.write(output.getBytes());
            fs.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }


    }


}
