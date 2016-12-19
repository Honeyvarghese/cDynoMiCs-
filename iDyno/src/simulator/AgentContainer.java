/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ______________________________________________________
 * <p>
 * Stores all the agents, call them and manages shoving/erosion of located agents
 *
 * @version 1.0
 * @author Andreas Doetsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sonia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Doetsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sonia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @since June 2006
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas Doetsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sonia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 *
 */

package simulator;

import farzin.Logger;
import idyno.SimTimer;

import java.util.*;

import simulator.agent.*;
import simulator.agent.zoo.Yeast;
import simulator.detachment.*;
import simulator.diffusionSolver.DiffusionSolver;
import simulator.diffusionSolver.Solver_pressure;
import simulator.geometry.*;
import simulator.geometry.boundaryConditions.AllBC;
import utils.ResultFile;
import utils.XMLParser;
import utils.LogFile;
import utils.ExtraMath;

public class AgentContainer {

	/* __________________ Properties __________________________ */

    //Define by Qanita for optimization
    public String species_Name;
    public double species_number;


    public Domain    domain;
    public Simulator mySim;

    // Container for all agents (even the non located ones)

    public LinkedList<SpecialisedAgent>   agentList;
    public ListIterator<SpecialisedAgent> agentIter;

    // Temporary containers used to store agents who will be added or removed
    //sonia 27.04.2010
    //changed visibility to public so that it can be accessed from LocatedGroup in killAll()
    public LinkedList<SpecialisedAgent> _agentToKill = new LinkedList<SpecialisedAgent>();


    private SpatialGrid[] _speciesGrid;

    // Definition of the spatial grid ____________________________ */
    private int _nI, _nJ, _nK, _nTotal;
    private   double         _res;
    private   LocatedGroup[] _grid;
    protected double[][][]   _erosionGrid;
    public    boolean        is3D;

    // Parameters of the shoving algorithm _________________________________ */
    private final double SHOV_FRACTION;

    public final  double     AGENTTIMESTEP;
    private final int        MAXITER;
    private final boolean    MUTUAL;
    // boolean to select erosion method: true is shrinkOnBorder, false is removeOnBorder
    private final boolean    EROSIONMETHOD;
    private final boolean    DOSLOUGHING;
    private       SoluteGrid _pressure;
    double vMass = 0; // tally of mass to be removed

    // Current number and maximal number of shoving iterations
    int shovIter, shovLimit, maxShoveIter, nMoved;
    double tMoved, deltaMove;

    private LevelSet _levelset;

    //sonia 23.11.09
    //variables related to the agentFlushedAway() method
    private int    dead;
    private double agentsToDilute;
    private double Dfactor;
    private double pDying;


	/* _______________________ CONSTRUCTOR __________________________________ */

    /**
     * XML protocol file-based constructor
     */
    public AgentContainer(Simulator aSimulator, XMLParser root, double agentTimeStep) throws Exception {
        // Read FINAL fields
        SHOV_FRACTION = root.getParamDbl("shovingFraction");
        MAXITER = root.getParamInt("shovingMaxIter");
        MUTUAL = root.getParamBool("shovingMutual");

        if (root.getParam("erosionMethod") == null)
            EROSIONMETHOD = true;
        else
            EROSIONMETHOD = root.getParamBool("erosionMethod");

        if (root.getParam("sloughDetachedBiomass") == null)
            DOSLOUGHING = false; // default to carry out sloughing //Changed from true to false by Farzin

        else
            DOSLOUGHING = root.getParamBool("sloughDetachedBiomass");

        //sonia 20/01/2011 - now using getParamTime() function that correctly converts time units
        //double value = root.getParamDbl("agentTimeStep");

        double value = agentTimeStep;


        if (Double.isNaN(value)) {
            AGENTTIMESTEP = SimTimer.getCurrentTimeStep();
            LogFile.writeLog("Using global timestep of " + AGENTTIMESTEP + " for agentTimeStep");
        } else {
            AGENTTIMESTEP = value;
            if (AGENTTIMESTEP > SimTimer.getCurrentTimeStep()) {
                LogFile.writeLog("ERROR: agentTimeStep in agentGrid markup MUST be " +
                        "less than or equal to the global timestep\n" +
                        "\tagentTimeStep was given as: " + AGENTTIMESTEP + "\n" +
                        "\tglobal time step is currently: " + SimTimer.getCurrentTimeStep());
                throw new Exception("agentTimeStep too large");
            }
            LogFile.writeLog("Agent time step is... " + value);
        }

        // Reference to the domain where this container is defined
        domain = (Domain) aSimulator.world.getDomain(root
                .getParam("computationDomain"));
        mySim = aSimulator;
        agentList = new LinkedList<SpecialisedAgent>();
        agentIter = agentList.listIterator();
        // Optimised the resolution of the grid used to sort located agents
        //System.out.println("checkgridsize");
        checkGridSize(aSimulator, root);

        // Now initialise the padded grid

        createShovGrid(aSimulator);
        // Initialise spatial grid used to display species distribution
        createOutputGrid(aSimulator);

        if (Simulator.isChemostat) {
            //skip detachment
        } else {
            // initialise the pressure grid, if there is one
            if (aSimulator.getSolver("pressure") != null)
                _pressure = ((Solver_pressure) aSimulator.getSolver("pressure")).getPressureGrid();

            // Initialise the levelset solver used for detachment

            //set a default value for detachment, set bu Farzin
            if (root.getChild("detachment").get_localRoot() == null) {
                mySim.useDetachment = false;
//				_levelset  =(LevelSet) Class.forName("simulator.detachment.DS_Quadratic").newInstance();
//				_levelset.init(this);
            } else {
                mySim.useDetachment = true;
                _levelset = LevelSet.staticBuilder(root.getChild("detachment"), this);
            }

        }
        LogFile.writeLog(" " + _nTotal + " grid elements, resolution: " + _res
                + " micrometers");

    }

