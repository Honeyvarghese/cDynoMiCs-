package iDynoOptimizer.Results.Feature;

import iDynoOptimizer.Global.ExtraMath;
import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Results.Agent.Agent;
import iDynoOptimizer.Results.Agent.Grid;
import iDynoOptimizer.Results.Agent.Location;
import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Names.FeatureName;
import iDynoOptimizer.Results.Feature.Names.FeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.NumericFeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.SpatialNumericFeatureNameIteration;
import iDynoOptimizer.Results.IterationResult;
import iDynoOptimizer.Search.SimulationIteration;

import java.util.*;

/**
 * Created by Chris on 4/16/2015.
 * <p>
 * A representation of the simulation grid at one iteration, i.e., a list of the agents preset at each grid site for the given iteration
 */
public class SpatialIterationFeature extends IterationFeature {


    public enum AgentTypeChoice {
        Alive,
        Dead,
        Both
    }


    Set<FeatureNameIteration>                   featureNamesToUse;
    Map<FeatureNameIteration, IterationFeature> featuresUsed; //contains all of the iteration features used

    private Grid grid;


    public class AgentList {
        private List<Agent> agents = new ArrayList<Agent>();

        public void add(Agent a) {
            agents.add(a);
        }

        public List<Agent> getAgents() {
            return agents;
        }

    }


    /*
        The list of grids, one from each random seed repeat
        A grid is a representation of the simulation grid at stores a list of the agents at each grid point
     */
    private List<AgentList[][][]> gridList = new ArrayList<AgentList[][][]>();


    /*
        A list of maps from the agent's birth id(unique id) to the agent itself
        Each map is from a different random repeat
     */
    private List<Map<Integer, Agent>> agentAtMap = new ArrayList<Map<Integer, Agent>>();

    /*
        Allows this class to represent a finer grid that the one the simulation itself represents (not sure if this will be accurate)
        So far have just used resUp = 1
     */
    private double resUp;

    /*
    The grid dimensions.
     */
    private int    nI; //Size of X (vertical dimension)
    private int    nJ; //Size of Y (horizontal dimension)
    private int    nK; //Size of Z (maxDepth dimension)
    private int    nR; //number of random repeats;
    private double resolution;

    public SpatialIterationFeature(SimulationIteration mySimulationIteration, int resolutionUpgrade, AgentTypeChoice agentTypeChoice, Set<FeatureNameIteration> features) {
        super(mySimulationIteration, SpatialNumericFeatureNameIteration.spatialIteration);

        //increases the resolution of the grid
        //this should be in the range [1,resolution], where resolution is the resolution of the simulation
        //if it equals 1, then the grid size used for calculating the features will be the original size of the grid
        //if it equals resolution, then each grid box will be 1um^2 or (^3) for 2D or 3D, respectively
        this.resUp = resolutionUpgrade;

        this.featureNamesToUse = features;
        featuresUsed = new HashMap<>();

        mySimulationIteration.readResults(agentTypeChoice);


    }


    @Override
    public double calulateError(Feature f, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ) {

//        SpatialIterationFeature compareTo = (SpatialIterationFeature) f;
//        setErrorCalculation(errorCalculation);
//        //this object is the target or "actual"/correct values
//        setError(SpatialIterationFeature.calculateSpatialError(compareTo.getMassGrid(), getMassGrid(), errorCalculation));
//        return getError();


        return 0;
    }


    @Override
    public void calculateAverage() {


    }


