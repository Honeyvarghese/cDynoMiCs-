/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 */

/** 
 * @since June 2006
 * @version 1.0
 * @author Andreas D�tsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author S�nia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */

package simulator.agent.zoo;

import java.awt.Color;

import iDynoOptimizer.Global.FileReaderWriter;
import org.jdom.Element;

import utils.ExtraMath;
import utils.XMLParser;

import simulator.Simulator;
import simulator.agent.*;
import simulator.geometry.ContinuousVector;

public class Bacterium extends LocatedAgent implements Cloneable {

	/* Parameters common to all agents of this class _____________________ */
	//static StringBuffer       tempString;
	/* Temporary variables stored in static fields _______________________ */

	/* Parameters common (same reference) to all agents of a Species _____ */
	protected boolean         _hasEps          = false;
	protected boolean         _hasInert        = false;
	protected Species         _inertSpecies;
	protected Species         _epsSpecies;

	/* __________________ CONSTRUCTOR ___________________________________ */

	/*
	 * Empty constructor ; called to build a progenitor
	 */
	public Bacterium() {
		super();
		_speciesParam = new BacteriumParam();
	}

	/**
	 * Used by makeKid to create a daughter cell
	 */
	public Object clone() throws CloneNotSupportedException {
		Bacterium o = (Bacterium) super.clone();
		o._hasEps = this._hasEps;
		o._epsSpecies = this._epsSpecies;
		return o;
	}

	/**
	 * Initialises the progenitor
	 */
	public void initFromProtocolFile(Simulator aSim, XMLParser aSpeciesRoot) {
		// Initialisation of the Active agent
		super.initFromProtocolFile(aSim, aSpeciesRoot);

		// Check if it is a EPS-producing species
		XMLParser parser;

		for (Element aChild : aSpeciesRoot.getChildren("particle")) {
			// Initialize the xml parser
			parser = new XMLParser(aChild);

			if (parser.getAttributeStr("name").equals("capsule")) {
				_hasEps = true;
				int spIndex = aSim.getSpeciesIndex(parser.getAttributeStr("class"));
				_epsSpecies = aSim.speciesList.get(spIndex);
			}
			if (parser.getAttributeStr("name").equals("inert")) {
				_hasInert = true;

			}						
		}

		/* If no mass defined, use the division radius to find the mass */
		if (this._totalMass==0)
			guessMass();

		init();
	}

	public void initFromResultFile(Simulator aSim, String[] singleAgentData) {
		// this writes no unique values, so doesn't need unique reading-in
		// (for a template on how to read in data, look in LocatedAgent.java)
		super.initFromResultFile(aSim,singleAgentData);
	}

	/**
	 * Used to initialise any new agent (progenitor or daughter cell)
	 * 
	 * @see sendNewAgent()
	 */
	public void init() {
		// Lineage management : this is a new agent, he has no known parents
		_generation = 0;
		_genealogy = 0;

		// Determine the radius, volume and total mass of the agent
		updateSize();
	}

	/**
	 * Called by Bacterium.createAgent and to obtain another instance of the
	 * same species (totally independent) The returned agent is NOT registered
	 * 
	 * @see Species.sendAgent(), Bacterium.createAgent()
	 */
	public Bacterium sendNewAgent() throws CloneNotSupportedException {
		// Clone the agent and initialise it
		Bacterium baby = (Bacterium) this.clone();
		baby.init();
		return baby;
	}

	/**
	 * Create an agent (who a priori is registered in at least one container;
	 * this agent is located !
	 */
	public void createNewAgent(ContinuousVector position) {
		try {
			// Get a clone of the progenitor
			Bacterium baby = (Bacterium) sendNewAgent();
			baby.giveName();


			updateMass();
			/* If no mass defined, use the division radius to find the mass */
			if (this._totalMass==0) guessMass();

			if (_species.randomizeMassOnCreation) {
				// randomise its mass
				baby.mutatePop();
				baby.updateSize();
			}

			// Just to avoid to be in the carrier
			position.x += this._totalRadius;
			baby.setLocation(position);

			baby.registerBirth();

		} catch (CloneNotSupportedException e) {
			utils.LogFile.writeLog("Error met in Bacterium:createNewAgent()");
		}
	}