	/* ___________________ STEPPERS ________________________________ */

    /**
     * Call each grid cell of the agentGrid
     */
    public void step(DiffusionSolver[] solverList, boolean solveDiffusionReactions) {

        SpecialisedAgent anAgent;
        int              nDead, nAgent, nBirth;

		/* STEP AGENTS ________________________________________________ */
        LogFile.chronoMessageIn();
        Collections.shuffle(agentList, ExtraMath.random);

        // record values at the beginning
        nDead = 0;
        nBirth = 0;
        nAgent = agentList.size();
        double dt             = 0;
        double elapsedTime    = 0;
        double globalTimeStep = SimTimer.getCurrentTimeStep();
        // for the local time step, choose the value according to which is best
        double localdt = Math.min(AGENTTIMESTEP, globalTimeStep);

        double nAgent0 = agentList.size();
        // Apply a shorter time step when visiting all the agents

        while (elapsedTime < globalTimeStep) {


            if (solveDiffusionReactions) {
                LogFile.chronoMessageIn();
                for (DiffusionSolver aSolver : solverList)
                    aSolver.initAndSolve();
                LogFile.chronoMessageOut("Solving Diffusion-reaction");
            }


            // by default use the saved agent timestep
            dt = localdt;


            // check for a smaller dt (usually for the last iterate)
            if (dt > (globalTimeStep - elapsedTime))
                dt = globalTimeStep - elapsedTime;

            elapsedTime += dt;


			/* Step all the agents */
            SimTimer.setCurrentTimeStep(dt);
            //SimTimer.applyRelativeAgentTimeStep(dt);

            //sonia:chemostat
            if (Simulator.isChemostat) {
                //sonia: bypass bacteria movement according to pressure field
            } else {

                //followPressure() call is  commited by Qanita if pressure is not effect the result
                //	followPressure(); //TJ Flann presure not needed for these problems
            }

            int agentCounter = 0;
            for (agentIter = agentList.listIterator(); agentIter.hasNext(); ) {
                anAgent = agentIter.next();
                anAgent.step();
                agentCounter++;
            }

            Collections.shuffle(agentList, ExtraMath.random);


            //Revision 1 Farzin
            //double[] turingValues=new double[agentCounter];
            agentCounter = 0;
            for (agentIter = agentList.listIterator(); agentIter.hasNext(); ) {
                anAgent = agentIter.next();
                anAgent.activationInhibitionOperation();
                //turingValues[agentCounter]= ((Yeast) anAgent).newTuringValue;
                agentCounter++;
            }

            Collections.shuffle(agentList, ExtraMath.random);
            //FarzinLogger.Logger.detailedTuringValue(turingValues, mySim, elapsedTime);

            Yeast.meanTuringValue /= agentCounter;
            //FarzinLogger.Logger.meanOfTuringValue(Yeast.meanTuringValue, mySim);
            Yeast.meanTuringValue = 0;
            //End Revision 1
            if (Simulator.isChemostat) {
                agentFlushedAway(dt);
            }


            // Add and remove agents
            nBirth += agentList.size() - nAgent;

            //sonia 26.04.2010
            //commented out removeAllDead
            // this call is now made at the end of the step in Simulator
            //nDead += removeAllDead();
            nAgent = agentList.size();

            //sonia 27.04.2010
            //removing mislocalized/death agents from the agentList, so that we can proceed
            //with the shoving algorithm
            // the piece of code below was taken from removeAllDead() but with the difference that we are not
            // removing the agents from the _agentToKill list (so that it can be used to record the
            //information on death agents later on)

            for (SpecialisedAgent aDeathAgent : _agentToKill) {
                if (aDeathAgent.isDead) {
                    nDead++;
                    agentList.remove(aDeathAgent);
                    removeLocated(aDeathAgent);
                }
            }

            // Apply moderate overlap relaxation

            //sonia:chemostat
            //11-06-09
            if (Simulator.isChemostat) {
                //sonia: bypass the shoving
            } else {
                shoveAllLocated(false, true, MAXITER, 1, 1);
            }
        }

        SimTimer.setCurrentTimeStep(globalTimeStep);


        LogFile.chronoMessageOut("Agents stepped/dead/born: " + nAgent0 + "/"
                + nDead + "/" + nBirth);

		/* MECHANICAL INTERACTIONS _____________________________________ */

        //sonia:chemostat
        //11-06-09
        if (Simulator.isChemostat) {
            // bypass the shoving, detachment and erosion processes

        } else {

            //sonia 26.04.2010
            //care as been take so that the death agents are removed from the
            // _agentList preventing their participation in the shoving process
            // spring and then shove only particles

            shoveAllLocated(false, true, MAXITER, 1, 1);
            LogFile.writeLog(nMoved + "/" + agentList.size() + " after " + shovIter
                    + " shove iterations");


            // EROSION & DETACHMENT _________________________________________ */
            // Refresh the space occupation map (-1:outside, 0:carrier,1:biofilm, 2:liquid, 3:bulk)
            refreshGroupStatus();

            // Rebuild the border of the biofilm and compute erosion-time for the
            // whole biofilm
            if (mySim.useDetachment) {
                _levelset.refreshBorder(true, mySim);
                _levelset.computeLevelSet(mySim);

                shrinkOnBorder();

                // mark biomass connected to the carrier and remove any non-connected portions
                if (DOSLOUGHING) {
                    refreshGroupStatus();
                    markForSloughing();
                }

                LogFile.chronoMessageOut("Detachment");
            } else
                LogFile.chronoMessageOut("Detachment Skipped");

        }
    }


