/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * <p>
 * <p>
 * ______________________________________________________
 *
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @since June 2006
 */

/**
 * ______________________________________________________
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */

package simulator.agent;

import iDynoOptimizer.Global.FileReaderWriter;
import idyno.SimTimer;
import org.jdom.Element;
import simulator.Simulator;
import simulator.SoluteGrid;
import simulator.SpatialGrid;
import simulator.geometry.ContinuousVector;
import simulator.geometry.Domain;
import simulator.geometry.boundaryConditions.AllBC;
import utils.ExtraMath;
import utils.LogFile;
import utils.XMLParser;

import java.awt.*;
import java.util.*;
import java.util.List;

public abstract class LocatedAgent extends ActiveAgent implements Cloneable {

    /* Temporary variables stored in static fields __________________________ */
    protected static ContinuousVector _diff;
    protected static ContinuousVector _newLoc;

    /* Parameters specific to the agent _____________________________________ */
    protected double _radius, _totalRadius;
    protected double _volume, _totalVolume;

    /* Agent's location ____________________________________________________ */
    // Agent position and agent movement are expressed with continuous
    // coordinates
    public    ContinuousVector         _location          = new ContinuousVector();
    public    ContinuousVector         _movement          = new ContinuousVector();
    protected ContinuousVector         _divisionDirection = new ContinuousVector();
    protected LinkedList<LocatedAgent> _myNeighbors       = new LinkedList<LocatedAgent>();
    protected LinkedList<LocatedAgent> _myTightJunctions  = new LinkedList<LocatedAgent>();
    // set to true when the particle has changed into its current type at the last iteration
    protected boolean                  _newInThisType     = true; //TJ Flann
    // Index of the agent position on the vectorized grid
    protected int _agentGridIndex;


    // Detachment priority
    public double detPriority = 0;

    // for timestep issues
    public double _timeSinceLastDivisionCheck = Double.MAX_VALUE;

    //sonia 8-12-2010
    // distance based probability from a given neighbour (used in HGT)
    public double _distProb    = 0;
    public double _distCumProb = 0;

    //Farzin Variables
    protected LinkedList<LocatedAgent> _tempNeighbors  = new LinkedList<LocatedAgent>();
    protected int                      _angleCount     = 12;
    protected boolean[]                _neighborAngles = new boolean[_angleCount];
    protected boolean                  atSkin          = false;


    List<AllBC> attachedToBoundaries = new ArrayList<>();





	/* _______________________ CONSTRUCTOR _________________________________ */

    /**
     * Empty constructor
     */
    public LocatedAgent() {
        super();
        _speciesParam = new LocatedParam();
        // FLANN create hashtable
        //_species.adhesionSpecies = new Hashtable<String, Double>();
    }

    @SuppressWarnings("unchecked")
    public Object clone() throws CloneNotSupportedException {
        LocatedAgent o = (LocatedAgent) super.clone();

        o._location = (ContinuousVector) this._location.clone();
        o._movement = (ContinuousVector) this._movement.clone();
        o._divisionDirection = (ContinuousVector) this._divisionDirection
                .clone();
        o._myNeighbors = (LinkedList<LocatedAgent>) this._myNeighbors.clone();
        o._myTightJunctions = (LinkedList<LocatedAgent>) this._myTightJunctions.clone(); //TJ Flann
        o._agentGridIndex = this._agentGridIndex;

        return (Object) o;
    }

    /**
     * Create a new agent with mutated parameters based on species default
     * values and specifies its position
     */
    /**
     * Create an agent (who a priori is registered in at least one container;
     * this agent is located !
     */
    public void createNewAgent(ContinuousVector position) {
        try {
            // Get a clone of the progenitor
            LocatedAgent baby = (LocatedAgent) sendNewAgent();
            baby.giveName();
                // randomize its mass
                baby.mutatePop();
                baby.updateSize();

            // Just to avoid to be in the carrier
            position.x += this._totalRadius;
            baby.setLocation(position);

            baby.registerBirth();
        } catch (CloneNotSupportedException e) {
            utils.LogFile.writeLog("Error met in LocAgent:createNewAgent()");
        }
    }

    /**
     * Register the agent on the agent grid and on the guilds
     */
    public void registerBirth() {
        //Added by Farzin
//		if(_species.canAttachToBoundary && _location.x<1.2*this._totalRadius)
//		{
//			_frozenVertical=true;
//			//_frozenHorizontal=true;
//		}
        //
        // Register on species and reaction guilds
        super.registerBirth();
    }

    // TJ Flann, move adhesion and TJ upto located agent so all particles can have these mechanisms
    public void initFromProtocolFile(Simulator aSim, XMLParser aSpeciesRoot) {
        // Initialisation of the Bacterium
        super.initFromProtocolFile(aSim, aSpeciesRoot);

        _diff = new ContinuousVector();
        _newLoc = new ContinuousVector();

        String withSpecies;
        double strength;
        //Reading Adhesion Parameters
        XMLParser adhesionParser = new XMLParser(aSpeciesRoot.getChildElement("adhesions"));
        if (adhesionParser.get_localRoot() != null) { //TJ Flann stop error if no adhesion specified
            for (Element adhesionMarkUp : adhesionParser.buildSetMarkUp("adhesion")) {
                XMLParser parser = new XMLParser(adhesionMarkUp);
                withSpecies = parser.getAttribute("withSpecies");
                strength = parser.getAttributeDbl("strength");
                if (_species.adhesionSpecies.get(withSpecies) == null)
                    _species.adhesionSpecies.put(withSpecies, strength);
            }
        }
        //Reading Tight Junction Parameters TJ Flann
        double stiffness = 0.0;
        XMLParser tightJunctionParser = new XMLParser(
                aSpeciesRoot.getChildElement("tightJunctions"));
        if (tightJunctionParser.get_localRoot() != null) {
            for (Element tightJunctionMarkUp : tightJunctionParser.buildSetMarkUp("tightJunction")) {
                XMLParser parser = new XMLParser(tightJunctionMarkUp);
                withSpecies = parser.getAttribute("withSpecies");
                stiffness = parser.getAttributeDbl("stiffness");

                if (_species.tightJunctionSpecies.get(withSpecies) == null)
                    _species.tightJunctionSpecies.put(withSpecies, stiffness);
            }
        }
    }

    public void initFromResultFile(Simulator aSim, String[] singleAgentData) {

        _diff = new ContinuousVector();
        _newLoc = new ContinuousVector();

        // this routine will read data from the end of the singleAgentData array
        // and then pass the remaining values onto the super class

        // Chemostat "if" added by Sonia 27.10.09
        // Rearranged by Rob 10.01.11

        // find the position to start at by using length and number of values to be read
        int nValsRead  = 5;
        int iDataStart = singleAgentData.length - nValsRead;

        if (Simulator.isChemostat) {

            // Rob: this is necessary for the case when biofilm agents in one simulation
            // are transferred into a chemostat for the next.
            _location.set(0, 0, 0);

        } else {

            double newAgentX, newAgentY, newAgentZ;
            newAgentX = Double.parseDouble(singleAgentData[iDataStart]);
            newAgentY = Double.parseDouble(singleAgentData[iDataStart + 1]);
            newAgentZ = Double.parseDouble(singleAgentData[iDataStart + 2]);
            _location.set(newAgentX, newAgentY, newAgentZ);

        }

        // agent size
        _radius = Double.parseDouble(singleAgentData[iDataStart + 3]);
        _totalRadius = Double.parseDouble(singleAgentData[iDataStart + 4]);

        // now go up the hierarchy with the rest of the data
        String[] remainingSingleAgentData = new String[iDataStart];
        for (int i = 0; i < iDataStart; i++)
            remainingSingleAgentData[i] = singleAgentData[i];

        super.initFromResultFile(aSim, remainingSingleAgentData);
    }

