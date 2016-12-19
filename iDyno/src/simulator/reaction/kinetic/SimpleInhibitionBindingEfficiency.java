/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

package simulator.reaction.kinetic;

import utils.ExtraMath;
import utils.XMLParser;

import org.jdom.Element;

public class SimpleInhibitionBindingEfficiency extends IsKineticFactor {
	// Serial version used for the serialisation of the class
	private static final long serialVersionUID = 1L;
	
	private double _Ki;
	private double _BindingEfficiency1;

	
	public SimpleInhibitionBindingEfficiency(){}
	
	public SimpleInhibitionBindingEfficiency(double Ki,double BindingEfficiency1){
		_Ki = Ki;
		_BindingEfficiency1=BindingEfficiency1;
		nParam = 2;
		
	}
	public void init(Element defMarkUp){
		
		_Ki = (new XMLParser(defMarkUp)).getParamDbl("Ki");	
		_BindingEfficiency1 = (new XMLParser(defMarkUp)).getParamDbl("BindingEfficiency1");		

		nParam = 2;
	}

	public void initFromAgent(Element defMarkUp,double[] kineticParam,int paramIndex){		
		kineticParam[paramIndex] = (new XMLParser(defMarkUp)).getParamDbl("Ki");
		kineticParam[paramIndex+1] = (new XMLParser(defMarkUp)).getParamDbl("BindingEfficiency1");

	}	
	
	public double kineticValue(double solute,double[] paramTable,int index) {
		//System.out.println("******"+paramTable[index] / (paramTable[index] + solute));
		return paramTable[index] / (paramTable[index] + solute);
	}
	public double kineticValue(double solute) {
		//solute=solute*_BindingEfficiency1;
		return _Ki / (_Ki + (solute*_BindingEfficiency1));
	}
	public  double kineticDiff(double solute,double[] paramTable,int index) {
		//solute=solute*_BindingEfficiency1;

		return -_Ki / ExtraMath.sq(paramTable[index] + (solute*_BindingEfficiency1));
	}
	public  double kineticDiff(double solute) {
	//	solute=solute*_BindingEfficiency1;

		return -_Ki / ExtraMath.sq(_Ki + (solute*_BindingEfficiency1));
	}
	public double kineticMax() {
		return 1;
	}
	public double kineticMax(double[] paramTable,int index) {
		return 1;
	}
}