    @Override
        /*
        Extracts the data from the iteration this class represents
        Populates the grid list and the map list
         */
    public void extract() {


        List<IterationResult> irs = getSimResults();


        grid = irs.get(0).getGrid(); //all repeats should have the same grid dimensions. Use the first one to determine what those are

        //increase the resolution so each grid block represents 1um on a side
        // resUp = grid.getResolution();

        //resUp = 4.5;
        resolution = grid.getResolution() / resUp;

        AgentList[][][] agentListAll;

        nR = irs.size();

        //adding +1 because normally this method is fed padded locations so it subtracts 1
        //but here it's not being fed padded values, so the +1 makes up for the subtraction


        if (nI == 0) nI = getBoundaryLengthResUP(grid.getnI(), resUp);
        if (nJ == 0) nJ = getBoundaryLengthResUP(grid.getnJ(), resUp);
        if (nK == 0) nK = grid.getnK() > 1 ? getBoundaryLengthResUP(grid.getnK(), resUp) : 1;


        initSumFeatures();


        int i = -1;
        for (IterationResult ir : irs) //random seed repeats
        {
            agentListAll = new AgentList[nI][nJ][nK];

            Map<String, List<Agent>> allAgentsInIr = ir.getAliveandDeadSpeciesLists();


            Location l;
            int x, y, z;
            agentAtMap.add(new HashMap<Integer, Agent>());

            i++;
            for (List<Agent> agentList : allAgentsInIr.values()) //all agents in this iteration(multiple species)
            {
                for (Agent a : agentList) //all agents in this iteration of a certain species
                {


                    l = a.getLocation();
                    x = realLocToGridLoc(l.getX(), resolution, nI);
                    y = realLocToGridLoc(l.getY(), resolution, nJ);
                    z = realLocToGridLoc(l.getZ(), resolution, nK);

                    //a hack because biocellion has z== 4 for 2D instead of 0
                    if (nK == 1) z = 0;


                    if (featuresUsed.containsKey(NumericFeatureNameIteration.agentCount)) {
                        NumericIterationFeature agentCount = (NumericIterationFeature) featuresUsed.get(NumericFeatureNameIteration.agentCount);

                        agentCount.addValueAt(i, 1);

                    }


                    if (featuresUsed.containsKey(NumericFeatureNameIteration.totalMass)) {
                        NumericIterationFeature agentCount = (NumericIterationFeature) featuresUsed.get(NumericFeatureNameIteration.totalMass);

                        agentCount.addValueAt(i, a.getMass().getBiomass());

                    }


                    if (featuresUsed.containsKey(NumericFeatureNameIteration.maxLength)) {
                        NumericIterationFeature maxLength = (NumericIterationFeature) featuresUsed.get(NumericFeatureNameIteration.maxLength);
                        if (l.getY() > maxLength.getValueAt(i))
                            maxLength.setValueAt(i, l.getY());
                    }

                    if (featuresUsed.containsKey(NumericFeatureNameIteration.minLength)) {
                        NumericIterationFeature minLength = (NumericIterationFeature) featuresUsed.get(NumericFeatureNameIteration.minLength);
                        if (l.getY() < minLength.getValueAt(i))
                            minLength.setValueAt(i, l.getY());
                    }


                    if (featuresUsed.containsKey(NumericFeatureNameIteration.maxHeight)) {
                        NumericIterationFeature height = (NumericIterationFeature) featuresUsed.get(NumericFeatureNameIteration.maxHeight);

                        if (l.getX() > height.getValueAt(i))
                            height.setValueAt(i, l.getX());

                    }

                    if (featuresUsed.containsKey(NumericFeatureNameIteration.minHeight)) {
                        NumericIterationFeature height = (NumericIterationFeature) featuresUsed.get(NumericFeatureNameIteration.minHeight);

                        if (l.getX() < height.getValueAt(i))
                            height.setValueAt(i, l.getX());

                    }


                    if (featuresUsed.containsKey(SpatialNumericFeatureNameIteration.massDensity))
                        ((SpatialNumericIterationFeature) featuresUsed.get(SpatialNumericFeatureNameIteration.massDensity)).setValueAt(i, x, y, z, a.getMass().getBiomass(), true);


                    //contour requires generating a smooth block representation of the biofilm
                    //this can be done by seeing which squares have at least one cell
                    //count density is the count of cells in each block, so it serves this purpose
                    if (featuresUsed.containsKey(SpatialNumericFeatureNameIteration.countDensity))
                        ((SpatialNumericIterationFeature) featuresUsed.get(SpatialNumericFeatureNameIteration.countDensity)).setValueAt(i, x, y, z, 1, true);


                    if (featuresUsed.containsKey(SpatialNumericFeatureNameIteration.contour)) {
                        SpatialNumericIterationFeature contour = (SpatialNumericIterationFeature) featuresUsed.get(SpatialNumericFeatureNameIteration.contour);

//                        Contour is the max height (x) at every y,z location
//
//
//                        double prev = Double.MAX_VALUE;
//                        int indexToReplae = -1;
//
//                        double next = l.getX();
//                        for (int b = 0; b < SpatialNumericIterationFeature.getContourAvgSampleCount(); b++) {
//
//
//                            double current = contour.getValueAt(i, b, y, z);
//
//                            if (next > current && current < prev) {
//                                indexToReplae = b;
//                            }
//                            prev = current;
//                        }
//
//                        if (indexToReplae >= 0)
//                            contour.setValueAt(i, indexToReplae, y, z, next, false);


                        if (l.getX() > contour.getValueAt(i, 0, y, z)) {
                            contour.setValueAt(i, 0, y, z, l.getX(), false);

                        }

                    }


                    try {
                        if (agentListAll[x][y][z] == null) agentListAll[x][y][z] = new AgentList();
                    } catch (ArrayIndexOutOfBoundsException ae) {
                        throw new ArrayIndexOutOfBoundsException("Spatial Features: Agent out of grid bounds\n"
                                + "Grid Location: " + x + " " + y + " " + z + "\n"
                                + "Agent List/Grid Size: " + agentListAll.length + " " + agentListAll[0].length + " " + agentListAll[0][0].length);
                    }

                    agentListAll[x][y][z].add(a);

                    agentAtMap.get(i).put(a.getFamily().getBirthId(), a);


                }
            }


            gridList.add(agentListAll);

        }

//        if (featuresUsed.containsKey(SpatialNumericFeatureNameIteration.contour)) {
//            SpatialNumericIterationFeature contour = (SpatialNumericIterationFeature) featuresUsed.get(SpatialNumericFeatureNameIteration.contour);
//            SpatialNumericIterationFeature smoothBlock = calculateSmoothBlock();
//
//            for (int r = 0; r < nR; r++) {
//
//                for (i = 0; i < nI; i++) {
//                    for (int j = 0; j < nJ; j++) {
//                        for (int k = 0; k < nK; k++) {
//
//                            if(smoothBlock.getValueAt(r, i, j, k) >0)
//                            {
//
//                                double curr = contour.getValueAt(r, 0, j, k);
//                                double test = i;
//                                if( test > curr)
//                                    contour.setValueAt(r, 0, j, k, test, false);
//                            }
//
//                        }
//                    }
//                }
//            }
//
//
//        }

        if (featuresUsed.containsKey(SpatialNumericFeatureNameIteration.contour)) {
            SpatialNumericIterationFeature contour = (SpatialNumericIterationFeature) featuresUsed.get(SpatialNumericFeatureNameIteration.contour);
            contour = smooth(removeZeros(contour, 0, 5), 0, 5);
            featuresUsed.put(SpatialNumericFeatureNameIteration.contour, contour);

            FeatureName name = contour.getMyName();

        }

        extractSubFeatures();


    }