	/* _____________________HIGH-LEVEL METHODS _____________________________ */

    /**
     * Called at each time step (under the control of the method Step of the
     * class Agent to avoid multiple calls
     */
    protected void internalStep() {
        // Compute mass growth over all compartments
        LogFile.writeLog("IN INTERNAL STEP");
        grow();

        // Apply this mass growth of all compounds on global radius and mass
        updateSize();

        // Divide if you have to
        if (willDivide()) divide();


        // Die if you have to
        if (willDie())
            die(true);


    }

    /**
     * Update the radius of the agent from the current mass (and then the
     * volume) of the agent (EPS included)
     */
    public void updateSize() {
        // Update the totalMass field (sum of the particles masses)
        updateMass();
        if (_totalMass < 0)
            LogFile.writeLog("Warning: negative mass on agent " + _family + ", " + _genealogy);

        // Sum of (particles masses / particles density)
        updateVolume();

        // Compute radius according to the volume
        updateRadius();

        //sonia:chemostat
        if (Simulator.isChemostat) {
            //don't do the update of attachment/detachment

        } else {

            // Check if by chance the agent is close enough to a support to be
            // attached

            // updateAttachment();
        }
    }

    /**
     *
     */
    public void divide() {
        try {
            // Create a daughter cell
            makeKid();
        } catch (CloneNotSupportedException e) {
            LogFile.writeLog("Error met in LocatedAgent.divide()");
        }
    }

    public boolean willDivide() {
        //jan: commented out since the logic of our simple cell division rule is divide if big enough
        //if (_netGrowthRate<=0) return false;

        // this ensures that the checks for when to divide don't occur too often;
        // at most they will occur at the rate of AGENTTIMESTEP
        _timeSinceLastDivisionCheck += SimTimer.getCurrentTimeStep();
        if (_timeSinceLastDivisionCheck < _agentGrid.getAgentTimeStep())
            return false;

        // at this point we will actually check whether to divide
        _timeSinceLastDivisionCheck = 0;

        double deviateFrom = ExtraMath.deviateFrom(getSpeciesParam().divRadius, getSpeciesParam().divRadiusCV);

        return getRadius(false) > deviateFrom;
    }

    public boolean willDie() {
        if (_totalMass < 0)
            return true;
        return getRadius(false) <= ExtraMath.deviateFrom(getSpeciesParam().deathRadius, getSpeciesParam().deathRadiusCV);
    }

    /**
     * Kill an agent. Called by detachment and starving test
     */
    public void die(boolean isStarving) {
        // TJ Flann MD Flann, fixed a bug when we need to clear ourselves
        // need to clear this particle from its connected tight junctions, if relevant
        if (!_myTightJunctions.isEmpty())
            clearTightJunctionsWithNeighbors();
        _myTightJunctions.clear();
        //do the normal death thing
        super.die(isStarving);
    }

	/* ________________________________________________________________ */

    /**
     * Create a new agent from an existing one
     *
     * @throws CloneNotSupportedException
     *             Called by LocatedAGent.divide()
     */
    public void makeKid() throws CloneNotSupportedException {

        // Create the new instance
        LocatedAgent baby = (LocatedAgent) sendNewAgent();
        // Note that mutateAgent() does nothing yet
        baby.mutateAgent();
        // TJ Flann set the is new into this type flag, so we know to update its tight junctions
        _newInThisType = true;
        // Update the lineage
        recordGenealogy(baby);

        // Share mass of all compounds between two daughter cells and compute
        // new size
        divideCompounds(baby, getBabyMassFrac());
        //sonia:chemostat
        if (Simulator.isChemostat) {
            // upon division the daughter cells remain with the coordinates of their progenitor

        } else {
            // Compute movement to apply to both cells
            setDivisionDirection(getInteractDistance(baby) / 2);

            // move both daughter cells
            baby._movement.subtract(_divisionDirection);
            _movement.add(_divisionDirection);
        }
        // Now register the agent inside the guilds and the agent grid
        baby.registerBirth();
        baby._netVolumeRate = 0;


    }

    public void divideCompounds(LocatedAgent baby, double splitRatio) {
        // Choose the division plan and apply position modifications
        for (int i = 0; i < particleMass.length; i++) {
            baby.particleMass[i] *= splitRatio;
            this.particleMass[i] *= 1 - splitRatio;
        }

        // Update radius, mass and volumes
        updateSize();
        baby.updateSize();
    }

    public void transferCompounds(LocatedAgent baby, double splitRatio) {
        // Choose the division plan and apply position modifications
        double m;
        for (int i = 0; i < particleMass.length; i++) {
            m = this.particleMass[i] * splitRatio;
            baby.particleMass[i] += m;
            this.particleMass[i] = this.particleMass[i] - m;
        }

        // Update radius, mass and volumes
        updateSize();
        baby.updateSize();
    }

    public void mutatePop() {
        // Mutate parameters inherited
        super.mutatePop();
        // Now mutate your parameters
    }

    /**
     * Set movement vector to put a new-created particle
     *
     * @param myBaby
     * @param distance
     */
    public void setDivisionDirection(double distance) {
        double phi, theta;

        phi = 2 * Math.PI * ExtraMath.getUniRand();
        theta = 2 * Math.PI * ExtraMath.getUniRand();

        _divisionDirection.x = distance * Math.sin(phi) * Math.cos(theta);
        _divisionDirection.y = distance * Math.sin(phi) * Math.sin(theta);
        _divisionDirection.z = (_agentGrid.is3D ? distance * Math.cos(phi) : 0);
    }

    /* ______________________ TIGHT JUNCTIONS __________________________ */
    // TJ Flann
    // MD fixed bug, need to use the local variable on neighbor, rather than a get
    protected void clearTightJunctionsWithNeighbors() {
        // this particle is going to die, so we loop through its tight junctions
        // and remove this particle from its neighbors list
        Iterator<LocatedAgent> iter = _myTightJunctions.iterator();
        while (iter.hasNext()) {
            LocatedAgent aNeighbor = iter.next();
            //LogFile.writeLog(Integer.toString(aNeighbor._myTightJunctions.size()));
            aNeighbor._myTightJunctions.remove(this);
            //LogFile.writeLog(Integer.toString(aNeighbor._myTightJunctions.size()));
        }
    }

    protected LinkedList<LocatedAgent> getTightJunctions() {
        return _myTightJunctions;
    }

