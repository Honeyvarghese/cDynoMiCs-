/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ______________________________________________________
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 */

package simulator.agent;

import idyno.SimTimer;

import java.util.ArrayList;
import java.util.HashMap;

import org.jdom.Element;

import utils.ExtraMath;
import utils.LogFile;
import utils.XMLParser;
import simulator.Simulator;
import simulator.agent.zoo.Molecule;
import simulator.reaction.Reaction;
import simulator.reaction.molecularReaction.Compartment;
import simulator.reaction.molecularReaction.IndexedPriorityQueue;
import simulator.reaction.molecularReaction.MolecularReaction;
import simulator.reaction.molecularReaction.ReactionDependencyGraph;
import simulator.reaction.molecularReaction.molecularReactionManager.ReactionProductFiringEvent;
import simulator.SpatialGrid;

public abstract class ActiveAgent extends SpecialisedAgent implements HasReaction {

	// Parameters common to all agents of this class

	// Parameters common (strict egality) to all agents of a Species

	// massic growth rate of the agent (the sum of the growth rate of all of
	// its compounds)

	public Reaction[]            allReactions;
	public MolecularReaction[]   allMolecularReactions;
	public ReactionDependencyGraph reactionDependencyGraph;
	public ArrayList<Integer> activeMolecularReactions;
	public ArrayList<Integer> reactionActive;
	public ArrayList<Integer> reactionKnown;
	public ArrayList<ReactionProductFiringEvent> reactionProductQueue;
	public double[] a;
	public double[] taus;
	public double[] lastNonInfTau;
	public double[] lastNonZeroA;
	public IndexedPriorityQueue tauQueue;

	public double             _netGrowthRate = 0;
	public double             _netVolumeRate = 0;
	
	protected double[]           growthRate;
	// Reaction parameters : (potentially)mutated from species parameters
	public double[][]            soluteYield;
	public double[][]            reactionKinetic;
	public double[][]            particleYield;

	// Mass of the agent (table for all particles belonging to the agent)
	public double[]              particleMass;
	// If the internal particle type is a regulator so it does not contribute to the mass MD Flann
	public boolean[] 		particleRegulator;
	// Sum of masses of all particles
	protected double             _totalMass;
	
	public HashMap<String, Molecule> molecules;
	public HashMap<String, simulator.reaction.molecularReaction.Compartment> compartments;
	Simulator aSim;
	//public Compartment[] compartments;
	/* ________________________ CONSTRUCTOR _________________________________ */

	/**
	 * The constructor is used to create the progenitor of the species
	 */
	public ActiveAgent() {
		super();
		_speciesParam = new ActiveParam();

	}