    //2*radius+1
    private SpatialNumericIterationFeature removeZeros(SpatialNumericIterationFeature contour, int indexOfContour, int radius) {
        SpatialNumericIterationFeature smoothBlock = new SpatialNumericIterationFeature(nR, nI + 1, nJ + 1, nK, resolution, getMySimulationIteration(), SpatialNumericFeatureNameIteration.contour, false);

        int countNothingAround = 0;
        for (int r = 0; r < nR; r++) {
            for (int j = 0; j < nJ; j++) {
                for (int k = 0; k < nK; k++) {

                    double value = contour.getValueAt(r, indexOfContour, j, k);
                    if (value == 0) {

                        int count = 0;
                        double sum = 0;
                        for (int jj = -radius; jj <= radius; jj++) {
                            for (int kk = -radius; kk <= radius; kk++) {

                                int jLoc = j + jj;
                                int kLoc = k + kk;
                                if (jLoc >= 0 && kLoc >= 0 && jLoc <= nJ && kLoc < nK) {
                                    double height = contour.getValueAt(r, indexOfContour, j + jj, k + kk);
                                    if (height != 0) {
                                        count++;
                                        sum += height;
                                    }
                                }

                            }
                        }
                        if (count != 0) value = sum / (double) count;

                    }

                    if (value == 0) countNothingAround++;

                    smoothBlock.setValueAt(r, indexOfContour, j, k, value, false);
                }
            }
        }
        return smoothBlock;
    }

    //2*radius+1
    private SpatialNumericIterationFeature smooth(SpatialNumericIterationFeature contour, int indexOfContour, int radius) {
        SpatialNumericIterationFeature smoothBlock = new SpatialNumericIterationFeature(nR, nI + 1, nJ + 1, nK, resolution, getMySimulationIteration(), SpatialNumericFeatureNameIteration.contour, false);

        for (int r = 0; r < nR; r++) {
            for (int j = 0; j < nJ; j++) {
                for (int k = 0; k < nK; k++) {

                    double smoothedHeight = 0;

                    int count = 0;
                    double sum = 0;
                    for (int jj = -radius; jj <= radius; jj++) {
                        for (int kk = -radius; kk <= radius; kk++) {

                            int jLoc = j + jj;
                            int kLoc = k + kk;
                            if (jLoc >= 0 && kLoc >= 0 && jLoc <= nJ && kLoc < nK) {
                                double height = contour.getValueAt(r, indexOfContour, j + jj, k + kk);
                                count++;
                                sum += height;
                            }

                        }
                    }
                    smoothedHeight = sum / (double) count;


                    for (int jj = -radius; jj <= radius; jj++) {
                        for (int kk = -radius; kk <= radius; kk++) {
                            int jLoc = j + jj;
                            int kLoc = k + kk;
                            if (jLoc >= 0 && kLoc >= 0 && jLoc <= nJ && kLoc < nK) {
                                smoothBlock.setValueAt(r, indexOfContour, j, k, smoothedHeight, false);
                            }
                        }
                    }
                }
            }
        }
        return smoothBlock;
    }


