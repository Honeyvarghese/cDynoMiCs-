
/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * 
 * _____________________________________________________
 * This class describes the solver used when simulating a chemostat environment. It is based on the Solver_multigrid but with 
 * some simplifications (the solute concentrations are stored as Solute grids and the updatake rates as vectors)
 * There are also two new methods that deal with solute concentration calculation: odeSolver and Jacobian.
 */

/**
 * @since October 2009
 * @version 1.0
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */

package simulator.diffusionSolver;

import java.util.ArrayList;
import Jama.LUDecomposition;
import Jama.Matrix;
import simulator.Simulator;
import simulator.SoluteGrid;
import simulator.diffusionSolver.multigrid.MultigridSolute;
import simulator.geometry.Bulk;
import simulator.geometry.Domain;
import simulator.geometry.boundaryConditions.AllBC;
import utils.XMLParser;
import idyno.SimTimer;

public class Solver_chemostat extends DiffusionSolver {
	
	protected  SoluteGrid[]      	allSolute;
	protected  SoluteGrid[] 		allReac;
	protected double [][] 			allDiffReac;
	protected double [] 			allDiffSum;
	protected double [] 			allReacSum;
	
	protected MultigridSolute []   _reactiveBiomass;
	protected static int        	iSolute;
	protected int                	nSolute, nReaction;
	protected Domain            	_domain;
	 
	//2.10.09
	protected double rtol;
	protected double hmax;
	protected double conLimit;
	
	//Jacobian
	ArrayList<Integer> reacInd = new ArrayList<Integer>();
	Matrix dFdY;
	double D ;	
	double dS_ds =0;
	
	//Bulk (chemostat) variables
	public  double bulk_sIn;
	
	
	
	// 21.09.09
	//1D array of arraylists containing the list of reactions in which each of the solutes participates
	protected ArrayList<Integer> solReacInd [];

	
	public void init(Simulator aSimulator, XMLParser xmlRoot) {
		super.init(aSimulator, xmlRoot);

		iSolute = 0;
		
		nSolute = _soluteList.length;
		allSolute = new SoluteGrid[nSolute];
		allReac = new SoluteGrid[nSolute];

		rtol = xmlRoot.getParamDbl("rtol");
		hmax = xmlRoot.getParamDbl("hmax");
		conLimit = xmlRoot.getParamDbl("conLimit");
		
		 _domain = aSimulator.world.getDomain(xmlRoot.getAttribute("domain"));
		
			//dilution rate
		 
		 for (AllBC aBC : _domain.getAllBoundaries()){
				if (aBC.hasBulk()){
					Bulk aBulk = aBC.getBulk();
						if(aBulk.getName().equals("chemostat")){
							 D = aBulk._D;
						}
				}
		}	
			
			 
			 
		 double sBulk=0;
		 
		for (int i = 0; i<nSolute; i++) {
			if (_soluteIndex.contains(i)) {
				 
				for (AllBC aBC : _domain.getAllBoundaries()){
					if (aBC.hasBulk()){
						Bulk aBulk = aBC.getBulk();
							if(aBulk.getName().equals("chemostat")){
								 sBulk = aBulk._sIn[i];
							}
					}	
				}	
				//double sBulk = Bulk._sIn[i];
				allSolute[i]= mySim.soluteList[i];
				allReac[i] = new SoluteGrid (_domain._nI, _domain._nJ, _domain._nK, _domain._resolution,
						mySim.soluteList[i].gridName, mySim.soluteList[i].getDomain());
				allReac[i].setAllValueAt(0);
				allSolute[i].setAllValueAt(sBulk);
				
				
			} else {
				allSolute[i] = null;
				allReac[i]= null;
				
			}
		}

		
		//sonia 21.09.00
		solReacInd = new ArrayList [nSolute];
		for (int i=0; i< solReacInd.length; i++){
			solReacInd[i] = new ArrayList<Integer>();
		}

		
		for (int iSol=0; iSol<nSolute; iSol++){

			if (_soluteIndex.contains(iSol)){
				for (int R=0; R < _reactions.size(); R++){
					for(int j=0; j< _reactions.get(R)._mySoluteIndex.length; j++){
						if(_reactions.get(R)._mySoluteIndex[j]==iSol){
							solReacInd[iSol].add(_reactions.get(R).reactionIndex);
						}
					}
				}
				
			}else{
				solReacInd[iSol]=null;
			}
		}
		

		nReaction = _reactions.size();	
		//allReac = new double [nReaction][nSolute];
		allDiffReac = new double [nReaction][nSolute];
		allDiffSum = new double [nSolute];
		allReacSum = new double [nSolute];
	
		// Initialize array of reactive biomasses
		_reactiveBiomass = new MultigridSolute[nReaction];
		for (int i = 0; i<nReaction; i++) {
			_reactiveBiomass[i] = new MultigridSolute(_soluteList[0], _reactions.get(i).reactionName);
			_reactiveBiomass[i]._conc[0].setAllValueAt(0);
			System.out.println("biomass conc is ----->>>   " + _reactiveBiomass[i]._conc[0].getAverageChemo());
		}
		
		//sonia 26.02.2010 jacobian
		dFdY = new Matrix (nSolute,nSolute);
	
	}
				
