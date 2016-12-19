package simulator.reaction.kinetic;

import org.jdom.Element;
import utils.XMLParser;

public class LinearKinetic extends IsKineticFactor {
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1100L;
	
	private double _Ks;

	public LinearKinetic() {
	}

	public LinearKinetic(double Ks) {
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
		return paramTable[index]*solute;
	}

	public double kineticValue(double solute) {
		return _Ks*solute;
	}

	public double kineticDiff(double solute, double[] paramTable, int index) {
		return _Ks;
	}

	public double kineticDiff(double solute) {
		return _Ks;
	}

	public double kineticMax() {
		return 1;
	}

	public double kineticMax(double[] paramTable, int index) {
		return 1;
	}
}