    protected void initializeTightJunctions() {

        //Farzin added
        //_myTightJunctions.clear();
        // this particle has either been created or needs to have its tightJunctions Initialized
        if (!_species.tightJunctionSpecies.isEmpty()) {
            Enumeration<String> keys = _species.tightJunctionSpecies.keys();
            // loop through species that we connect with
            while (keys.hasMoreElements()) {
                //LogFile.writeLog("initialize TJ me="+this.getSpecies().speciesName);
                String speciesName = keys.nextElement();
                if (_species.tightJunctionSpecies.get(speciesName) <= 0)
                    return;

                findTouchingNeighbors(speciesName);
                //findTouchingNeighbors(speciesName,getInteractDistance()*2);

                //_myTightJunctions.clear();

                Iterator<LocatedAgent> iter = _myNeighbors.iterator();
                while (iter.hasNext()) {
                    LocatedAgent aNeighbor = iter.next();
                    if (!_myTightJunctions.contains(aNeighbor)) _myTightJunctions.add(aNeighbor);
//                                        else
//                                        {
//                                            double actualDistance = computeDifferenceVector(_location, aNeighbor.getLocation());
//                                            double distance = _species.attachDestroyFactor * getInteractDistanceBetween(aNeighbor);
//
//                                            if(actualDistance > distance) _myTightJunctions.remove(aNeighbor);
//                                        }

                }

                if (farzin.Logger.writeTightJunctionCount)
                    farzin.Logger.addtightJunctionCount(_birthId, _myTightJunctions.size());

                //LogFile.writeLog("initialize TJ me="+this.getSpecies().speciesName +" neighbors="+Integer.toString(_myTightJunctions.size()));
                _myNeighbors.clear();
            }

            List<LocatedAgent> toRemove = new ArrayList<>();

            for (LocatedAgent tj : _myTightJunctions) {
                double actualDistance = computeDifferenceVector(_location, tj.getLocation());
                double distance = _species.attachDestroyFactor * getInteractDistanceBetween(tj);

                if (actualDistance > distance) {
                    toRemove.add(tj);

                }
            }
            _myTightJunctions.removeAll(toRemove);

        } else
            _myTightJunctions.clear();


        //  if (_species.tightJunctionToBoundaryStrength > 0) attachmentForceToBoundaries();


    }

    protected void applyTightJunctions() {
        // applies the movement vectors for this object
        if (willDie())
            LogFile.writeLog("move dieing particle?");
        Iterator<LocatedAgent> iter = _myTightJunctions.iterator();
        while (iter.hasNext()) {
            LocatedAgent aNeighbor = iter.next();
//			if(AgentContainer.interactionMatrixTightJunctions[_birthId][aNeighbor._birthId] ||
//					AgentContainer.interactionMatrixTightJunctions[aNeighbor._birthId][_birthId])
//				continue;
            if (aNeighbor.willDie())
                LogFile.writeLog("move dieing neighbor?");
            addTightJunctionMovementWithPrediction(aNeighbor, iter);


            //	addTightJunctionMovement(aNeighbor, iter);
//			AgentContainer.interactionMatrixTightJunctions[_birthId][aNeighbor._birthId]=true;
//			AgentContainer.interactionMatrixTightJunctions[aNeighbor._birthId][_birthId]=true;
        }
    }

    protected void addTightJunctionMovement(LocatedAgent aNeighbor, Iterator<LocatedAgent> iter) {
        // actually adds the movement for this tight junction between me and my neighbor
        double distance   = computeDifferenceVector(_location, aNeighbor._location);
        double target     = getInteractDistanceBetween(aNeighbor);
        double difference = distance - target;

        if (difference <= 0)
            return;

        if (difference >= target) {
            _diff.reset();
            iter.remove();
            return;
        }
        // + is move closer
        //LogFile.writeLog("DIFFERENCE="+Double.toString(difference));
        String neighborSpeciesName = aNeighbor.getSpecies().speciesName; // get species name of my neighbor
        // the higher the stiffness, the sharper the curve so the distance is enforced more
        if (!_species.tightJunctionSpecies.containsKey(neighborSpeciesName))
            return;
        double stiffness = _species.tightJunctionSpecies.get(neighborSpeciesName); // strength
        // tanh goes between +1 and -1, magnitude between +/- target distance/10
        double magnitude = Math.abs(difference) * Math.tanh(difference * stiffness);
        // _diff is set in compute difference vector
        // 1/2 each to me and my partner
        // _diff points from neighbor to me
        _diff.normalizeVector();
        magnitude = Math.min(magnitude, 0.5);
        _diff.times(-0.5 * magnitude);
        // add this move to the particle
        //LogFile.writeLog("T.Jun="+Double.toString(magnitude));
        //farzin.Logger.addMovementMagnitudes(this, aNeighbor, magnitude);

        if (farzin.Logger.coloringTightJunctions) {
            // farzin.Logger.tightJunctions[_birthId][aNeighbor._birthId]-= Math.abs(0.5*magnitude);
            // farzin.Logger.tightJunctions[aNeighbor._birthId][_birthId]-= Math.abs(0.5*magnitude);

            farzin.Logger.addModifyTightJunctionRecord(_birthId, aNeighbor._birthId, -Math.abs(0.5 * magnitude));
            farzin.Logger.addModifyTightJunctionRecord(aNeighbor._birthId, _birthId, -Math.abs(0.5 * magnitude));
        }

        //   if (_frozenVertical || aNeighbor._frozenVertical)
        //       _diff.times(new ContinuousVector(2, 1, 1));
        //if (_frozenHorizontal || aNeighbor._frozenHorizontal)
        //     _diff.times(new ContinuousVector(1, 2, 1));

        this._movement.add(_diff);
        aNeighbor._movement.subtract(_diff);
    }


    protected void attachmentForceToBoundaries() {
        tightJunctionToBoundaries();

    }

    private void tightJunctionToBoundaries() {
        double distance;
        for (AllBC ab : updateAttachment()) {

            if (ab != null) {
                distance = computeDifferenceVector(_location, ab.getOrthoProj(_location));

                double magnitude = Math.abs(distance) * Math.tanh(distance * _species.tightJunctionToBoundaryStrength);
                _diff.normalizeVector();
                _diff.times(-magnitude);
                this._movement.add(_diff);
            }
        }
    }

    protected void addTightJunctionMovementWithPrediction(LocatedAgent aNeighbor, Iterator<LocatedAgent> iter) {
        // actually adds the movement for this tight junction between me and my neighbor

        ContinuousVector myTempLoc = new ContinuousVector();
        myTempLoc.set(_location);
        myTempLoc.add(_movement);

//		if(_location.x<10 && _frozenVertical)
//			myTempLoc.x=_totalRadius;

        ContinuousVector neighborTempLoc = new ContinuousVector();
        neighborTempLoc.set(aNeighbor._location);
        neighborTempLoc.add(aNeighbor._movement);
//
//		if(aNeighbor._location.x<10 && aNeighbor._frozenVertical)
//			neighborTempLoc.x= aNeighbor._totalRadius;


        double distance = computeDifferenceVector(myTempLoc, neighborTempLoc);
        double target   = getInteractDistanceBetween(aNeighbor);

        //when positive force is attractive (negative force)
        //when negative force is repellant (positive force)
        double difference = distance - target;

//	    if (difference<=0)
//	    	return;

        // + is move closer
        //LogFile.writeLog("DIFFERENCE="+Double.toString(difference));
        String neighborSpeciesName = aNeighbor.getSpecies().speciesName; // get species name of my neighbor
        // the higher the stiffness, the sharper the curve so the distance is enforced more
        if (!_species.tightJunctionSpecies.containsKey(neighborSpeciesName)) {
            iter.remove();
            return;
        }
        double stiffness = _species.tightJunctionSpecies.get(neighborSpeciesName); // strength
        if (difference >= target || stiffness == 0) {
            _diff.reset();
            iter.remove();
            return;
        }

        // tanh goes between +1 and -1, magnitude between +/- target distance/10
        double magnitude = Math.abs(difference) * Math.tanh(difference * stiffness);


//		int now = SimTimer.getCurrentIter();
//		double ageMult = Math.min(1, (now  - _birthday) / 250);
//		magnitude *= (_radius / getSpeciesParam().divRadius);
//		magnitude *= ageMult;

        // _diff is set in compute difference vector
        // 1/2 each to me and my partner
        // _diff points from neighbor to me
        _diff.normalizeVector();
        // magnitude = Math.min(magnitude, 0.25);
        _diff.times(-0.5 * magnitude);

        //  if(_frozenVertical || aNeighbor._frozenVertical)
        // 	_diff.times(new ContinuousVector(2,1,1));
//	    if(_frozenHorizontal || aNeighbor._frozenHorizontal)
//	    	_diff.times(new ContinuousVector(1,2,1));


        // add this move to the particle
        //LogFile.writeLog("T.Jun="+Double.toString(magnitude));
        // farzin.Logger.addMovementMagnitudes(this, aNeighbor, 0.5 * magnitude);
        if (farzin.Logger.coloringTightJunctions) {
            //farzin.Logger.tightJunctions[_birthId][aNeighbor._birthId]-= Math.abs(0.5*magnitude);
            // farzin.Logger.tightJunctions[aNeighbor._birthId][_birthId]-= Math.abs(0.5*magnitude);


            // farzin.Logger.addModifyTightJunctionRecord(_birthId, aNeighbor._birthId, -Math.abs(0.5*magnitude));
            // farzin.Logger.addModifyTightJunctionRecord(_birthId, aNeighbor._birthId, Math.abs(0.5*magnitude));
            // farzin.Logger.addModifyTightJunctionRecord(aNeighbor._birthId, _birthId, -Math.abs(0.5*magnitude));

            farzin.Logger.addModifyTightJunctionRecord(_birthId, aNeighbor._birthId, 0.5 * magnitude);
            farzin.Logger.addModifyTightJunctionRecord(aNeighbor._birthId, _birthId, 0.5 * magnitude);
        }

        this._movement.add(_diff);
        aNeighbor._movement.subtract(_diff);
    }