	@Override
	public void initializeConcentrationFields() {
	
	
		//reset biomass concentration in the grid
		for (int i = 0; i<nReaction; i++) {
			_reactiveBiomass[i]._conc[0].setAllValueAt(0);
		}
		
		// Get the catalyst (biomass and other particulates) CONCENTRATION
		for (int i = 0; i<_reactions.size(); i++) {
			_reactions.get(i).fitAgentMassOnGrid(_reactiveBiomass[i].getGrid());
			System.out.println("biomass conc is ----->>>   " + _reactiveBiomass[i]._conc[0].getAverageChemo());
		}
		

	}

	// modified from Solver_multigrid
	public void updateReacRateAndDiffRate(double tdel) {
			
		
		//reset the uptake and diffuptake rates to zero
		for (int i=0; i< allDiffReac.length; i++){
			for(int j=0; j< allDiffReac[i].length; j++){
				allDiffReac[i][j] = 0;	
				}		
		}
		
		for (int i=0; i< allReac.length; i++){
			//allReac[i].setAllValueAt(0);
			allReac[i].grid[0][0][0] = 0;
		}
		
		//reset the diffSum vector to zero
		for (int i=0; i< allDiffSum.length; i++){			
				allDiffSum[i] = 0;					
			}

		// Calls the agents of the guild and updates the reaction and diff uptake rates
		for (int iReac = 0; iReac<_reactions.size(); iReac++)
			_reactions.get(iReac).applyChemostatReaction(allSolute, allReac, allDiffReac, allDiffSum,
			        _reactiveBiomass[iReac]._conc[0], _reactions.get(iReac).reactionIndex, tdel);
	}


	@Override
	public void solveDiffusionReaction() {
		odeSolver(SimTimer.getCurrentTime(), rtol, hmax);
		updateBulk(true);
		
			
	}
	
	public void updateBulk(boolean implicit) {
		
		// Find the connected bulks and update their concentration
			
			for (AllBC aBC : myDomain.getAllBoundaries()){
			if (aBC.hasBulk()) aBC.updateBulk(allSolute, allReac, 0);
			}	
		
	}

	
	/**
	 * @author Sonia Martins
	 * 
	 * The ode Solver implemented is an attempt of reproducing the ode solver description for stiff systems found
	 * in "The Matalab ODE Suite", namely the use of linearly implicit formulas for stiff systems based on
	 * Rosenbrock methods (one-step method). Other important references are cited throughout the code.
	 * The first solver time step, h, is hmax (parameter given by the user). 
	 * The method is based on the calculation of a Jacobian matrix and two evaluations of the function. From this
	 * information a set of parameters, k1,k2 and k3 are determined in order to estimate the error involved in the 
	 * evaluation of the function. This error is then compared to a relative tolerance parameter, rtol, that we've set
	 * a priori. If the condition (error < rtol) is not satisfied the time step h is decreased, otherwise the solver 
	 * can advance one iteration step. At each iteration step the solute grid is updated with the new concentrations
	 * and the updateReacRate() method is called in order to update the allReac and allDiffReac grids that will
	 * then be used in the calculation of the jacobian matrix. 
	 * 
	 * 
	 * @param t0 - initial time (not iteration time, hence will always be zero)
	 * @param rtol - relative tolerance of our error
	 * @param hmax - maximum internal step of the solver
	 * @return 
	 * 
	 */
	
