/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 *______________________________________________________
 * Bulk is an object used to define the environment connected to the simulated 
 * system : this environment can impose concentrations on the boundaries 
 * (constant boundary) or exchange matter through the boundaries (bulk boundary)
 * 
 */

/**
 * @since August 2006
 * @version 1.0
 * @author Ahmadreza Ghaffarizadeh (ghaffarizadeh@aggiemail.usu.edu)
 */

package simulator.geometry;

import simulator.Simulator;
import simulator.SoluteGrid;
import utils.ExtraMath;

import utils.XMLParser;
public class Agar {

	private String   _name;
	public Simulator mySim;
	
	double[]         _dT;

//	int   Dim1 =30 ;
//	int   Dim2 =30 ;
//	int   Depth =30 ;
	public int   _nI ;
	public int   _nJ ;
	public int   _nK ;
	public int _currentT; //either 0 or 1
	
	double _concentration;
	int   T=300;
	public double[][][][] u;
	public double[][] R;
	
	public  double   _D;
	public double _advectionRate;
	public int   _depth;
	public int totalDepth;
	public  int   _updateFreq;
	
	double agarThresholdValue=5e-5;
	boolean hasBeenSolved=false;
	
	private SoluteGrid GlucoseGrid;
	/* __________________ CONSTRUCTOR _________________________________ */

	/**
	 * Constructor based on XML file
	 * @param anAgarRoot
	 */
	public Agar(Simulator aSim, XMLParser anAgarRoot) {
		mySim = aSim;
		_name = anAgarRoot.getAttribute("name");
		_depth= anAgarRoot.getParamInt("depth");
		_nJ = anAgarRoot.getParamInt("width");
		_nK = anAgarRoot.getParamInt("height");
//		_nI = mySim.world.domainList.get(0)._nI;
//		_nJ = mySim.world.domainList.get(0)._nJ;
//		_nK = mySim.world.domainList.get(0)._nK;
		_concentration=anAgarRoot.getParamConc("concentration");

		//totalDepth=_depth+aSim.world.domainList.get(0)._nK;
		_D = anAgarRoot.getParamDbl("D");
		_advectionRate = anAgarRoot.getParamDbl("advRate");
		T = anAgarRoot.getParamInt("T");
		
		_updateFreq=anAgarRoot.getParamInt("updateFreq");
		
		
		// u(x,y,z,t) retain the concentration of glucose in (x,y,z) position in time t. 
		// note that u must be defined and initialized outside of this class as public variable. 
		// cause we don't want to change u elements values in each run of method. 
		
//		Dim1=aSim.world.domainList.get(0)._nI;
//		Dim2=aSim.world.domainList.get(0)._nJ;
		
	}
	
	public void initialize()
	{
		// USE I,J,K same as X (VERTICAL), Y, Z AS IN BULK
		u = new double[_depth+2][_nJ+2][_nK+2][2]; 
		// initializing concentration u
		for (int i=0; i<_depth+2; i++)
			for (int j=0; j<_nJ+2; j++)
				for (int k=0; k<_nK+2; k++)
					for (int t=0; t<2; t++)
						u[i][j][k][t] = 0;

		// initial concentration of u in the agar

		for (int i=1; i<_depth; i++)
			for (int j=1; j<_nJ+1; j++)
				for (int k=1; k<_nK+1; k++)
					u[i][j][k][0] = _concentration;  	

	}
	public void setFromBoundary(double[][] boundary)
	{
		//TODO: check to see if indexing is correct here
	}
	
	public void updateAgar(SoluteGrid[] soluteGrid, SoluteGrid[] reacGrid, double timeStep)
	{
		GlucoseGrid=soluteGrid[mySim.getSoluteIndex("Glucose")];
		updateAgar(soluteGrid[mySim.getSoluteIndex("Glucose")], timeStep);	
	}
	
