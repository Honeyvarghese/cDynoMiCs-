/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *______________________________________________________
 * This class allows you to create a Reaction object whose the reaction rate 
 * can be decomposed in several kinetic factor (one factor by solute)
 * 
 */

/**
 * @since January 2007
 * @version 1.0
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */
 

package simulator.reaction;

import utils.XMLParser;
import utils.UnitConverter;
import utils.LogFile;

import org.jdom.Element;

import simulator.Simulator;
import simulator.agent.*;
import simulator.geometry.ContinuousVector;
import simulator.reaction.kinetic.*;

public class ReactionFactor extends Reaction {

	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;

	private double            _muMax;
	private IsKineticFactor[] _kineticFactor;
	private int[]             _soluteFactor;
	
	private String[] _moleculeNames;
	// Temporary variable
	private static int        paramIndex;
	private static double     value;
	private double[]          marginalMu, marginalDiffMu;
	private StringBuffer      unit;

	/* ________________________ CONSTRUCTORS ________________________________ */
	public ReactionFactor() {
	}

	/* ________________ Used during initialisation ______________________ */
	public void init(Simulator aSim, XMLParser xmlRoot) {

		// Call the init of the parent class (populate yield arrays)
		super.init(aSim, xmlRoot);

		paramIndex = 0;
		value = 0;

		// Create the kinetic factors __________________________________________

		// Build the array of different multiplicative limiting expressions
		_kineticFactor = new IsKineticFactor[xmlRoot.getChildren("kineticFactor").size()];
		// one solute factor per kinetic factor
		_soluteFactor = new int[_kineticFactor.length];
		marginalMu = new double[_kineticFactor.length];
		marginalDiffMu = new double[_kineticFactor.length];
		_moleculeNames= new String[_kineticFactor.length];
		
		// muMax is the first factor
		unit = new StringBuffer("");
		value = xmlRoot.getParamDbl("muMax", unit);
		_muMax = value*UnitConverter.time(unit.toString());

		int iFactor = 0;
		int mFactor =0;
		try {
			// Create and initialise the instance
			for (Element aChild : xmlRoot.getChildren("kineticFactor")) {
				_kineticFactor[iFactor] = (IsKineticFactor) (new XMLParser(aChild))
				        .instanceCreator("simulator.reaction.kinetic");
				_kineticFactor[iFactor].init(aChild);
				if(aChild.getAttribute("molecule")!=null)
				{
					
					_moleculeNames[mFactor]= aChild.getAttributeValue("molecule");
					if(!aSim.molecularKineticRegulators.containsKey(_moleculeNames[mFactor]))
					{
						aSim.molecularKineticRegulators.put(_moleculeNames[mFactor], new double[aSim.world.domainList.get(0)._nI]
								[aSim.world.domainList.get(0)._nJ][aSim.world.domainList.get(0)._nK]);
					}
					_soluteFactor[iFactor] = farzin.Variable.molecularReactionIndexInitiator-mFactor;
					mFactor++;
				}
				else
					_soluteFactor[iFactor] = aSim.getSoluteIndex(aChild.getAttributeValue("solute"));
				
				//Changed by Farzin for having Pressure inhibited growth
				if(aChild.getAttributeValue("solute")!=null && _soluteFactor[iFactor] ==-1 && aChild.getAttributeValue("solute").equalsIgnoreCase("density"))
				{
					_soluteFactor[iFactor]=farzin.Variable.densitySolute;
				}
				if(aChild.getAttributeValue("solute")!=null && aChild.getAttributeValue("solute").equalsIgnoreCase("pressure"))
				{
					_soluteFactor[iFactor]=farzin.Variable.pressureSolute;
				}
				//End Farzin
				
				if(aChild.getAttributeValue("solute")!=null && _soluteFactor[iFactor]==-1)
				{
					System.out.println("\nUndefined solute:" + aChild.getAttributeValue("solute"));
					throw new Exception();
				}
				iFactor++;
			}

			_kineticParam = new double[getTotalParam()];
			_kineticParam[0] = _muMax;

			// Populate the table collecting all kinetic parameters of this
			// reaction term
			paramIndex = 1;
			iFactor = 0;
			for (Element aChild : xmlRoot.getChildren("kineticFactor")) {
				_kineticFactor[iFactor].initFromAgent(aChild, _kineticParam, paramIndex);
				paramIndex += _kineticFactor[iFactor].nParam;
				iFactor++;
			}
		} catch (Exception e) {
			LogFile.writeLog("Error met during ReactionFactor.init()\n");
		}
	}