    public static double sumSlope(SpatialNumericIterationFeature contour, int indexOfContour, int radius) {

        int nR = contour.getnR();
        int nI = contour.getnI();
        int nJ = contour.getnJ();
        int nK = contour.getnK();

        SpatialNumericIterationFeature smoothBlock = new SpatialNumericIterationFeature(nR, nI, nJ, nK, contour.getResolution(), contour.getMySimulationIteration(), SpatialNumericFeatureNameIteration.contour, false);

        double sum = 0;
        for (int r = 0; r < nR; r++) {
            for (int j = 0; j < nJ; j++) {
                for (int k = 0; k < nK; k++) {


                    double myHeight = contour.getValueAt(r, indexOfContour, j, k);

                    for (int jj = -radius; jj <= radius; jj++) {
                        for (int kk = -radius; kk <= radius; kk++) {

                            int jLoc = j + jj;
                            int kLoc = k + kk;
                            if (jLoc >= 0 && kLoc >= 0 && jLoc <= nJ && kLoc < nK) {

                                double otherHeight = contour.getValueAt(r, indexOfContour, jLoc, kLoc);

                                if (Math.abs(myHeight - otherHeight) < 2)
                                    sum += Math.abs(myHeight - otherHeight);

                            }

                        }
                    }

                }
            }
        }

        return sum;
    }


    private SpatialNumericIterationFeature calculateSmoothBlock() {

        SpatialNumericIterationFeature smoothBlock  = null;
        SpatialNumericIterationFeature countDensity = null;
        try {
            countDensity = (SpatialNumericIterationFeature) featuresUsed.get(SpatialNumericFeatureNameIteration.countDensity);


            smoothBlock = new SpatialNumericIterationFeature(nR, nI, nJ, nK, resolution, getMySimulationIteration(), SpatialNumericFeatureNameIteration.countDensity, false);

            //this must be odd
            //side length of the cube used for smoothing
            int area = 1;
            int half = (area - 1) / 2;
            double threshold = 1.0 / 3.0;
            for (int r = 0; r < nR; r++) {

                for (int i = 0; i < nI; i++) {
                    for (int j = 0; j < nJ; j++) {
                        for (int k = 0; k < nK; k++) {
                            int massLocCount = 0;
                            //this value will be 27 for points within the biofilm
                            //it will be less for points on the corners and even less for points on the edge
                            //we could solve analytically for what this value will be in the various cases, but its easier just to count in the algorithm
                            int pointsSearched = 0;
                            //search the 3x3 grid ground the point
                            for (int ii = 0; ii <= 0; ii++) {
                                for (int jj = -half; jj <= half; jj++) {
                                    for (int kk = -half; kk <= half; kk++) {
                                        int iLoc = i + ii;
                                        int jLoc = j + jj;
                                        int kLoc = k + kk;

                                        if (iLoc >= 0 && jLoc >= 0 && kLoc >= 0 && iLoc <= nI && jLoc <= nJ && kLoc < nK) {
                                            pointsSearched += 1;
                                            //there is at least one agent here
                                            if ((int) countDensity.getValueAt(r, iLoc, jLoc, kLoc) > 0)
                                                massLocCount += 1;
                                        }


                                    }
                                }
                            }

                            //if there are enough points around me, then i'm part of the biofilm smooth block
                            if ((double) massLocCount / (double) pointsSearched >= threshold) {
                                smoothBlock.setValueAt(r, i, j, k, 1, false);
                            }
                        }
                    }
                }
            }


        } catch (ClassCastException ce) {
            MyPrinter.Printer().printErrorln("The count density feature must have been extracted to generate the smooth block and the contour feature needs to be in list to extract");
            ce.printStackTrace();
        }

        return smoothBlock;
    }


    private void initSumFeatures() {
        if (featureNamesToUse == null) return;


        for (FeatureNameIteration fni : featureNamesToUse) {
            if (fni.getClass().equals(NumericFeatureNameIteration.class))
                featuresUsed.put(fni, new NumericIterationFeature(nR, getMySimulationIteration(), (NumericFeatureNameIteration) fni));

            if (fni.getClass().equals(SpatialNumericFeatureNameIteration.class)) {

                featuresUsed.put(fni, new SpatialNumericIterationFeature(nR, nI, nJ, nK, resolution, getMySimulationIteration(), (SpatialNumericFeatureNameIteration) fni, false));
            }
        }


//        if (featureNamesToUse.contains(SpatialNumericFeatureNameIteration.contour) && !featureNamesToUse.contains(SpatialNumericFeatureNameIteration.countDensity)) {
//            featuresUsed.put(SpatialNumericFeatureNameIteration.countDensity, new SpatialNumericIterationFeature(nR, nI + 1, nJ + 1, nK, resolution, getMySimulationIteration(), SpatialNumericFeatureNameIteration.countDensity, false));
//        }


//
//        if(featureNamesToUse.contains(FeatureNameIteration.agentCount))
//            featuresUsed.put(FeatureNameIteration.agentCount, new NumericIterationFeature(nR, getMySimulationIteration(), FeatureNameIteration.agentCount));
//
//        if(featureNamesToUse.contains(FeatureNameIteration.maxLength))
//            featuresUsed.put(FeatureNameIteration.maxLength, new NumericIterationFeature(nR, getMySimulationIteration(), FeatureNameIteration.maxLength));
//
//        if(featureNamesToUse.contains(FeatureNameIteration.maxHeight))
//            featuresUsed.put(FeatureNameIteration.maxHeight, new NumericIterationFeature(nR, getMySimulationIteration(), FeatureNameIteration.maxHeight));
//
//        if(featureNamesToUse.contains(FeatureNameIteration.contour))
//            featuresUsed.put(FeatureNameIteration.contour, new SpatialNumericIterationFeature(nR, nI + 1, nJ + 1, nK, getMySimulationIteration(), FeatureNameIteration.contour, false));
//
//        if(featureNamesToUse.contains(FeatureNameIteration.massDensity))
//            featuresUsed.put(FeatureNameIteration.massDensity, new SpatialNumericIterationFeature(nR, nI + 1, nJ + 1, nK, getMySimulationIteration(), FeatureNameIteration.massDensity, false));


    }