	/* ______________________ SHOVING ___________________________________ */

    /**
     * Mechanical interaction between two located agents
     *
     * @param aGroup :
     *            neighbourhood of the agent
     * @param MUTUAL :
     *            movement shared between 2 agents or applied only to this one
     * @pull : false for shoving, true for pulling (shrinking biofilm)
     * @seq : apply immediately the movement or waits the end of the step
     */
    public double interact(boolean MUTUAL, boolean shoveOnly, boolean seq,
                           double gain) {
        boolean willShove = false;

        // MD Flann
//		for(int i=0; i<particleRegulator.length; i++)
//		    if (particleRegulator[i])
//			LogFile.writeLog("value="+Double.toString(particleMass[i]));
        //move();
        // rebuild your neighbourhood
        // if die, dont bother moving
        if (willDie())
            return 0.0;


        if (shoveOnly)
            getPotentialShovers(getInteractDistance());
        else
            getPotentialShovers(getInteractDistance() + getShoveRadius());

        //added by Farzin
        addBiasMovement();
        //
        Iterator<LocatedAgent> iter = _myNeighbors.iterator();
        while (iter.hasNext()) {
            LocatedAgent aNeighbor = iter.next();
            //Farzin

//			if(AgentContainer.interactionMatrixPush[_birthId][aNeighbor._birthId]||
//					AgentContainer.interactionMatrixPush[aNeighbor._birthId][_birthId])
//				continue;
            // MD Flann just return if will die
            if (aNeighbor.willDie() || this.equals(aNeighbor))
                continue;
            // always do a shove
            willShove |= addPushMovement(aNeighbor, MUTUAL, gain);
            String neighborSpeciesName = aNeighbor.getSpecies().speciesName; // get species name of my neighbor
            // If doing shove only, then don't do adhesion
            if (_species.adhesionSpecies.get(neighborSpeciesName) != null) // do we have an adhesive entry for this neighboring particle with us?
                willShove |= addSpringMovement(aNeighbor, MUTUAL, gain);
            // END FLANN

        }
        _myNeighbors.clear();


        // TJ Flann
        if (!_species.tightJunctionSpecies.isEmpty())
            applyTightJunctions();


        if (_species.tightJunctionToBoundaryStrength > 0) attachmentForceToBoundaries();

        willShove = isMoving();

        if (seq)
            return move();
        else
            return 0;

    }

    private void addBiasMovement() {
        if (_species.speciesName.equalsIgnoreCase("Sides"))
            _movement.add(new ContinuousVector(0, 0.2, 0));
        if (_species.speciesName.equalsIgnoreCase("Sides2"))
            _movement.add(new ContinuousVector(0, -0.4, 0));
    }

    /** CHEMOTAXIS **/
    public double moveChemotaxis(boolean seq, double gain) {
        // implements a move based on chemotaxis (if this mechanism is part of this particle)
        move();
        addChemotacticMovement(gain);
        if (seq)
            return move();
        else
            return 0;
    }

    public boolean addChemotacticMovement(double gain) {
		// adds a vector to this agents movement if it is responsive to a chemotactic gradient
		int soluteIndex = 0;
		ContinuousVector gradient;
		double coefficient; 
		// loop through the potential chemotactic forces on this particle
		for(String key: _species.chemotaxis.keySet())
		{	
			
			soluteIndex = Integer.parseInt(key);
			coefficient=_species.chemotaxis.get(key);
			
			if (soluteIndex == -1) //like switchControlIndex but the index of the solute
				return false;
						// get the grid of the chemotactic solute
			SoluteGrid chemotaxisGrid = this.getSpeciesParam().aSim.soluteList[soluteIndex];
			// sample the gradient at this location
			if (chemotaxisGrid.is3D())
			     gradient = chemotaxisGrid.getGradient3DChemotaxis(_location);
			else gradient = chemotaxisGrid.getGradient2DChemotaxis(_location, _radius);
			//System.out.println(gradient);
			//Delin
			//ContinuousVector turnAroundGradient = new ContinuousVector(gradient);
			//get a copy of gradient
			//Delin
			//turnAroundGradient.turnAround();// turn around the copy of gradient
			//if(particleExistsInDirection(gradient))
			//{//some thing in front of the particle
			//	if(particleExistsInDirection(turnAroundGradient))
			//	{//contact Inhibited, can't move because another particle in the opposite direction of the vector "gradient"
			//		gradient = new ContinuousVector();
			//	}
				//gradient = new ContinuousVector();
			//}
			/*if(particleExistsInDirection(turnAroundGradient))
			{//contact Inhibited, cant move because another particle in the opposite direction of the vector "gradient"
				gradient = new ContinuousVector();
			}*/
				
//			LogFile.writeLog("field="+gradient.toString());			

			gradient.times(coefficient);
			//akshay edit. now the gradient is a shift vector which will add to the movement vector and will have
			//chemotatctic effect on the movement of the particle
			// add this move to the particle
			//check the new location if its empty
			if(gradient.x==Double.NaN)
				return true;
			
			if(_species.chemotaxis.get(key) > 0){
				if(!hasOpenBoundary() && _species.contactInhibition.get(key)==1){
					return true;
				}
				@SuppressWarnings("unchecked")
				LinkedList<LocatedAgent> neighbors = (LinkedList<LocatedAgent>) _myNeighbors.clone();
				findTouchingNeighbors(this.getSpecies().speciesName);
								
				if((chemotaxisGrid.is3D() && _myNeighbors.size()>=5)||((!chemotaxisGrid.is3D()) && _myNeighbors.size()>=5)){
					_myNeighbors = neighbors;
					return true;
				}
				_myNeighbors = neighbors;
					
			}
		/*	if(gradient.x < -1.0)
				gradient.x = -1.0;
			if(gradient.y < -1.0)
				gradient.y = -1.0;
			if(gradient.z < -1.0)
				gradient.z = -1.0;
			if(gradient.x > 1.0)
				gradient.x = 1.0;
			if(gradient.y > 1.0)
				gradient.y = 1.0;
			if(gradient.z > 1.0)
				gradient.z = 1.0;*/
				
			//System.out.println("cV : " + gradient);
			//if(ExtraMath.getUniRandInt(0, 100) <= 70)
				this._movement.add(gradient);		
			//LogFile.writeLog("Gain = "+ Double.toString(gain));
		}		
		return true;    }
    