	/**
	 * Initialize Reaction fields Called by CreateSpecies to initalize the
	 * progenitor The species parameter have already been defined
	 */
	public void initFromProtocolFile(Simulator aSim, XMLParser xmlMarkUp) {
		// Initialisation common to all specialised agents
		super.initFromProtocolFile(aSim, xmlMarkUp);
		this.aSim=aSim;
		/* Create internal compounds________________________________________ */

		// Initialize tables for the compartments description
		int nParticle = aSim.particleDic.size();
		int nReaction = aSim.reactionList.length;
		int nSolute = aSim.soluteList.length;
		int reacIndex;
		molecules=new HashMap<String, Molecule>();
		compartments = new HashMap<String, simulator.reaction.molecularReaction.Compartment>();
		particleMass = new double[nParticle];
		// MD Flann
		particleRegulator = new boolean[nParticle];

		// Build the list of particles
		XMLParser parser;
		int particleIndex;
		
		for (Element aChild : xmlMarkUp.getChildren("particle")) {
			// Initialize the xml parser
			parser = new XMLParser(aChild);
			particleIndex = aSim.getParticleIndex(parser.getAttribute("name"));
			// Set the average mass of the particle within the initial
			// population 
			particleMass[particleIndex] = parser.getParamMass("mass");
			// MD Flann
			// initialize the local array particleRegulator to be true for those particles that are
			// regulators and do not contribute to the mass of the particle
			particleRegulator[particleIndex] = aSim.getparticleRegulator(parser.getAttribute("name"));
		}
		
		updateMass();

		/* Create description of reactions _________________________________ */

		// Initialize the arrays
		allReactions = aSim.reactionList;
		reactionKnown = new ArrayList<Integer>();
		reactionActive = new ArrayList<Integer>();
		growthRate = new double[nReaction];

		soluteYield = new double[nReaction][nSolute];
		reactionKinetic = new double[nReaction][];
		particleYield = new double[nReaction][nParticle];
		
		// Read the XML file

		for (Element aReactionMarkUp : xmlMarkUp.buildSetMarkUp("reaction")) {
			reacIndex = aSim.getReactionIndex(aReactionMarkUp.getAttributeValue("name"));
			Reaction aReaction = allReactions[reacIndex];

			// Add the reaction to the list of known (and active) reactions
			reactionKnown.add(reacIndex);
			if (aReactionMarkUp.getAttributeValue("status").equals("active")) {
				reactionActive.add(reacIndex);
			}

			// If reaction parameters have been redefined, load them ; else load
			// the parameters defined for the reaction
			if (aReactionMarkUp.getContentSize()==0) {
				soluteYield[reacIndex] = aReaction.getSoluteYield();
				particleYield[reacIndex] = aReaction.getParticulateYield();
				reactionKinetic[reacIndex] = aReaction.getKinetic();
			} else {
				aReaction.initFromAgent(this, aSim, new XMLParser(aReactionMarkUp));
			}
		}

		// Now copy these value in the speciesParam structure
		getSpeciesParam().soluteYield = soluteYield.clone();
		getSpeciesParam().particleYield = particleYield.clone();
		getSpeciesParam().reactionKinetic = reactionKinetic.clone();


		//%%%%%%%%%%%%% Build the list of molecules %%%%%%%%%%%%%%%%%%%%%
		String name, type, locale;
		Boolean isEssential, isMainCompartment;
		double divisionThreshold, secretionRate=0;
		int number;
		XMLParser moleculeParser = new XMLParser(xmlMarkUp.getChildElement("molecules"));
		String receptorTriggers="";

		if(moleculeParser.get_localRoot()!=null){ 
			for (Element compartmentMarkUp : moleculeParser.buildSetMarkUp("compartment")) {
				parser=new XMLParser(compartmentMarkUp);
				name = parser.getAttribute("name");
				number= parser.getAttribute("initNumber")==null?1:(int) parser.getAttributeDbl("initNumber");
				isEssential = parser.getAttribute("essential")==null?false:parser.getAttribute("essential").equalsIgnoreCase("true");
				isMainCompartment = parser.getAttribute("mainCompartment")==null?false:parser.getAttribute("mainCompartment").equalsIgnoreCase("true");
				aSim.molecularReactionManager.registerCompartment(this, name, number, isEssential, isMainCompartment);
			}
		    for (Element moleculeMarkUp : moleculeParser.buildSetMarkUp("molecule")) {
		    	receptorTriggers="";
		    	secretionRate=0;
				parser=new XMLParser(moleculeMarkUp);
				name = parser.getAttribute("name").trim();
				locale = parser.getAttribute("at").trim();
				type = parser.getAttribute("type")==null?"default":parser.getAttribute("type").trim();
				if(type.equalsIgnoreCase("receptor"))
					receptorTriggers= parser.getAttribute("triggeredBy").trim();
				else if(type.equalsIgnoreCase("signaller"))
					secretionRate=parser.getAttributeDbl("secretionRate");
				//initialValue= parser.getAttribute("initValue")==null?0:parser.getAttributeDbl("initValue");
				number= parser.getAttribute("initNumber")==null?0:(int) parser.getAttributeDbl("initNumber");
				divisionThreshold= parser.getAttribute("divisionThreshold")==null?1: parser.getAttributeDbl("divisionThreshold");
				if(molecules.containsKey(name))
					throw new RuntimeException("Repeated molecule: "+name);
				molecules.put(name, new Molecule(this, name, locale, type, receptorTriggers, secretionRate, number, divisionThreshold, aSim));
		    }
		}
		
		//%%%%%%%%%%%%%%%% Create the description of all molecular reactions %%%%%%%%%%%%%%%%%%%%
		allMolecularReactions=aSim.molecularReactionList;
		activeMolecularReactions=new ArrayList<Integer>();
		reactionProductQueue=new ArrayList<ReactionProductFiringEvent>();
		XMLParser molecularReactionParser = new XMLParser(xmlMarkUp.getChildElement("agentMolecularReactions"));
		if (molecularReactionParser.get_localRoot() != null){
			for (Element aMReactionMarkUp : molecularReactionParser.buildSetMarkUp("molecularReaction")) {
				reacIndex = aSim.getMolecularReactionIndex(aMReactionMarkUp.getAttributeValue("name"));
				if(reacIndex==-1)
					LogFile.writeError("Unknown molecular reaction: "+aMReactionMarkUp.getAttributeValue("name"), "ActiveAgent initFromProtocolFile");
				//MolecularReaction aReaction = allMolecularReactions[reacIndex];
	
				// Add the reaction to the list of known (and active) reactions
				
				if (aMReactionMarkUp.getAttributeValue("status").equalsIgnoreCase("active")) {
					activeMolecularReactions.add(reacIndex);
				}
			}
		}
		if(aSim.molecularReactionManager!=null)
		{
			reactionDependencyGraph= new ReactionDependencyGraph(this, aSim);		
			aSim.molecularReactionManager.initializeReactionsForAgent(this);
		}
	}
	
