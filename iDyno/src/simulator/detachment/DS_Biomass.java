/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 * 
 */

/**
 * @since June 2006
 * @version 1.0
 * @author  * @author João Xavier (xavierj@mskcc.org), Memorial Sloan-Kettering Cancer Center (NY, USA)
 * 
 */

package simulator.detachment;

import simulator.AgentContainer;
import simulator.Simulator;
import simulator.agent.LocatedGroup;
import utils.XMLParser;
import utils.ExtraMath;

public class DS_Biomass extends LevelSet {

	private double kDet;
	private double maxTh;

	public void init(AgentContainer anAgentGrid, XMLParser root) {
		super.init(anAgentGrid, root);
		// kDet has units of: fg.um-4.hr-1
		// this gives speed in um.hr-1
		kDet = root.getParamDbl("kDet");
		double value=root.getParamDbl("maxTh");
		maxTh=(Double.isNaN(value)? Double.POSITIVE_INFINITY:value);
	}

	protected double getLocalDetachmentSpeed(LocatedGroup aGroup, Simulator aSim) {
		if (aGroup.cc.x>maxTh) return Double.MAX_VALUE;
		return kDet*ExtraMath.sq(aGroup.cc.x)/aGroup.totalConcentration;
	}

}
