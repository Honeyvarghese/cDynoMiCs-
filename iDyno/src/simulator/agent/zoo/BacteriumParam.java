/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 */

/**
 * 
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 */

package simulator.agent.zoo;

import simulator.Simulator;
import simulator.agent.LocatedParam;
import utils.XMLParser;
import java.awt.Color;

/** Parameters common to all instances of a same species */
public class BacteriumParam extends LocatedParam {
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;

	// Maximal fraction of eps volume before excretion 
	public double epsMax = .15;
	public Color epsColor = Color.lightGray;


	public BacteriumParam() {
		super();
	}

	public void init(Simulator aSim, XMLParser aSpeciesRoot){
		super.init(aSim,aSpeciesRoot);
		double value;

		value = aSpeciesRoot.getParamDbl("epsMax");
		if(!Double.isNaN(value)) epsMax = value;

		String colorName;
		colorName= aSpeciesRoot.getParam("epsColor");
		if(colorName!=null) epsColor = utils.UnitConverter.getColor(colorName);

	}

}