    /**
     * Compute pressure field and apply resulting advection movement
     */
    public double followPressure() {
        double moveMax;

        // Find a solver for pressure field and use it
        if (mySim.getSolver("pressure") == null) return 0;

        // don't use the pressure if it's not active
        if (!mySim.getSolver("pressure").isActive()) return 0;

        LogFile.writeLog("Doing pressure calculations.");


        // get local timestep (which was set in the step() routine calling this one)
        double dt = SimTimer.getCurrentTimeStep();


        // Solve for pressure field
        mySim.getSolver("pressure").initAndSolve();
        _pressure = ((Solver_pressure) mySim.getSolver("pressure")).getPressureGrid();


        // copy calculated pressure field to the solute list
        // (allows easy output of pressure field)
        mySim.getSolute("pressure").setGrid(_pressure.getGrid());

        // Determine local advection speed
        moveMax = 0;
        int testS = 0;
        for (LocatedGroup aGroup : _grid) {
            testS++;
            moveMax = Math.max(moveMax, aGroup.computeMove(_pressure, AGENTTIMESTEP));
        }

        // bvm 04.03.09: new method to address any high velocities:
        // use smaller local timesteps to keep the movement under control
        double dtlocal = dt;
        int    itlocal = 1;
        while (dtlocal > this._res / moveMax) {
            // if the move takes an agent farther than one grid element,
            // apply scaling factor until move is within limit
            dtlocal /= 10.;
            itlocal *= 10;
        }
        if (itlocal > 1) {
            LogFile.writeLog("PRESSURE MOVEMENT HAS LOCAL TIMESTEP "
                    + dtlocal + " (" + itlocal + " iterations)");
        }

        // scale movement vectors based on new, smaller timestep and apply
        // the movement to each agent in each group
        double alpha = dtlocal / dt;
        for (LocatedGroup aGroup : _grid)
            aGroup.addMoveToAgents(alpha);

        // now apply the scaled agent movements to each agent
        deltaMove = 0;
        for (int i = 0; i < itlocal; ++i) {
            agentIter = agentList.listIterator();
            while (agentIter.hasNext())
                deltaMove += agentIter.next().move();
        }

        return deltaMove;
    }


    /**
     * Solve spatial spreading (acts only on located agents)
     *
     * @param fullRelax
     * @param maxShoveIter
     */
    public void shoveAllLocated(boolean fullRelax, boolean shoveOnly,
                                double maxShoveIter, double gainMin, double gainMax) {

        if (fullRelax)
            maxShoveIter = MAXITER * 5;

        nMoved = shovLimit = Math.max(1,
                (int) (agentList.size() * SHOV_FRACTION));
        shovIter = 0;
        double[] moved = new double[(int) maxShoveIter];
        //CHEMOTAXIS, added a flag set to false when shoving
        while ((nMoved >= shovLimit) & (shovIter++ < maxShoveIter)) {
            moved[(int) shovIter - 1] = performMove(shoveOnly, false, false, 1);
           // System.out.println("shovIteration"+shovIter+"...>" + moved[(int) shovIter - 1]);
        }
        // once shoved, do the chemotaxis
        if (!fullRelax)
            performMove(shoveOnly, false, true, 1);
        if (Logger.writeNumMovementsFlag)
            farzin.Logger.detailedNumberofMovements(moved, mySim);
    }

    /**
     * Used during initialisation to start from a coherent state
     */
    public void relaxGrid() {

        //sonia:chemostat
        if (!Simulator.isChemostat) {
            Collections.shuffle(agentList, ExtraMath.random);
            shoveAllLocated(true, true, MAXITER / 2, 0.1, 0.25);
            shoveAllLocated(true, true, MAXITER / 2, 0.1, 1);
        }
    }

	/* _________________________________________________________________ */

