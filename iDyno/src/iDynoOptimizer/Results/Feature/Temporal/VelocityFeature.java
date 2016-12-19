package iDynoOptimizer.Results.Feature.Temporal;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Results.Agent.Agent;
import iDynoOptimizer.Results.Agent.Grid;
import iDynoOptimizer.Results.Agent.Location;
import iDynoOptimizer.Results.Feature.Error.IErrorCalculation;
import iDynoOptimizer.Results.Feature.Feature;
import iDynoOptimizer.Results.Feature.Names.FeatureName;
import iDynoOptimizer.Results.Feature.Names.SpatialNumericFeatureNameIteration;
import iDynoOptimizer.Results.Feature.Names.TemporalNumericFeatureName;
import iDynoOptimizer.Results.Feature.SpatialIterationFeature;
import iDynoOptimizer.Search.SimulationIteration;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Chris on 4/16/2015.
 * <p>
 * <p>
 * A representation of the convergence at each grid point (using the InOut class) between two iterations it1 and it2, such that, it1 < it2 in time
 * Uses a SpatilIterationFeature class generated from each of the iterations to get the agent locations used to calculate their convergence vectors
 */
public class VelocityFeature extends Feature {


    //A list of grids of convergence, where each grid is from a different random repeat and contains the convergence of the agents at each grid point
    private List<InOut[][][]> vectorGridList = new ArrayList<InOut[][][]>();

    //The average grid convergence (averaged over the random repeats)
    private InOut[][][] avgVectorSums;

    //this is a list of the numbers contained in avgVectorSums
    private double[][][][] flatListNumbers;

    //Temporal requires two iterations because it is essentially velocity
    private SpatialIterationFeature from; //where the agents were
    private SpatialIterationFeature to; //where the agents went
    private double                  resUp;
    private double                  resolution;

    private int nI;
    private int nJ;
    private int nK;

    private int fromIteration;
    private int toIteration;


    public VelocityFeature(SimulationIteration from, SimulationIteration to) {

        super(from, to, TemporalNumericFeatureName.velocity);

        this.from = from.getMySpatialIterationFeature();
        this.to = to.getMySpatialIterationFeature();
        fromIteration = from.getIteration();
        toIteration = to.getIteration();
        resUp = this.from.getResUp();


    }

    public VelocityFeature(String fileNameFull) {
        super(TemporalNumericFeatureName.velocity);
        flatListNumbers = new double[1][1][1][1];
        flatListNumbers[0][0] = SpatialIterationFeature.load(fileNameFull);

    }

    public void setup() {
        super.setup();
    }

    @Override
    public void output(String filePath) {
        FileReaderWriter.writeSingleFile(toString(), filePath, getMyName() + "-" + fromIteration + "-" + toIteration, false);
    }


    public void deleteFeatures() {
        flatListNumbers = null;
    }


    @Override
    public void deleteIntermediateData() {

        vectorGridList = null;
        avgVectorSums = null;
    }


    @Override
    public double calulateError(Feature f, IErrorCalculation errorCalculation, int paddingX, int paddingY, int paddingZ) {
        VelocityFeature compareTo = (VelocityFeature) f;

        setErrorCalculation(errorCalculation);

        //this object is the target or "actual"/correct values
        //for convergence, when comparing a bigger array to a smaller one, it makes sense to sum the convergence values, rather than average them
        setError(SpatialIterationFeature.calculateSpatialError(compareTo.getFlatListNumbers(), getFlatListNumbers(), errorCalculation, false, paddingX, paddingY, paddingZ));

        return getError();

    }