	public void mutatePop() {
		// Mutate inherited parameters
		super.mutatePop();

		// Now mutate your parameters
		// Rob 13/01/2011: Changed to make use of the ExtraMath.deviateFrom function
		for (int i = 0; i<particleMass.length; i++) {
			double deviate =  ExtraMath.deviateFrom(particleMass[i], getSpeciesParam().initialMassCV);
			particleMass[i] =deviate;
		}
	}

	/* _____________________ CELL DIVISION _____________________________ */

	public void mutateAgent() {
		// Mutate inherited parameters
		super.mutateAgent();

		// Now mutate your parameters
	}

	/**
	 * Called by Bacterium.divide() to create a daughter cell
	 */
	public void makeKid() throws CloneNotSupportedException {
		super.makeKid();
	}

	/* ___________________ STEP METHODS _______________________________ */

	/**
	 * Called at each time step (under the control of the method Step of the
	 * class Agent to avoid multiple calls
	 */
	protected void internalStep() {
		// Compute mass growth over all compartments
		grow();

		updateSize();

		// test if the EPS capsule has to be excreted
		manageEPS();

		// Divide if you have to
		if (willDivide()) divide();

		// Die if you have to
		if (willDie()){
			this.death = "tooSmall";
			die(true);
		}
	}
	
	public void activationInhibitionOperation(){};

	/**
	 * The agent is killed ; its body is converted into particle EPS and inert
	 */
	public void die(boolean isStarving) {
		super.die(isStarving);
		if (isStarving) {
			if (_hasEps) excreteEPS(1);
		}
	}

	/**
	 * If the EPS capsule is to thick, an EPS particle is excreted with 75% of
	 * the initial mass
	 */
	public void manageEPS() {
		if (!_hasEps) { return; }

		// manage excretion
		if (_volume/_totalVolume<(1-getSpeciesParam().epsMax)) {
			double ratio = ExtraMath.getUniRand(.6, .9);
			excreteEPS(ratio);
		}
	}

	/**
	 * Excrete part of your bound EPS as slime EPS
	 * 
	 * @param ratio
	 */
	public void excreteEPS(double ratio) {
		int indexEPS = this._agentGrid.mySim.getParticleIndex("capsule");

		// Check the mass to excrete exist
		if (particleMass[indexEPS]*ratio==0) return;

		// Create the particle
		ParticulateEPS eps = (ParticulateEPS) _epsSpecies.getProgenitor();
		// If the particle has been sucessfully created, update your size
		if (eps.createByExcretion(this, ratio)) {
			particleMass[indexEPS] *= (1-ratio);
			updateSize();
		}
	}


	public void excreteInert(double ratio) {
		int indexInert = this._agentGrid.mySim.getParticleIndex("inert");

		// Check the mass to excrete exist
		// if (particleMass[indexInert]*ratio==0) return;

		// Create the particle
		ParticulateEPS eps = (ParticulateEPS) _inertSpecies.getProgenitor();
		double totalMass = 0;
		if (eps.createInertByExcretion(this, ratio)) {
			for (int index = 0; index<particleMass.length; index++) {
				totalMass += particleMass[indexInert];
				particleMass[indexInert] = 0;
			}
			eps.particleMass[indexInert] = totalMass;
			updateSize();
		}
	}

	/**
	 * Test if the cell is big enough to divide (EPS is not included in this
	 * radius computation) 
	 * 
	 * bvm removed this 16.07.09 so that the routine in LocatedAgent is used instead
	 *  (they are the same)
	public boolean willDivide() {
		if (_netGrowthRate<=0) return false;


		//System.out.println("radii: "+getRadius(false)+", "+getSpeciesParam().divRadius);

		return getRadius(false) > getSpeciesParam().divRadius;

		//return getRadius(false)>ExtraMath.deviateFrom(getSpeciesParam().divRadius,
		//        getSpeciesParam().aleaDivRadius);
	}
	 */