    /**
     *
     */
    protected double performMove(boolean pushOnly, boolean isSynchro, boolean chemotaxisOnly,
                                 double gain) {
        SpecialisedAgent anAgent;
        nMoved = 0;
        tMoved = 0;
        double nMoved2 = 0;

        resetInteractionMatrix();
        for (agentIter = agentList.listIterator(); agentIter.hasNext(); ) {
            // Compute movement, deltaMove is relative movement
            anAgent = agentIter.next();
            // MD Flann
            // do chemotaxis between shoves
            if (chemotaxisOnly)
                deltaMove = anAgent.moveChemotaxis(!isSynchro, gain);
            else deltaMove = anAgent.interact(MUTUAL, pushOnly, !isSynchro, gain);

            tMoved += deltaMove;
            nMoved += (deltaMove >= 0.1 * gain ? 1 : 0);
            nMoved2 += (deltaMove >= 0.1 ? 1 : 0);
        }

        if (!isSynchro)
            return nMoved2;

        for (agentIter = agentList.listIterator(); agentIter.hasNext(); ) {
            // Compute movement, deltaMove is relative movement
            anAgent = agentIter.next();
            deltaMove = anAgent.move();

            tMoved += deltaMove;
            nMoved += (deltaMove >= 0.1 * gain ? 1 : 0);
            nMoved2 += (deltaMove >= 0.1 ? 1 : 0);

            if (anAgent.isDead) {
                anAgent.death = "invalidMove";
                //_agentToKill.add(anAgent);
                //sonia 26.04.2010
                //added agentList.remove(anAgent);
                //agentList.remove(anAgent);
            }
        }
        //sonia 26.04.2010
        //commented out removeAllDead()
        //removeAllDead();
        return nMoved2;
    }
    /* ____________________________ SHOVING FUNCTIONS _____________________ */

    protected void refreshGroupStatus() {
        for (int index = 0; index < _nTotal; index++)
            _grid[index].refreshElement();
    }

    /**
     * Explore grid cells around the current one and sends (sonia: don't you mean returns?..) all agents contained :
     * including grid cells on the other side of the cyclic boundary
     *
     * @param agentPosition
     * @param range :
     *            maximal range to screen
     * @param nbList:
     *            the container where to store found particles
     */
    public void getPotentialShovers(int index, double range,
                                    LinkedList<LocatedAgent> nbList) {
        LocatedGroup aGroup;
        int          radius = Math.max(1, (int) Math.floor(range / this._res));

        nbList.clear();

        for (int i = -radius; i <= radius; i++) {
            if (_grid[index].moveX(i) == null)
                continue;
            for (int j = -radius; j <= radius; j++) {

                if (!is3D) {
                    aGroup = _grid[index].moveX(i).moveY(j);

                    if (aGroup != null)
                        nbList.addAll(aGroup.group);

                } else {
                    if (_grid[index].moveX(i).moveY(j) == null)
                        continue;
                    for (int k = -radius; k <= radius; k++) {
                        aGroup = _grid[index].moveX(i).moveY(j).moveZ(k);
                        if (aGroup != null)
                            nbList.addAll(aGroup.group);
                    }
                }
            }
        }
    }

	/* ________________ TOOLS:GRID, MAP & TREE MANAGEMENT __________________ */

    public void registerBirth(SpecialisedAgent anAgent) {
        // Add the agent to agentList
        agentIter.add(anAgent);
        anAgent._birthId = mySim.agentNum++;
        // Add the agent on the grid
        if (anAgent instanceof LocatedAgent) {
            LocatedAgent aLoc = (LocatedAgent) anAgent;
            try {
                if (Simulator.isChemostat) {
                    _grid[0].add(aLoc);
                } else {
                    int index = getIndexedPosition(aLoc.getLocation());
                    if (!Double.isNaN(index))
                        _grid[index].add(aLoc);
                }
            } catch (Exception e) {
                LogFile.writeLog("Error:Failed to add an agent on the grid");
            }
        }

    }

    public void registerDeath(SpecialisedAgent anAgent) {

        //sonia 27.04.2010
        // just to make sure we are not adding death agents to the list that have already been added...
        if (!_agentToKill.contains(anAgent))
            _agentToKill.add(anAgent);

    }

    public int removeAllDead() {
        int nDead = 0;

        ListIterator<SpecialisedAgent> iter = _agentToKill.listIterator();
        SpecialisedAgent               anAgent;

        while (iter.hasNext()) {
            anAgent = iter.next();
            if (anAgent.isDead) {
                nDead++;
                iter.remove();
                agentList.remove(anAgent);
                removeLocated(anAgent);
            }
        }

        _agentToKill.clear();
        return nDead;
    }

    /**
     * @author Sonia
     *
     * @param agentTimestep - this should be the same as the global timeStep or lower
     * This method removes a number of agents from the system according to the dilution
     * set for the chemostat.
     * The global time step should be set to 0.10*(1/D) so that around 10% of the agents
     * will be removed from the system in each iteration.
     * Remember, agents stand for all type of particles that can be removed (aka deleted)
     * from the system, from bacteria to eps.
     */
    public void agentFlushedAway(double agentTimeStep) {

        // after having shuffled the list (during the step()) with all the agents we are now ready to kill
        // agents according to the dilution value read from the Bulk class

        //sonia:2.03.2010 Just in case, let's do it again
        Collections.shuffle(agentList, ExtraMath.random);

        double randNum;

        dead = 0;

        for (AllBC aBC : domain.getAllBoundaries()) {
            if (aBC.hasBulk()) {
                Bulk aBulk = aBC.getBulk();
                if (aBulk.getName().equals("chemostat")) {
                    Dfactor = aBulk._D;
                }
            }
        }

        agentsToDilute = Math.round(Dfactor * (agentTimeStep) * agentList.size());

        pDying = agentsToDilute / agentList.size();

        for (int i = 0; i < agentList.size(); i++) {
            randNum = ExtraMath.getUniRand();
            if (randNum < pDying) {
                agentList.get(i).isDead = true;
                agentList.get(i).death = "dilution";
                agentList.get(i).die(false);
                dead++;
            }

        }

        SpecialisedAgent anAgent;
        agentIter = agentList.listIterator();

        while (agentIter.hasNext()) {
            anAgent = agentIter.next();
            if (anAgent.isDead) {
                agentIter.remove();
                agentList.remove(anAgent);
            }
        }


    }