	public void updateAgar(SoluteGrid aSoluteGrid, double timeStep)
	{
		// called before refresh boundary to copy over into agar 
		//
		for(int i=_depth;i<_depth+2;i++)
			for(int j=1;j<_nJ+1;j++)
				for(int k=1;k<_nK+1;k++)
					// must avoid padding in aSoluteGrid at the bottom (0,j,k)
					u[i][j][k][_currentT]=aSoluteGrid.getValueAt((i+1)-_depth, j, k);
					//u[i][j][k][_currentT]=0.5;
		Advection_Diffusion_Solver();
	}
	
	public void Advection_Diffusion_Solver()
	{
		//IF we suppose the surface between colony and the agar is circle then r would be the radius. we can also 
		//change boundary condition as what we want. so in time step t1 this method takes the colony radius as a region 

		//which glucose could diffuse to colony through that. apparently in each time step this radius will increase. 
		//so some part of boundary conditions is changing in each time.


		//this method will give us concentration of glucose in surface after OUR-SET time steps. if time needed to colony 
		//growth in first time step is t1 then same time should be given to glucose to diffuse into colony. then we can 
		//split t1 to lower intervals and run solver. anyway it should be tested and tinker. 

		//then in time step t2, we cann't run solver from beginning. concentration of glucose at center of circle now
		//is lower than other parts. so we need to save the previous concentration values and use it in new run. 


		// Dim1 Dim2 and Depth are the dimension of cube. one/third of cube in bottom is considered as agar part. 




		// bx,by and bz are the speed of advection in each direction respectively. I have no idea how to set them! 
		// but here for example I set them as follow:
		hasBeenSolved=true;
		double  bx = _advectionRate ;
		double  by = _advectionRate ;
		double  bz = _advectionRate ;

		// ax,ay and az are the diffusion constant in each direction respectively. I have no idea how to set them! 
		// but here for example I set them as follow:

		double ax = _D;
		double ay = _D;
		double az = _D;



		double  delta_x = 1.0/_depth+2 ;
		double  delta_y = 1.0/_nJ ;
		double  delta_z = 1.0/_nK ;
		double  delta_t = 1.0/T ;


		double  cx= bx * delta_t  / delta_x ; 
		double  cy= by * delta_t  / delta_y ; 
		double  cz= bz * delta_t  / delta_z ; 


		double  sx = ax * delta_t  / Math.pow( delta_x, 2); 
		double  sy = ay * delta_t  / Math.pow( delta_x, 2); 
		double  sz = az * delta_t  / Math.pow( delta_x, 2); 

		// solver stability condition

		if ( Math.pow( cx, 2) / sx + Math.pow( cy, 2) / sy + Math.pow( cz, 2) / sz > 3 || sx + sy +sz >0.5 )
		{
			// main program must not come to this part. this this just for user information to set 
			// the parameter correct

			System.out.println("Not Stable");  
		}

		else

		{
			// repeat solver for each position and for each time.
			// here we use Fully explicit finite difference method. implicit methods like Crank-Nicolson are
			// more accurate but time costing. error for this method is of 5.5 * 10^(-3) and for Crank-Nicolson
			// is 5.5 * 10^(-4). 10 time less accurate than Crank-Nicolson method but more than 10 times faster.
            // FLANN run the solver one step
			int nowT = _currentT;
			int nextT = (nowT+1)%2;
//			for(int i=0;i< mySim.world.domainList.get(0)._nI;i++)
//				for(int j=0;j< mySim.world.domainList.get(0)._nJ;j++)
//					for(int k=0;k< mySim.world.domainList.get(0)._nK;k++)
//					{
//						if (mySim.world.domainList.get(0).getBiomass().grid[i][j][k] > 0)
//							break;
//					}
//						
			for(int i=_depth; i > 0;i--)
					for(int j=1;j<_nJ+1;j++)
						for(int k=1;k<_nK+1;k++)
                            // only update the top layer that contains mass in the domain or any in agar is updated
							if (i==_depth && mySim.world.domainList.get(0).getBiomass().grid[1][j][k] > 0 || i!=_depth)
							{
								//System.out.println("f "+String.valueOf(i)+","+String.valueOf(j)+","+String.valueOf(k)+","+String.valueOf(t));
								u[i][j][k][nextT] = ( sx + cx/2) * getValueAt(u,i-1,j,k,nowT,-1) + ( sx - cx/2) * getValueAt(u,i+1,j,k,nowT,1) + 
								( sy + cy/2) * getValueAt(u,i,j-1,k,nowT,-2) + ( sy - cy/2) * getValueAt(u,i,j+1,k,nowT,2) + 
								( sz + cz/2) * getValueAt(u,i,j,k-1,nowT,-3) + ( sz - cz/2) * getValueAt(u,i,j,k+1,nowT,3) + 
								( 1 -2*sx -2*sy -2*sz) * u[i][j][k][nowT];
							}
			// clock time forward
			_currentT = nextT;
		}
	}
	