    public boolean addChemotacticMovementFLANN(double gain) {
        // adds a vector to this agents movement if it is responsive to a chemotactic gradient
        // int soluteIndex = getSpeciesParam().chemotaxisSoluteIndex;
        int              soluteIndex = 0;
        ContinuousVector gradient;
        double           coefficient = -50; //-50;
        for (String key : _species.chemotaxis.keySet()) {
            soluteIndex = Integer.parseInt(key);
            coefficient = _species.chemotaxis.get(key);
            if (soluteIndex == -1) //like switchControlIndex but the index of the solute
                return false;
//			String mySpeciesName = this.getSpecies().speciesName;
//			if (!mySpeciesName.equals("Go")) return false;
            // get the grid of the chemotactic solute
            SoluteGrid chemotaxisGrid = this.getSpeciesParam().aSim.soluteList[soluteIndex];
            // sample the gradient at this location
            if (chemotaxisGrid.is3D())
                gradient = chemotaxisGrid.getGradient(_location);
            else gradient = chemotaxisGrid.getGradient2D(_location);
            // may have to look and see if this vector is going towards mass or open space (ECM)
            // coefficient is the mapping between the vector and its affect on movement, may be negative
            //double coefficient = getSpeciesParam().chemotaxisCoeficient;
            //gradient = new ContinuousVector(0.0, 0.01, 0.0);
            // negative coefficient is towards, positive is away

            gradient.times(coefficient);
            // add this move to the particle
            LogFile.writeLog("Chemo=" + Double.toString(gradient.norm()));
            this._movement.add(gradient);
            //LogFile.writeLog("Gain = "+ Double.toString(gain));
        }
        return true;
    }


    /**
     * Mutual shoving : The movement by shoving of an agent is calculated based
     * on the cell overlap and added to the agents movement vector. Both agents
     * are moved of half the overlapping distance in opposite directions.
     *
     * @param aNeighbour
     *            reference to the potentially shoving neighbour
     * @return true, if a shoving is detected
     */
    public boolean addPushMovement(LocatedAgent aNeighbour, boolean isMutual,
                                   double gain) {
        double d, distance;

        if (aNeighbour == this)
            return false;

        // Build the escape vector and find the distance between you and your
        // neighbourhood
        d = computeDifferenceVector(_location, aNeighbour._location);

        _diff.normalizeVector();

        // Compute effective cell-cell distance
        distance = getShoveRadius() + aNeighbour.getShoveRadius();
        distance += getSpeciesParam().shoveLimit;
        distance = d - distance;

		/* Apply shoving _________________________________________________ */


        // Compute shoving distance for the current agent
        if (distance <= 0) {

            if (farzin.Logger.coloringTightJunctions) {
                //farzin.Logger.tightJunctions[_birthId][aNeighbour._birthId]+=Math.abs(gain*0.5*distance);
                // farzin.Logger.tightJunctions[aNeighbour._birthId][_birthId]+=Math.abs(gain*0.5*distance);
                // farzin.Logger.addModifyTightJunctionRecord(_birthId, aNeighbour._birthId, Math.abs(gain *0.5 * distance));
                // farzin.Logger.addModifyTightJunctionRecord(aNeighbour._birthId, _birthId, Math.abs(gain *0.5 * distance));

            }
            if (isMutual) {
                // farzin.Logger.addMovementMagnitudes(this, aNeighbour, gain*0.25*Math.abs(distance));
                _diff.times(gain * 0.5 * Math.abs(distance));
                // _diff.times(gain*0.5*Math.abs(distance));
                this._movement.add(_diff);
                aNeighbour._movement.subtract(_diff);
            } else {
                //  farzin.Logger.addMovementMagnitudes(this, aNeighbour, gain*0.5*Math.abs(distance));
                _diff.times(Math.abs(gain * distance));
                // _diff.times(Math.abs(gain*distance));
                this._movement.add(_diff);
            }
            //LogFile.writeLog("Shove="+Double.toString(_diff.norm()));
            return true;
        } else {
            return false;
        }

    }

    /**
     *
     * @param aNeighbor
     * @param isMutual
     * @return
     */
    public boolean addSpringMovement(LocatedAgent aNeighbor, boolean isMutual,
                                     double gain) {
        double d, distance, delta;

        if (aNeighbor == this)
            return false;
        // FLANN
        // get species name of aNeighbor and extract its adhesion parameter from the
        String neighborSpeciesName = aNeighbor.getSpecies().speciesName; // get species name of my neighbor
        double adhesionStrength    = _species.adhesionSpecies.get(neighborSpeciesName); // strength

        //Adhoc for experiment, remove this part after using
//		if(SimTimer.getCurrentIter()>8)
//		{
//			double tempDistance = (computeDifferenceVector(_location, new ContinuousVector(13,250,0))/8.0)/_radius;
//			if(tempDistance<2)
//				adhesionStrength=adhesionStrength-tempDistance/2;
//		}
        //END Adhoc for experiment


        if (adhesionStrength <= 0.0)
            return false; // TJ Flann
        // END FLANN

        // Build the escape vector and find the distance between you and your
        // neighbourhood
        d = computeDifferenceVector(_location, aNeighbor._location);

        _diff.normalizeVector();

        distance = getShoveRadius() + aNeighbor.getShoveRadius();
        distance += getSpeciesParam().shoveLimit;

        delta = d - distance;
//		double lMax = _totalRadius;

        // FLANN, calculate adhesion based on the strength
        if (delta > 0) {
            gain *= Math.exp(-(delta * delta) / adhesionStrength);
        } else gain = 0.0;
		/* Apply shoving _________________________________________________ */

        if (farzin.Logger.coloringTightJunctions) {
            //farzin.Logger.tightJunctions[_birthId][aNeighbor._birthId]-=Math.abs(0.5*delta*gain);
            //farzin.Logger.tightJunctions[aNeighbor._birthId][_birthId]-=Math.abs(0.5*delta*gain);

            // farzin.Logger.addModifyTightJunctionRecord(_birthId, aNeighbor._birthId, -Math.abs(0.5*delta*gain));
            // farzin.Logger.addModifyTightJunctionRecord(aNeighbor._birthId, _birthId, -Math.abs(0.5*delta*gain));
        }
        if (isMutual) {
            _diff.times(-0.5 * delta * gain);
            this._movement.add(_diff);
            aNeighbor._movement.subtract(_diff);
        } else {
            _diff.times(-delta * gain);
            this._movement.add(_diff);
        }
        return (_movement.norm() > _radius * gain);
    }


//        public boolean addSpringAttachment()
//            {
//                AllBC mySupport = updateAttachment();
//                double d, distance, delta;
//
//                d = computeDifferenceVector(_location, mySupport
//                        .getOrthoProj(_location));
//                _diff.normalizeVector();
//
//                distance = _totalRadius * getShoveFactor();
//                delta = d - distance;
//
//		/* Apply elastic interaction _______________________________________ */
//                double gain = 0.1;
//                if (delta < 0)
//                    gain = 0.1;
//                if (delta > 0)
//                    gain = 0.1;
//                if (delta > _totalRadius)
//                    gain = 0;
//
//                _diff.times(-delta * gain);
//                this._movement.add(_diff);
//
//                if (_movement.norm() > _radius * 0.1)
//                    {
//                        return true;
//                    }
//                else
//                    {
//                        return false;
//                    }
//            }

