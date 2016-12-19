
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
 * @author Andreas D�tsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA) 
 */

package simulator.diffusionSolver;

import idyno.SimTimer;
import simulator.diffusionSolver.multigrid.MultigridSolute;
import simulator.geometry.Domain;
import simulator.geometry.boundaryConditions.AllBC;
import simulator.Simulator;
import simulator.SoluteGrid;

import utils.XMLParser;

public class Solver_multigrid extends DiffusionSolver {

	protected MultigridSolute _bLayer, _diffusivity;
	protected MultigridSolute[] _solute, _biomass;

	protected SoluteGrid[]      allSolute, allReac, allDiffReac;

	protected static int        iSolute, order;
	protected int               maxOrder, nSolute, nReaction;
	protected int               nCoarseStep, vCycles, nPreSteps, nPosSteps;
	protected Domain            _domain;

	public void init(Simulator aSimulator, XMLParser xmlRoot) {
		super.init(aSimulator, xmlRoot);


		order = 0;
		iSolute = 0;
		nCoarseStep = (int) xmlRoot.getParamDbl("coarseStep");
		vCycles = (int) xmlRoot.getParamDbl("nCycles");
		nPreSteps = (int) xmlRoot.getParamDbl("preStep");
		nPosSteps = (int) xmlRoot.getParamDbl("postStep");

		// Create the table of solute grids
		nSolute = _soluteList.length;
		_solute = new MultigridSolute[nSolute];
		allSolute = new SoluteGrid[nSolute];
		allReac = new SoluteGrid[nSolute];
		allDiffReac = new SoluteGrid[nSolute];

		_domain = aSimulator.world.getDomain(xmlRoot.getAttribute("domain"));
		_bLayer = new MultigridSolute(_soluteList[0], "boundary layer");
		_diffusivity = new MultigridSolute(_soluteList[0], "relative diffusivity");

		for (int i = 0; i<nSolute; i++) {
			if (_soluteIndex.contains(i)) {
				double sBulk = mySim.world.getMaxBulkValue(_soluteList[i].soluteIndex);
				_solute[i] = new MultigridSolute(_soluteList[i], _diffusivity, _bLayer, sBulk);
				if(_soluteList[i].useRandomInit)
					_solute[i].randomSetMultigridCopies(_soluteList[i].minConc, _soluteList[i].maxConc);
			} else {
				_solute[i] = null;
			}
		}
		// From this moment, nSolute is the number of solutes SOLVED by THIS
		// solver
		nSolute = _soluteIndex.size();
		nReaction = _reactions.size();
		maxOrder = _solute[_soluteIndex.get(0)]._conc.length;

		// Initialize array of reactive biomasses
		_biomass = new MultigridSolute[nReaction];
		for (int i = 0; i<nReaction; i++) {
			_biomass[i] = new MultigridSolute(_soluteList[0], _reactions.get(i).reactionName);
			_biomass[i].resetMultigridCopies(0d);
		}
	}

	public void initializeConcentrationFields() {
		minimalTimeStep = SimTimer.getCurrentTimeStep()/10;

		// Refresh then insert here the boundary layer and the diffusivity grid
		_domain.refreshBioFilmGrids();

		_bLayer.setFinest(_domain.getBoundaryLayer());
		_bLayer.restrictToCoarsest();
		_diffusivity.setFinest(_domain.getDiffusivity());
		_diffusivity.restrictToCoarsest();

		// Prepare a soluteGrid with catalyst CONCENTRATION
		for (int i = 0; i<_biomass.length; i++) {
			_biomass[i].resetFinest(0d);
			_reactions.get(i).fitAgentMassOnGrid(_biomass[i].getFinest());
			_biomass[i].restrictToCoarsest();
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
	public void stepSolveDiffusionReaction() {

		for (int iSolute : _soluteIndex)
			_solute[iSolute].resetMultigridCopies();

		// solve chemical concentrations on coarsest grid
		solveCoarsest();

		// nested iteration loop
		for (int outer = 1; outer<maxOrder; outer++) {

			order = outer;
			for (int i = 0; i<nSolute; i++)
				_solute[_soluteIndex.get(i)].initLoop(order);

			// V-cycle loop
			for (int v = 0; v<vCycles; v++) {
				// downward stroke of V
				while (order>0) {
					// pre-smoothing
					relax(nPreSteps);
					for (int j = 0; j<nSolute; j++)
						_solute[_soluteIndex.get(j)].downward1(order, outer);

					updateReacRateAndDiffRate(order-1);
					for (int j = 0; j<nSolute; j++)
						_solute[_soluteIndex.get(j)].downward2(order, outer);

					// reduce grid value _g for good
					order--;
				}

				// bottom of V
				solveCoarsest();

				// upward stroke of V
				while (order<outer) {
					order++;
					for (int iSolute : _soluteIndex)
						_solute[iSolute].upward(order);

					for (int iSolute : _soluteIndex)
						_solute[iSolute].truncateConcToZero(order);

					// post-smoothing
					relax(nPosSteps);
				}

				// break the V-cycles if remaining error is dominated
				// by local truncation error (see p. 884 of Numerical Recipes)
				boolean breakVCycle = true;

				updateReacRateAndDiffRate(order);
				for (int iSolute : _soluteIndex)
					breakVCycle &= _solute[iSolute].breakVCycle(order, v);

				if (breakVCycle) break;
			}
		}
	}

	/**
	 * Update concentration in the reactor
	 * @param lastIter
	 */
	public void updateBulk() {
		System.out.println("connectedbulk detected..........\n\n\n");

		// Update reaction rates
		// this yields solute change rates in fg.L-1.hr-1
		updateReacRateAndDiffRate(maxOrder-1);

		// Find the connected bulks and agars and update their concentration
		for (AllBC aBC : myDomain.getAllBoundaries()) {
			System.out.println("connectedbulk detected..........\n\n\n");

			if (aBC.hasBulk())
			{
				System.out.println("connectedbulk detected..........\n\n\n");
				aBC.updateBulk(allSolute, allReac, internTimeStep);
			}
			if (aBC.hasAgar()) aBC.updateAgar(allSolute, allReac, internTimeStep);
		}

		// Refresh the bulk concentration of the multigrids
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
		for (int j = 0; j<nIter; j++) {
			updateReacRateAndDiffRate(order);
			for (int iSolute : _soluteIndex)
				_solute[iSolute].relax(order);
		}
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
