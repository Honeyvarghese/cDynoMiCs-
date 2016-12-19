
/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */
 


package simulator.reaction;
import java.io.Serializable;
import java.util.*;

import simulator.*;
import simulator.agent.*;
import simulator.geometry.Bulk;
import simulator.geometry.ContinuousVector;
import simulator.geometry.DiscreteVector;
import simulator.geometry.boundaryConditions.AllBC;
import utils.LogFile;
import utils.ResultFile;
import utils.XMLParser;

@SuppressWarnings("serial")
public abstract class Reaction implements Serializable {

	/* ________________________ PROPERTIES ________________________________ */
	// List of process created
	public String                     reactionName;
	public int                        reactionIndex;
	public int                        _catalystIndex;
	public String                     _catalystSpeciesName;
	public int                        _catalystSpeciesIndex;

	// Agents hosting this process
	protected LinkedList<ActiveAgent> _guild = new LinkedList<ActiveAgent>();

	// List of solute grids needed by THIS process (however the size of the
	// array is defined by the total number of solutes)
	protected SoluteGrid[]            _soluteList;
	//sonia 21.09.09 I changed the visibility of _mySoluteIndex to public to access it from the Solver_chemostat class
	public int[]                   _mySoluteIndex;

	protected SoluteGrid              _reacGrid, _guildGrid;
	public double[]                   totalUptake;
	public double                     globalReactionRate;

	// Temporary storage used during computation of reaction rates
	protected double                  _specRate;
	// Buffer vectors used to communicate with a solver. If a solute is
	// not concerned by the current reaction, the uptake will be zero
	protected double[]                _diffUptakeRate;
	protected double[]                _uptakeRate;
	


	// Parameters of the reaction : yield and kinetic params
	// If a solute is not concerned by the current reaction, the yield will be
	// zero
	protected double[]                _soluteYield;
	protected double[]                _kineticParam;
	protected double[]                _particleYield;
	
	//sonia: 21-05-09
	protected String[]             _particleNameYield ;
	
	//sonia 21-04-10
	//dilution rate from associated bulk
	public double Dil;
	protected Simulator aSim;
	// Temporary variables
	static int                        nSolute;

	/* _________________________ CONSTRUCTOR________________________________ */

	/**
	 * Initialization procedure ; based on XML file Kinetic parameters will be
	 * managed by the subdefined method in the children class
	 * @param aSim
	 * @param aReactionRoot
	 * @see Simulator.createReaction()
	 */
	public void init(Simulator aSim, XMLParser aReactionRoot) {

		nSolute = 0;


		reactionName = aReactionRoot.getAttribute("name");
		
	
			nSolute = aSim.soluteList.length;
		int nParticulate = aSim.particleDic.size();
		
		// Create a simple array of all solutes
		_soluteList = aSim.soluteList;
		_reacGrid = new SoluteGrid(_soluteList[0]);
		_reacGrid.gridName = reactionName+"-rate";

		_guildGrid = new SoluteGrid(_soluteList[0]);
		_guildGrid.gridName = reactionName+"-pop";
	
		// Initialize buffer arrays
		_uptakeRate = new double[nSolute];
		_diffUptakeRate = new double[nSolute];
		totalUptake = new double[nSolute];

		// Initialize arrays storing reaction parameters
		_soluteYield = new double[nSolute];
		_particleYield = new double[nParticulate];
		_particleNameYield = new String[nParticulate];
		
		for (AllBC aBC : _reacGrid.getDomain().getAllBoundaries()){
			if (aBC.hasBulk()){
				Bulk aBulk = aBC.getBulk();
					if(aBulk.getName().equals("chemostat")){
						Dil = aBulk._D;
					}
			}	
		}

		// Extract the yields for solutes and particulates from the XML file
		fillParameters(aSim, aReactionRoot);
	}

	/**
	 * Called by an ActiveAgent to populate their reaction parameters vectors
	 * Populate only the yield vector, the kinetic vector will be filled by the
	 * subdefined method in the child classes
	 * @param anActiveAgent : the agent calling the method
	 * @param aReactionRoot : the root describing the reaction hosted by the
	 * agent in the XML file
	 */
	public void initFromAgent(ActiveAgent anAgent, Simulator aSim, XMLParser xmlRoot) {

		nSolute = 0;
		double yield;

		// Populate yield for solutes __________________________________
		XMLParser parser = new XMLParser(xmlRoot.getChildElement("yield"));
		for (int iSolute = 0; iSolute<_soluteList.length; iSolute++) {
			yield = parser.getParamSuchDbl("solute", _soluteList[iSolute].getName());
			if (!Double.isNaN(yield)) {
				anAgent.soluteYield[reactionIndex][iSolute] = yield;
			}
		}

		// Populate yields for particles _________________________________
		for (int iParticle = 0; iParticle<aSim.particleDic.size(); iParticle++) {
			yield = parser.getParamSuchDbl("particle", aSim.particleDic.get(iParticle));
			if (!Double.isNaN(yield)) {
				anAgent.particleYield[reactionIndex][iParticle] = yield;
			}
		}
	}

