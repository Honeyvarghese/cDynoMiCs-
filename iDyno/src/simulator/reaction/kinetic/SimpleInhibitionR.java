/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

package simulator.reaction.kinetic;

import utils.ExtraMath;
import utils.XMLParser;

import org.jdom.Element;

public class SimpleInhibitionR extends IsKineticFactor {
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;
	
	private double _Ki;

	
	public SimpleInhibitionR(){}
	
	public SimpleInhibitionR(double Ki){
		_Ki = Ki;
		nParam = 1;
	}
	public void init(Element defMarkUp){
		_Ki = (new XMLParser(defMarkUp)).getParamDbl("Ki");		
		nParam = 1;
	}

	public void initFromAgent(Element defMarkUp,double[] kineticParam,int paramIndex){		
		kineticParam[paramIndex] = (new XMLParser(defMarkUp)).getParamDbl("Ki");	
	}	
	
	public double kineticValue(double solute,double[] paramTable,int index) {
		return solute / (paramTable[index] + solute);
	}
	public double kineticValue(double solute) {
		return solute / (_Ki + solute);
	}
	public  double kineticDiff(double solute,double[] paramTable,int index) {
		return -solute / ExtraMath.sq(paramTable[index] + solute);
	}
	public  double kineticDiff(double solute) {
		return -solute / ExtraMath.sq(_Ki + solute);
	}
	public double kineticMax() {
		return 1;
	}
	public double kineticMax(double[] paramTable,int index) {
		return 1;
	}
}