	public void odeSolver (double t0, double rtol, double hmax){
	
		Matrix dFdY;
		
		double tfinal;
		double t;
		
		tfinal =  SimTimer.getCurrentTimeStep();
		t = 0;
		
		double tnext=0;
		double tint=0;
		double h=0;
		double hmin=0;

			
		// variables used throughout the solver calculations;
		
		//because we are using a method of order 3, the power variable will be used to
		//calculate a better h that makes sure the error obtained with the new h is smaller than the 
		// relative tolerance
		double power = 1.0/3.0;
		double d = 1.0 / (2.0 + Math.sqrt(2.0));
		double e32 = 6.0 + Math.sqrt(2.0);		
		double sqrtE = Math.sqrt(2.22e-16); // numerical accuracy for EPS (error per step)
		double EPS = 2.22e-16 ; // the smallest positive floating-point number such that  1.0+EPS > 1.0
	
		//vectors used to store function evaluations
	
		double [] f1 = new double [nSolute];
		double [] f2= new double [nSolute];
		double [] dSdT= new double [nSolute];
		double [] dFdT = new double [nSolute];
		
		
		//Matrix-related variables
		Matrix I = Matrix.identity(nSolute, nSolute);
		Matrix W, L, U, invL, invU;
		LUDecomposition LU;
		int [] p;
		
		//k family of parameters
		double [] k1aux = new double [nSolute];
		double [] k1 = new double [nSolute];
		double [] k2 = new double [nSolute];
		double [] k3 = new double [nSolute];
		double [] k3aux = new double [nSolute];
		
		// variables related to error calculation
		double error =0;
		double [] vCalc = new double [nSolute];
		double vMax = vCalc[0]; 
		
		//  y is f0, i.e, the function value at t0, meaning the current concentrations
		nSolute = allSolute.length;
		
		double[] ynext = new double [nSolute];
		
		// creating a vector with the solute concentrations at t0
		double[] y = new double [nSolute];
		
		for(int iSol : _soluteIndex){
			if((_soluteIndex.contains(iSol))){				
				y[iSol] = allSolute[iSol].getAverageChemo();
			}else{
				y[iSol] = 0;
			}			
		}
		
		// sonia 10.11.09 control statement in case hmax is higher than the global time step
		if(hmax > tfinal){
			hmax = 0.5 * hmax;
			rtol = 0.5 * rtol;
		}
		
		h = hmax;
		
		
		//>>>>>>>>>--->  LOOP for "h" Steps  <---<<<<<<<<<<<<<//
		
		boolean lastStep = false;
		
		while (!lastStep){
			
			//hmin is a small number such that t + hmin > t
			hmin = EPS * Math.abs(t);
		
			h = Math.min(hmax, h);
			
			
			//if the time step is within 5% of tfinal, take h = tfinal-t;		
			if ((h + h * 0.05) >= Math.abs(tfinal-t)){
				h = tfinal - t;
				lastStep = true;
			}
	
			
			//interval of time used to perform the integration	deltat = (t+h) - t;				
			tint = (t + sqrtE *(t+h)) - t;
			
			
			//update the uptake rates and diffuptake rates before the calculation of the jacobian and
			//of the dSdT; use tdel, and not h, because that's the integration time.
			
			updateReacRateAndDiffRate(tint) ;
			
			
			// Jacobian Matrix contains the first derivatives which are calculated using the diffuptake rates:
			// dfdy = diffReacRates - D;
				
			dFdY = Jacobian(D);
			
			//calculating dSdT
			for(int i=0; i < allReac.length; i++){			
				if(allReac[i]==null){
					dSdT[i]=0;					
				}else{
					
					for (AllBC aBC : _domain.getAllBoundaries()){
						if (aBC.hasBulk()){
							Bulk aBulk = aBC.getBulk();
								if(aBulk.getName().equals("chemostat")){
									bulk_sIn = aBulk._sIn[i];
								}
						}	
					}
					
					for(int iSol : _soluteIndex){
						if((_soluteIndex.contains(iSol))){				
							y[iSol] = allSolute[iSol].getAverageChemo();
						}else{
							y[iSol] = 0;
						}			
					}
	
					dSdT[i] = (D * (bulk_sIn - y[i])) + allReac[i].getAverageChemo() ;
					
				}	
			}
			
			// calculate function value at t+tdel and y
			// f1 = y + dSdT * (t+tdel);
			//calculating f1 
			for (int i=0; i < f1.length; i++){	
				double f1Val=0;			
				 f1Val = y[i] + dSdT[i] * (t+tint);
				f1[i]=f1Val;
				if(f1Val<0){
				}
			}
			
			// vector containing the slopes
			// dFdT = (f1 - f0) / tdel		
			for (int i =0; i < dFdT.length ; i++){
				double dFdTVal=0;
				dFdTVal=(f1[i]-y[i])/ tint;
				dFdT[i] = dFdTVal;
			}
	
			
			//--------> Loop for advancing one step, if step is successful <----------//
			
			boolean successfulStep = true;
			
			while (true) {
				
								
				// calculating: W = I - (h*d*dFdY)
				W = I.plus((dFdY.times(h*d)).uminus()) ;
				LU = W.lu();
				L = LU.getL();
				U = LU.getU();
				invL = L.inverse();
				invU = U.inverse();
				p = LU.getPivot();
				
				
				//calculating k1aux = f0 + (h*d*dfdt);
				for (int i=0; i < k1aux.length; i++){	
					k1aux[i]= y[i] + (h*d) * dFdT[i];
				}
				
				double rSumk1L=0;
				for ( int row = 0; row< invL.getRowDimension(); row++){
					for (int col = 0; col< invL.getColumnDimension(); col++){						
						int ind = p[col];
						rSumk1L += invL.get(row,col)* k1aux[ind];
					}
					//I've only multiplied invL by k1aux(p), still need to multiply
					//the result of this by invU
					k1[row]= rSumk1L;
				}
				
				//finishing calculations for k1 (k1 = invW * (F0 +h*d*dFdT))
				double rSumk1=0;
				for ( int row = 0; row< invU.getRowDimension(); row++){
					for (int col = 0; col< invU.getColumnDimension(); col++){
						rSumk1 += invU.get(row, col)*k1[col];
					}
					k1[row]=rSumk1;
				}
				
				
				//evaluating f1 = F(tn+0.5 * h, y + 0.5*h*k1)
				for (int i=0; i < f1.length; i++){
					double f1V = (y[i]+0.5*h*k1[i]) + dSdT[i] * (t+0.5*h);
					f1[i] = f1V;
				}
				
				
				//calculating k2 
				double rSumk2L =0;
				for ( int row = 0; row< invL.getRowDimension(); row++){
					for (int col = 0; col< invL.getColumnDimension(); col++){
						rSumk2L += invL.get(row, col)*(f1[p[col]]-k1[p[col]])+k1[col];
					}
					k2[row]= rSumk2L;
				}
				
				//finishing calculations for k2 = invW * (F1-k1) + k1;
				double rSumk2 = 0;
				for ( int row = 0; row< invU.getRowDimension(); row++){
					for (int col = 0; col< invU.getColumnDimension(); col++){
					rSumk2 += k2[col]* invU.get(row, col);	
					}
					k2[row]= rSumk2;
				}				
			
				tnext = t + h;
			
			if (lastStep){
				tnext = tfinal;
			}
			
			
			//calculating ynext = y + h*k2
		
			for (int i=0; i<ynext.length; i++){
				ynext[i]= y[i] + h * k2[i];
			}
			
						
			//calculating f2 = F(tn+1, yn+1)
			for (int i=0; i< f2.length; i++){
				double f2Val = ynext[i] + dSdT[i] * (tnext) ;
				f2[i]=f2Val;
			}
			
			//calculating k3aux
			double rSumk3aux=0;
			for ( int row = 0; row< I.getRowDimension(); row++){
				for (int col = 0; col< I.getColumnDimension(); col++){					
				rSumk3aux += (f2[col] - e32 * (k2[col]-f1[col]) - 2.0*(k1[col]-y[col])+(h*d)*dFdT[col]);
				}
				k3aux[row]= rSumk3aux;	
			}
			
			//calculating k3
			double rSumk3=0;
			for ( int row = 0; row< I.getRowDimension(); row++){
				for (int col = 0; col< I.getColumnDimension(); col++){
				rSumk3 = invU.get(row, col) * (invL.get(row, col)*k3aux[p[col]]);	
				}
				k3[row]= rSumk3;
				
			}

			
			//Estimate the error 			
			for(int i=0; i< k1.length; i++){
				vCalc[i] = (k1[i]-2.0*k2[i]+k3[i])/ (Math.max(Math.abs(y[i]),Math.abs(ynext[i])));	
			}
				
		    for (int i=1; i<vCalc.length; i++) {
		        if (Math.abs(vCalc[i]) > vMax) {
		            vMax= Math.abs(vCalc[i]);   // new maximum
		        }
		    }
	
		    error = (h/6.0) * vMax;
			
			
			// The solution is accepted if the wheighted error is less than the relative tolerance rtol.
			
			if (error > rtol){ 
				
					
				successfulStep = false;
				lastStep = false;		
				
				//if the step fails, calculate a new h based on the standard rule for selecting 
				//a stepsize in numerical integration of initial value problems: 
				//hn+1 = (rtol / error) ^ (1/order of method,in our case is 3) * hn;
				// 90% of this estimated value is then used in the next step to decrease the probability 
				//of further failures.
				
				//reference: GEAR, C. W. 1971. Numerical Initial Value Problems in Ordinary Differential Equattons.
				// Prentice-Hall, Englewood Cliffs, N.J.
				
				h =  Math.max(hmin, h * 0.9*(Math.pow((rtol/error), power)));
				
			}else{
				
				successfulStep = true;
				break;
				
			}	
			
			
		}//end while (true)

			
			//If there were no failures compute a new h.
			if(successfulStep){
				
				//we use the same formula as before to compute a new step, h. 
				//But in addition, we adjust the next time step depending on how stiff
				// the problem is.
				
				//reference: Shampine LF. 1982. Implementation of Rosenbrock Methods. 
				// ACM Transactions on Mathematical Software. 8: 93-113.
				
				double test = Math.pow((rtol/error), power);
				
				//if the system is extremely stiff, the increase of step size is limited to a factor of 1.2
				if(test<1.2){
					h = h*test;
					
				}else{
				//if our problem is barely stiff the increase of step size is limited to a factor of 5
			
				h=h*5;
				
				}
			}

			//update the values in allSolute grid
			
			boolean isConstSol = false;
			for (int i=0; i< allSolute.length; i++){
				
				if(allSolute[i]==null){
					allSolute[i].setAllValueAt(0);
					
				}else{
				
					for (AllBC aBC : _domain.getAllBoundaries()){
						if (aBC.hasBulk()){
							Bulk aBulk = aBC.getBulk();
								if(aBulk.getName().equals("chemostat")){
									isConstSol = aBulk._isConstant[i];
								}
						}	
					}
					
					if(isConstSol){
					//do nothing, i.e., its concentration will remain constant = _sIn
					}else{
							if(f2[i]<=0){
									allSolute[i].setAllValueAt(0);
							}else{
		
									allSolute[i].setAllValueAt(f2[i]);
					
							}	
					}
			
				}
			}
			
			
			if(lastStep){ // if we've reached the end of the global time step
	
				break;
				
			}else{
						
				t = tnext;	
			
			}
			
		} //end while(!lastStep)

	}
	