	/**
	 * End of the initialization procedure
	 * @param aReactionName
	 * @param aSimulator
	 * @see Simulator.addReaction()
	 */
	public void register(String aReactionName, Simulator aSimulator) {
		reactionName = aReactionName;
		reactionIndex = aSimulator.getReactionIndex(aReactionName);
		aSim=aSimulator;
	}

	/**
	 * Called by a solver instance to know the solutes affected by this reaction
	 * @return a list of solutes affected by this reaction
	 * @see DiffusionSolver.addReaction()
	 */
	public LinkedList<String> declareSolutes() {
		LinkedList<String> affectedSolutes = new LinkedList<String>();
		for (int index : _mySoluteIndex) {

			affectedSolutes.add(_soluteList[index].getName());
			}
		
		return affectedSolutes;
	}

	/**
	 * Used during the initialization
	 */
	public void fillParameters(Simulator aSim, XMLParser xmlRoot) {

		// Who is mediating the reaction ?
		String catalystName = xmlRoot.getAttributeStr("catalyzedBy");
		_catalystSpeciesName = xmlRoot.getAttributeStr("catalyst");
		_catalystIndex = aSim.particleDic.indexOf(catalystName);

		// Populate yield for solutes __________________________________
		double yield;
//		int nSolute = 0;
		XMLParser parser = new XMLParser(xmlRoot.getChildElement("yield"));
		for (int iSolute = 0; iSolute<_soluteList.length; iSolute++) {
			//yield = parser.getParamSuchDbl("solute", _soluteList[iSolute].getName());
			yield = parser.getParamDbl( _soluteList[iSolute].getName());
			if (!Double.isNaN(yield)) {
				nSolute++;
				_soluteYield[iSolute] = yield;
			}
		}

		// Populate yields for particle _________________________________
		String particleName;
		for (int iParticle = 0; iParticle<aSim.particleDic.size(); iParticle++) {
			//yield = parser.getParamSuchDbl("particle", aSim.particleDic.get(iParticle));
			yield = parser.getParamDbl(aSim.particleDic.get(iParticle));
			particleName = aSim.particleDic.get(iParticle);
			if (!Double.isNaN(yield)) {
				_particleYield[iParticle] = yield;
				_particleNameYield[iParticle] = particleName;
			}
		}

		int jSolute = 0;
		for (int iSolute = 0; iSolute<_soluteList.length; iSolute++)
			if (_soluteYield[iSolute]!=0) jSolute++;

		_mySoluteIndex = new int[jSolute];
		jSolute = 0;
		for (int iSolute = 0; iSolute<_soluteList.length; iSolute++) {
			if (_soluteYield[iSolute]!=0) {
				_mySoluteIndex[jSolute] = iSolute;
				jSolute++;
			}
		}
		

	}

	/* ______________ METHODS FOR REACTION MANAGEMENT_________________________ */

	/**
	 * Register an agent among the guild of this pathway
	 * @param anAgent
	 */
	public void addAgent(ActiveAgent anAgent) {
		_guild.add(anAgent);
	}

	/**
	 * Remove an agent among the guild of this pathway
	 * @param anAgent
	 */
	public void removeAgent(ActiveAgent anAgent) {
		_guild.remove(anAgent);
	}

	/**
	 * Compute the total mass of all members of the guild
	 * @return the reacting mass
	 */
	public double getReactingMass() {
		double totalMass = 0;
		for (ActiveAgent anAgent : _guild) {
			totalMass += anAgent.getParticleMass(_catalystIndex);
		}
		return totalMass;
	}

	public double getUptakeRate(int soluteIndex) {
		return _uptakeRate[soluteIndex];
	}

	/* ________________ COMMUNICATION WITH THE SOLVER _____________________ */

	/**
	 * Mass growth-rate (in gX.h-1)
	 */
	public abstract double computeMassGrowthRate(ActiveAgent anAgent);

	/* Specific growth-rates are independant of agent mass ______________ */
	public abstract void computeSpecificGrowthRate(ActiveAgent anAgent);

	public abstract void computeSpecificGrowthRate(double[] s, ActiveAgent anAgent);

	public abstract void computeSpecificGrowthRate(double[] s);

	/**
	 * Add the contribution of this agent on the reaction grid and the diff
	 * reaction grid Catalyst quantity is expressed in CONCENTRATION
	 */
	public abstract void computeUptakeRate(double[] s, double conc, double h);
	public abstract void computeUptakeRate(double[] s, double[][][] concGrid, double h, ContinuousVector cv);

