package simulator.agent.zoo;


/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 */

/**
 * ______________________________________________________
 * @since June 2006
 * @version 1.1
 * @author Farzin (scoodnim@gmail.com), USU, Logan, Utah
 * ____________________________________________________________________________
 */


import simulator.Simulator;
import simulator.agent.LocatedParam;
import utils.XMLParser;

/** Parameters common to all instances of a same species */
public class AgarParticleParam extends LocatedParam {

	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 144L;
	public double transferRadius;

	public AgarParticleParam() {
		super();
	}

	public void init(Simulator aSim, XMLParser aSpeciesRoot) {
		double value;
		super.init(aSim, aSpeciesRoot);

		value = aSpeciesRoot.getParamLength("transferRadius");
		if(!Double.isNaN(value)) transferRadius = value;

	}
}