	/* _________________________ GET & SET ________________________________ */

	private double getValueAt(double[][][][] u2, int i, int j, int k, int T, int mySwitch) {
		 	
		switch(mySwitch)
			{
			case(-1): return i==0? u2[i+1][j][k][T]:u2[i][j][k][T];
			case(1) : return u2[i][j][k][T];
			case(-2): return j==0? u2[i][j+1][k][T]:u2[i][j][k][T];
			case(2) : return j==_nJ+1? u2[i][j-1][k][T]:u2[i][j][k][T];
			case(-3): return k==0? u2[i][j][k+1][T]:u2[i][j][k][T];
			case(3) : return k==_nK+1? u2[i][j][k-1][T]:u2[i][j][k][T];
			}
		return 0;
	}

	public String getName() {
		return _name;
	}

	/**
	 * Send the time constraint of that bulk
	 * @return
	 */
	public double getTimeConstraint() {
		double out = ExtraMath.max(_dT);
		for (int iGrid = 0; iGrid<_dT.length; iGrid++) {
			if (_dT[iGrid]==0) continue;
			out = Math.min(out, Math.abs(_dT[iGrid]));
		}
		if (out==0) out = Double.POSITIVE_INFINITY;

		return out;
	}

	/**
	 * Insert the description of the bulk in the result file
	 * @param buffer
	 */
//	public void writeReport(ResultFile buffer) throws Exception {
//		StringBuffer text = new StringBuffer();
//		String soluteName;
//
//		text.append("<bulk name=\"").append(_name).append("\">\n");
//
//		// Concentration
//		// bvm 23.01.09: added units to the output (ARE THEY CORRECT?)
//		for (int i = 0; i<_bulkValue.length; i++) {
//			soluteName = mySim.soluteDic.get(i);
//			text.append("<solute name=\"")
//				.append(soluteName)
//				.append("\" unit=\"g.L-1\">");
//			text.append(_bulkValue[i]).append("</solute>\n");
//		}
//		buffer.write(text.toString());
//
//		// bvm 23.01.09: added units to the output (ARE THEY CORRECT?)
//		text = new StringBuffer();
//		for (int i = 0; i<_bulkValue.length; i++) {
//			soluteName = mySim.soluteDic.get(i);
//			// bvm 04.12.08: modified the XML output to make it valid
//			text.append("<uptake_rate name=\"")
//				.append(soluteName)
//				.append("\" unit=\"g.L-1.hour-1\">");
//			text.append(this._reacRate[i]).append("</uptake_rate>\n");
//		}
//		buffer.write(text.toString());
//		buffer.write("</bulk>\n");
//	}

    public boolean hasBeenSolved()
    { 
    	return hasBeenSolved;
    }
    
	public double getValue(int i,int j) {
		
		return u[_depth][i][j][_currentT];
	}
	
	public double getSectionValue(int w,int d) {
		
		int j=_nK/2;
		return u[d][w][1][_currentT];
	}
}