    public double getConnectingInteractDistance() {
        return getShoveRadius() + ((LocatedParam) _speciesParam).shoveLimit;
    }

    protected boolean hasOpenBoundary() {
        // returns true if particle is not surrounded by cells on all sides
        // no skin at the bottom of the colony
        if (_location.x < 30)
            return false;
        // clear angle array that records if a neighbor particle is in direction
        // if _neighborAngles[i] == true then particle in i*2pi/_angleCount segment
        for (int i = 0; i < _angleCount; i++)
            _neighborAngles[i] = false;
        // unit vector a angle is 0 position
        ContinuousVector yIsZero = new ContinuousVector(1, 0, 0);
        // get neighbors
        getPotentialShovers(getConnectingInteractDistance());
        //LogFile.writeLog("connect with="+Integer.toString(_myNeighbors.size()));
        Iterator<LocatedAgent> iter = _myNeighbors.iterator();
        int                    index, index0;
        // loop through neighbors and determine angle, then set index in _neighborAngles[i]
        while (iter.hasNext()) {
            LocatedAgent aNeighbor = iter.next();
            double d = computeDifferenceVector(aNeighbor._location, _location);
            // compute the angle between neighbor and 0 angle vector
            _diff.normalizeVector();
            double angle = Math.acos(_diff.cosAngle(yIsZero));
            // LogFile.writeLog("angle="+Double.toString(angle));
            // which side of the y==0 line?
            if (_diff.y <= 0.0)
                angle = Math.PI + angle;
            // angle to index into array
            index = (int) Math.floor(_angleCount * angle / (2 * Math.PI));
            if (index >= 12)
                index = 11;
            _neighborAngles[index] = true;
        }
        // scan over _neighborAngles[i] to see if there exists two adjacent locations that are false
        // must cycle over the array (hence mod)
        boolean foundGap = false;
        for (int i = 0; i < _angleCount + 2; i++) {
            index = i % _angleCount;
            index0 = (i + 1) % _angleCount;
            if (!_neighborAngles[index] && !_neighborAngles[index0]) {
                foundGap = true;
                break;
            }
        }
        // now process neighbors
//		iter = _myNeighbors.iterator();
        // loop through neighbors and determine angle, then set index in _neighborAngles[i]
//		double maxDistance = _agentGrid.getResolution() * getShoveRadius()*.25;
//		if(foundGap)
//		    atSkin = true;
//		    while (iter.hasNext()) 
//		    {
//			LocatedAgent aNeighbor = iter.next();
//			if (getDistance(aNeighbor) <= maxDistance)
//			    aNeighbor.atSkin = true;
//		    }
        _myNeighbors.clear();
        // if we have transitivity of skin, thickness of skin,
        // loop through _myNeighbors and if distAnce then flag as skin
        return foundGap;
    }


    /**
     * @param ContinuousVector
     *            a location
     * @param ContinuousVector
     *            a location
     * @return the shortest movement vector to go from a to b, take into account
     * the cyclic boundary
     * @see addOverlapMovement
     * @see addPullMovement works in 2 and 3D
     */
    public double computeDifferenceVector(ContinuousVector me,
                                          ContinuousVector him) {
        double gridLength;

        _diff.x = me.x - him.x;
        // check periodicity in X
        gridLength = _species.domain.length_X;
        if (Math.abs(_diff.x) > .5 * gridLength)
            _diff.x -= Math.signum(_diff.x) * gridLength;

        _diff.y = me.y - him.y;
        // check periodicity in Y
        gridLength = _species.domain.length_Y;

        if (Math.abs(_diff.y) > .5 * gridLength)
            _diff.y -= Math.signum(_diff.y) * gridLength;

        if (_agentGrid.is3D) {
            _diff.z = me.z - him.z;
            // check periodicity in Z
            gridLength = _species.domain.length_Z;
            if (Math.abs(_diff.z) > .5 * gridLength)
                _diff.z -= Math.signum(_diff.z) * gridLength;

        } else {
            _diff.z = 0;
        }
        double d = Math.sqrt(_diff.x * _diff.x + _diff.y * _diff.y + _diff.z
                * _diff.z);

        if (d == 0) {
            d = 1e-2 * _radius;
            _diff.alea(_agentGrid.is3D);
        }

        return d;
    }

    /**
     * Look for neighbours in a range around you
     */
    public void getPotentialShovers(double radius) {
        _agentGrid.getPotentialShovers(_agentGridIndex, radius, _myNeighbors);
    }

    public void getNeighborhood(double radius) {
        _agentGrid.getPotentialShovers(_agentGridIndex, radius, _tempNeighbors);
    }

    /**
     * Pick randomly a Neighbor from the _myNeigbors collection
     *
     * @return
     */
    public LocatedAgent pickNeighbor() {
        if (_myNeighbors.isEmpty())
            return null;
        else
            return _myNeighbors.get(ExtraMath.getUniRandInt(0, _myNeighbors
                    .size()));
    }

    //TJ Flann
    public void findTouchingNeighbors(String speciesName) {
        // just those neighbors really close
        double actualDistance, distance;
        getPotentialShovers(getInteractDistance());
        //LogFile.writeLog("Potential shovers="+Integer.toString(_myNeighbors.size()));
        int size = _myNeighbors.size();
        for (int index = 0; index < size; index++) {
            LocatedAgent aNeighbor = _myNeighbors.removeFirst();
            if (!aNeighbor.equals(this) && speciesName.equals(aNeighbor._species.speciesName)) {
                actualDistance = computeDifferenceVector(_location, aNeighbor.getLocation());
                distance = _species.attachCreateFactor * getInteractDistanceBetween(aNeighbor); // to allow for noise
                //LogFile.writeLog("Given distance="+Double.toString(distance)+" actual distance= "+ Double.toString(actualDistance));
                if (actualDistance <= distance) {
                    _myNeighbors.addLast(aNeighbor);
                }
            }
        }
    }

    public void findTouchingNeighbors(String speciesName, double radius) {
        // just those neighbors really close
        double actualDistance, distance;
        getPotentialShovers(radius);
        //LogFile.writeLog("Potential shovers="+Integer.toString(_myNeighbors.size()));
        int size = _myNeighbors.size();
        for (int index = 0; index < size; index++) {
            LocatedAgent aNeighbor = _myNeighbors.removeFirst();
            if (!aNeighbor.equals(this) && speciesName.equals(aNeighbor._species.speciesName)) {
                actualDistance = computeDifferenceVector(_location, aNeighbor.getLocation());
                distance = _species.attachCreateFactor * getInteractDistanceBetween(aNeighbor); // to allow for noise
                //LogFile.writeLog("Given distance="+Double.toString(distance)+" actual distance= "+ Double.toString(actualDistance));
                if (actualDistance <= distance) {
                    _myNeighbors.addLast(aNeighbor);
                }
            }
        }
    }

