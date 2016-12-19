/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

package simulator.reaction.kinetic;

import utils.ExtraMath;
import utils.XMLParser;

import org.jdom.Element;

public class SimpleInhibitionWithAgonist extends IsKineticFactor {
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;
	
	private double _Ki;
	private double _Agonist;
	
	
	public SimpleInhibitionWithAgonist(){}
	
	public SimpleInhibitionWithAgonist(double Ki,double Agonist){
		_Ki = Ki;
		_Agonist=Agonist;
		nParam = 2;
	}
	public void init(Element defMarkUp){
		_Ki = (new XMLParser(defMarkUp)).getParamDbl("Ki");		
		_Agonist = (new XMLParser(defMarkUp)).getParamDbl("Agonist");		

		nParam = 2;
	}

	public void initFromAgent(Element defMarkUp,double[] kineticParam,int paramIndex){		
		kineticParam[paramIndex] = (new XMLParser(defMarkUp)).getParamDbl("Ki");
		kineticParam[paramIndex+1] = (new XMLParser(defMarkUp)).getParamDbl("Agonist");
	}	
	
	public double kineticValue(double solute,double[] paramTable,int index) {
		return paramTable[index] / (paramTable[index] + (solute+_Agonist));
	}
	public double kineticValue(double solute) {
		return _Ki / (_Ki + (solute+_Agonist));
	}
	public  double kineticDiff(double solute,double[] paramTable,int index) {
		return -_Ki / ExtraMath.sq(paramTable[index] + (solute+_Agonist));
	}
	public  double kineticDiff(double solute) {
		return -_Ki / ExtraMath.sq(_Ki + (solute+_Agonist));
	}
	public double kineticMax() {
		return 1;
	}
	public double kineticMax(double[] paramTable,int index) {
		return 1;
	}
}