    /*
    Requires extract() to be called first
    Uses the list of convergence grids(list of InOut) to calculate the average InOut grid (average over the random seed repeats)
     */
    @Override
    public void calculateAverage() {
        int count = vectorGridList.size();


        avgVectorSums = new InOut[vectorGridList.get(0).length][vectorGridList.get(0)[0].length][vectorGridList.get(0)[0][0].length];

        for (InOut[][][] vs : vectorGridList) {
            for (int i = 0; i < vs.length; i++) {
                for (int j = 0; j < vs[0].length; j++) {
                    for (int k = 0; k < vs[0][0].length; k++) {
                        InOut current = avgVectorSums[i][j][k];
                        if (current == null) {
                            current = new InOut();
                            avgVectorSums[i][j][k] = current;
                        }

                        //add
                        if (vs[i][j][k] != null) {
                            InOut next = vs[i][j][k];
                            current.add(next);
                        }

                    }
                }
            }
        }

        for (int i = 0; i < avgVectorSums.length; i++) {
            for (int j = 0; j < avgVectorSums[0].length; j++) {
                for (int k = 0; k < avgVectorSums[0][0].length; k++) {
                    if (avgVectorSums[i][j][k] != null) {
                        avgVectorSums[i][j][k].divide((double) count);


                    }
                }
            }
        }


        vectorGridList = null;

        createFlatListNumbers();


    }

    @Override
        /*
        Uses the two SpatilIterationFeature classes to calculate the convergence vectors (InOut)
        Populates the list of convergence grids (list of InOut)
         */
    public void extract() {

        //if we are 3D, then set nI = 1
        //if we are 2D, then set it to it's real value
        //this is because we only care about convergence at the bottom of the colony
        nI = from.getnK() > 1 ? 1 : from.getnI();
        nJ = from.getnJ();
        nK = from.getnK();
        resolution = from.getGrid().getResolution() / resUp;
        int count = -1;
        for (SpatialIterationFeature.AgentList[][][] al : from.getGridList()) //for each random seed repeat
        {
            InOut[][][] vectorSumAt = new InOut[nI][nJ][nK];
            count++;
            for (int i = 0; i < nI; i++) {
                for (int j = 0; j < nJ; j++) {
                    for (int k = 0; k < nK; k++) {

                        if (al[i][j][k] != null) {
                            for (Agent a : al[i][j][k].getAgents()) //get each agent at each grid point
                            {
                                Location locFrom = a.getLocation();
                                Agent aTo = to.getAgentAtMap().get(count).get(a.getFamily().getBirthId());
                                if (aTo != null && !aTo.getLocation().equals(a.getLocation())) {
                                    Location locTo = aTo.getLocation();


                                    double massOut = a.getMass().getBiomass();
                                    double massIn = aTo.getMass().getBiomass();
                                    Vector v = new Vector(locFrom, locTo, 1);
                                    Vector vMomOut = new Vector(locFrom, locTo, massOut);
                                    Vector VMomIn = new Vector(locFrom, locTo, massIn);


                                    InOut inOutFrom = vectorSumAt[i][j][k];


                                    int iOut = SpatialIterationFeature.realLocToGridLoc(locTo.getX(), resolution, nI);
                                    int jOut = SpatialIterationFeature.realLocToGridLoc(locTo.getY(), resolution, nJ);
                                    int kOut = SpatialIterationFeature.realLocToGridLoc(locTo.getZ(), resolution, nK);

                                    InOut inOutTo = vectorSumAt[iOut][jOut][kOut];


                                    if (inOutFrom == null) {
                                        inOutFrom = new InOut();
                                        vectorSumAt[i][j][k] = inOutFrom;
                                    }
                                    if (inOutTo == null) {
                                        inOutTo = new InOut();
                                        vectorSumAt[iOut][jOut][kOut] = inOutTo;
                                    }


                                    VectorSums outgoing = new VectorSums();
                                    VectorSums incoming = new VectorSums();

                                    outgoing.add(v, Vector.abs(v), vMomOut, Vector.abs(vMomOut), massOut, 1);
                                    incoming.add(v, Vector.abs(v), VMomIn, Vector.abs(VMomIn), massIn, 1);

                                    inOutFrom.addOutgoing(outgoing);
                                    inOutTo.addIncoming(incoming);
                                }

                                if (vectorSumAt[i][j][k] == null) {
                                    InOut inOutFrom = new InOut();
                                    vectorSumAt[i][j][k] = inOutFrom;
                                }


                            }
                        }
                    }
                }
            }
            vectorGridList.add(count, vectorSumAt);
        }


    }

