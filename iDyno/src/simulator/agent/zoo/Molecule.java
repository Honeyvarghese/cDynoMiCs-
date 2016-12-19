/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 *  
 */

/** 
 * @since June 2006
 * @version 1.0
 * @author Andreas D�tsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @author S�nia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 */

package simulator.agent.zoo;

import java.util.ArrayList;

import simulator.Simulator;
import simulator.agent.ActiveAgent;
import simulator.reaction.molecularReaction.Compartment;


public class Molecule {
	public String name;
	public String type;
	public String locale;
	public double mass;
	public double divisionThreshold;
	public ArrayList<Integer> instances;
	public double amount;
	public String[] triggers;
	public double secretionRate;
	public int defaultInitialValue=1;
	
	public Molecule() {
		type="internal";
		mass=1;
	}
	public Molecule(ActiveAgent anAgent, String name, String locale, String type, String triggersString, double secretionRate, int number, double divisionThreshold, Simulator sim) {
		this(anAgent,name,locale, type, new String[0], secretionRate, number, divisionThreshold, sim);
		String [] splittedTriggers= triggersString.split(",");
		triggers=new String[splittedTriggers.length];
		for(int i=0;i<splittedTriggers.length;i++)
		{
			triggers[i]=splittedTriggers[i].trim();
		}
	}
	
	public Molecule(ActiveAgent anAgent, String name, String locale, String type, String[] triggers, double secretionRate, int number, double divisionThreshold, Simulator sim)
	{
		instances=new ArrayList<Integer>();
		this.name=name;
		this.type=type;
		this.locale=locale;
		this.secretionRate=secretionRate;
		this.divisionThreshold=divisionThreshold;
		this.triggers=triggers.clone();
		String []compartment= name.split("@");
		Compartment compartmentBelongsTo=null;
		if(compartment.length>1)
		{
			sim.molecularReactionManager.registerToCompartment(anAgent, compartment[1], name);
			compartmentBelongsTo=anAgent.compartments.get(compartment[1]);
		}
		else
		{
			sim.molecularReactionManager.registerToCompartment(anAgent, locale, name);
			compartmentBelongsTo=anAgent.compartments.get(locale);
		}
		this.secretionRate=secretionRate;
		int copiesNeeded=compartmentBelongsTo==null?1:compartmentBelongsTo.numberOfInstances;;
		for(int i=0;i<copiesNeeded;i++)
		{
			addInstance(number); 
		}
		//amount=number*copiesNeeded;
		
	}
	public void removeInstance(int indexToMove) {
		amount-=instances.get(indexToMove);
		instances.remove(indexToMove);		
	}
	public void addInstance(int yield) {
		amount+=yield;
		instances.add(yield);
	}
	public void setInstanceValue(int index, double value) {
		double changedAmount=value- instances.get(index);
		instances.set(index, (int) value);
		amount+=changedAmount;
		
		
	}
}
