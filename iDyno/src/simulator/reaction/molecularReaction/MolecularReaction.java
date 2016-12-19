package simulator.reaction.molecularReaction;

import java.util.ArrayList;
import java.util.LinkedList;

import org.jdom.Element;

import simulator.Simulator;
import utils.XMLParser;

public class MolecularReaction {
	public class FiringCondition {
		public String conditionType; //For example amount, time, age...
		public String conditionDecider; 
		public String conditionMeasure; // lessThan or greaterThan
		public double conditionValue1;
		public double conditionValue2; //if you want to determine the range
	}
	protected  FiringCondition[] firingConditions;
	public double[] _reactantMoleculeYield;
	public String[] _reactantMolecules;
	public double[] _productMoleculeYield;
	public String[] _productMolecules;
	protected boolean[] _isMoleculeConsumed;
	protected double[] rates;
	protected String rateFunction;
	protected Simulator aSim;
	public String reactionName;
	public String type;
	public String compartment;  //Used for compartment divison or removal only
	public String correspondningSolute;
	public double sensingThreshold;
	public double signallingRate;
	public String player;
	double productDelay;
	ArrayList<Integer> compartmentIndicesReadyForReacting;  //compartments with satisfied firing conditions used  
	public int numOccurence;
	
	public MolecularReaction()
	{
		compartmentIndicesReadyForReacting=new ArrayList<Integer>();
		numOccurence=0;
	}
	
	public void initFromProtocolFile(Simulator aSim, XMLParser aReactionRoot)
	{
		this.aSim= aSim;
		reactionName = aReactionRoot.getAttribute("name").trim();
		String reacType=aReactionRoot.getAttribute("type");
		productDelay=aReactionRoot.getAttribute("timing")==null? 0: aReactionRoot.getAttributeDbl("timing");
		if(reacType!=null)
			this.type=reacType.trim();
		else
			this.type="default";
		
		if(type.equalsIgnoreCase("sensing") || type.equalsIgnoreCase("signalling"))
		{
			correspondningSolute=aReactionRoot.getAttribute("solute");
			if(type.equalsIgnoreCase("sensing"))
			{
				player=aReactionRoot.getAttribute("receptor");
				sensingThreshold=aReactionRoot.getParamDbl("sensingThreshold");
			}
			else
			{
				player=aReactionRoot.getAttribute("signaller");
				signallingRate=aReactionRoot.getParamDbl("signallingRate");
			}
		}
		
		if(type.equalsIgnoreCase("compartmentRemoval") || type.equalsIgnoreCase("compartmentDivision"))
		{
			compartment=aReactionRoot.getAttribute("compartment");
		}
		
		rateFunction="regular";
		if(aReactionRoot.getAttribute("rateFunction")!=null)
			rateFunction=aReactionRoot.getAttribute("rateFunction");
		readRates(aReactionRoot.getAttributeStr("reactionRate"));
		
		LinkedList<Element> allFiringConditionElements= aReactionRoot.buildSetMarkUp("firingCondition");
		firingConditions=new FiringCondition[allFiringConditionElements.size()];
		int counter=0;
		for (Element condMarkUp :allFiringConditionElements) {
			XMLParser cond= new XMLParser(condMarkUp);
			firingConditions[counter]= new FiringCondition();
			firingConditions[counter].conditionType=cond.getAttribute("type");
			firingConditions[counter].conditionMeasure= cond.getAttribute("when");
			firingConditions[counter].conditionDecider= cond.getAttribute("decider");
			firingConditions[counter].conditionValue1= cond.getAttributeDbl("value");		
			counter++;
		}
		
		int reactantSize=0, productSize=0;
		double value;
		for (Element paramMarkUp : aReactionRoot.buildSetMarkUp("yield")) {
			XMLParser reac= new XMLParser(paramMarkUp);
		
			value= reac.getAttributeDbl("value");
			if(value<=0)
				reactantSize++;
			else
				productSize++;
		}
		
		
		_reactantMolecules=new String[reactantSize];
		_reactantMoleculeYield = new double[reactantSize];
		_isMoleculeConsumed=new boolean[reactantSize];
		_productMolecules=new String[productSize];
		_productMoleculeYield = new double[productSize];
		
		String name;
		int reactantCounter=0;
		int productCounter=0;
		for (Element paramMarkUp : aReactionRoot.buildSetMarkUp("yield")) {
			XMLParser reac= new XMLParser(paramMarkUp);
			
			name=paramMarkUp.getAttributeValue("name").trim();
			value= reac.getAttributeDbl("value");
			if(value<=0)
			{
				_reactantMolecules[reactantCounter]=name;
				_reactantMoleculeYield[reactantCounter]= value;
				_isMoleculeConsumed[reactantCounter]=  paramMarkUp.getAttribute("virtual")==null?true: paramMarkUp.getAttributeValue("virtual").equalsIgnoreCase("false");
				reactantCounter++;
			}
			else 
			{
				_productMolecules[productCounter]=name;
				_productMoleculeYield[productCounter]= value;
				productCounter++;
			}
		}		
	}

	private void readRates(String ratesString) {
		String [] splittedRates= ratesString.split(",");
		rates=new double[splittedRates.length];
		for(int i=0;i<splittedRates.length;i++)
		{
			rates[i]=XMLParser.getVariableByName(splittedRates[i].trim());
		}
	}
	
}