    /**
     *
     * @param anAgent
     */
    public void removeLocated(SpecialisedAgent anAgent) {
        if (anAgent instanceof LocatedAgent) {
            LocatedAgent aLoc = (LocatedAgent) anAgent;
            int index = getIndexedPosition(aLoc.getLocation());
            if (!Double.isNaN(index))
                _grid[index].remove(aLoc);
        }
    }

    /**
     * Update the position of the agent on the agent grid
     * @param anAgent
     */
    public void registerMove(LocatedAgent anAgent) {
        // Compute the theoretical index on the agentGrid
        int newIndex = getIndexedPosition(anAgent.getLocation());
        int oldIndex = anAgent.getGridIndex();

        // If gridIndex has changed, update the references
        if (isValid(anAgent.getLocation())) {
            if (newIndex != oldIndex) {
                _grid[oldIndex].remove(anAgent);
                _grid[newIndex].add(anAgent);
                anAgent.setGridIndex(newIndex);
            }
        } else {
            utils.LogFile.writeLog("Agent location is not valid -> Killed");
            //anAgent.death = "overBoard";
            anAgent.die(false);
        }
    }

    public void fitAgentMassOnGrid(SpatialGrid biomassGrid) {
        for (int i = 0; i < _nTotal; i++) {
            for (LocatedAgent aLoc : _grid[i].group) {
                aLoc.fitMassOnGrid(biomassGrid);
            }
        }
    }

    public void fitAgentMassOnGrid(SpatialGrid yeastGrid, String type) {
        yeastGrid.setAllValueAt(0);
        int occuredIndex;
        for (int i = 0; i < _nTotal; i++) {
            for (LocatedAgent aLoc : _grid[i].group) {
                //if(aLoc.getSpecies().speciesName.equalsIgnoreCase(type))
                occuredIndex = aLoc.getClass().getName().lastIndexOf(type);
                if (occuredIndex >= 0 && occuredIndex + type.length() == aLoc.getClass().getName().length())
                    aLoc.fitMassOnGrid(yeastGrid);
            }
        }
    }


    public void fitAgentVolumeRateOnGrid(SpatialGrid biomassGrid) {
        biomassGrid.setAllValueAt(0d);
        for (int i = 0; i < _nTotal; i++) {
            for (LocatedAgent aLoc : _grid[i].group) {
                aLoc.fitVolRateOnGrid(biomassGrid);
                ;
            }
        }
    }

    public void resetInteractionMatrix() {
        //interactionMatrixPush=new boolean[mySim.agentNum][mySim.agentNum];
//		interactionMatrixTightJunctions=new boolean[mySim.agentNum][mySim.agentNum];
    }


	/* ____________________ REPORT FILE EDITION__________________________ */

    /**
     *
     */
    public void writeGrids(Simulator aSim, ResultFile bufferState,
                           ResultFile bufferSum) throws Exception {
        LocatedAgent aLoc;

        //sonia:chemostat
        //I've modified the refreshElement() method for the chemostat case

        // Refresh the space occupation map (0:carrier,1:biofilm or 2:liquid)
        for (int index = 0; index < _nTotal; index++) {
            _grid[index].refreshElement();
        }


		/* Build a grid of biomass concentration */

        // Set existing grid to zero
        for (int iSpecies = 0; iSpecies < aSim.speciesList.size(); iSpecies++)
            _speciesGrid[iSpecies].setAllValueAt(0);

        // Sum biomass concentrations
        for (SpecialisedAgent anA : agentList) {
            if (anA instanceof LocatedAgent) {
                aLoc = (LocatedAgent) anA;
                aLoc.fitMassOnGrid(_speciesGrid[aLoc.speciesIndex]);

            }
        }

        // now output the biomass values
        for (int iSpecies = 0; iSpecies < aSim.speciesList.size(); iSpecies++) {
            _speciesGrid[iSpecies].writeReport(bufferState, bufferSum);
        }

        //		// output pressure field (not needed here b/c it's output w/ the solutes)
        //		if (aSim.getSolver("pressure").isActive())
        //			_pressure.writeReport(bufferState, bufferSum);

		/* Build a grid of detachment */
        //printLevelSet(bufferState);
    }

    /**
     *
     * @param aSim
     * @param bufferState
     * @param bufferSum
     * @throws Exception
     */


    public void writeReport(Simulator aSim, ResultFile bufferState, ResultFile bufferSum) throws Exception {

        // bvm 10.2.2009: include information about the shoving grid
        StringBuffer gridInfo = new StringBuffer();
        gridInfo.append("<grid");
        gridInfo.append(" resolution=\"").append(_res).append("\"");
        gridInfo.append(" nI=\"").append(_nI).append("\"");
        gridInfo.append(" nJ=\"").append(_nJ).append("\"");
        gridInfo.append(" nK=\"").append(_nK).append("\"");
        gridInfo.append("/>\n");
        bufferState.write(gridInfo.toString());
        bufferSum.write(gridInfo.toString());


        // Detail the header
        Species aSpecies;
        int     spIndex, nSpecies;

        nSpecies = aSim.speciesList.size();
        StringBuffer[] speciesBuffer = new StringBuffer[nSpecies];

        // Initialise a Species markup for each present species
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
            aSpecies = aSim.speciesList.get(iSpecies);
            speciesBuffer[iSpecies] = new StringBuffer();

            speciesBuffer[iSpecies].append("<species name=\"");
            speciesBuffer[iSpecies].append(aSpecies.speciesName).append("\" ");

            speciesBuffer[iSpecies].append("header=\"");
            speciesBuffer[iSpecies].append(
                    aSpecies.getProgenitor().sendHeader()).append("\"");
            speciesBuffer[iSpecies].append(">\n");
        }