    /**
     * Find a sibling
     *
     * @param indexSpecies
     * @return
     */
    public void findCloseSiblings(int indexSpecies) {
        int          nNb;
        boolean      test;
        double       shoveDist;
        LocatedAgent aNb;

        getPotentialShovers(getInteractDistance());
        nNb = _myNeighbors.size();

        for (int iNb = 0; iNb < nNb; iNb++) {
            aNb = _myNeighbors.removeFirst();
            // test EPS-species
            test = (indexSpecies == aNb.speciesIndex);

            // Test distance
            shoveDist = 2 * (getShoveRadius() + aNb.getShoveRadius());
            test = test
                    && computeDifferenceVector(_location, aNb.getLocation()) <= shoveDist;

            if (test & aNb != this)
                _myNeighbors.addLast(aNb);
        }
    }

    /**
     * Apply the movement stored taking care to respect boundary conditions
     */
    public double move() {
    	
    	//random agitation-created by honey
    	if(!(getSpeciesParam().agitationCV==0))
    	{
    		//System.out.println("here");
    		double lower =0-getSpeciesParam().agitationCV ;
    		double upper = getSpeciesParam().agitationCV;
    		double resultx= Math.random() * (upper - lower) + lower;
    		//System.out.println(resultx);
    		double resulty = Math.random() * (upper - lower) + lower;
    		//System.out.println(resulty);
    		_movement.add(resultx,resulty,0.0);
    		
    	}
    	

        if (!_movement.isValid()) {
            LogFile.writeLog("Incorrect movement coordinates");
            _movement.reset();
        }

        if (!_agentGrid.is3D && _movement.z != 0) {
            _movement.z = 0;
            _movement.reset();
            LogFile.writeLog("Try to move in z direction !");
        }


        //Added by Farzin to cancel forces applied fixed particle
        if (getSpeciesParam().fixed)
            //This needs more work; fixed particles can move if the force is higher than a threshold.
        	_movement.reset();

        	//------------random agitation testing--------        	
        // No movement planned, finish here
       
        
        if (_movement.isZero())
        {
        	
            return 0;
        }

        // Test the boundaries
        checkBoundaries();

        // Now apply the movement
        _location.set(_newLoc);
        _agentGrid.registerMove(this);

        double delta = _movement.norm();
        _movement.reset();

        return delta / _totalRadius;
    }

    public void checkBoundaries() {

        // Search a boundary which will be crossed
        _newLoc.set(_location);
        _newLoc.add(_movement);
        AllBC   aBoundary = getDomain().testCrossedBoundary(_newLoc);
        int     nDim      = (_agentGrid.is3D ? 3 : 2);
        boolean test      = (aBoundary != null);
        int     counter   = 0;

        // Test all boundaries and apply corrections according to crossed
        // boundaries
        while (test) {
            counter++;
            aBoundary.applyBoundary(this, _newLoc);
            aBoundary = getDomain().testCrossedBoundary(_newLoc);

            test = (aBoundary != null) | (counter > nDim);
            if (counter > nDim)
                System.out.println("LocatedAgent.move() : problem!");
        }
    }

	/* ____________________CELL DIVISION __________________________________ */

    /**
     * Mutation Function If you don't want apply a mutation in a specified
     * class, do not redefine this method. If you want, you are free to choose
     * which fields to mutate for each different class by a simple redefinition
     *
     * @param alea
     */
    public void mutateAgent() {
        // Mutate parameters inherited
        super.mutateAgent();
        // Now mutate your parameters
    }

    /**
     * Add the reacting CONCENTRATION of an agent on the received grid
     *
     * @param aSpG :
     *            grid used to sum catalysing mass
     * @param catalyst
     *            index : index of the compartment of the cell supporting the
     *            reaction
     */
    public void fitMassOnGrid(SpatialGrid aSpG, int catalystIndex) {
        if (isDead)
            return;

        double value = particleMass[catalystIndex] / aSpG.getVoxelVolume();
        if (Double.isNaN(value) | Double.isInfinite(value))
            value = 0;
        aSpG.addValueAt(value, _location);
    }

    /**
     * Add the total CONCENTRATION of an agent on received grid
     *
     * @param aSpG :
     *            grid used to sum catalysing mass
     */
    public void fitMassOnGrid(SpatialGrid aSpG) {
        if (isDead)
            return;

        double value = _totalMass / aSpG.getVoxelVolume();
        if (Double.isNaN(value) | Double.isInfinite(value))
            value = 0;

        //aSpG.addValueAtCoordNotCorrected(value, _location);
        aSpG.addValueAt(value, _location);
    }

    public void fitVolRateOnGrid(SpatialGrid aSpG) {
        double value;
        value = _netVolumeRate / aSpG.getVoxelVolume();
        if (Double.isNaN(value) | Double.isInfinite(value))
            value = 0;
        aSpG.addValueAt(value, _location);
    }

    public void fitReacRateOnGrid(SpatialGrid aRateGrid, int reactionIndex) {
        if (isDead)
            return;

        // growthRate is in [fgX.hr-1] so convert to concentration:
        // [fgX.um-3.hr-1 = gX.L-1.hr-1]
        double value = growthRate[reactionIndex] / aRateGrid.getVoxelVolume();

        if (Double.isNaN(value) | Double.isInfinite(value))
            value = 0;

        aRateGrid.addValueAt(value, _location);
    }

	/* _______________ FILE OUTPUT _____________________ */


    public String sendHeader() {
        // return the header file for this agent's values after sending those for super
        StringBuffer tempString = new StringBuffer(super.sendHeader());
        tempString.append(",");

        // location info and radius
        //tempString.append("locationX,locationY,locationZ,radius,totalRadius,neighborsNO");
        tempString.append("locationX,locationY,locationZ,radius,totalRadius");

        return tempString.toString();
    }

    public String writeOutput() {
        // write the data matching the header file
        StringBuffer tempString = new StringBuffer(super.writeOutput());
        tempString.append(",");

        // location info and radius
        tempString.append(_location.x + "," + _location.y + "," + _location.z + ",");
        tempString.append(_radius + "," + _totalRadius + ",");

        //added by Farzin to find number of neighbors
        //getPotentialShovers(_radius*1.25);
        //tempString.append(_myNeighbors.size());

        return tempString.toString();
    }

	/* _______________ RADIUS, MASS AND VOLUME _____________________ */

    /**
     * Compute the volume on the basis of the mass and density of different
     * compounds defined in the cell
     */
    // MD Flann !!!! see below
    public void updateVolume() {
        _volume = 0;
        for (int i = 0; i < particleMass.length; i++) {

            // do not take regulators into account when computing volume
            //if (!particleRegulator[i])

            _volume += particleMass[i] / getSpeciesParam().particleDensity[i];
        }
        _totalVolume = _volume;
        LogFile.writeLog("Volume=" + Double.toString(_volume));
    }

    /**
     * Compute the radius on the basis of the volume The radius evolution is
     * stored in deltaRadius (used for shrinking)
     */
    public void updateRadius() {

        //sonia:chemostat 22.02.2010
        if (Simulator.isChemostat) {
            _radius = ExtraMath.radiusOfASphere(_volume);
            _totalRadius = ExtraMath.radiusOfASphere(_totalVolume);

        } else {

            if (_volume < 0)
                LogFile.writeLog("BUG");
            if (_species.domain.is3D) {
                _radius = ExtraMath.radiusOfASphere(_volume);
                _totalRadius = ExtraMath.radiusOfASphere(_totalVolume);
            } else {
                _radius = ExtraMath.radiusOfACylinder(_volume,
                        _species.domain.length_Z);
                _totalRadius = ExtraMath.radiusOfACylinder(_totalVolume,
                        _species.domain.length_Z);
            }
        }
    }