    public Set<FeatureNameIteration> getFeatureNamesToUse() {
        return featureNamesToUse;
    }

    public Map<FeatureNameIteration, IterationFeature> getFeaturesUsed() {
        return featuresUsed;
    }

    private void extractSubFeatures() {
        for (Feature f : featuresUsed.values()) {
            f.setup();
        }

    }


    public List<AgentList[][][]> getGridList() {
        return gridList;
    }

    public double getResUp() {
        return resUp;
    }

    public List<Map<Integer, Agent>> getAgentAtMap() {
        return agentAtMap;
    }


    /*
The simulator pads the grid by adding 2 units to each dimension(one at the bottom and one at the top)
So a simulation of size nI x nJ x nK will have a grid size of  nI+2 x nJ+2 x nK +2
i = 0 and i = nI+1, j= 0 and nJ+1... are the pads

The grid in the optimizer is not padded
To rectify this :
    grid at i in the simulation maps to i-1 in the optimizer
    the pads get mapped to the adjacent index

Example:

nI = 6 (i=0...i=5)

The simulator locations will have particles all the way up to nI+2(in the range i=0...i=7)
Mapping of simulator index to optimizer index:
7 --> 5 (pad)
6 --> 5
5--> 4
4 --> 3
3 -- > 2
2 -- > 1
1 --> 0
0 --> 0 (pad)


NOTE: the nI, nJ, and nK that get loaded into the optimizer are the real nI, nJ and nK (not the padded versions)
It's just that there will be agents with locations outside of this range because there are agents in the padding areas

 */

    public static int realLocToGridLoc(double real, double res, double nD) {

        int loc = (int) Math.floor(real / res);

        //account for the padding
        if (loc >= nD) //7 or 6 (example)
            loc = (int) nD; //change it to 6 (example)

        if (loc == 0)
            loc = 1; //so that 0 gets mapped to 0, would return -1 if we didn't do this

        return loc - 1; //mapping is to the one just below it (see example)
    }


    public static int getBoundaryLengthResUP(double originalD, double resUp) {
        return (int) Math.floor(originalD * resUp);
    }

    public int getnI() {
        return nI;
    }

    public int getnJ() {
        return nJ;
    }

    public int getnK() {
        return nK;
    }


    /*
    Loads a 2D array from a file
    Meant to represent a 2D cross section of a biofilm, i.e, the y and z at a specific x
    rows are zs (separated by a space) and columns are y
    so line 1 is the ys at z = 0
     */
    public static double[][] load(String fileNameFull) {

        List<String> lines    = FileReaderWriter.readLines(fileNameFull);
        String[]     spaceSep = lines.get(0).split(" ");

        double[][] twoDArray = new double[spaceSep.length][lines.size()];

        for (int k = 0; k < lines.size(); k++) {

            String line = lines.get(k);
            spaceSep = line.split(" ");

            for (int j = 0; j < spaceSep.length; j++) {

                twoDArray[j][k] = Double.parseDouble(spaceSep[j]);

            }

        }
        return twoDArray;

    }


    private static double[][][][] addLists(double[][][] arry) {
        double[][][][] arryList = new double[1][arry.length][arry[0].length][arry[0][0].length];
        for (int i = 0; i < arry.length; i++) {
            for (int j = 0; j < arry[0].length; j++) {
                for (int k = 0; k < arry[0][0].length; k++) {

                    arryList[0][i][j][k] = arry[i][j][k];
                }
            }

        }

        return arryList;
    }

    public static double calculateSpatialError(double[][][] exp, double[][][] actual, IErrorCalculation errorCalculation, boolean average, int paddingX, int paddingY, int paddingZ) {
        //  List<Double>[][][] expList = (ArrayList<Double>[][][])new ArrayList<?>[exp.maxLength][exp[0].maxLength][exp[0][0].maxLength];
        // List<Double>[][][] actList = (ArrayList<Double>[][][])new ArrayList<?>[exp.maxLength][exp[0].maxLength][exp[0][0].maxLength];

        double[][][][] expList = addLists(exp);
        double[][][][] actList = addLists(actual);

        return calculateSpatialError(expList, actList, errorCalculation, average, paddingX, paddingY, paddingZ);
    }

