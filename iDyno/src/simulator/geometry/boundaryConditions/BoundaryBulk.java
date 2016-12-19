/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 * ___________________________________________________________________________
 * BoundaryBulk : the concentration on the boundary is fixed by a dynamical 
 * bulk, the agents crossing this line die
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */
/**
 * @ December 2016
 * Boundary Bulk is modified by Honey to create bulk with new properties to model granulation
 * 
 */

package simulator.geometry.boundaryConditions;

import utils.UnitConverter;
import utils.XMLParser;

import java.util.Arrays;

import org.jdom.Element;

import simulator.*;
import simulator.geometry.*;
import simulator.agent.LocatedAgent;
import simulator.agent.LocatedGroup;

public class BoundaryBulk extends AllBC{


	/* ____________________ INTERNAL TEMPRARY VARIABLES ________________________ */
	private static ContinuousVector vectorIn;

	// _____________________________ FIELDS _______________________________ 
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;

	// At which bulk the liquid phase is connected
	protected Bulk            _connectedBulk;
	static double             bulkValue;
	//added by honey
	protected boolean[]               isPermeableTo;

	// ___________________________ CONSTRUCTOR _____________________________ 
	public BoundaryBulk() {
		hasBulk = true;
	}

	public void init(Simulator aSim, Domain aDomain, XMLParser aBoundCondMarkUp) {
		// Load the geometry of the boundary
		
		System.out.println("hey boundary bulk is called------>\n\n");
		readGeometry(aBoundCondMarkUp, aDomain);		
		
		aDomain.addBoundary(this);
	
		
		// Load description of the connected bulk
		String bulkName = aBoundCondMarkUp.getParam("bulk");
		//added by honey-------------------
		isPermeableTo = new boolean[aSim.soluteDic.size()];
		Arrays.fill(isPermeableTo, false);

		for (Element aChild :  aBoundCondMarkUp.getChildren("param")) {
			if (!aChild.getAttributeValue("name").equals("isPermeableTo")) continue;
			String soluteName = aChild.getAttributeValue("detail");
			isPermeableTo[aSim.getSoluteIndex(soluteName)] = true;
			
			StringBuffer unit=new StringBuffer("");
			//double paramValue =  aBoundCondMarkUp.getParamDbl("isPermeableTo", unit);
			
			
		}
		//----------------------------
		System.out.println("bulkname"+bulkName);
		_connectedBulk = aSim.world.getBulk(bulkName);
		bulkValue = 0;
	}

	// ____________________________ SOLVER _________________________________ 
	//refreshBoundary modified by honey to get properties of selective permeability

	public void refreshBoundary1(SoluteGrid aSoluteGrid) {
		// Store the concentration in the bulk
		bulkValue = _connectedBulk.getValue(aSoluteGrid.soluteIndex);
		
		//bulkValue = 1;
		// Initialise the course along the shape of the boundary
		_myShape.readyToFollowBoundary(aSoluteGrid);

		while (_myShape.followBoundary(dcIn, dcOut, aSoluteGrid)) {
			aSoluteGrid.setValueAt(bulkValue, dcOut);
		}
	}

	public void refreshBoundary(SoluteGrid aSoluteGrid) {

		// Initialise the course along the shape of the boundary
		_myShape.readyToFollowBoundary(aSoluteGrid);

		if (isPermeableTo[aSoluteGrid.soluteIndex]) {
			//System.out.println("glucose ----------->>>>>>>"+aSoluteGrid.soluteIndex);
			while (_myShape.followBoundary(dcIn, dcOut, aSoluteGrid)) {
				aSoluteGrid.setValueAt(_connectedBulk.getValue(aSoluteGrid.soluteIndex), dcOut);
			}

		} else {
			// The membrane has the same behaviour than a zero-flux boundary
			while (_myShape.followBoundary(dcIn, dcOut, aSoluteGrid)) {
				aSoluteGrid.setValueAt(aSoluteGrid.getValueAt(dcIn), dcOut);
			}
		}
	}

// bvm note 15.12.08: modified inputs to allow reaction or flux-based bulk treatment
	/*public void updateBulk(SoluteGrid[] allSG, SoluteGrid[] allRG, double timeStep) {
		
		System.out.println("connected bulk detected..........................");
		
		_connectedBulk.updateBulk(allSG, allRG, timeStep);
	}
	*/
	//FN WRITTEN BY HONEY----------------------------------------------
	public void updateBulk(SoluteGrid[] soluteGrid,SoluteGrid[] reacGrid, double timeStep)
{
_connectedBulk.updateBulk(soluteGrid, reacGrid, timeStep);
}

	public Bulk getBulk() {
		return _connectedBulk;
	}

	public double getBulkValue(int soluteIndex) {
		return _connectedBulk.getValue(soluteIndex);
	}

//	 _______________________ LOCATED Agents ______________________________ 

	public ContinuousVector lookAt(ContinuousVector cc) {
		return cc;
	}

	/**
     * Label a LocatedGroup which has been identified being outside this
     * boundary
     */
	public void setBoundary(LocatedGroup aGroup) {
		aGroup.status = 3;
		// status 3 -> bulk
	}

	/**
     * An agent is crossing the boundary ; he is leaving the simulated system,
     * kill him
     */
	public void applyBoundary1(LocatedAgent anAgent, ContinuousVector target) {
		//sonia 27.04.2010
		//recording reason of death (agent will be moved to agentToKill list when die() calls registerDeath()
		anAgent.death = "overBoard";
		
		anAgent.die(false);
		//LogFile.writeLog("agent killed by Bulk Boundary");
		
		// to label this agent as "shoving solved", set to zero its movement.
		anAgent.getMovement().reset();
		target.set(anAgent.getLocation());
		
	}
//Added by Honey--to stop killing the cells that cross boundary
	public void applyBoundary(LocatedAgent anAgent, ContinuousVector target) {
		// Define coordinates of the corrected position
		
		_myShape.orthoProj(target,target);
		
		// Build a vector normal to the boundary and starting from the
        // orthogonal projection		
		vectorIn = new ContinuousVector(_myShape.getNormalInside(target));
		
		// The whole cell has to be inside, so make a translation equal to the
        // total radius		
		vectorIn.times(anAgent.getRadius(true));
		
		// Compute the new position
		target.add(vectorIn);
		
		// Compute and update the movement vector leading to this new position
		anAgent.getMovement().sendDiff(anAgent.getLocation(), target);
	}

	public String toString() {
		return new String("Bulk:"+this._mySide);
	}
}