    public List<AllBC> updateAttachment() {
        double distance;
        for (AllBC aBoundary : getDomain().getAllBoundaries()) {
            if (aBoundary.isSupport() && aBoundary.canAttachTo()) {
                distance = aBoundary.getDistance(this._location);
                if (distance <= _species.attachToBoundaryCreateFactor * this.getShoveRadius() && !attachedToBoundaries.contains(aBoundary)) {
                    attachedToBoundaries.add(aBoundary);
                } else if (attachedToBoundaries.contains(aBoundary) && distance > _species.attachToBoundaryDestroyFactor * this.getShoveRadius()) {
                    attachedToBoundaries.remove(aBoundary);
                }


            }
        }
        return attachedToBoundaries;

    }

    public void addMovement(ContinuousVector aMove) {
        this._movement.add(aMove);
    }

    /* __________________ ACCESSORS ___________________________________ */
    public LocatedParam getSpeciesParam() {
        return (LocatedParam) _speciesParam;
    }

    public double getVolume(boolean withCapsule) {
        return (withCapsule ? _totalVolume : _volume);
    }

    public double getRadius(boolean withCapsule) {
        return (withCapsule ? _totalRadius : _radius);
    }

    public double getMass(boolean withCapsule) {
        return (withCapsule ? _totalMass : _totalMass);
    }

    public double getMaximumRadius() {
        return getSpeciesParam().divRadius
                * (1 + getSpeciesParam().divRadiusCV);
    }

    public boolean hasEPS() {
        return false;
    }

    public boolean hasInert() {
        return false;
    }

    public double getShoveFactor() {
        return ((LocatedParam) _speciesParam).shoveFactor;
    }

    public double getShoveRadius() {
        return _totalRadius * ((LocatedParam) _speciesParam).shoveFactor;
    }

    public double getInteractDistance() {
        return 2 * getShoveRadius() + ((LocatedParam) _speciesParam).shoveLimit;
    }

    public double getInteractDistanceBetween(LocatedAgent aNeighbor) {
        return getShoveRadius() + aNeighbor.getShoveRadius() +
                ((LocatedParam) _speciesParam).shoveLimit +
                ((LocatedParam) aNeighbor._speciesParam).shoveLimit;
    }

    public double getInteractDistance(LocatedAgent baby) {
        return getShoveRadius() + baby.getShoveRadius()
                + ((LocatedParam) _speciesParam).shoveLimit;
    }

    public double getBabyMassFrac() {
        return ExtraMath.deviateFrom(getSpeciesParam().babyMassFrac,
                getSpeciesParam().babyMassFracCV);
    }

    public boolean isMoving() {
        return (_movement.norm() > _totalRadius / 10);
    }


    public double getActiveFrac() {
        return 1.0;
    }

    public Color getColor() {
        return _species.color;
    }

    public Color getColorCapsule() {
        return Color.green;
    }

    public ContinuousVector getLocation() {
        return _location;
    }

    /**
     * Comparator used by AgentContainer.erodeBorder()
     * @author Rob Clegg
     */
    public static class detPriorityComparator implements java.util.Comparator<Object> {

        public int compare(Object b1, Object b2) {
            return (((LocatedAgent) b1).detPriority > ((LocatedAgent) b2).detPriority ? 1 : -1);
        }
    }

    /**
     * Comparator used by AgentContainer.erodeBorder()
     * @author Rob Clegg
     */
    public static class totalMassComparator implements java.util.Comparator<Object> {

        public int compare(Object b1, Object b2) {
            return (((LocatedAgent) b1)._totalMass > ((LocatedAgent) b2)._totalMass ? 1 : -1);
        }
    }

    /**
     * @param aLoc
     * @return distance bw 2 agents assuming cyclic boundaries
     */
    public double getDistance(LocatedAgent aLoc) {
        return computeDifferenceVector(_location, aLoc._location);
    }

    public void setLocation(ContinuousVector cc) {

        //sonia:chemostat
        //set the location of the newborns to zero

        if (Simulator.isChemostat) {
            cc.set(0, 0, 0);
            _location.x = cc.x;
            _location.y = cc.y;
            _location.z = cc.z;

        } else {
            _location.x = cc.x;
            _location.y = cc.y;
            _location.z = cc.z;
        }
    }

    public ContinuousVector getMovement() {
        return _movement;
    }

    public int getGridIndex() {
        return _agentGridIndex;
    }

    public LocatedGroup getGridElement() {
        return _agentGrid.getShovingGrid()[_agentGridIndex];
    }

    public void setGridIndex(int aGridIndex) {
        _agentGridIndex = aGridIndex;
    }

    public Domain getDomain() {
        return _species.domain;
    }


    public double calcDensityAround() {
        double[][][] grid       = getSpeciesParam().aSim.world.domainList.get(0).getBiomass().grid;
        int          resolution = (int) getSpeciesParam().aSim.world.domainList.get(0)._resolution;
        int          x, y, z;
        x = (int) (this.getLocation().x / resolution) + 1;
        y = (int) (this.getLocation().y / resolution) + 1;
        z = (int) (this.getLocation().z / resolution) + 1;
        return Simulator.calcDensityAround(x, y, z, grid);

//
//		double [][][] grid=getSpeciesParam().aSim.world.domainList.get(0).getYeastGrid().grid;
//		int nI=grid.length;
//		int nJ=grid[0].length;
//		int nK=grid[0][0].length;
//
//		double accMass=0;
//		for(int i=-1;i<2;i++)
//			for(int j=-1;j<2;j++)
//				for(int k=-1;k<2;k++)
//				{
//					int p1=x+i;
//					int p2=y+j;
//					int p3=z+k;
//					if(p1>=0 && p1<nI && p2>=0 && p2<nJ && p3>=0 && p3<nK)
//						accMass+=grid[p1][p2][p3];
//				}
//
//		return accMass;
    }

    public double calcPressureAround() {
        int resolution = (int) getSpeciesParam().aSim.world.domainList.get(0)._resolution;
        int x, y, z;
        x = (int) (this.getLocation().x / resolution) + 1;
        y = (int) (this.getLocation().y / resolution) + 1;
        z = (int) (this.getLocation().z / resolution) + 1;

        double[][][] grid = getSpeciesParam().aSim.getSolute("pressure").grid;

//		int nI=grid.length;
//		int nJ=grid[0].length;
//		int nK=grid[0][0].length;

        double accPressure = grid[x][y][z];

//		for(int i=-1;i<2;i++)
//			for(int j=-1;j<2;j++)
//				for(int k=-1;k<2;k++)
//				{
//					int p1=x+i;
//					int p2=y+j;
//					int p3=z+k;
//					if(p1>=0 && p1<nI && p2>=0 && p2<nJ && p3>=0 && p3<nK)
//						accPressure+=grid[p1][p2][p3];
//				}

        return accPressure;
    }

    public void setNewInThisType() // TJ Flann
    {
        // set true when the particle has changed into this type at the previous iteration
        _newInThisType = true;
    }

    public LinkedList<LocatedAgent> getMyTightJunctions() {
        return _myTightJunctions;
    }

    public boolean equals(Object obj) {
        return (obj instanceof LocatedAgent
                && _birthId == ((LocatedAgent) obj)._birthId);
    }


}