	/**
	 * Use the reaction class to fill the parameters fields of the agent
	 */
	public void initFromAgent(ActiveAgent anAgent, Simulator aSim, XMLParser aReactionRoot) {
		// Call the init of the parent class (populate yield arrays)
		super.initFromAgent(anAgent, aSim, aReactionRoot);

		paramIndex = 0;
		value = 0;

		anAgent.reactionKinetic[reactionIndex] = new double[getTotalParam()];

		// Set muMax
		unit = new StringBuffer("");
		value = aReactionRoot.getParamSuchDbl("kinetic", "muMax", unit);
		double muMax = value*UnitConverter.time(unit.toString());
		anAgent.reactionKinetic[reactionIndex][0] = muMax;

		// Set parameters for each kinetic factor
		paramIndex = 1;
		for (Element aChild : aReactionRoot.getChildren("kineticFactor")) {
			int iSolute = aSim.getSoluteIndex(aChild.getAttributeValue("solute"));
			_kineticFactor[iSolute].initFromAgent(aChild, anAgent.reactionKinetic[reactionIndex],
			        paramIndex);
			paramIndex += _kineticFactor[iSolute].nParam;
		}
	}

	/**
	 * @return the total number of parameters needed to describe the kinetic of
	 * this reaction (muMax included)
	 */
	public int getTotalParam() {
		// Sum the number of parameters of each kinetic factor
		int totalParam = 1;
		for (int iFactor = 0; iFactor<_kineticFactor.length; iFactor++) {
			if (_kineticFactor[iFactor]==null) continue;
			totalParam += _kineticFactor[iFactor].nParam;
		}
		return totalParam;
	}

	/* _________________ INTERACTION WITH THE SOLVER_______________________ */

	/**
	 * Update the array of uptake rates and the array of its derivative Based on
	 * parameters sent by the agent
	 * @param s
	 * @param mass
	 */
	public void computeUptakeRate(double[] s, ActiveAgent anAgent) {

		// First compute specific rate
		computeSpecificGrowthRate(s, anAgent);

		double mass = anAgent.particleMass[_catalystIndex];

		// Now compute uptake rates
		for (int iSolute : _mySoluteIndex) {
			_uptakeRate[iSolute] = mass*_specRate*anAgent.soluteYield[reactionIndex][iSolute];
		}
		int iSolute;
		for (int i = 0; i<_soluteFactor.length; i++) {
			iSolute = _soluteFactor[i];
			_diffUptakeRate[iSolute] = mass*marginalDiffMu[i]
			        *anAgent.soluteYield[reactionIndex][iSolute];
		}
	}

	/**
	 * Update the array of uptake rates and the array of its derivative Based on
	 * default values of parameters Unit is fg.h-1
	 * @param s : the concentration locally observed
	 * @param mass : mass of the catalyst (cell...)
	 */
	public void computeUptakeRate(double[] s, double mass, double tdel) {

		// First compute specific rate
		computeSpecificGrowthRate(s, mass);
		//sonia:chemostat 27.11.09
		if(Simulator.isChemostat){

			for (int iSolute : _mySoluteIndex) {
				_uptakeRate[iSolute] = (tdel*mass*Dil) + (mass *_specRate*_soluteYield[iSolute] ) ;

			}
			int iSolute;
			for (int i = 0; i<_soluteFactor.length; i++) {
				iSolute = _soluteFactor[i];
				if(iSolute!=-1){	
					_diffUptakeRate[iSolute] =(tdel*mass*Dil) + (mass*marginalDiffMu[i]*_soluteYield[iSolute])  ;	
				}
			}

		}else{
			// Now compute uptake rate
			for (int iSolute : _mySoluteIndex) {
				_uptakeRate[iSolute] = mass*_specRate*_soluteYield[iSolute];
			}

			int iSolute;
			for (int i = 0; i<_soluteFactor.length; i++) {
				iSolute = _soluteFactor[i];
				if(iSolute>-1)//Change 2
					_diffUptakeRate[iSolute] = mass*marginalDiffMu[i]*_soluteYield[iSolute];
			}
		}

	}
	
