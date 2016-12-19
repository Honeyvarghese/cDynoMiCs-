/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ___________________________________________________________________________
 * BactAdaptable: a species that can change its reaction based on local conditions 
 * 
 */

/**
 * 
 * @since Nov 2008
 * @version 1.0
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * ____________________________________________________________________________
 */

package simulator.agent.zoo;

import simulator.Simulator;
import simulator.SoluteGrid;
import utils.XMLParser;

public class YeastParam extends BactEPSParam {

	// this array is used to read the solute concentrations
	protected SoluteGrid[] _soluteList;
	public boolean useActivationInhibition=false;
	public int startingTimeForActivationInhibition;
	public double nieighborhoodRadiusCoefficient=2.5;
	public YeastParam() {
		super();
	}
	public void init(Simulator aSim, XMLParser aSpeciesRoot){
		
		_soluteList = aSim.soluteList;
		if(aSpeciesRoot.getParamBool("useActivationInhibition")!=null)
		{
			useActivationInhibition=aSpeciesRoot.getParamBool("useActivationInhibition");
			nieighborhoodRadiusCoefficient=aSpeciesRoot.getParamDbl("neighborhoodRadiusCoefficient");
		}
//		if(aSpeciesRoot.getParamInt("startingTimeActivationInhibition")!=0)
//		{
			startingTimeForActivationInhibition=aSpeciesRoot.getParamInt("startingTimeActivationInhibition");
//		}
		
		super.init(aSim,aSpeciesRoot);
	}

}