    public static double calculateSpatialError(double[][][][] exp, double[][][][] actual, IErrorCalculation errorCalculation, boolean average, int paddingX, int paddingY, int paddingZ) {


        //remove padding from the simulation grid


        //for now assumes removing padding leaves some grid left
        //might want to check for that
        double[][][][] upExp = new double[exp.length][exp[0].length - 2 * paddingX][exp[0][0].length - 2 * paddingY][exp[0][0][0].length - 2 * paddingZ];


        for (int a = 0; a < upExp.length; a++) {
            for (int i = 0; i < upExp[0].length; i++) {
                for (int j = 0; j < upExp[0][0].length; j++) {
                    for (int k = 0; k < upExp[0][0][0].length; k++) {

                        upExp[a][i][j][k] = exp[a][i][j][k];

                    }
                }
            }
        }

        exp = upExp;


        int x1 = exp[0].length;
        int y1 = exp[0][0].length;
        int z1 = exp[0][0][0].length;

        int x2 = actual[0].length;
        int y2 = actual[0][0].length;
        int z2 = actual[0][0][0].length;


        //When 2D z=1, so only thing that matters is the ratio of x to y

        //When 3D and comparing to a target simulation, none of the dimensions should be 1

        //When 3D and comparing to real data, x=1 because we only have data on the bottom of the simulation
        //....
        //So if any of the numbers are 1, then just make sure its counterpart is 1 and don't consider it in a ratio

        double ratioxy1;
        double ratioxy2;
        double ratioyz1;
        double ratioyz2;
        double ratioxz1;
        double ratioxz2;

        boolean considerX = !(x1 == 1 && x1 == x2);
        boolean considerY = !(y1 == 1 && y1 == y2);
        boolean considerZ = !(z1 == 1 && z1 == z2);

        if (!considerX || !considerY) {
            ratioxy1 = ratioxy2 = 1;
        } else {
            ratioxy1 = (double) x1 / y1;
            ratioxy2 = (double) x2 / y2;
        }

        if (!considerZ || !considerY) {
            ratioyz1 = ratioyz2 = 1;
        } else {
            ratioyz1 = (double) y1 / z1;
            ratioyz2 = (double) y2 / z2;
        }


        if (!considerX || !considerZ) {
            ratioxz1 = ratioxz2 = 1;
        } else {
            ratioxz1 = (double) x1 / z1;
            ratioxz2 = (double) x2 / z2;
        }

        //change to rough equality??


        double epsilon = .1;


        if (!ExtraMath.approxEqual(ratioxy1, ratioxy2, epsilon) || !ExtraMath.approxEqual(ratioyz1, ratioyz2, epsilon) || !ExtraMath.approxEqual(ratioxz1, ratioxz2, epsilon)) {
            IllegalArgumentException iae = new IllegalArgumentException("The grids being compared do not have the same ratio of  dimensions");
            iae.printStackTrace();
            System.exit(-5);
        }


        //box size refers to how many array elements of the bigger array corespond to the small array
        //if the bigger array is twice as big, then boxSize = 2, i.e., 4 (2x2) elements of the bigger one corresponds to 1 element of the smaller one
        //if 3x as big, then boxSize = 3

        //there's a single box size because if the ratio's above hold, e.g., x1/y1 = x2/y2....
        //then x1/x2 = y1/y2 = z1/z2
        double boxSize;
        //y1, z1 bigger
        if (y1 >= y2) boxSize = (double) y1 / y2;
            //y2, z2 bigger
        else boxSize = (double) y2 / y1;


        double boxSizeFloor = Math.floor(boxSize);

        if (!(ExtraMath.approxEqual(boxSize - boxSizeFloor, .5, epsilon) || ExtraMath.approxEqual(boxSize - boxSizeFloor, 0, epsilon))) {
            IllegalArgumentException iae = new IllegalArgumentException("The ratio of the grids being compared is not a multiple of .5");
            iae.printStackTrace();
            System.exit(-5);
        }

        //round thte box size to the nearest .5
        //make sure this works
        boxSize = Math.round((boxSize / .5)) * .5;


        return calculateSpatialErrorHelper(exp, actual, boxSize, errorCalculation, considerX, considerY, considerZ, average);
    }