	public void initFromResultFile(Simulator aSim, String[] singleAgentData) {
		// this routine will read data from the end of the singleAgentData array
		// and then pass the remaining values onto the super class

		// find the position to start at by using length and number of values read
		int nValsRead = 2 + particleMass.length;
		int iDataStart = singleAgentData.length - nValsRead;

		// read in info from the result file IN THE SAME ORDER AS IT WAS OUTPUT
		
		// Particle Masses
		for (int iComp = 0; iComp<particleMass.length; iComp++)
			particleMass[iComp] = Double.parseDouble(singleAgentData[iDataStart+iComp]);
		
		// other values
		_netGrowthRate = Double.parseDouble(singleAgentData[iDataStart+particleMass.length]);
		_netVolumeRate = Double.parseDouble(singleAgentData[iDataStart+particleMass.length+1]);

		// now go up the hierarchy with the rest of the data
		String[] remainingSingleAgentData = new String[iDataStart];
		for (int i=0; i<iDataStart; i++)
			remainingSingleAgentData[i] = singleAgentData[i];
		super.initFromResultFile(aSim, remainingSingleAgentData);

		// finally some creation-time calls
		updateSize();
		registerBirth();		
	}	

	public void mutatePop() {
		// Mutate parameters inherited
		super.mutatePop();
		// Now mutate your own class parameters
	}

	/**
	 * Create an agent (who a priori is registered in at least one container;
	 * this agent is NOT located ! Implemented here for compatibility reasons
	 */
	public void createNewAgent() {
		try {
			ActiveAgent baby = (ActiveAgent) sendNewAgent();
			baby.mutatePop();

			// Register the baby in the pathway guilds an
			baby.registerBirth();

		} catch (CloneNotSupportedException e) {
			System.out.println("At ActiveAgent: createNewAgent error " + e);
		}
	}

	public void registerBirth() {
		super.registerBirth();
		// register the agent in the metabolic containers
		registerOnAllActiveReaction();
	}

	/* ___________________ DIVISION ______________________________ */