        // Initialise statistics (population total mass, growth-rate)
        double[] spPop    = new double[nSpecies];
        double[] spMass   = new double[nSpecies];
        double[] spGrowth = new double[nSpecies];


        for (int i = 0; i < nSpecies; i++) {
            spPop[i] = 0;
            spMass[i] = 0;
            spGrowth[i] = 0;
        }

        // Fill the agent_state file, build the state for the summary
        LocatedAgent aLoc;


        for (SpecialisedAgent anAgent : agentList) {
            spIndex = anAgent.getSpecies().speciesIndex;
            spPop[spIndex]++;

            if (anAgent instanceof LocatedAgent & !anAgent.isDead) {
                aLoc = (LocatedAgent) anAgent;
                spMass[spIndex] += aLoc.getTotalMass();
                spGrowth[spIndex] += aLoc.getNetGrowth();


                speciesBuffer[spIndex].append(aLoc.writeOutput() + ";\n");

            }
        }

        StringBuffer text;
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
            text = new StringBuffer();
            aSpecies = aSim.speciesList.get(iSpecies);
            text.append("<species name=\"");
            text.append(aSpecies.speciesName).append("\" ");
            text.append("header=\"");
            bufferSum.write(text.toString());

            text = new StringBuffer("population,mass,growthRate");


            text.append("\" ");
            text.append(">\n");

            bufferSum.write(text.toString());

            //writing stats on population, mass and growth
            text = new StringBuffer("");
            text.append(spPop[iSpecies] + "," + spMass[iSpecies] + ","
                    + spGrowth[iSpecies]);


            text.append("</species>\n");
            bufferSum.write(text.toString());

            //Qanita agent number
            species_Name = aSpecies.speciesName;
            species_number = spPop[iSpecies];