    /*
    helper method used in calculating the error

    calculates the spatial error by comparing the arrays element-wise and averaging over the whole thing
    takes an array of 3D arrays as input, the inner 3D arrays represent some number at each grid point

    if average == true, then the elements of the bigger array are averaged
    if average == false, then the elements of the bigger array are just summed



     */
    private static double calculateSpatialErrorHelper(double[][][][] exp, double[][][][] actual, double boxSize, IErrorCalculation errorCalculation, boolean considerX, boolean considerY, boolean considerZ, boolean average) {

        //the last dimensions of these two and exp and actual should all be the same
        double[][][][] small;
        double[][][][] big;

        int     bsFloor   = (int) Math.floor(boxSize);
        int     bsCeiling = (int) Math.ceil(boxSize);
        boolean expBiggerThanActual;


        //find out which array is the bigger one
        if (exp[0][0][0].length + exp[0][0].length + exp[0].length >= actual[0][0][0].length + actual[0][0].length + actual[0].length) {
            big = exp;
            small = actual;
            expBiggerThanActual = true;
        } else {
            big = actual;
            small = exp;
            expBiggerThanActual = false;
        }

        int nI = small[0].length;
        int nJ = small[0][0].length;
        int nK = small[0][0][0].length;


        //average the element values of the bigger array so it can be compared to the smaller one
        int            x      = 0, y = 0, z = 0;
        double[][][][] bigAvg = new double[small.length][nI][nJ][nK];

        //holds the average (or summed) values of each of the bigger 3D arrays
        double[] bigBoxAvgVal = new double[big.length];


        int boxRX = considerX ? bsCeiling : 1;
        int boxRY = considerY ? bsCeiling : 1;
        int boxRZ = considerZ ? bsCeiling : 1;


        //used so that only a fraction of the value of a grid square is added as part of the average or sum
        //this is so that a grid, say, 4.5 x bigger can be compared
        //if a grid square is used twice, then .5 of its value will be used each time
        //if a grid square ie used four times, then .25 of its value will be used each time
        double partialMultiplier;

        //after each sum over the bigger box values, this should be equal to boxSize^2 (if we are only considering 2 dimensions) or boxSize^3 (3 dimensions)
        double actualSum   = -1;
        double expectedSum = 1;
        double count       = 1;


        if (considerX) expectedSum *= boxSize;
        if (considerY) expectedSum *= boxSize;
        if (considerZ) expectedSum *= boxSize;


        //go over each point in the 3D arrays
        for (int i = 0; i < nI; i++) {
            for (int j = 0; j < nJ; j++) {
                for (int k = 0; k < nK; k++) {
                    bigBoxAvgVal = new double[big.length];

                    //it will only be -1 the first time we hit this line
                    if (actualSum != -1) {
                        if (expectedSum != actualSum) {
                            Exception iae = new Exception("Something went wrong in comparing grids." +
                                    " The expected number of grids (" + expectedSum + ") summed over does not equal the actual number of grids (" + actualSum + "). The boxSize is " + boxSize);

                            iae.printStackTrace();
                            System.exit(-5);
                        }
                    }

                    actualSum = 0;

                    for (int ii = 0; ii < boxRX; ii++) {
                        for (int jj = 0; jj < boxRY; jj++) {
                            for (int kk = 0; kk < boxRZ; kk++) {
                                x = i * boxRX + ii;
                                y = j * boxRY + jj;
                                z = k * boxRZ + kk;

                                partialMultiplier = 1;
                                if (bsCeiling != bsFloor) {
                                    if (((x + 1) % bsCeiling) == 0 && considerX) partialMultiplier *= .5;
                                    if (((y + 1) % bsCeiling) == 0 && considerY) partialMultiplier *= .5;
                                    if (((z + 1) % bsCeiling) == 0 && considerZ) partialMultiplier *= .5;
                                }

                                actualSum += partialMultiplier;


                                if (x < big[0].length && y < big[0][0].length && z < big[0][0][0].length) {

                                    //goes over the values of each of the 3D arrays at index x y z
                                    for (int a = 0; a < bigBoxAvgVal.length; a++) {
                                        bigBoxAvgVal[a] = bigBoxAvgVal[a] + (big[a][x][y][z] * partialMultiplier);
                                    }
                                } else {
//                                    System.out.println("In comparing grids, " + x + " " + y + " " + z + " does not exist in the bigger array. " +
//                                            "This means the bigger grid wasn't quite big enough to be a clean multiple of .5 times bigger than the smaller one, e.g., it was 4.48 x bigger instead of 4.5");
                                }
                            }

                        }
                    }


                    //for a box size of 2, count=2^3 (3 dimensions)
                    //however, if one of the dimensions' maxLength=1,then we effectively have one less dimension


                    if (average) count = actualSum;

                    for (int a = 0; a < bigBoxAvgVal.length; a++) {
                        bigBoxAvgVal[a] = bigBoxAvgVal[a] / count;
                        bigAvg[a][i][j][k] = bigBoxAvgVal[a];
                    }


                }

            }
        }

//        int XsNotConsidered = (big[0].length - 1) - x;
//        int YsNotConsidered = (big[0][0].length - 1) - y;
//        int ZsNotConsidered = (big[0][0][0].length - 1) - z;
//        if (XsNotConsidered > 0) {
//            System.out.println("In comparing grids, there were " + XsNotConsidered + " X rows in the bigger array that weren't used in the average.");
//        }
//        if (YsNotConsidered > 0) {
//            System.out.println("In comparing grids, there were " + YsNotConsidered + " Y columns in the bigger array that weren't used in the average.");
//        }
//        if (ZsNotConsidered > 0) {
//            System.out.println("In comparing grids, there were " + ZsNotConsidered + " Z bars in the bigger array that weren't used in the average.");
//        }
//
//        if (XsNotConsidered > 0 || YsNotConsidered > 0 || ZsNotConsidered > 0) {
//            System.out.println("This means the bigger array was a little too big to be a clean multiple of .5 times the smaller one, e.g., it was 5.02 x bigger instead of 5.");
//        }


        double[]   smallBoxVal = new double[small.length];
        double[][] smallMinMax = calcMinandMax(small);
        double[][] bigMinMax   = calcMinandMax(bigAvg);

        //calculate the error between the small array and the averaged version of the bigger array
        //the averaged version of the bigger array and the smaller array will have the same dimensions
        double error = 0;
        for (int i = 0; i < nI; i++) {
            for (int j = 0; j < nJ; j++) {
                for (int k = 0; k < nK; k++) {

                    //scale the values to be between [0, 1], inclusive

                    for (int a = 0; a < small.length; a++) {

                        smallBoxVal[a] = scale(small[a][i][j][k], smallMinMax[0][a], smallMinMax[1][a]);
                        bigBoxAvgVal[a] = scale(bigAvg[a][i][j][k], bigMinMax[0][a], bigMinMax[1][a]);
                    }

                    if (expBiggerThanActual)
                        error += errorCalculation.calculateError(bigBoxAvgVal, smallBoxVal);
                    else error += errorCalculation.calculateError(smallBoxVal, bigBoxAvgVal);
                }
            }
        }


        error /= (nI * nJ * nK);
        return error;

    }


