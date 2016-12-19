
/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 *______________________________________________________
 * DiffusionSolver is an abstract class used as parent for all diffusion_solvers 
 * you could define
 * 
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA) 
 */

package simulator.diffusionSolver;

import idyno.SimTimer;
import simulator.diffusionSolver.multigrid.SimpleSolute;
import simulator.geometry.Domain;
import simulator.geometry.boundaryConditions.AllBC;
import simulator.Simulator;
import simulator.SoluteGrid;

import utils.XMLParser;

public class SolverSimple extends DiffusionSolver {

	protected SimpleSolute _bLayer, _diffusivity;
	protected SimpleSolute[] _solute, _biomass;

	protected SoluteGrid[]      allSolute, allReac, allDiffReac;

	protected static int        iSolute, order;
	protected int               maxOrder, nSolute, nReaction;
	protected int               nCoarseStep, vCycles, nPreSteps, nPosSteps;
	protected Domain            _domain;

	public void init(Simulator aSimulator, XMLParser xmlRoot) {
		super.init(aSimulator, xmlRoot);
		iSolute = 0;
		order = 0;

		nCoarseStep = (int) xmlRoot.getParamDbl("coarseStep");
		vCycles = (int) xmlRoot.getParamDbl("nCycles");
		nPreSteps = (int) xmlRoot.getParamDbl("preStep");
		nPosSteps = (int) xmlRoot.getParamDbl("postStep");

		// Create the table of solute grids
		nSolute = _soluteList.length;
		_solute = new SimpleSolute[nSolute];
		allSolute = new SoluteGrid[nSolute];
		allReac = new SoluteGrid[nSolute];
		allDiffReac = new SoluteGrid[nSolute];

		_domain = aSimulator.world.getDomain(xmlRoot.getAttribute("domain"));
		_bLayer = new SimpleSolute(_soluteList[0], "boundary layer");
		_diffusivity = new SimpleSolute(_soluteList[0], "relative diffusivity");

		for (int i = 0; i<nSolute; i++) {
			if (_soluteIndex.contains(i)) {
				double sBulk = mySim.world.getMaxBulkValue(_soluteList[i].soluteIndex);
				_solute[i] = new SimpleSolute(_soluteList[i], _diffusivity, _bLayer, sBulk);
				if(_soluteList[i].useRandomInit)
					_solute[i].randomSimpleCopies(_soluteList[i].minConc, _soluteList[i].maxConc);
			} else {
				_solute[i] = null;
			}
		}
		// From this moment, nSolute is the number of solutes SOLVED by THIS
		// solver
		nSolute = _soluteIndex.size();
		nReaction = _reactions.size();
		maxOrder = 1; //_solute[_soluteIndex.get(0)]._conc.length; ??????

		// Initialize array of reactive biomasses
		_biomass = new SimpleSolute[nReaction];
		for (int i = 0; i<nReaction; i++) {
			_biomass[i] = new SimpleSolute(_soluteList[0], _reactions.get(i).reactionName);
			_biomass[i].resetSimpleCopies(0d);
		}
	}

	public void initializeConcentrationFields() {
		minimalTimeStep = SimTimer.getCurrentTimeStep()/10;

		// Refresh then insert here the boundary layer and the diffusivity grid
		_domain.refreshBioFilmGrids();

		_bLayer.setFinest(_domain.getBoundaryLayer());
		_diffusivity.setFinest(_domain.getDiffusivity());
		
		// Prepare a soluteGrid with catalyst CONCENTRATION
		for (int i = 0; i<_biomass.length; i++) {
			_biomass[i].resetFinest(0d);
			_reactions.get(i).fitAgentMassOnGrid(_biomass[i].getFinest());
		}

		for (int iSolute : _soluteIndex)
			_solute[iSolute].readBulk();
	}

	/**
	 * Solve by iterative relaxation
	 */
	public void solveDiffusionReaction() {
		double timeToSolve = SimTimer.getCurrentTimeStep();
		internalIteration = 0;
		internTimeStep = timeToSolve;

		// changed by Farzin: read solute values to make sure changed grids are used
		for (int iSolute : _soluteIndex)
			_solute[iSolute].readSoluteGrid();
		
		
		// bvm note 13.7.09:
		// this iterative loop is only passed through once because of
		// the value of internTimeStep used above; we leave the loop
		// as-is though to allow future use of iterates if needed
		while (timeToSolve>0) {
			// Compute new equilibrium concentrations
			stepSolveDiffusionReaction();

			// update bulk concentration
			updateBulk();

			// Manage iterations
			internalIteration += 1;
			timeToSolve = timeToSolve-internTimeStep;
		}

		// Apply results on solute grids
		for (int iSolute : _soluteIndex)
			_solute[iSolute].applyComputation();

	}

	/**
	 * One step of the solver
	 */
	// not called publically, should be private
	public void stepSolveDiffusionReaction() {

		order = 0;
		relax(nPreSteps);
	}

	/**
	 * Update concentration in the reactor
	 * @param lastIter
	 */
	public void updateBulk() {
		// Update reaction rates
		// this yields solute change rates in fg.L-1.hr-1
		updateReacRateAndDiffRate(maxOrder-1);

		// Find the connected bulks and agars and update their concentration
		for (AllBC aBC : myDomain.getAllBoundaries()) {
			if (aBC.hasBulk()) aBC.updateBulk(allSolute, allReac, internTimeStep);
			if (aBC.hasAgar()) aBC.updateAgar(allSolute, allReac, internTimeStep);
		}

		// Refresh the bulk concentration of the simple
		for (int iSolute : _soluteIndex)
			_solute[iSolute].readBulk();
	}

	/**
	 * Solve the coarsest grid by relaxation Coarse grid is initialised to bulk
	 * concentration
	 */
	public void solveCoarsest() {
		order = 0;

		// reset coarsest grid to bulk concentration
//		for (int iSolute : _soluteIndex)
//			_solute[iSolute].setSoluteGridToBulk(order);

		// relax NSOLVE times
		relax(nCoarseStep);
	}

	/**
	 * Apply several relaxations to the grid at the current resolution
	 * @param nIter
	 */
	public void relax(int nIter) {
		double totalResidue = 0;
		double oldResidue = 0;
		for (int j = 0; j<nIter; j++) {
			updateReacRateAndDiffRate(order);
			for (int iSolute : _soluteIndex){
				oldResidue = totalResidue;
				totalResidue = _solute[iSolute].relax(order);
				if (iSolute== _soluteIndex.get(0)){
					double change = oldResidue - totalResidue;
					//System.out.println("..> iSolute"+iSolute+"...>totalResidue"+ change);
				}
			}
		}
		
//		for (int iSolute : _soluteIndex)
//			_solute[iSolute].flush(order);
	}

	/**
	 * Call all the agents and read their uptake-rate for the current
	 * concentration
	 * @param resOrder
	 */
	public void updateReacRateAndDiffRate(int resOrder) {
		// Reset rates and derivative rates grids
		for (int iSolute : _soluteIndex) {
			_solute[iSolute].resetReaction(resOrder);
			allSolute[iSolute] = _solute[iSolute]._conc[resOrder];
			allReac[iSolute] = _solute[iSolute]._reac[resOrder];
			allDiffReac[iSolute] = _solute[iSolute]._diffReac[resOrder];
		}
		

		// Calls the agents of the guild and sums their uptake-rate
		for (int iReac = 0; iReac<_reactions.size(); iReac++)
			_reactions.get(iReac).applyReaction(allSolute, allReac, allDiffReac,_biomass[iReac]._conc[resOrder]);
	}

}