	@SuppressWarnings("unchecked")
	public Object clone() throws CloneNotSupportedException {
		ActiveAgent o = (ActiveAgent) super.clone();
		// Shallow copy : the reaction are not cloned
		o.reactionActive = (ArrayList<Integer>) this.reactionActive.clone();
		o.reactionKnown = (ArrayList<Integer>) this.reactionKnown.clone();
		o.allReactions = this.allReactions.clone();
		
		///////Molecular system clone
		if(this.allMolecularReactions!=null)
		{
			o.allMolecularReactions=this.allMolecularReactions.clone();
			o.activeMolecularReactions= (ArrayList<Integer>) this.activeMolecularReactions.clone();
			o.reactionDependencyGraph=this.reactionDependencyGraph;
			o.molecules=new HashMap<String, Molecule>();
			o.compartments=new HashMap<String, Compartment>();
			//Check if it is the first instance of this molecule being created in the system
			if(simulator.Simulator.isInitializing)
			{
				for(String compartmentKey:this.compartments.keySet())
				{
					Compartment compart= compartments.get(compartmentKey);
					o.compartments.put(compartmentKey, new Compartment(compartmentKey, compart.numberOfInstances, compart.isEssential, compart.isMainCompartment));

					for(String moleculeName:compart.containedMolecules)
					{
						Molecule molecule= molecules.get(moleculeName);
						Molecule newMolecule=new Molecule(o, moleculeName, compartmentKey, molecule.type, 
								molecule.triggers, molecule.secretionRate, 0, molecule.divisionThreshold, aSim);
						for(int i=0; i<compart.numberOfInstances;i++)
						{
							newMolecule.instances.set(i, molecule.instances.get(i));
							newMolecule.amount+=molecule.instances.get(i);
						}
						o.molecules.put(moleculeName, newMolecule);
					}
				}

			}
			else
			{

				int numMoleculesToAdd;
				for(String compartmentKey:this.compartments.keySet())
				{
					Compartment compart= compartments.get(compartmentKey);
					if(compart.numberOfInstances==1)
					{
						//Check it this compartment is essential, so it should divide
						if(compart.isEssential)
						{
							o.compartments.put(compartmentKey, new Compartment(compartmentKey, 1,true, compart.isMainCompartment));
							for(String moleculeName:compart.containedMolecules)
							{
								Molecule molecule= molecules.get(moleculeName);
								numMoleculesToAdd=(int) Math.round(molecule.instances.get(0)*0.5);
								o.molecules.put(moleculeName, new Molecule(o, moleculeName, compartmentKey, molecule.type, molecule.triggers, molecule.secretionRate, numMoleculesToAdd, molecule.divisionThreshold, aSim));
								molecule.setInstanceValue(0,molecule.amount-numMoleculesToAdd);
								//						molecules.get(moleculeName).instances.set(0, (int) (molecule.amount-numMoleculesToAdd));
								//						molecules.get(moleculeName).amount-=numMoleculesToAdd;
							}
						}
						continue;
					}
					int o_InstanceNumber= (int) Math.round(compart.numberOfInstances*0.5);
					o.compartments.put(compartmentKey, new Compartment(compartmentKey, o_InstanceNumber, compart.isEssential, compart.isMainCompartment));
					compartments.get(compartmentKey).numberOfInstances=compart.numberOfInstances-o_InstanceNumber;

					for(String moleculeName:compart.containedMolecules)
					{
						Molecule molecule= molecules.get(moleculeName);
						for(int i=0; i<o_InstanceNumber ;i++)
						{
							//select a random compartment to move to the daughter
							int indexToMove=ExtraMath.getUniRandInt(0, molecule.instances.size());
							o.molecules.put(moleculeName, new Molecule(o, moleculeName, compartmentKey, molecule.type, molecule.triggers, molecule.secretionRate, molecule.instances.get(indexToMove), molecule.divisionThreshold, aSim));
							molecule.removeInstance(indexToMove);
							compartments.get(compartmentKey).instancesName.remove(indexToMove);
							//molecule.instances.remove(indexToMove);
						}
					}
				}
			}
			aSim.molecularReactionManager.initializeReactionsForAgent(o);
			aSim.molecularReactionManager.initializeReactionsForAgent(this);
		}
		//////////////////////////////////////////////////////////////////////
		o.growthRate = new double[growthRate.length];
		
		
		o.soluteYield = new double[soluteYield.length][];
		for (int iter = 0; iter<soluteYield.length; iter++) {
			o.soluteYield[iter] = this.soluteYield[iter].clone();
		}

		o.reactionKinetic = new double[reactionKinetic.length][];
		o.particleYield = new double[particleYield.length][];

		for (int iter = 0; iter<reactionKnown.size(); iter++) {
			int jReac = reactionKnown.get(iter);
			if (this.reactionKinetic[jReac]!=null) o.reactionKinetic[jReac] = this.reactionKinetic[jReac]
			        .clone();
			o.particleYield[jReac] = this.particleYield[jReac].clone();
		}

		o.particleMass = this.particleMass.clone();

		return (Object) o;
	}