	/**
	 * Add the contribution of this agent on the reaction grid and the diff
	 * reaction grid
	 */
	public abstract void computeUptakeRate(double[] s, ActiveAgent anAgent);

	/**
	 * Compute reaction rate on each concerned solute grids Assumes same
	 * parameters for all the agents of a same guild
	 * @param concGrid : solute concentration
	 * @param reacGrid : contribution of the reaction to the solute
	 * concentration dynamics
	 * @param diffReacGrid : derivative of the previous grid
	 * @param biomassGrid : CONCENTRATION of the catalyst
	 * @see multigrid solver
	 */
	public void applyReaction(SpatialGrid[] concGrid, SpatialGrid[] reacGrid,
	        SpatialGrid[] diffReacGrid, SpatialGrid biomassGrid) {

		nSolute = concGrid.length;
		double[] s = new double[nSolute];

		int _nI, _nJ, _nK;
		_nI = biomassGrid.getGridSizeI();
		_nJ = biomassGrid.getGridSizeJ();
		_nK = biomassGrid.getGridSizeK();
		globalReactionRate = 0;

		
		for (int i = 1; i<_nI+1; i++) {
			for (int j = 1; j<_nJ+1; j++) {
				for (int k = 1; k<_nK+1; k++) {
					// If there is no biomass, go to the next grid elt
					if (biomassGrid.grid[i][j][k]==0) continue;

					// Read local solute concentration
					for (int iGrid : _mySoluteIndex)
						s[iGrid] = concGrid[iGrid].grid[i][j][k];

					// First compute local uptake-rates in g.h-1
					//sonia:chemostat added a parameter to the computeUptakerate method
					//computeUptakeRate(s, biomassGrid.grid[i][j][k], 0);
					computeUptakeRate(s, biomassGrid.grid, 0, new ContinuousVector(i, j, k));	
					globalReactionRate += _specRate;
		
					// Now add them on the received grids
					for (int iGrid : _mySoluteIndex) {
						reacGrid[iGrid].grid[i][j][k] += _uptakeRate[iGrid];
						diffReacGrid[iGrid].grid[i][j][k] += _diffUptakeRate[iGrid];
						if (Double.isNaN(reacGrid[iGrid].grid[i][j][k])) 
							LogFile.writeLog("NaN generated in Reaction");
					}
					
				}
			}
		}
	}

	
	//sonia:chemostat
	
	public void applyChemostatReaction(SpatialGrid[] concGrid, SpatialGrid[] reacGrid, double [][] diffReacGrid, 
			double [] allDiffSum, SpatialGrid biomassGrid, int reacIndex, double tdel) {
		
		
		nSolute = concGrid.length;
		double[] s = new double[nSolute];

		int _nI, _nJ, _nK;
		_nI = biomassGrid.getGridSizeI();
		_nJ = biomassGrid.getGridSizeJ();
		_nK = biomassGrid.getGridSizeK();
		globalReactionRate = 0;

		for (int i = 0; i<_nI; i++) {
			for (int j = 0; j<_nJ; j++) {
				for (int k = 0; k<_nK; k++) {
					// If there is no biomass, go to the next grid element - which in this case is only one
					if (biomassGrid.grid[i][j][k]==0) continue;
					
					// Read local solute concentration
					for (int iGrid : _mySoluteIndex){
						//s[iGrid] = concGrid[iGrid].grid[i][j][k];
						s[iGrid] = concGrid[iGrid].getAverageChemo();
					}
					
					// First compute local uptake-rates in g.h-1
				
					computeUptakeRate(s, biomassGrid.grid[i][j][k], tdel );
					globalReactionRate += _specRate;	
					
					//sonia 18.09.09
					// filling the 2D array with the diff rates of each solute in each reaction
					
					for (int iGrid : _mySoluteIndex){
						//iGrid will be the index of a unique solute
						diffReacGrid[reacIndex][iGrid] = _diffUptakeRate[iGrid];

					}
					
					for (int iGrid : _mySoluteIndex){
						allDiffSum[iGrid] += _diffUptakeRate[iGrid];
						reacGrid[iGrid].grid[i][j][k] += _uptakeRate[iGrid];
					}
				}
			}
		}
	}

	
	/**
	 * @param concGrid
	 * @param reacGrid
	 */
	public void applyReactionCA(SoluteGrid[] concGrid, SoluteGrid[] reacGrid,
	        SoluteGrid[] diffReacGrid) {
		double s[];
		double mass;

		if (true) {
			for (ActiveAgent anAgent : _guild) {
				s = readConcentrationSeen(anAgent, concGrid);
				mass = anAgent.getParticleMass(_catalystIndex);

				// Compute all rates and store them in your fields
				computeUptakeRate(s, mass, 0);
				//computeUptakeRate(s, mass);

				// Apply these rates on the grid
				fitUptakeRatesOnGrid(reacGrid, diffReacGrid, anAgent);
			}
		} else {
			for (ActiveAgent anAgent : _guild) {
				s = readConcentrationSeen(anAgent, concGrid);

				// Compute all rates and store them in your fields
				computeUptakeRate(s, anAgent);

				// Apply these rates on the grid
				fitUptakeRatesOnGrid(reacGrid, diffReacGrid, anAgent);
			}
		}

	}