    public InOut[][][] getAvgVectorSums() {
        return avgVectorSums;
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

    public void createFlatListNumbers() {
        int howManyNumbersToInclude = 1;
        flatListNumbers = new double[howManyNumbersToInclude][avgVectorSums.length][avgVectorSums[0].length][avgVectorSums[0][0].length];
        for (int i = 0; i < avgVectorSums.length; i++) {
            for (int j = 0; j < avgVectorSums[0].length; j++) {
                for (int k = 0; k < avgVectorSums[0][0].length; k++) {


                    VectorSums inoutDiff = avgVectorSums[i][j][k].getIncOutDiff();

                    double jKConvergence = inoutDiff.getJKConvergence();

                    flatListNumbers[0][i][j][k] = inoutDiff.getJKConvergence();


//                        flatListNumbers[i][j][k][0] = avgVectorSums[i][j][k].getIncOutSum().getCount();
//                        flatListNumbers[i][j][k][1] = avgVectorSums[i][j][k].getIncOutSum().getvMom().getI();
//                        flatListNumbers[i][j][k][2] = avgVectorSums[i][j][k].getIncOutSum().getvMom().getJ();
//                        flatListNumbers[i][j][k][3] = avgVectorSums[i][j][k].getIncOutSum().getvMom().getK();
//                        flatListNumbers[i][j][k][4] = avgVectorSums[i][j][k].getIncOutSum().getV().getI();
//                        flatListNumbers[i][j][k][5] = avgVectorSums[i][j][k].getIncOutSum().getV().getJ();
//                        flatListNumbers[i][j][k][7] = avgVectorSums[i][j][k].getIncOutSum().getV().getK();
//                        flatListNumbers[i][j][k][8] = avgVectorSums[i][j][k].getIncOutSum().getMagMom();
//                        flatListNumbers[i][j][k][9] = avgVectorSums[i][j][k].getIncOutSum().getMag();
//                        flatListNumbers[i][j][k][10] = avgVectorSums[i][j][k].getIncOutSum().getMass();

                    // flatListNumbers[i][j][k][11] = avgVectorSums[i][j][k].getIncOutSum().getV().getSum();
                }
            }
        }


    }

    public double[][][][] getFlatListNumbers() {
        return flatListNumbers;
    }

    public void removeNoise(InOut overallAvg) {

        if (overallAvg == null) return;

        for (int i = 0; i < avgVectorSums.length; i++) {
            for (int j = 0; j < avgVectorSums[0].length; j++) {
                for (int k = 0; k < avgVectorSums[0][0].length; k++) {

                    avgVectorSums[i][j][k].subtract(overallAvg);
                }
            }
        }

    }


    //Outputs the information contained in Vector Sum like:
    //LINE 1 : x1 xn y1 yn z1 zn (range for the three axis)
    //The rest is laid out like a grid(rows and columns corresponding to the graph's rows and columns (upside down). There are 9 values for each grid point (three dimensional)
    //
    //[[i1 j1 k1 im1 jm1 km1 mass1 mag1 magm1],[i1 j1 k1 im1 jm1 km1 mass1 mag1 magm1]] [...repeat](the net velocity vector, the net momentum vector, delta mass, delta magnitude of velocity vector , delta magnitude  of momentum vector
    //embedded brackets because three dimensions have to be fit on a 2 dimensional file

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append(SpatialIterationFeature.rangeLine(0, nI, 0, nJ, 0, nK, resolution));
        for (int i = 0; i < avgVectorSums.length; i++) {
            for (int j = 0; j < avgVectorSums[0].length; j++) {

                sb.append('[');
                for (int k = 0; k < avgVectorSums[0][0].length; k++) {
                    sb.append(avgVectorSums[i][j][k].getIncOutDiff().toString());
                    if (k != avgVectorSums[0][0].length - 1) sb.append(',');
                }
                sb.append(']');
            }
            sb.append('\n');
        }

        return sb.toString();
    }


    public String toStringJKConvergence() {

        StringBuilder sb = new StringBuilder();
        sb.append(SpatialIterationFeature.rangeLine(0, nI, 0, nJ, 0, nK, resolution));
        for (int j = 0; j < avgVectorSums[0].length; j++) {

            for (int k = 0; k < avgVectorSums[0][0].length; k++) {

                sb.append(avgVectorSums[0][j][k].getIncOutDiff().getJKConvergence()).append(' ');
            }

            sb.append('\n');
        }

        return sb.toString();

    }


}