            //	System.out.println(aSpecies.speciesName +"$$$"+ spPop[iSpecies]);
            //		System.out.println("the number of "+ species_Name +"equal:   "+ species_number);


        }

        //brian
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
            speciesBuffer[iSpecies].append("</species>\n");
            bufferState.write(speciesBuffer[iSpecies].toString());
        }

    }


    /**
     * @author Sonia
     * recording data from agents that will be removed from the simulated environment
     */


    public void writeReportDeath(Simulator aSim, ResultFile bufferStateDeath, ResultFile bufferSumDeath) throws Exception {

//Qanitaprint
        //	System.out.println("size of agentToKill list at beginning of the writeReportDeath:  " + _agentToKill.size());

        // bvm 10.2.2009: include information about the shoving grid
        StringBuffer gridInfo = new StringBuffer();
        gridInfo.append("<grid");
        gridInfo.append(" resolution=\"").append(_res).append("\"");
        gridInfo.append(" nI=\"").append(_nI).append("\"");
        gridInfo.append(" nJ=\"").append(_nJ).append("\"");
        gridInfo.append(" nK=\"").append(_nK).append("\"");
        gridInfo.append("/>\n");
        //sonia 26.04.2010 writing result files for death/removed biomass
        bufferSumDeath.write(gridInfo.toString());
        bufferStateDeath.write(gridInfo.toString());


        // Detail the header
        Species aSpecies;
        int     spIndex, nSpecies;

        nSpecies = aSim.speciesList.size();
        StringBuffer[] speciesBuffer = new StringBuffer[nSpecies];

        // Initialise a Species markup for each present species
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
            aSpecies = aSim.speciesList.get(iSpecies);
            speciesBuffer[iSpecies] = new StringBuffer();

            speciesBuffer[iSpecies].append("<species name=\"");
            speciesBuffer[iSpecies].append(aSpecies.speciesName).append("\" ");

            speciesBuffer[iSpecies].append("header=\"");
            speciesBuffer[iSpecies].append(
                    aSpecies.getProgenitor().sendHeader());
            //sonia 27.04.2010
            //introducing reason of death - header
            speciesBuffer[iSpecies].append(",death");
            speciesBuffer[iSpecies].append("\"");
            speciesBuffer[iSpecies].append(">\n");
        }

        // Initialise statistics (population total mass, growth-rate)
        double[] spPop    = new double[nSpecies];
        double[] spMass   = new double[nSpecies];
        double[] spGrowth = new double[nSpecies];


        for (int i = 0; i < nSpecies; i++) {
            spPop[i] = 0;
            spMass[i] = 0;
            spGrowth[i] = 0;
        }

        // Fill the agent_state file, build the state for the summary
        LocatedAgent aLoc;

        for (SpecialisedAgent anAgent : _agentToKill) {
            spIndex = anAgent.getSpecies().speciesIndex;
            spPop[spIndex]++;

            if (anAgent instanceof LocatedAgent) {
                aLoc = (LocatedAgent) anAgent;
                spMass[spIndex] += aLoc.getTotalMass();
                spGrowth[spIndex] += aLoc.getNetGrowth();


                speciesBuffer[spIndex].append(aLoc.writeOutput());
                //sonia 27.04.2010
                //added death information to agent's state description
                speciesBuffer[spIndex].append("," + aLoc.death + ";\n");

            }
        }

        StringBuffer text;
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
            text = new StringBuffer();
            aSpecies = aSim.speciesList.get(iSpecies);
            text.append("<species name=\"");
            text.append(aSpecies.speciesName).append("\" ");
            text.append("header=\"");
            bufferSumDeath.write(text.toString());

            text = new StringBuffer("population,mass,growthRate");


            text.append("\" ");
            text.append(">\n");

            bufferSumDeath.write(text.toString());

            text = new StringBuffer("");
            text.append(spPop[iSpecies] + "," + spMass[iSpecies] + ","
                    + spGrowth[iSpecies]);

            text.append("</species>\n");


            text.append("</species>\n");
            bufferSumDeath.write(text.toString());
        }

        //brian
        for (int iSpecies = 0; iSpecies < nSpecies; iSpecies++) {
            speciesBuffer[iSpecies].append("</species>\n");
            bufferStateDeath.write(speciesBuffer[iSpecies].toString());
        }


    }


    public void preprintLevelSet() {
        // Build the matrix of erosion time
        // _levelset.refreshFromBorder();

        for (int index = 0; index < _nTotal; index++) {
            _grid[index].printLevelSet(_erosionGrid);
        }
    }


    public void printLevelSet(ResultFile bufferState) throws Exception {

        // Edit the markup for the solute grid
        StringBuffer value = new StringBuffer();
        value.append("<solute name=\"").append("erosionSpeed");
        value.append("\" unit=\"hr");
        value.append("\" resolution=\"").append(_res);
        value.append("\" nI=\"").append(_nI);
        value.append("\" nJ=\"").append(_nJ);
        value.append("\" nK=\"").append(_nK);
        value.append("\">\n");

        // Write the markup in the file
        bufferState.write(value.toString());

        // Fill the mark-up
        if (_nK == 1) {
            // We have a 2D grid
            for (int i = 0; i < _nI + 2; i++) {
                for (int j = 0; j < _nJ + 2; j++) {
                    bufferState.write(Arrays.toString(_erosionGrid[i][j]));
                    bufferState.write(";\n");
                }
            }
        } else {
            for (int i = 0; i < _nI + 2; i++) {
                for (int j = 0; j < _nJ + 2; j++) {
                    bufferState.write(Arrays.toString(_erosionGrid[i][j]));
                    bufferState.write(";\n");
                }
            }
        }

        // Close the mark-up
        bufferState.write("\n</solute>\n");
    }

	/* ________________ INITIALISATION __________________________ */

    public void checkGridSize(Simulator aSimulator, XMLParser root) {
        _res = root.getParamDbl("resolution");

        //sonia:chemostat
        if (Simulator.isChemostat) {
            //set the resolution to the resolution of the domain
            _res = domain._resolution;
            //do not correct the grid size

        } else {
            // Eventually correct grid size
            _nI = (int) Math.ceil(domain.length_X / _res);
            _res = domain.length_X / _nI;

            _nJ = (int) Math.ceil(domain.length_Y / _res);
        }

        if (domain.is3D()) {
            _nK = (int) Math.ceil(domain.length_Z / _res);
            is3D = true;
        } else {
            _nK = 1;
            is3D = false;
        }

        //sonia:chemostat
        if (Simulator.isChemostat) {
            //sonia:chemostat
            //set the number of grid elements to 1
            _nTotal = 1;
        } else {
            _nTotal = (_nI + 2) * (_nJ + 2) * (_nK + 2);
        }

    }

    /**
     * Create a vectorized array of spatial groups, build their neighbourhood
     * and store it Note :Shoving grid is a padded of LocatedGroups
     */
    public void createShovGrid(Simulator aSimulator) {

        _grid = new LocatedGroup[_nTotal];
        for (int index = 0; index < _nTotal; index++)
            _grid[index] = new LocatedGroup(index, this, aSimulator);

        for (int index = 0; index < _nTotal; index++) {
            _grid[index].init();
        }


    }

    public void createOutputGrid(Simulator aSim) {
        _erosionGrid = new double[_nI + 2][_nJ + 2][_nK + 2];
        _speciesGrid = new SpatialGrid[aSim.speciesList.size()];
        for (int iSpecies = 0; iSpecies < aSim.speciesDic.size(); iSpecies++)
            _speciesGrid[iSpecies] = domain.createGrid(aSim.speciesDic.get(iSpecies), 0);
    }

	/* _____________________ EROSION & DETACHMENT ___________________________ */

    /**
     *
     */
    protected void markForSloughing() {
        // perform connected volume filtration (connected to bottom
        // (cvf is true for connected elements, and false for non-connected)
        boolean[] cvf = (new ConnectedVolume(_nI, _nJ, _nK)).computeCvf(_grid);

        int    nRemoved = 0;
        double mRemoved = 0;

        // mark as detachable all particles in non-valid map positions
        for (int index = 0; index < _nTotal; index++) {
            // if (_grid[index].status==1&&!cvf[index]) {
            if (!cvf[index]) {
                // if it's not connected, remove the agents
                if (_grid[index].totalMass > 0) {
                    nRemoved += _grid[index].group.size();
                    mRemoved += _grid[index].totalMass;

                    _grid[index].killAll();
                }
            }
        }

        LogFile.writeLog("Sloughing " + nRemoved + " ("
                + ExtraMath.toString(mRemoved, false) + " fg)");
    }

    /**
     *
     *
     */
    public void shrinkOnBorder() {
        // find the border points (erosion and sloughing have changed the
        // configuration)


        _levelset.refreshBorder(true, mySim);

        double mass    = 0;
        double vMass   = 0;
        int    nDetach = 0;
        int    index;
        double ratio;

        // System.out.print("erosion ratio: ");
        for (LocatedGroup aBorderElement : _levelset.getBorder()) {
            index = aBorderElement.gridIndex;

            ratio = SimTimer.getCurrentTimeStep() / _grid[index].erosionTime;
            ratio = Math.max(0, Math.min(ratio, 1));
            vMass = aBorderElement.totalMass * ratio;


            for (LocatedAgent aLoc : _grid[index].group) {
                mass += aLoc.getTotalMass() * ratio;
                for (int iComp = 0; iComp < aLoc.particleMass.length; iComp++)
                    aLoc.particleMass[iComp] *= 1 - ratio;

                aLoc.updateSize();
                if (aLoc.willDie()) {
                    mass += aLoc.getTotalMass();
                    aLoc.die(false);
                    aLoc.death = "detachment";

                    nDetach++;
                }
            }
            if (mass > vMass)
                continue;
        }

        //sonia 26.04.2010
        //commented out removeAllDead();
        //removeAllDead();

        LogFile.writeLog("Eroding " + nDetach + " ("
                + ExtraMath.toString(mass, true) + "/"
                + ExtraMath.toString(vMass, true) + " fg)");
    }


    private double detFunction(double i, double location) {
        if (i < 1) {
            return ExtraMath.sq(_res - (location % _res));
        } else {
            return ExtraMath.sq(location % _res);
        }
    }

	
	/* __________________________ GET & SET _________________________________ */

    /**
     * Check that these coordinates are defined on this grid (the grid is padded
     * but dc uses shifted coordinates)
     */
    public boolean isValid(DiscreteVector dC) {
        boolean out = true;
        out &= (dC.i >= 0) & (dC.i < _nI);
        out &= (dC.j >= 0) & (dC.j < _nJ);
        out &= (dC.k >= 0) & (dC.k < _nK);
        return out;
    }

    // Check that the nb grid cell has valid coordinates
    public boolean isValid(ContinuousVector cC) {
        return isValid(getGridPosition(cC));
    }


    /**
     * find the voxel a continuous coordinate lies in and return the index
     *
     * @param cc:the
     *            continuous coordinate to find the index for
     * @return index on the 1D (vectorized) array
     */
    public int getIndexedPosition(ContinuousVector cc) {

        //sonia:chemostat
        // to guarantee that the agents are effectively removed
        if (Simulator.isChemostat) {
            return 0;

        } else {

            int i = (int) Math.floor(cc.x / _res) + 1;
            int j = (int) Math.floor(cc.y / _res) + 1;
            int k = (int) Math.floor(cc.z / _res) + 1;

            return i + j * (_nI + 2) + k * (_nI + 2) * (_nJ + 2);
        }
    }


    public int getIndexedPosition(DiscreteVector dc) {
        int i = dc.i + 1;
        int j = dc.j + 1;
        int k = dc.k + 1;

        return i + j * (_nI + 2) + k * (_nI + 2) * (_nJ + 2);
    }

    public DiscreteVector getGridPosition(ContinuousVector cC) {
        int i = (int) Math.floor(cC.x / _res);
        int j = (int) Math.floor(cC.y / _res);
        int k = (int) Math.floor(cC.z / _res);

        return new DiscreteVector(i, j, k);
    }

    public DiscreteVector getGridPosition(int index) {
        int k = (int) Math.floor(index / (_nI + 2) / (_nJ + 2));
        int j = (int) Math.floor((index - k * ((_nI + 2) * (_nJ + 2)))
                / (_nI + 2));
        int i = (int) Math.floor((index - (k * (_nI + 2) * (_nJ + 2)) - j
                * (_nI + 2)));

        return new DiscreteVector(i - 1, j - 1, k - 1);
    }

    public ContinuousVector getGridLocation(int index) {
        int k = (int) Math.floor(index / (_nI + 2) / (_nJ + 2));
        int j = (int) Math.floor((index - k * ((_nI + 2) * (_nJ + 2)))
                / (_nI + 2));
        int i = (int) Math.floor((index - (k * (_nI + 2) * (_nJ + 2)) - j
                * (_nI + 2)));

        return new ContinuousVector((i + .5 - 1) * _res, (j + .5 - 1) * _res,
                (k + .5 - 1) * _res);
    }


    public double getAgentTimeStep() {
        return AGENTTIMESTEP;
    }


    public double getResolution() {
        return _res;
    }

    public int[] getGridDescription() {
        int[] out = {_nI, _nJ, _nK};
        return out;
    }

    public LocatedGroup[] getShovingGrid() {
        return _grid;
    }

    public LevelSet getLevelSet() {
        return _levelset;
    }

}