	public void mutateAgent() {
		// Mutate parameters inherited
		super.mutateAgent();
		// Now mutate your own class parameters

	}

	public void die(boolean isStarving) {
		super.die(isStarving);
		// If you are too small, you must die !
		// Decrease the population of your species


		// Unregister from the metabolic guilds
		unregisterFromAllActiveReactions();
	}

	/* ___________________ STEP ______________________________ */
	/**
	 * Called at each time step (under the control of the method Step of the
	 * class Agent to avoid multiple calls
	 */
	protected void internalStep() {
		grow();
		updateSize();
	}

	/**
	 * Perform growth by calling all active pathways
	 */
	protected void grow() {
		double deltaMass;
		int reacIndex;
		_netGrowthRate = 0;
		_netVolumeRate = 0;

		// Compute mass growth rate of each active reaction
		for (int iReac = 0; iReac<reactionActive.size(); iReac++) {
			// Compute the growth rate
			reacIndex = reactionActive.get(iReac);
			// get the growth rate in [fgX.hr-1]
			growthRate[reacIndex] = allReactions[reacIndex].computeMassGrowthRate(this);

			// Apply the growth rate on the particles
			for (int i = 0; i<particleYield[reacIndex].length; i++) {
				deltaMass = particleYield[reacIndex][i]*growthRate[reacIndex];
				_netGrowthRate += deltaMass;
				_netVolumeRate += deltaMass/getSpeciesParam().particleDensity[i];

				particleMass[i] += (deltaMass*SimTimer.getCurrentTimeStep());
			}
		}
	}

	/**
	 * Take into account all growth
	 */
	public void updateSize() {
		updateMass();
	}

	public void updateMass() {
	    // MD flann, sum up the mass of the particles for this agent, but do not include regulators
	    _totalMass = 0.0;
	    for (int i=0; i<particleMass.length;i++ )
		if (!particleRegulator[i])
		    _totalMass += particleMass[i];
	    // old code just summed them all
	    //_totalMass = ExtraMath.sumVector(particleMass);
		// debug code
		//String mess = "Mass=";
		//    for (int i=0; i<particleMass.length;i++)
		//	mess = mess+Integer.toString(i)+"="+particleMass[i]+" ";
		//	
		//LogFile.writeLog(mess);
	}

	/* ______________________ REACTION MANAGEMENT __________________________ */

	public void addReaction(Reaction aReaction, boolean useDefaultParam) {
		// Add the reaction to the list of known reaction
		reactionKnown.add(aReaction.reactionIndex);

		// Test if specific parameters exist for this reaction
		int index = aReaction.reactionIndex;
		boolean test = getSpeciesParam().soluteYield[index]==null;

		if (useDefaultParam||test) {
			// Use parameters defined in the reaction object
			reactionKinetic[index] = aReaction.getKinetic();
			soluteYield[index] = aReaction.getSoluteYield();
			particleYield[index] = aReaction.getParticulateYield();
		} else {

			// Use parameters defined in the speciesParam structure
			reactionKinetic[index] = getSpeciesParam().reactionKinetic[index];
			soluteYield[index] = getSpeciesParam().soluteYield[index];
			particleYield[index] = getSpeciesParam().particleYield[index];
		}
	}

	public void addActiveReaction(Reaction aReaction, boolean useDefaultParam) {
		addReaction(aReaction, useDefaultParam);
		switchOnReaction(aReaction);
	}

