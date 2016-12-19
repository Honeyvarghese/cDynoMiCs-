/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

package simulator.reaction.kinetic;

import org.jdom.Element;
import utils.XMLParser;

public class MonodKinetic extends IsKineticFactor {
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;
	
	private double _Ks;

	public MonodKinetic() {
	}

	public MonodKinetic(double Ks) {
		_Ks = Ks;
		nParam = 1;
	}

	public void init(Element defMarkUp) {
		_Ks = (new XMLParser(defMarkUp)).getParamDbl("Ks");
		nParam = 1;
	}

	public void initFromAgent(Element defMarkUp, double[] kineticParam, int paramIndex) {
		kineticParam[paramIndex] = (new XMLParser(defMarkUp)).getParamDbl("Ks");
	}

	public double kineticValue(double solute, double[] paramTable, int index) {
		return solute/(paramTable[index]+solute);
	}

	public double kineticValue(double solute) {
		if(_Ks+solute==0)
			return 0;
		return solute/(_Ks+solute);
	}

	public double kineticDiff(double solute, double[] paramTable, int index) {
		return paramTable[index]/(paramTable[index]+solute)/(paramTable[index]+solute);
	}

	public double kineticDiff(double solute) {
		if(_Ks+solute==0)
			return 0;
		return _Ks/(_Ks+solute)/(_Ks+solute);
	}

	public double kineticMax() {
		return 1;
	}

	public double kineticMax(double[] paramTable, int index) {
		return 1;
	}
}