	public void computeUptakeRate(double[] s, double[][][] massGrid, double tdel, ContinuousVector cv) {

		// First compute specific rate
//		double massAround=calcYeastMassAround(cv,farzin.Variable._8neighborHood);
//		double pressureAround=calcPressureAround(cv,farzin.Variable._8neighborHood);
//		computeSpecificGrowthRate(s, massAround, pressureAround);
		//computeSpecificGrowthRate(s, massAround); //Changed by Farzin to have pressure read, it should be fixed
		computeSpecificGrowthRate(s, cv);
		
		double mass=massGrid[(int) cv.x][(int) cv.y][(int) cv.z];
		//sonia:chemostat 27.11.09
		if(Simulator.isChemostat){

			for (int iSolute : _mySoluteIndex) {
				_uptakeRate[iSolute] = (tdel*mass*Dil) + (mass *_specRate*_soluteYield[iSolute] ) ;

			}
			int iSolute;
			for (int i = 0; i<_soluteFactor.length; i++) {
				iSolute = _soluteFactor[i];
				if(iSolute!=-1){	
					_diffUptakeRate[iSolute] =(tdel*mass*Dil) + (mass*marginalDiffMu[i]*_soluteYield[iSolute])  ;	
				}
			}

		}else{
			// Now compute uptake rate
			for (int iSolute : _mySoluteIndex) {
				_uptakeRate[iSolute] = mass*_specRate*_soluteYield[iSolute];
			}

			int iSolute;
			for (int i = 0; i<_soluteFactor.length; i++) {
				iSolute = _soluteFactor[i];
				if(iSolute>-1)//Change 2
					_diffUptakeRate[iSolute] = mass*marginalDiffMu[i]*_soluteYield[iSolute];
			}
		}

	}

	

	/**
	 * Return the specific reaction rate
	 * @see ActiveAgent.grow()
	 * @see Episome.computeRate(EpiBac)
	 */
	public void computeSpecificGrowthRate(ActiveAgent anAgent) {

		// Build the array of concentration seen by the agent
		computeSpecificGrowthRate(readConcentrationSeen(anAgent, _soluteList), anAgent);
	}

