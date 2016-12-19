
/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas D�tsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 */


package simulator.reaction;

import simulator.Simulator;
import simulator.agent.*;
import simulator.geometry.ContinuousVector;

import utils.XMLParser;

public class FirstOrder extends Reaction {

	// Serial version used for the serialisation of the class
	private static final long   serialVersionUID = 1L;

	private double              _k;
	private static int          iSolute;

	/* _______________________ CONSTRUCTOR _________________________ */
	public void init(Simulator aSim, XMLParser aReactionRoot) {
		super.init(aSim, aReactionRoot);
		_k = aReactionRoot.getParamTime("k");	

		_kineticParam = new double[1];
		_kineticParam[0] = _k;
		iSolute = 0;
	}

	/**
	 */
	public void initFromAgent(ActiveAgent anAgent, Simulator aSim, XMLParser aReactionRoot) {
		// Call the init of the parent class (populate yield arrays)
		super.initFromAgent(anAgent, aSim, aReactionRoot);


		anAgent.reactionKinetic[reactionIndex] = new double[1];
		anAgent.reactionKinetic[reactionIndex][0] = aReactionRoot.getParamTime("k");
		iSolute = 0;
	}

	/* __________________ METHODS _________________________ */

	public void computeUptakeRate(double[] s, ActiveAgent anAgent) {
		_specRate = anAgent.reactionKinetic[reactionIndex][0];
		// Now compute uptake rate and its derivative for each solute
		for (int i = 0; i<_mySoluteIndex.length; i++) {
			iSolute = _mySoluteIndex[i];
			_uptakeRate[iSolute] = anAgent.particleMass[_catalystIndex]*_specRate
			        *anAgent.soluteYield[reactionIndex][iSolute];
			_diffUptakeRate[iSolute] = 0;
		}
	}

	/**
     * @param double[] s : array of concentration
     * @param mass : concentration of reactant
     */
	public void computeUptakeRate(double[] s, double mass) {

		_specRate = _k;
		// Now compute uptake rate and its derivative for each solute
		for (int i = 0; i<_mySoluteIndex.length; i++) {
			iSolute = _mySoluteIndex[i];
			_uptakeRate[iSolute] = mass*_specRate*this._soluteYield[iSolute];
			_diffUptakeRate[iSolute] = 0;
		}
	}
	
	/**
     * Return the specific reaction rate
     * @see ActiveAgent.grow()
     * @see Episome.computeRate(EpiBac)
     */
	public void computeSpecificGrowthRate(ActiveAgent anAgent) {
		_specRate = anAgent.reactionKinetic[reactionIndex][0];
	}
	
	public void computeSpecificGrowthRate(double[] s) {
		_specRate = this._k;
	}
	
	/**
     * Compute specific growth rate in fonction to concentrations sent
     * @param double[] s : aray of solute concentration
     * @param anAgent Parameters used are those defined for THIS agent
     */
	public void computeSpecificGrowthRate(double[] s, ActiveAgent anAgent) {
		_specRate = anAgent.reactionKinetic[reactionIndex][0];
	}

	/**
     * return the marginal growth rate (i.e the specific growth rate times the
     * mass of the particle who is mediating this reaction)
     * @param anAgent
     * @return
     */
	public double computeMassGrowthRate(ActiveAgent anAgent) {
		computeSpecificGrowthRate(anAgent);
		return _specRate*anAgent.getParticleMass(_catalystIndex);
	}

	public double computeSpecGrowthRate(ActiveAgent anAgent) {
		computeSpecificGrowthRate(anAgent);
		return _specRate;
	}

	@Override
	public void computeUptakeRate(double[] s, double conc, double h) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void computeUptakeRate(double[] s, double conc[][][], double h, ContinuousVector cv) {
		// TODO Auto-generated method stub
		
	}
	/**
     * Add the contribution of this agent on the reaction grid and the diff
     * reaction grid
     * @see : Reaction.applyReactionCA() and Reaction.applyReactionIbM()
     */

}