	public void removeReaction(Reaction aPathway) {
		switchOffreaction(aPathway);
		reactionKnown.remove(aPathway);
	}

	// bvm 27.11.08: added the '.reactionIndex' calls to the two
	// lines below in order to get this function to work correctly
	public void switchOffreaction(Reaction aPathway) {
		if (reactionActive.contains(aPathway.reactionIndex)) {
			// need to remove using indexOf because the remove(object) version thinks
			// the int being passed in is the index to remove rather than the object to remove
			reactionActive.remove(reactionActive.indexOf(aPathway.reactionIndex));
			aPathway.removeAgent(this);
		}
	}

	// bvm 27.11.08: added the if statement to prevent adding a reaction if
	// it is already present
	public void switchOnReaction(Reaction aReaction) {
//		System.out.println("Turn it on? "+aReaction.reactionName);
		if (!reactionActive.contains(aReaction.reactionIndex)) {
//			System.out.println("Turn on: "+aReaction.reactionName);
			reactionActive.add(aReaction.reactionIndex);
			aReaction.addAgent(this);
		}
	}

	/**
	 * Register the agent on each guild of its activated pathways. Called by
	 * makeKid
	 */
	public void registerOnAllActiveReaction() {
		for (int iReac = 0; iReac<reactionActive.size(); iReac++) {
			allReactions[reactionActive.get(iReac)].addAgent(this);
		}
	}

	/**
	 * Called by the die method
	 */
	public void unregisterFromAllActiveReactions() {
		for (int iReac = 0; iReac<reactionActive.size(); iReac++) {
			allReactions[reactionActive.get(iReac)].removeAgent(this);
		}
	}

	/**
	 * Add the mass of an agent on received grid
	 * @param aSpG : grid used to sum catalysing mass
	 * @param catalyst index : index of the compartment of the cell supporting
	 * the reaction TODO
	 */
	public void fitMassOnGrid(SpatialGrid aSpG, int catalystIndex) {
	}

	public void fitReacRateOnGrid(SpatialGrid aRateGrid, int reactionIndex) {
	}

	/**
	 * Add the mass of an agent on received grid
	 * @param aSpG : grid used to sum catalysing mass TODO
	 */
	public void fitMassOnGrid(SpatialGrid aSpG) {
	}

	/* _______________ FILE OUTPUT _____________________ */

	public String sendHeader() {
		// return the header file for this agent's values after sending those for super
		StringBuffer tempString = new StringBuffer(super.sendHeader());
		tempString.append(",");
		
		// particle types
		for (int i = 0; i<particleMass.length; i++) {
			tempString.append(_species.currentSimulator.particleDic.get(i));
			tempString.append(",");
		}
		tempString.append("growthRate,volumeRate");
		return tempString.toString();
	}

	public String writeOutput() {
		// write the data matching the header file
		StringBuffer tempString = new StringBuffer(super.writeOutput());
		tempString.append(",");

		// Mass of different particles
		for (int i = 0; i<particleMass.length; i++) {
			tempString.append(particleMass[i]);
			tempString.append(",");
		}
		// Agent growth and volume rates
		tempString.append(_netGrowthRate+","+_netVolumeRate);

		return tempString.toString();
	}

	/* ____________________ ACCESSORS & MUTATORS _________________________ */
	public double getTotalMass() {
		return _totalMass;
	}

	public double getParticleMass(int particleIndex) {
		return particleMass[particleIndex];
	}

	public double getNetGrowth() {
		return _netGrowthRate;
	}

	public double getVolGrowth() {
		return _netVolumeRate;
	}

	public void setNetGrowth(double value) {
		_netGrowthRate = value;
	}

	public double[] getSoluteYield(int indexReaction) {
		return soluteYield[indexReaction];
	}

	public double[] getReactionKinetic(int indexReaction) {
		return reactionKinetic[indexReaction];
	}

	public ActiveParam getSpeciesParam() {
		return (ActiveParam) _speciesParam;
	}

	public abstract double calcDensityAround();
	
	public abstract double calcPressureAround();
		
	

	

}
