/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ______________________________________________________
 * top-level class of the simulation core. It is used to create and run a
 * simulation; this class is called by the class Idynomics.
 *
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @since June 2006
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */

package simulator;

import de.schlichtherle.io.FileInputStream;
import farzin.Variable;
import idyno.Idynomics;
import idyno.SimTimer;
import org.jdom.Element;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import povray.PovRayWriter;
import simulator.agent.SpecialisedAgent;
import simulator.agent.Species;
import simulator.diffusionSolver.DiffusionSolver;
import simulator.geometry.Agar;
import simulator.geometry.Bulk;
import simulator.geometry.Domain;
import simulator.reaction.Reaction;
import simulator.reaction.SoluteComposition;
import simulator.reaction.molecularReaction.MolecularReaction;
import simulator.reaction.molecularReaction.molecularReactionManager;
import utils.*;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class Simulator {

	/* ______________ PROPERTIES ____________________________ */

    /** Protocol and optional input XML files describing the simulation * */
    public transient XMLParser _protocolFile;
    private XMLParser agentFile;
    private boolean useAgentFile;
    private XMLParser bulkFile;
    private boolean useBulkFile;


    //added by Chris Johnson so that simulations can be restarted with solutes in place
    private XMLParser soluteFile;
    private boolean useSoluteFile;


    /** Variables used to generate result files and back-ups * */
    private double _outputPeriod, _lastOutput;
    public transient PovRayWriter povRayWriter;
    public transient ResultFile[] result;
    private String _resultPath;
    public boolean useDetachment = false;
    public int agentNum = 0;
    /** Variable used to test the chemostat condition */

    public static boolean isChemostat = false;
    public static boolean isFluctEnv = false;
    public static boolean isInitializing = false;
    public static boolean is3D = false;

    private static boolean writeEnvOutput;
    private static boolean writePOV;

    /** Timer of the simulation */
    public static SimTimer simTimer;
    //Defined by Qanita to store all amount of solute
//    public double TotalSolute = 0;
//    public String GfileName = "";
//    public static String output2 = "Iter;" + "AllVEGF;" + "NumberofCells;" + "VEGFperCell";
//    public static String output3 = "";
    /** agentTimeStep */
    // to make it available to AgentContainer class and at the same time be readable from the simulator
    // markup in the protocol file
    public double agentTimeStep;

    /* The world lists the geometry and the bulks connected to the system */
    public World world;

	/*
     * Dictionary of objects : allows to know the index of an object before its
	 * creation
	 */

    public ArrayList<String> particleDic, soluteDic, speciesDic, reactionDic, molecularReactionDic, solverDic;
    public static farzin.Variable[] vars;
    // Flann MD added particleRegulatorDic
    public HashMap<String, Boolean> particleRegulatorDic;
    public HashMap<String, double[][][]> molecularKineticRegulators;
    public static long randSeed;

	/* MAIN CONTAINERS ______________________________________________________ */

    /* List of solutes, reactions, solvers and species */
    public SoluteGrid[] soluteList;
    public Reaction[] reactionList;
    public MolecularReaction[] molecularReactionList;
    public DiffusionSolver[] solverList;
    public ArrayList<Species> speciesList = new ArrayList<Species>();
    public molecularReactionManager molecularReactionManager;
    //sonia:07-05-09
    //used to write output information about plasmid carriage
    public ArrayList<String> plasmidList = new ArrayList<String>();

    //sonia 11.10.2010 list with all the scan speeds of all plasmids
    public ArrayList<Double> scanSpeedList = new ArrayList<Double>();

    /* Grid where all located agents are stored */
    public AgentContainer agentGrid;

    private Chart _graphics;


    private SoluteComposition[] soluteCompositionList;


    //Added by Chris Johnson 10/1/2015
    //If true, all reaction-diffusion solvers will be solved at each agent time step
    //This will slow simulations down, I've added it so that it behaves more like Biocellion
    //if false, they will be solved at each global time step (like normal)
    private boolean diffusionReactionOnAgentTime;


	/* __________________________ CONSTRUCTOR _______________________________ */

    /** Open the protocol_file and initialise the system with */


    public Simulator(String protocolFile, String resultPath, boolean writeEnvOutput, boolean writePOV) {
        try {
            LogFile.chronoMessageIn("System initialisation:");

            this.writeEnvOutput = writeEnvOutput;
            this.writePOV = writePOV;

            // Create pointers to protocolFiles (scenario and agents)
            _protocolFile = new XMLParser(protocolFile);
            _resultPath = resultPath + File.separator;
            detectInputs(protocolFile);

            createSimulator();
            System.out.println("Simulator created");
            createWorld();
            System.out.println("World created");
            // createFiles was here
            createSolutes();
            System.out.println("Solutes created");
            createReactions();
            System.out.println("Reactions created");
            createSoluteCompositions();
            System.out.println("Solute Compositions created");
            createSolvers();
            System.out.println("Solvers created");
            createSpecies();
            System.out.println("Species created");
            //createCharts();


            //added by Farzin to allow late bindings
            lateBindings();
            // bvm 27.1.2009 moved this from above to allow better povray outputs
            createFiles(resultPath);
            System.out.println("Result files created");
            LogFile.chronoMessageOut("done");

            // Describe initial conditions
            if (!Simulator.isChemostat && this.writePOV)
                povRayWriter.write(SimTimer.getCurrentIter());
            writeReport();

        } catch (Exception e) {
            LogFile.writeLog("Simulator.CreateSystem(): error met: " + e);
            System.exit(-1);
        }

    }

    private void lateBindings() throws Exception {
        for (Reaction aReac : reactionList) {
            if (aReac._catalystSpeciesName != null) {
                aReac._catalystSpeciesIndex = this.getSpeciesIndex(aReac._catalystSpeciesName);
                if (aReac._catalystSpeciesIndex == -1) {
                    LogFile.writeLog("Undefined Species as Catalyst: " + aReac._catalystSpeciesName);
                    throw new Exception();
                }
            } else
                aReac._catalystSpeciesIndex = -2;
        }
        world.domainList.get(0).refreshBioFilmGrids();

        for (Species spec : speciesList) {
            for (Species spec2 : speciesList) {
                if (!spec.speciesName.equalsIgnoreCase(spec2.speciesName) && !spec.switchingLags.containsKey(spec2.speciesName)) {
                    spec.switchingLags.put(spec2.speciesName, 0);
                    LogFile.writeLog("Undefined switching lag from: " + spec.speciesName + " to " + spec2.speciesName + ".\n The default value of zero will be used.");
                }
            }
        }
        for (SoluteGrid aSolute : soluteList) {
            //mySim.world.domainList.get(0).getBiomass().grid[1][j][k] > 0
            if (aSolute.useRandomInit) {
                for (int i = 0; i < world.domainList.get(0)._nI + 1; i++)
                    for (int j = 0; j < world.domainList.get(0)._nJ + 1; j++)
                        for (int k = 1; k < world.domainList.get(0)._nK + 1; k++)
                            if (world.domainList.get(0).getBiomass().grid[i][j][k] > 0)
                                aSolute.setValueAt(ExtraMath.getUniRand(aSolute.minConc, aSolute.maxConc), i, j, k);
            }

        }
    }

	/* _____________________ TOP-LEVEL METHODS ______________________________ */

    /**
     * This is the method that starts the simulation. It is called by the class
     * with the "main" method
     */
    public void run() {

        while (simulationIsRunning())
            step();
        if (molecularReactionManager != null) {
            molecularReactionManager.writeMolecularReport();
            writeReactionFrequencyReport();
        }

    }

    /**
     * Check, if the simulation should continue or stop.
     *
     * @return true, if simulation is still running
     */
    public boolean simulationIsRunning() {
        return !SimTimer.simIsFinished();
    }

    public void createCharts() {

        _graphics = new Chart("Simulation outputs");
        _graphics.setPath(_resultPath);

        XYSeriesCollection[] graphSet = new XYSeriesCollection[2];
        String[] xLegend = {"Time(h)", "Time(h)"};
        String[] yLegend = {"Conc (g.L-1)", "Conc (g.L-1)"};

        // Create a chart for Solutes
        graphSet[0] = new XYSeriesCollection();
        graphSet[1] = new XYSeriesCollection();
        int nSolute = soluteDic.size();
        int nSpecies = speciesDic.size();
        for (int iSolute = 0; iSolute < nSolute; iSolute++)
            graphSet[0].addSeries(new XYSeries(soluteDic.get(iSolute)));
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++)
            graphSet[1].addSeries(new XYSeries(speciesDic.get(iSpecies)));

        _graphics.init(graphSet, xLegend, yLegend);

        _graphics.pack();
        _graphics.setVisible(true);


    }

    private static int count = 0;
    /**
     * Perform a full iteration
     */
    public void step() {
    	//if(count%1000 == 0){
    		System.out.println("Step Count: " + count++);
    	//}
//		long startTime = System.currentTimeMillis();
//		
//
//		// Increment system time
//		SimTimer.applyTimeStep();
//		if(molecularReactionList!=null)
//			molecularReactionManager.stepMolecularReactions(molecularReactionList);
//			
//		if ( ((SimTimer.getCurrentTime()-_lastOutput)
//				>= (_outputPeriod - 0.01*SimTimer.getCurrentTimeStep())) ||
//				SimTimer.simIsFinished() )
//			writeReport();
//
//
//		SimTimer.updateTimeStep(world);
//		LogFile.writeEndOfStep(System.currentTimeMillis()-startTime);
//		

        long startTime = System.currentTimeMillis();


        // Increment system time
        SimTimer.applyTimeStep();
        if (molecularReactionList != null)
            molecularReactionManager.stepMolecularReactions(molecularReactionList);
        // Check if new agents should be created
        checkAgentBirth();
        if (farzin.Logger.coloringTightJunctions)
            farzin.Logger.initTightJunctionsArray(this.agentNum);


        // Perform diffusion-reaction relaxation

        if(!diffusionReactionOnAgentTime) {
            LogFile.chronoMessageIn();
            for (DiffusionSolver aSolver : solverList)
                aSolver.initAndSolve();
            LogFile.chronoMessageOut("Solving Diffusion-reaction");
        }


        // Perform agent stepping
        agentGrid.step(solverList, diffusionReactionOnAgentTime);
        LogFile.chronoMessageOut("Simulating agents");


        //calculate density solute
        //Chris Johnson 4/9/2014
        // calculateDensitySolute();


        //calculate Carrying CapacityS solute
        //Chris Johnson 4/23/2014
        //calculateCarryingCapacitySolute();


        for (SoluteComposition sc : soluteCompositionList) {
            sc.compose(Simulator.is3D);
        }


        // output result files
        // this will output if we're close to the output period but haven't
        // quite hit it (which happens sometimes due to floating point issues)
        // (this will also output for sure on the last step)

        if (((SimTimer.getCurrentTime() - _lastOutput)
                >= (_outputPeriod - 0.01 * SimTimer.getCurrentTimeStep())) ||
                SimTimer.simIsFinished())
            writeReport();


        //sonia 26.04.2010
        //only remove the agents from the system after recording all the information about active
        //and death/removed biomass
        agentGrid.removeAllDead();
        //updateChart();

        SimTimer.updateTimeStep(world);
        LogFile.writeEndOfStep(System.currentTimeMillis() - startTime);
    }


    public void updateChart() {
        int nSolute = soluteDic.size();
        int nSpecies = speciesDic.size();

        for (int iSolute = 0; iSolute < nSolute; iSolute++)
            _graphics.updateChart(0, iSolute, SimTimer.getCurrentTime(), world.getBulk("tank")
                    .getValue(iSolute));

        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++)
            _graphics.updateChart(1, iSpecies, SimTimer.getCurrentTime(),
                    speciesList.get(iSpecies).getPopulation());

        _graphics.repaintAndSave();


    }

	/* ________________ INITIALIZATION PROCEDURE ___________________________ */

    /**
     *
     */
    public void createSimulator() {
        //System.out.print("\t Simulator: ");
        XMLParser localRoot = new XMLParser(_protocolFile.getChildElement("simulator"));

        //sonia: read the flag from protocol file to decide if this is a chemostat run
        isChemostat = localRoot.getParamBool("chemostat");

        agentTimeStep = localRoot.getParamTime("agentTimeStep");

        diffusionReactionOnAgentTime = localRoot.getParamBool("diffusionReactionOnAgentTime");


        //sonia 05.2011 bug fix: the code was not finding the random.state file because no path was
        //being given to the File class to check if the file existed.
        File randomFile = new File(Idynomics.currentPath + File.separator + "random.state");

        if (randomFile.exists()) {
            /* if a file called random.state exists, the random number generator is initialised using this file.
             *  this ensures that the random number stream does not overlap when running repeated simulations with the same protocol file
			 *  Chinmay 11/08/2009
			 */

            FileInputStream randomFileInputStream;
            ObjectInputStream randomObjectInputStream;
            try {
                randomFileInputStream = new FileInputStream(Idynomics.currentPath + File.separator + "random.state");
                randomObjectInputStream = new ObjectInputStream(randomFileInputStream);
                ExtraMath.setRandom((MTRandom) randomObjectInputStream.readObject());
                LogFile.writeLog("Read in random number generator");
            } catch (Exception e) {
                LogFile.writeLog("Simulator.createSimulator() : error met while reading in random number state file" + e);
                System.exit(-1);
            } finally {
                try {
                    System.out.println("rng test: " + ExtraMath.random.nextInt());
                } catch (java.lang.NullPointerException npe) {
                    System.out.println("blah!");
                }
            }
        } else {
            //Chinmay - added MTRandom.java to utils and changed the RNG to use that class instead. 11/08/2009
            // System.out.println("No random file here!");
            randSeed = (long) localRoot.getParamDbl("randomSeed");
            ExtraMath.setRandom(new MTRandom(randSeed));
        }

        simTimer = new SimTimer(localRoot);

        // need to reset the time & iterate if we're restarting a run
        if (localRoot.getParamBool("restartPreviousRun")) {

            simTimer.setTimerState(_resultPath + File.separator
                    + "lastIter" + File.separator
                    + "env_Sum(last).xml");
        }

        createDictionary();

        //System.out.println("done");
    }

    public void createFiles(String resultPath) {
        XMLParser localRoot = new XMLParser(_protocolFile.getChildElement("simulator"));

        _outputPeriod = localRoot.getParamTime("outputPeriod");

        // Initialise data files
        // bvm 26.1.2009: added passing of current iterate to output files to
        // make restarting more robust
        result = new ResultFile[6];
        int currentIter = SimTimer.getCurrentIter();
        result[0] = new ResultFile(resultPath, "env_State", currentIter);
        result[1] = new ResultFile(resultPath, "env_Sum", currentIter);
        result[2] = new ResultFile(resultPath, "agent_State", currentIter);
        result[3] = new ResultFile(resultPath, "agent_Sum", currentIter);

        //sonia 26.04.2010
        //result files for death/removed biomass
        result[4] = new ResultFile(resultPath, "agent_StateDeath", currentIter);
        result[5] = new ResultFile(resultPath, "agent_SumDeath", currentIter);
        // Initialise povray files
        // Rob: no need in a chemostat
        if (!Simulator.isChemostat && writePOV) {
            povRayWriter = new PovRayWriter();
            povRayWriter.initPovRay(this, resultPath);
        }
    }

    public void createDictionary() {
        LinkedList<Element> list;

		/* Build the list of "solutes" markup _________________________ */
        list = _protocolFile.buildSetMarkUp("solute");
        soluteDic = new ArrayList<String>(list.size());
        soluteList = new SoluteGrid[list.size()];
        for (Element aChild : list)
            soluteDic.add(aChild.getAttributeValue("name"));

		/* Build the dictionary of particles _________________________ */
        list = _protocolFile.buildSetMarkUp("particle");
        particleDic = new ArrayList<String>(list.size());
        particleRegulatorDic = new HashMap<String, Boolean>(list.size());
        for (Element aChild : list) {
            particleDic.add(aChild.getAttributeValue("name"));
            // MD Flann add the regualtor flag into the dictionary
            String S = aChild.getAttributeValue("regulator");
            boolean value = Boolean.parseBoolean(S);
            particleRegulatorDic.put(aChild.getAttributeValue("name"), value);
            LogFile.writeLog(S);
        }

        // Trick to guarantee that the EPS compartment (capsule) is in
        // last position if it exists
        if (particleDic.remove("capsule")) particleDic.add("capsule");

		/* Build the dictionary of reactions _________________________ */
        list = _protocolFile.buildSetMarkUp("reaction");
        reactionDic = new ArrayList<String>(list.size());
        reactionList = new Reaction[list.size()];
        for (Element aChild : list)
            reactionDic.add(aChild.getAttributeValue("name"));

		/* Build the dictionary of molecular reactions _________________________ */
        XMLParser molecularReactionParser = new XMLParser(_protocolFile.getChildElement("molecularReactions"));
        if (molecularReactionParser.get_localRoot() != null) {
            list = molecularReactionParser.buildSetMarkUp("molecularReaction");
            molecularReactionDic = new ArrayList<String>(list.size());
            molecularReactionList = new MolecularReaction[list.size()];
            for (Element aChild : list)
                molecularReactionDic.add(aChild.getAttributeValue("name"));
        }

		/* Build the dictionary of species ___________________________ */
        list = _protocolFile.buildSetMarkUp("species");
        speciesDic = new ArrayList<String>(list.size());
        for (Element aChild : list)
            speciesDic.add(aChild.getAttributeValue("name"));

		/* Build the dictionary of solvers ___________________________ */
        list = _protocolFile.buildSetMarkUp("solver");
        solverDic = new ArrayList<String>(list.size());
        solverList = new DiffusionSolver[list.size()];
        for (Element aChild : list)
            solverDic.add(aChild.getAttributeValue("name"));

    }

    /**
     * Create the world properties e.g. system size and boundary conditions.
     * Currently you can create zero-flow, cyclic, constant and dilution
     * boundary ; to create on on your own, create a new class extending the
     * abstract class "BoundaryCondition"
     */
    public void createWorld() {
        // Creation of the world
        try {
            //System.out.print("\t World: \n");
            XMLParser parser = new XMLParser(_protocolFile.getChildElement("world"));
            world = new World();
            world.init(this, parser);

            for (Agar anAgar : world.agarList)
                anAgar.initialize();
            // now set the bulk concentrations if it is needed
            if (useBulkFile) recreateBulkConditions();

            //System.out.println("\t done");
        } catch (Exception e) {
            LogFile.writeLog("Simulator.createWorld() : error met");
            System.exit(-1);
        }
    }

    /**
     * Get the bulk concentrations from the input file and assign them to the
     * current bulks
     *
     * @added 23.1.2009
     * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu)
     */
    public void recreateBulkConditions() throws Exception {
        String bulkName;
        int soluteIndex;
        String soluteName;
        XMLParser simulationRoot = new XMLParser(bulkFile.getChildElement("simulation"));

        for (Element aBulkMarkUp : simulationRoot.buildSetMarkUp("bulk")) {

            XMLParser aBulkRoot = new XMLParser(aBulkMarkUp);
            bulkName = aBulkRoot.getAttributeStr("name");

            // check to make sure the bulk exists
            if (!world.containsBulk(bulkName))
                throw new Exception("Bulk " + bulkName + " is not specified in protocol file");

            LogFile.writeLog("\t\tInitializing bulk '" + bulkName + "' from input file.");

            Bulk thisBulk = world.getBulk(bulkName);

            // now set the solutes within this bulk
            for (Element aSoluteMarkUp : aBulkRoot.buildSetMarkUp("solute")) {
                XMLParser aSoluteRoot = new XMLParser(aSoluteMarkUp);

                soluteName = aSoluteRoot.getAttributeStr("name");
                soluteIndex = getSoluteIndex(soluteName);

                // check consistency with protocol file declarations
                if (!soluteDic.contains(soluteName))
                    throw new Exception("Solute " + soluteName + " is not in protocol file");

                // finally set the value

                thisBulk.setValue(soluteIndex, Double.parseDouble(aSoluteRoot.getElement().getValue()));
                LogFile.writeLog("\t\tsolute " + soluteName + " is now: " + thisBulk.getValue(soluteIndex));
            }

            LogFile.writeLog("\t\tInitialized bulk '" + bulkName + "' from input file.");
        }

    }

    /**
     * Create all soluble species as spatial grids. The protocol File specifies
     * the solute name, its diffusivity, eventually the connected bulks and
     * their concentrations
     */
    public void createSolutes() {
        //System.out.print("\t Solutes: \n");
        try {

            XMLParser simulationRoot = null;
            if (useSoluteFile) simulationRoot = new XMLParser(soluteFile.getChildElement("simulation"));

            int iSolute = 0;
            String soluteName;
            for (Element aSoluteMarkUp : _protocolFile.buildSetMarkUp("solute")) {
                XMLParser aSoluteRoot = new XMLParser(aSoluteMarkUp);
                soluteList[iSolute] = new SoluteGrid(this, aSoluteRoot);
                soluteName = soluteList[iSolute].getName();
                LogFile.writeLog("\t\t" + soluteName + " (" + soluteList[iSolute].soluteIndex + ")");


                if (useSoluteFile) {
                    recreateSolutes(soluteName, simulationRoot, iSolute);
                }
                iSolute++;

            }

            //System.out.println("\t done");
        } catch (Exception e) {
            LogFile.writeLog("Simulator.createSolutes() : error met " + e);

            System.exit(-1);
        }
    }

    //added by chris johnson
    //5/28/2015
    //recreates the solutes to recreate a simulation
    private void recreateSolutes(String soluteName, XMLParser simulationRoot, int soluteIndex) {
        for (Element aSoluteMarkup : simulationRoot.buildSetMarkUp("solute")) {
            XMLParser aSoluteRoot = new XMLParser(aSoluteMarkup);
            if (soluteName.equalsIgnoreCase(aSoluteRoot.getAttributeStr("name"))) {
                soluteList[soluteIndex].loadFromReport(aSoluteRoot.getElement().getValue());
            }
        }
    }


    public void createSoluteCompositions() {
        SoluteComposition aSoluteComposition;
        XMLParser soluteCompositionsMarkUp = _protocolFile.getChild("SoluteCompositions");
        if (soluteCompositionsMarkUp.get_localRoot() != null) {
            String domain = soluteCompositionsMarkUp.getAttribute("domain");
            List<Element> soluteCompositionsMarkUps = soluteCompositionsMarkUp.buildSetMarkUp("SoluteComposition");
            soluteCompositionList = new SoluteComposition[soluteCompositionsMarkUps.size()];
            int i = 0;
            for (Element aCompositionMarkUp : soluteCompositionsMarkUps) {
                XMLParser aCompositioRoot = new XMLParser(aCompositionMarkUp);
                aSoluteComposition = (SoluteComposition) aCompositioRoot.instanceCreator("simulator.reaction");
                aSoluteComposition.init(this, aCompositioRoot, domain);
                soluteCompositionList[i++] = aSoluteComposition;
            }
        } else soluteCompositionList = new SoluteComposition[0];

    }

    /**
     * Create all reactions described in the protocol file
     */
    public void createReactions() throws Exception {
        Reaction aReaction;

        //System.out.print("\t Reactions: \n");
        molecularKineticRegulators = new HashMap<String, double[][][]>();
        int iReaction = 0;
        for (Element aReactionMarkUp : _protocolFile.buildSetMarkUp("reaction")) {
            XMLParser aReactionRoot = new XMLParser(aReactionMarkUp);
            aReaction = (Reaction) aReactionRoot.instanceCreator("simulator.reaction");
            aReaction.init(this, aReactionRoot);

            // register the created object into the reactions container
            reactionList[iReaction] = aReaction;
            iReaction++;
            aReaction.register(aReaction.reactionName, this);
            System.out.println("\t\t" + aReaction.reactionName + " (" + aReaction.reactionIndex + ")");
        }
        //System.out.println("\t done");

        //System.out.print("\t Molecular Reactions: \n");


        iReaction = 0;
        XMLParser molecularReactionParser = new XMLParser(_protocolFile.getChildElement("molecularReactions"));
        MolecularReaction mReaction;
        if (molecularReactionParser.get_localRoot() != null) {
            for (Element aReactionMarkUp : molecularReactionParser.buildSetMarkUp("molecularReaction")) {
                XMLParser aReactionRoot = new XMLParser(aReactionMarkUp);
                mReaction = new MolecularReaction();
                mReaction.initFromProtocolFile(this, aReactionRoot);
                // register the created object into the reactions container

                System.out.println("\t\t" + mReaction.reactionName + " (" + iReaction + ")");
                molecularReactionList[iReaction] = mReaction;
                iReaction++;
            }
        }
        //System.out.println("\t done");
    }

    /**
     * Create the solvers for the diffusion-reaction-system described in the
     * protocol file.
     */
    public void createSolvers() throws Exception {
        // System.out.print("\t Solvers: \n");

        for (Element aSolverMarkUp : _protocolFile.buildSetMarkUp("solver")) {
            // Initialise the XML parser
            XMLParser parser = new XMLParser(aSolverMarkUp);

            // Create the solver,initialise it and register it
            DiffusionSolver aSolver =
                    (DiffusionSolver) parser.instanceCreator("simulator.diffusionSolver");
            aSolver.init(this, parser);
            aSolver.register();
            //System.out.println("\t\t" + aSolver.solverName + " (" + aSolver.solverIndex + ")");
        }


        //Create the list of molecular solvers.
        int solverIndex = 0;
        for (Element aRSolverMarkUp : _protocolFile.buildSetMarkUp("molecularSolver")) {
            // Initialise the XML parser
            XMLParser parser = new XMLParser(aRSolverMarkUp);

            // Create the solver,initialise it and register it
            molecularReactionManager = new molecularReactionManager();
            molecularReactionManager.init(this, parser);
            System.out.println("\t\t" + molecularReactionManager.solverName + " (" + solverIndex + ")");
            solverIndex++;
        }

        //System.out.println("\t done");
    }

    /**
     * Create the agentGrid, the species and the agents
     */
    public void createSpecies() throws Exception {
        try {
            // Create the Species (and the progenitor) and register it
            //System.out.print("\t Species: \n");
            for (Element aSpeciesMarkUp : _protocolFile.buildSetMarkUp("species")) {
                Species aSpecies = new Species(this, new XMLParser(aSpeciesMarkUp));
                speciesList.add(aSpecies);
                LogFile.writeLog("\t\t" + aSpecies.speciesName + " (" + aSpecies.speciesIndex + ")");
            }
            // System.out.print("\t done\n");

            // Create the agent grid
            // System.out.print("\t Agent Grid: \n");
            XMLParser parser = new XMLParser(_protocolFile.getChildElement("agentGrid"));
            agentGrid = new AgentContainer(this, parser, agentTimeStep);
            is3D = agentGrid.is3D;
            // System.out.print("\t done\n");

            // Finalise the initialisation of the progenitor
            // System.out.print("\t Species progenitor: \n");
            for (Element aSpeciesMarkUp : _protocolFile.buildSetMarkUp("species")) {
                parser = new XMLParser(aSpeciesMarkUp);
                getSpecies(parser.getAttribute("name")).getProgenitor().initFromProtocolFile(this, parser);
                //sonia: creating a list with the plasmid names which will be used afterwards to write the agentSum report
            }
            //System.out.print("\t done\n");

            // Create the population
            //System.out.print("\t Species populations: \n");

            if (useAgentFile) {
                recreateSpecies();

                //sonia: 20-07-09
                //I've added the line of code below, so that it is possible to create new agents from the protocol file when restarting
                //from a previous simulation (environment) using a new protocol file
                checkAgentBirth();

            } else {
                checkAgentBirth();
            }
            // System.out.print("\t done\n");
        } catch (Exception e) {
            LogFile.writeLog("Simulator.createSpecies(): error met: " + e);
        }
    }

    /**
     * Create the agents from a file describing individually all the agents
     * species by species
     * @throws Exception
     * @author ad
     */
    public void recreateSpecies() throws Exception {
        int spIndex, counterSpecies;
        SpecialisedAgent progenitor;
        XMLParser simulationRoot = new XMLParser(agentFile.getChildElement("simulation"));
        counterSpecies = 0;

        for (Element aSpeciesMarkUp : simulationRoot.buildSetMarkUp("species")) {

            XMLParser aSpeciesRoot = new XMLParser(aSpeciesMarkUp);
            spIndex = getSpeciesIndex(aSpeciesRoot.getAttribute("name"));

            // check consistency with protocol file declarations
            boolean isConsistent = (speciesList.get(counterSpecies).speciesName.equals(aSpeciesRoot
                    .getAttributeStr("name")));
            if (!isConsistent) throw new Exception(
                    "Agent input file is inconsistent with protocol file: ");

            // Process agents description
            String dataSource = aSpeciesRoot.getElement().getContent(0).toString();
            String[] allAgentData = dataSource.split(";\n");
            // this removes the '[Text: \n' line from the first item in the list
            // so that all agents will be treated equally
            allAgentData[0] = allAgentData[0].substring(8);

            progenitor = speciesList.get(spIndex).getProgenitor();

            // don't use the last index because it is a string with only ']'
            for (int i = 0; i < allAgentData.length - 1; i++)
                (progenitor.sendNewAgent()).initFromResultFile(this, allAgentData[i].split(","));

            LogFile.writeLog(speciesList.get(counterSpecies).speciesName + " : "
                    + speciesList.get(counterSpecies).getPopulation()
                    + " agents created from input file.");
            counterSpecies++;
        }
    }

    /**
     * Test if new bacteria should be created in the system
     */
    public void checkAgentBirth() {
        isInitializing = true;
        XMLParser parser;
        int spIndex;
        boolean creatingAgents = false;

        for (Element aSpeciesMarkUp : _protocolFile.buildSetMarkUp("species")) {
            parser = new XMLParser(aSpeciesMarkUp);
            spIndex = getSpeciesIndex(parser.getAttribute("name"));

            for (Element aInitMarkUp : parser.buildSetMarkUp("initArea")) {
                parser = new XMLParser(aInitMarkUp);
                if (SimTimer.isDuringNextStep(parser.getParamTime("birthday"))) {
                    this.agentGrid.agentIter = agentGrid.agentList.listIterator();
                    speciesList.get(spIndex).createPop2(parser);
                    creatingAgents = true;
                }
            }
        }
        isInitializing = false;
        //Changed by Farzin Check this later
        //if (creatingAgents) agentGrid.relaxGrid();
    }

	/* _____________________________________________________________________ */

    /**
     * Generate a report with concentrations of solutes and biomass on the
     * default grid and a report with the exhaustive description of all agents
     * Each report is a new file with a new index
     */
    public void writeReactionFrequencyReport() {
        try {
            FileWriter fs = new FileWriter(_resultPath + File.separator + "ReactionFrequencySummary.txt");
            StringBuffer output = new StringBuffer("Reaction ID\tFrequency\n");

            for (int i = 0; i < molecularReactionList.length; i++) {
                String reac = "";
                if (molecularReactionList[i]._reactantMolecules.length > 0)
                    reac = molecularReactionList[i]._reactantMolecules[0];
                for (int j = 1; j < molecularReactionList[i]._reactantMolecules.length; j++)
                    reac += "+" + molecularReactionList[i]._reactantMolecules[j];
                reac += " -> ";
                if (molecularReactionList[i]._productMolecules.length > 0)
                    reac += molecularReactionList[i]._productMolecules[0];
                for (int j = 1; j < molecularReactionList[i]._productMolecules.length; j++)
                    reac += "+" + molecularReactionList[i]._productMolecules[j];
                if (reac.equalsIgnoreCase(" -> "))
                    reac = molecularReactionList[i].type;
                output.append(reac + "\t" + molecularReactionList[i].reactionName + "\t" + molecularReactionList[i].numOccurence + "\n");
            }
            fs.write(output.toString());
            fs.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void writeReport() {


        // Update saving counters and file index
        _lastOutput = SimTimer.getCurrentTime();
        int currentIter = SimTimer.getCurrentIter(); // bvm added 26.1.2009

        farzin.Logger.iterationNumberWriter(this);

        if (writeEnvOutput) {
            //Write section values for each solute
            for (int i = 0; i < soluteList.length; i++)
                soluteList[i].writeOutput(_resultPath, SimTimer.getCurrentIter());


            if (farzin.Logger.writeMovementsMagnitudeFlag)
                farzin.Logger.writeMovementMagnitudes(this);


            if (farzin.Logger.writeTightJunctionCount)
                farzin.Logger.writeTightJunctionCount(this, agentGrid.agentList.size());

            // first restart log file to avoid non-write trouble
            LogFile.reopenFile();

            try {
            /* Grids and environment ______________________________________ */
                // env_State
                result[0].openFile(currentIter);
                // env_Sum
                result[1].openFile(currentIter);
                // bvm added 16.12.08

                //sonia:chemostat
                if (Simulator.isChemostat) {
                    //sonia:chemostat
                    //I've modified refreshBiofilmGrids()
                    soluteList[0].getDomain().refreshBioFilmGrids();
                } else {
                    // output the biofilm thickness data
                    //sonia 12.10.09

                    double[] intvals;
                    StringBuffer value = new StringBuffer();
                    for (Domain aDomain : world.domainList) {

                        //double _resolution = aDomain._resolution;
                        aDomain.refreshBioFilmGrids();
                        if (useDetachment) {
                            intvals = aDomain.getInterface();

                            value.append("<thickness domain=\"" + aDomain.domainName + "\" unit=\"um\">\n");
                            value.append("\t<mean>" + (ExtraMath.mean(intvals)) + "</mean>\n");
                            value.append("\t<stddev>" + (ExtraMath.stddev(intvals)) + "</stddev>\n");
                            value.append("\t<max>" + (ExtraMath.max(intvals)) + "</max>\n");
                            value.append("</thickness>\n");
                        }
                    }
                    result[0].write(value.toString());
                    result[1].write(value.toString());
                }

                // Add description of each solute grid
                for (SoluteGrid aSG : soluteList) {
                    aSG.writeReport(result[0], result[1]);
                }

                // Add description of each reaction grid
                for (Reaction aReac : reactionList) {
                    aReac.writeReport(result[0], result[1]);
                }

                // Add description of each species grid
                agentGrid.writeGrids(this, result[0], result[1]);

                // Add description of total biomass
                for (Domain aDomain : world.domainList) {
                    aDomain.refreshBioFilmGrids();
                    aDomain.getBiomass().writeReport(result[0], result[1]);
                }

                // Add description of bulks
                for (Bulk aBulk : world.bulkList) {
                    aBulk.writeReport(result[1]);
                }

                result[0].closeFile();
                result[1].closeFile();

            } catch (Exception e) {
                LogFile.writeError("System description of grids failed:" + e.getMessage(),
                        "Simulator.writeReport()");
            }
        }

        try {
            /* Agents ____________________________________________________ */
            result[2].openFile(currentIter);
            result[3].openFile(currentIter);
            result[4].openFile(currentIter);
            result[5].openFile(currentIter);

            agentGrid.writeReport(this, result[2], result[3]);
            agentGrid.writeReportDeath(this, result[4], result[5]);


            result[2].closeFile();
            result[3].closeFile();
            result[4].closeFile();
            result[5].closeFile();

            // Rob 15/2/2011: No need to write povray if it's a chemostat
            if (!Simulator.isChemostat && writePOV) {
                povRayWriter.write(currentIter);
                //povRayWriter.writeTuring(currentIter);
            }

            LogFile.writeLog("System description finalized");


        } catch (Exception e) {
            LogFile.writeError("System description of agents failed:" + e.getMessage(),
                    "Simulator.writeReport()");
        }

    }

    private void writeAgar() {
        int currentIter = SimTimer.getCurrentIter();
        try {
            FileOutputStream fs = new FileOutputStream(_resultPath + File.separator + "SoluteConcentration" + File.separator + "Agar " + LogFile.numberToString(currentIter) + ".txt");
            String output = "";

            for (int i = 0; i < world.agarList.get(0)._depth + 2; i++) {
                for (int j = 0; j < world.agarList.get(0)._nJ + 2; j++)
                    output += Double.toString(world.agarList.get(0).getSectionValue(j, i)) + "\t";
                output += '\n';
            }
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


//    private void writeSoluteSectionOutput(int index) {
//        int currentIter = SimTimer.getCurrentIter();
//
//        //density is now calculated earlier so it can be used in the simulation
//        //so no need to calculate it again here
//        //just grab it like you would any solute
//        //Chris Johnson 4/9/2014
//        try {
//            String fileName;
//
//
//            fileName = soluteList[index].getName();
//            //k = (soluteList[index]._nK + 1) / 2;
//            String output;
//            //Qanita Total Solute in each iteration
//            TotalSolute = 0;
//            int[] is = new int[5];
//            int inc = soluteList[index]._nI / 4;
//            is[0] = 0;
//            is[1] = 1;
//            is[2] = 2;
//            is[3] = 3;
//            is[4] = 4;
//
//            for (int i : is) {
//                output = "";
//                for (int j = 0; j < soluteList[0]._nJ; j++) {
//                    for (int k = 0; k < soluteList[0]._nK; k++) {
//                        output += Double.toString((soluteList[index].grid[i][j][k])) + "\t";
//                        if (fileName.endsWith("VEGF"))
//                            TotalSolute = TotalSolute + soluteList[index].grid[i][j][k];
//
//                    }
//                    output += '\n';
//                }
//                File f = new File(_resultPath + File.separator + "SoluteConcentration" + File.separator + i + File.separator + fileName + " solute " + LogFile.numberToString(currentIter) + ".txt");
//                f.getParentFile().mkdir();
//                FileWriter fs = new FileWriter(f);
//
//                if (fileName.endsWith("VEGF")) {
//
//                    FileWriter fs2 = new FileWriter(_resultPath + File.separator + "Concentration_Per_cell" + File.separator + fileName + "Allsolute_per_cell" + ".txt");
////		FileWriter fs3=new FileWriter(_resultPath+File.separator+"Concentration_Per_cell"+File.separator+ fileName +" solute per cell  "+ LogFile.numberToString(currentIter)+".txt");
//                    FileWriter fs3 = new FileWriter(_resultPath + File.separator + "Concentration_Per_cell" + File.separator + fileName + "Allsolute_per_cellDetailed" + ".txt");
//
//                    if (currentIter != 0 && agentGrid.species_Name.endsWith("Patch00")) {
//
//                        output2 += currentIter + "\t" + TotalSolute + "\t" + agentGrid.species_number + "\t" + TotalSolute / (agentGrid.species_number);
//                    }
//                    output3 += TotalSolute / (agentGrid.species_number);
//                    output2 += '\n';
//                    output3 += '\n';
//
//                    fs2.append(output3);
//                    fs2.close();
//                    fs3.append(output2);
//                    fs3.close();
//
//                }
//                if (currentIter == 72) {
//                    output2 = "";
//                    output3 = "";
//                }
//
//                fs.write(output);
//                fs.close();
//
//            }
//        } catch (FileNotFoundException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        } catch (IOException e) {
//            // TODO Auto-generated catch block
//            e.printStackTrace();
//        }
//
//    }


    private static int findSoluteIndexByName(String name, SoluteGrid[] sgs) {
        for (int i = 0; i < sgs.length; i++) {
            if (sgs[i].getName().equalsIgnoreCase(name)) return i;
        }
        return -1;
    }

    //calculate density solute at a specific coordinate
    //Chris Johnson 4/9/2014
    public static double calcDensityAround(int x, int y, int z, double[][][] grid) {
//                int xUp = 2;
//                int yUp = 4;
//
//                int zUp = 2;
//                if (is3D) zUp = -1;
//
//                int nI = grid.length;
//                int nJ = grid[0].length;
//                int nK = grid[0][0].length;
//                double accMass = 0;
//                for (int i = -1; i < xUp; i++)
//                    {
//                        int p1 = x + i;
//                        p1 = p1 <= 0 ? 1 : p1;
//                        p1 = p1 >= nI ? nI - 1 : p1;
//                        for (int j = -1; j < yUp; j++)
//                            {
//                                int p2 = y + j;
//                                p2 = p2 <= 0 ? 1 : p2;
//                                p2 = p2 >= nJ ? nJ - 1 : p2;
//
//                                for (int k = -1; k < zUp; k++)
//                                    {
//                                        int p3 = z + k;
//                                        p3 = p3 <= 0 ? 1 : p3;
//                                        p3 = p3 >= nK ? nK - 1 : p3;
//
//                                    }
//                            }
//                    }
//
//                if(is3D) zUp = 1;
        // return accMass / (xUp * yUp * zUp);


//                if(x ==grid.length) x  =grid.length -1;
//                if(y ==grid[0].length) y  =grid[0].length -1;
//                if(is3D && z == grid[0][0].length) z  =grid[0][0].length - 1;

        return grid[x][y][z];
    }




	/* ____________________ ACCESSORS & MUTATORS ___________________________ */

    /**
     * Find a Species on the basis of its nickname
     *
     * @param aSpeciesName
     * @return the speciesIndex
     */
    public int getSpeciesIndex(String aSpeciesName) {
        return speciesDic.indexOf(aSpeciesName);
    }

    public Species getSpecies(String aSpeciesName) {
        return speciesList.get(getSpeciesIndex(aSpeciesName));
    }

    public int getSoluteIndex(String aSoluteName) {
        return soluteDic.indexOf(aSoluteName);
    }

    public SoluteGrid getSolute(String aSoluteName) {
        return soluteList[getSoluteIndex(aSoluteName)];
    }

    public int getReactionIndex(String aReactionName) {
        return reactionDic.indexOf(aReactionName);
    }

    public Reaction getReaction(String aReactionName) {
        return reactionList[getReactionIndex(aReactionName)];
    }

    public int getMolecularReactionIndex(String aReactionName) {
        return molecularReactionDic.indexOf(aReactionName);
    }

    public MolecularReaction getMolecularReaction(String aReactionName) {
        return molecularReactionList[getMolecularReactionIndex(aReactionName)];
    }


    public int getSolverIndex(String aSolverName) {
        return solverDic.indexOf(aSolverName);
    }

    public DiffusionSolver getSolver(String aSolverName) {
        int solInd = getSolverIndex(aSolverName);
        if (solInd >= 0)
            return solverList[solInd];
        else
            return null;
    }

    public int getParticleIndex(String particleName) {
        return particleDic.indexOf(particleName);
    }

    // MD Flann
    public boolean getparticleRegulator(String particleName) {
        return particleRegulatorDic.get(particleName);
    }

	/* _____________________________ GUI ________________________________ */

    public void play() {
    }

    public void pause() {
    }

    public void stop() {
    }

    /**
     * Check whether we will initialize from agent and bulk input files
     * If the "restartPreviousRun" param is true, this will also set
     * the correct files for reading in the last state
     *
     * @param protocolFile
     */
    public void detectInputs(String protocolFile) {

        // first check whether we are restarting from a previous run
        XMLParser restartInfo = new XMLParser(_protocolFile.getChildElement("simulator"));

        if (restartInfo.getParamBool("restartPreviousRun")) {
            // if this is true, then we set the input files as the last files
            // that were output

            useAgentFile = true;
            useBulkFile = true;

            agentFile = new XMLParser(_resultPath + File.separator
                    + "lastIter" + File.separator
                    + "agent_State(last).xml");
            bulkFile = new XMLParser(_resultPath + File.separator
                    + "lastIter" + File.separator
                    + "env_Sum(last).xml");

            LogFile.writeLog("Restarting run from previous state in directory: "
                    + _resultPath);

            return;
        }

        // otherwise just do things as usual, but only if input is specified
        if (_protocolFile.getChildElement("input") == null) return;

        XMLParser input = new XMLParser(_protocolFile.getChildElement("input"));

        useAgentFile = input.getParamBool("useAgentFile");
        if (useAgentFile) {
            String agentFileName = input.getParam("inputAgentFileURL");
            // construct the input file name using the path of the protocol file
            //int index = protocolFile.lastIndexOf(File.separator);
            // agentFileName = protocolFile.subSequence(0, index + 1) + agentFileName;

            agentFile = new XMLParser(agentFileName);
            LogFile.writeLog("Using agent input file: " + agentFileName);
        }
        useBulkFile = input.getParamBool("useBulkFile");
        if (useBulkFile) {
            String bulkFileName = input.getParam("inputBulkFileURL");
            // construct the input file name using the path of the protocol file
            int index = protocolFile.lastIndexOf(File.separator);
            bulkFileName = protocolFile.subSequence(0, index + 1) + bulkFileName;

            bulkFile = new XMLParser(bulkFileName);
            LogFile.writeLog("Using bulk input file: " + bulkFileName);
        }


        useSoluteFile = input.getParamBool("useSoluteFile");
        if (useSoluteFile) {
            String soluteFileName = input.getParam("inputSoluteFileURL");
            soluteFile = new XMLParser(soluteFileName);
            LogFile.writeLog("Using solute input file: " + soluteFileName);
        }


        XMLParser varParser = new XMLParser(input.getChildElement("variables"));
        if (varParser.get_localRoot() == null)
            return;
        vars = new farzin.Variable[varParser.buildSetMarkUp("variable").size()];
        int counter = 0;
        for (Element varMarkUp : varParser.buildSetMarkUp("variable")) {
            String name = varMarkUp.getAttributeValue("name");
            double value = Double.valueOf(varMarkUp.getText());
            vars[counter] = new Variable(name, value);
            counter++;
        }

    }

    public String getResultPath() {
        return _resultPath;
    }

}