    public static double[] calcMinandMax(double[][][] grid) {
        double[][][][] gridList = addLists(grid);

        double[][] minmaxs = calcMinandMax(gridList);


        double[] minmax = new double[2];

        minmax[0] = minmaxs[0][0];
        minmax[1] = minmaxs[1][0];

        return minmax;
    }

    public static double[][] calcMinandMax(double[][][][] gridNumbers) {

        double[][] minMax = new double[2][gridNumbers.length];


        //set the minimums to a big value
        for (int a = 0; a < minMax[0].length; a++) {
            minMax[0][a] = Double.MAX_VALUE;
        }

        //set the maximums to a small value
        for (int a = 0; a < minMax[0].length; a++) {
            minMax[1][a] = -Double.MAX_VALUE;
        }


        for (int i = 0; i < gridNumbers[0].length; i++) {
            for (int j = 0; j < gridNumbers[0][0].length; j++) {
                for (int k = 0; k < gridNumbers[0][0][0].length; k++) {


                    for (int a = 0; a < gridNumbers.length; a++) {

                        //look for new minimums
                        if (gridNumbers[a][i][j][k] < minMax[0][a])
                            minMax[0][a] = gridNumbers[a][i][j][k];

                        //look for new maximums
                        if (gridNumbers[a][i][j][k] > minMax[1][a])
                            minMax[1][a] = gridNumbers[a][i][j][k];

                    }
                }
            }
        }
        return minMax;
    }

    public static double scale(double number, double min, double max) {

        double denom = max - min;

        if (denom == 0) return 0;
        else return (number - min) / (max - min);

    }


    @Override
    public void output(String filePath) {

        for (IterationFeature itrFeat : featuresUsed.values()) {
            itrFeat.output(filePath);
        }

    }


    public void deleteFeatures() {

        for (IterationFeature itrFeat : featuresUsed.values()) {
            itrFeat.deleteFeatures();
        }
        featuresUsed = null;
        featureNamesToUse = null;


    }

    @Override
    public void deleteIntermediateData() {

        for (IterationFeature itrFeat : featuresUsed.values()) {
            itrFeat.deleteIntermediateData();
        }
        gridList = null;
        agentAtMap = null;
    }


    public void addFeatureNamesToUse(Set<FeatureNameIteration> newFeatureNamesToUse) {
        if (featureNamesToUse == null) featureNamesToUse = new HashSet<>();
        featureNamesToUse.addAll(newFeatureNamesToUse);
    }

    public static String rangeLine(int x1, int xn, int y1, int yn, int z1, int zn, double resolution) {

        StringBuilder sb = new StringBuilder();
        return sb.append(x1).append(' ').append(xn).append(' ').append(y1).append(' ').append(yn).append(' ').append(z1).append(' ').append(zn).append(' ').append(resolution).append('\n').toString();
    }

    public Grid getGrid() {
        return grid;
    }


}