	public boolean willDie() {
		updateRadius();
		if (_totalMass<0) return true;
		// Test cell radius
		if (getRadius(false)<=ExtraMath.deviateFrom(getSpeciesParam().deathRadius,
				getSpeciesParam().deathRadiusCV)) return true;

		// Test inert ratio
		/*
		 * if (_hasInert) { int indexInert =
		 * this._agentGrid.mySim.getParticleIndex("inert"); double ratioInert =
		 * particleMass[indexInert]/(particleMass[indexInert]+particleMass[0]);
		 * if(ratioInert>0.7) return true; }
		 */
		return false;
	}

	/* ________________________ TOOLBOX _________________________________ */

	public void guessMass(){
		double r, rdiff, volume;


		// Get randomly a radius between division radius and after-division
		// radius
		rdiff = 0.75*(getSpeciesParam().getMaximalRadius() - getSpeciesParam().getMinimalRadius());
		r = getSpeciesParam().getMinimalRadius() + rdiff*ExtraMath.getUniRand(0,1);

		if(Simulator.isChemostat){
			volume = 4/3*Math.PI*r*r*r;
		}else{
			if (_agentGrid.is3D)
				volume = 4/3*Math.PI*r*r*r;
			else
				volume = Math.PI*r*r*_species.domain.length_Z;
		}

		this.particleMass[0] = volume*getSpeciesParam().particleDensity[0];
		if(_hasEps){
			double value=getSpeciesParam().epsMax;
			value = ExtraMath.getUniRand(0, value);
			int indexEPS = this._agentGrid.mySim.getParticleIndex("capsule");
			particleMass[indexEPS] = value*particleMass[0]/getSpeciesParam().particleDensity[0]
			                                                                                 *getSpeciesParam().particleDensity[indexEPS];
		}
	}

	/* _______________ FILE OUTPUT _____________________ */

	public String sendHeader() {
		// return the header file for this agent's values after sending those for super
		// (for a template on how to write the header, look in LocatedAgent.java)
		StringBuffer tempString = new StringBuffer(super.sendHeader());

		return tempString.toString();
	}

	public String writeOutput() {
		// write the data matching the header file
		// (for a template on how to write data, look in LocatedAgent.java)
		StringBuffer tempString = new StringBuffer(super.writeOutput());

		return tempString.toString();
	}


	/* _______________ RADIUS, MASS AND VOLUME _____________________ */

	// MD Flann dont count volume of regulator
	public void updateVolume() {
		_totalVolume = 0;
		for (int i = 0; i<particleMass.length; i++) {
		    if (!particleRegulator[i])
			_totalVolume += particleMass[i]/getSpeciesParam().particleDensity[i];
		}

		// Compute volume without EPS capsule
		if (_hasEps) {
			int i = particleMass.length-1;
			_volume = _totalVolume-particleMass[i]/getSpeciesParam().particleDensity[i];
		} else {
			_volume = _totalVolume;
		}
	}

	/* ____________________ ACCESSORS & MUTATORS ______________________ */
	public BacteriumParam getSpeciesParam() {
		return (BacteriumParam) _speciesParam;
	}

	public boolean hasEPS() {
		return _hasEps;
	}

	public boolean hasInert() {
		return _hasInert;
	}

	//sonia: 23-07-09
	public double getBirthday(){
		return this._birthday;
	}

	/**
	 * compute the active fraction of the bacterium, ignoring EPS
	 * (i.e. only compare active and inert compartments)
	 */
	public double getActiveFrac() {
		if (!hasInert()) return 1.0;

		int indexActive = this._agentGrid.mySim.getParticleIndex("biomass");
		int indexInert = this._agentGrid.mySim.getParticleIndex("inert");

		double val = particleMass[indexActive]/(particleMass[indexActive] + particleMass[indexInert]); 

		if (Double.isNaN(val)) val = 1.0;

		return val;
	}



	/**
	 * Send the colour associated to the species defined for the EPS capsule
	 * 
	 * @redefine LocatedAgent.getColorCapsule()
	 */
	public Color getColorCapsule() {
		if (_epsSpecies==null) return getSpeciesParam().epsColor;
		else return _epsSpecies.color;
	}


	public Color getColor() {
		return super.getColor();


	}


	@Override
	protected void conjugate(double elapsedHGTtime) {
		// TODO Auto-generated method stub

	}


}