	/**
	 * Compute specific growth rate in function of concentrations sent
	 * Parameters used are those defined for the reaction
	 * @param double[] s : array of solute concentration
	 */
	public void computeSpecificGrowthRate(double[] s) {
		_specRate = _muMax;
		int soluteIndex;

		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			soluteIndex = _soluteFactor[iFactor];
			if (soluteIndex==-1) {    ////Change 1
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(0);
				marginalDiffMu[iFactor] = _muMax*_kineticFactor[iFactor].kineticDiff(0);
			} else {
				double sTemp=s[_soluteFactor[iFactor]];
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(sTemp);
				marginalDiffMu[iFactor] = _muMax*_kineticFactor[iFactor].kineticDiff(sTemp);
			}
		}

		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			_specRate *= marginalMu[iFactor];
			for (int jFactor = 0; jFactor<_soluteFactor.length; jFactor++) {
				if (jFactor!=iFactor) marginalDiffMu[jFactor] *= marginalMu[iFactor];
			}
		}
	}
	
	public void computeSpecificGrowthRate(double[] s, double mass) {
		_specRate = _muMax;
		int soluteIndex;

		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			soluteIndex = _soluteFactor[iFactor];
			if (soluteIndex==-1) {    
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(0);
				marginalDiffMu[iFactor] = _muMax*_kineticFactor[iFactor].kineticDiff(0);
			} else {
				
				double sTemp;
				if(_soluteFactor[iFactor]==farzin.Variable.densitySolute)
					sTemp=mass;
				else
					sTemp=s[_soluteFactor[iFactor]];
				
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(sTemp);
				marginalDiffMu[iFactor] = _muMax*_kineticFactor[iFactor].kineticDiff(sTemp);
			}
		}

		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			_specRate *= marginalMu[iFactor];
			for (int jFactor = 0; jFactor<_soluteFactor.length; jFactor++) {
				if (jFactor!=iFactor) marginalDiffMu[jFactor] *= marginalMu[iFactor];
			}
		}
	}
	
	public void computeSpecificGrowthRate(double[] s, ContinuousVector cv) {
		_specRate = _muMax;
		int soluteIndex, moleculeIndex;
		double massAround, pressureAround, resolution;
		
		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			soluteIndex = _soluteFactor[iFactor];
			if (soluteIndex==-1) {    
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(0);
				marginalDiffMu[iFactor] = _muMax*_kineticFactor[iFactor].kineticDiff(0);
			} else {
				
				double sTemp;
				if(_soluteFactor[iFactor]==farzin.Variable.densitySolute)
				{
					massAround=calcYeastMassAround(cv,farzin.Variable._8neighborHood);
					sTemp=massAround;
				}
				else if(_soluteFactor[iFactor]==farzin.Variable.pressureSolute)
				{
					pressureAround=calcPressureAround(cv,farzin.Variable._8neighborHood);
					sTemp=pressureAround;
				}
				else if(_soluteFactor[iFactor]<=farzin.Variable.molecularReactionIndexInitiator)
				{
					moleculeIndex=farzin.Variable.molecularReactionIndexInitiator-_soluteFactor[iFactor];
					resolution=aSim.world.domainList.get(0)._resolution;
					sTemp=aSim.molecularKineticRegulators.get(_moleculeNames[moleculeIndex])[(int)(cv.x/resolution)][(int)(cv.y/resolution)][(int)(cv.z/resolution)];
					//sTemp=;
				}
				else
					sTemp=s[_soluteFactor[iFactor]];
				
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(sTemp);
				marginalDiffMu[iFactor] = _muMax*_kineticFactor[iFactor].kineticDiff(sTemp);
			}
		}

		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			_specRate *= marginalMu[iFactor];
			for (int jFactor = 0; jFactor<_soluteFactor.length; jFactor++) {
				if (jFactor!=iFactor) marginalDiffMu[jFactor] *= marginalMu[iFactor];
			}
		}
	}

	/**
	 * Compute specific growth rate in function to concentrations sent
	 * @param double[] s : array of solute concentration
	 * @param anAgent Parameters used are those defined for THIS agent
	 */
	public void computeSpecificGrowthRate(double[] s, ActiveAgent anAgent) {
		double[] kineticParam = anAgent.reactionKinetic[reactionIndex];

		paramIndex = 1;
		double resolution;
		int moleculeIndex, x,y,z;
		// Compute contribution of each limiting solute
		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			if (_soluteFactor[iFactor]==-1) { //meaning, if there's no such solute
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(0, kineticParam,
				        paramIndex);
				marginalDiffMu[iFactor] = _muMax
				        *_kineticFactor[iFactor].kineticDiff(0, kineticParam, paramIndex);
				paramIndex += _kineticFactor[iFactor].nParam;
			} else {
				double sTemp;
//				if(_soluteFactor[iFactor]==farzin.Variable.densitySolute)
//					sTemp=anAgent.calcDensityAround();
//				else
				if(_soluteFactor[iFactor]==farzin.Variable.pressureSolute)
					sTemp=anAgent.calcPressureAround();
				else if(_soluteFactor[iFactor]<=farzin.Variable.molecularReactionIndexInitiator)
				{
					moleculeIndex=farzin.Variable.molecularReactionIndexInitiator-_soluteFactor[iFactor];
					resolution=aSim.world.domainList.get(0)._resolution;
					x=(int)(((LocatedAgent)anAgent)._location.x/resolution);
					y=(int)(((LocatedAgent)anAgent)._location.y/resolution);
					z=(int)(((LocatedAgent)anAgent)._location.z/resolution);
					sTemp=aSim.molecularKineticRegulators.get(_moleculeNames[moleculeIndex])[x][y][z];
				}
				else
					sTemp=s[_soluteFactor[iFactor]];
				marginalMu[iFactor] = _kineticFactor[iFactor].kineticValue(sTemp, kineticParam, paramIndex);
				marginalDiffMu[iFactor] = _muMax
				        *_kineticFactor[iFactor].kineticDiff(sTemp,kineticParam, paramIndex);
				paramIndex += _kineticFactor[iFactor].nParam;
			}
		}

		// First multiplier is muMax
		_specRate = kineticParam[0];

		// Finalise the computation
		for (int iFactor = 0; iFactor<_soluteFactor.length; iFactor++) {
			_specRate *= marginalMu[iFactor];
			for (int jFactor = 0; jFactor<_soluteFactor.length; jFactor++) {
				if (jFactor!=iFactor) marginalDiffMu[jFactor] *= marginalMu[iFactor];
			}
		}
	}

	/* __________________ Methods called by the agents ___________________ */

	/**
	 * @param anAgent
	 * @return the marginal growth rate (i.e the specific growth rate times the
	 * mass of the particle which is mediating this reaction)
	 */
	public double computeMassGrowthRate(ActiveAgent anAgent) {
		computeSpecificGrowthRate(anAgent);
		return _specRate*anAgent.getParticleMass(_catalystIndex);
	}

	public double computeSpecGrowthRate(ActiveAgent anAgent) {
		computeSpecificGrowthRate(anAgent);
		return _specRate;
	}

/*_______________________________Tools_______________________________ */
	//Aded by Farzin
	private double calcYeastMassAround(ContinuousVector cv, int _8neighborHood) {
		int x=(int) cv.x;
		int y=(int) cv.y;
		int z=(int) cv.z;
		double [][][] grid= aSim.world.domainList.get(0).getBiomass().grid;
		return Simulator.calcDensityAround(x, y, z, grid);
		
//		double [][][] grid= aSim.world.domainList.get(0).getYeastGrid().grid;
//		int nI=grid.length;
//		int nJ=grid[0].length;
//		int nK=grid[0][0].length;
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
	
	private double calcPressureAround(ContinuousVector cv, int _8neighborHood) {
		int x=(int) cv.x;
		int y=(int) cv.y;
		int z=(int) cv.z;
		
		double [][][] grid= aSim.getSolute("pressure").grid;
//		int nI=grid.length;
//		int nJ=grid[0].length;
//		int nK=grid[0][0].length;
		double accPressure=grid[x][y][z];
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
	
}