	/**
	 * @param reacGrid
	 * @param diffReacGrid
	 * @param anAgent
	 */
	public void fitUptakeRatesOnGrid(SoluteGrid[] reacGrid, SoluteGrid[] diffReacGrid,
	        ActiveAgent anAgent) {
		DiscreteVector dC = new DiscreteVector(0, 0, 0);

		if (anAgent instanceof LocatedAgent) {

			for (int iGrid : _mySoluteIndex) {
				dC = reacGrid[iGrid].getDiscreteCoordinates(((LocatedAgent) anAgent).getLocation());
				reacGrid[iGrid].addValueAt(_uptakeRate[iGrid], dC);
				diffReacGrid[iGrid].addValueAt(_diffUptakeRate[iGrid], dC);
			}

		} else {
			for (int iGrid : _mySoluteIndex) {
				reacGrid[iGrid].addAllValues(_uptakeRate[iGrid]);
				diffReacGrid[iGrid].addAllValues(_diffUptakeRate[iGrid]);
			}
		}
	}

	/**
	 * Add the mass of all the agents of the guild on a received grid TODO : use
	 * this method for all ApplyReaction stuff ! The stored value is a
	 * CONCENTRATION
	 * @param aSpG
	 */
	public void fitAgentMassOnGrid(SpatialGrid aSpG) {
		for (ActiveAgent anActiveAgent : _guild) {
			if(this._catalystSpeciesIndex==-2 || 
					(this._catalystSpeciesIndex!=-1 && _catalystSpeciesName.compareTo(anActiveAgent.getSpecies().speciesName)==0))
				anActiveAgent.fitMassOnGrid(aSpG, this._catalystIndex);
		}
	}

	/* ______________________ TOOLBOX _______________________________ */

	/**
	 * @param anAgent
	 * @return all the concentration seen by an agent on the default solute grid
	 */
	public double[] readConcentrationSeen(ActiveAgent anAgent, SoluteGrid[] concGrid) {
		
		double[] out = new double[concGrid.length];
		
		//sonia:chemostat
		//the concentration read by the agents is the one stored in the bulk (which has been previously updated)
		
	if (Simulator.isChemostat){
			
		
		
			for (int index =0; index<_soluteList.length; index++){
				
				
				for (AllBC aBC : _reacGrid.getDomain().getAllBoundaries()){
					if (aBC.hasBulk()){
						Bulk aBulk = aBC.getBulk();
							if(aBulk.getName().equals("chemostat")){
								out[index] = aBulk.getValue(_soluteList[index].soluteIndex);
							}
					}	
				}
					
				
				//System.out.println("solute " + _soluteList[index].getName());
				//System.out.println("concentrations seen by the agent...." + out[index]);
				
			}
		}else{

			if (anAgent instanceof LocatedAgent) {
				// The agent is a located agent, use the local concentration
				for (int iGrid = 0; iGrid<_soluteList.length; iGrid++){
					out[iGrid] = _soluteList[iGrid].getValueAround(((LocatedAgent) anAgent));
				}
			} else {
			// The agent is a non-located agent, use the average concentration
				for (int iGrid = 0; iGrid<_soluteList.length; iGrid++)
					out[iGrid] = _soluteList[iGrid].getAverage();
			}
		}
		
		return out;
	}

	public void writeReport(ResultFile bufferState, ResultFile bufferSum) throws Exception {
		fitGuildOnGrid();
		
		_reacGrid.writeReport(bufferState, bufferSum);
	}

	/**
	 * Build a grid with mass and mass-growth-rate of all agents of the guild
	 */
	public void fitGuildOnGrid() {
		
		_reacGrid.setAllValueAt(0);
		for (ActiveAgent anAgent : _guild) {
			// Sum mass of catalyser compartments on each grid cell
			anAgent.fitMassOnGrid(_guildGrid, _catalystIndex);

			// Apparent reaction rate on each grid cell
			anAgent.fitReacRateOnGrid(_reacGrid, reactionIndex);
		}
	}

	/* ______________ ACCESSORS _______________________________ */
	public double[] getSoluteYield() {
		return _soluteYield;
	}

	public double[] getParticulateYield() {
		return _particleYield;
	}
	

	public double[] getKinetic() {
		return _kineticParam;
	}

	public LinkedList<ActiveAgent> getGuild() {
		return _guild;
	}

}
