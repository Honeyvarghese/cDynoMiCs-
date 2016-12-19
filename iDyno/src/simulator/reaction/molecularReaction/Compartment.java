package simulator.reaction.molecularReaction;

import java.util.ArrayList;

public class Compartment {

	public ArrayList<String> containedMolecules;
	//public ArrayList<Integer> containedMoleculesInitialNumber;
	public int numberOfInstances;
	public int createdInstancesCounter; 
	public ArrayList<String> instancesName;
	public Boolean isEssential, isMainCompartment;
	public String name;
	public Compartment(String name, int numberOfInstances, Boolean isEssential, Boolean isMainCompartment)
	{
		this.numberOfInstances=numberOfInstances;
		this.name=name;
		containedMolecules=new ArrayList<String>();
		instancesName=new ArrayList<String>();
		for(int i=0;i<numberOfInstances;i++)
		{
			createdInstancesCounter++;
			instancesName.add(name+createdInstancesCounter);
		}
		//containedMoleculesInitialNumber=new ArrayList<Integer>();
		this.isEssential=isEssential;
		this.isMainCompartment=isMainCompartment;
	}
}