	/**
	 * @author Sonia Martins
	 * This is a simple method to fill the Jacobian matrix using the diff uptake rates stored
	 * @param dilRate
	 * @return
	 */
	public Matrix Jacobian (double dilRate){

		//Calculating the Jacobian Matrix
		reacInd.clear();
		for (int r=0; r < dFdY.getRowDimension(); r++){
			for (int c=0; c< dFdY.getColumnDimension(); c++){
				dFdY.set(r,c,0);
				
			}
		}
		
	
		D = dilRate;
		dS_ds=0;
		
	
		//the row is the index of the "main" substrate
		for (int r=0; r < dFdY.getRowDimension(); r++){
			//the column is the index of the "other" substrates relative to which we will calculate the partial derivatives
			//of the main substrate
			for (int c=0; c< dFdY.getColumnDimension(); c++){
				if(c==r){
					//if the row and column indices are the same, then this is the place to put the partial
					// derivative of the "main" substrate to itself
					
					dFdY.set(r, c, ((allDiffSum[r]-D)));
				
				}else{
					
					//if not, we are calculating the partial derivative of the "main" substrate (S) relative to 
					//the other substrates. We have to sum the diff rates of the "other" substrates (s) over the
					//reactions in which S participates
					reacInd = solReacInd[r];
					dS_ds=0;
						for(int i=0; i<reacInd.size(); i++){
							
						dS_ds += ((allDiffReac[reacInd.get(i)][c]));
						
						}
						dFdY.set(r, c, dS_ds);			
				}
			}
		}	
	
		return dFdY;
		
	}
	
	

}
