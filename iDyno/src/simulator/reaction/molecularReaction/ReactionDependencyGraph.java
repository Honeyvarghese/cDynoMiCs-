
package simulator.reaction.molecularReaction;
import java.util.LinkedList;

import simulator.Simulator;
import simulator.agent.ActiveAgent;
import simulator.reaction.molecularReaction.MolecularReaction.FiringCondition;


public class ReactionDependencyGraph {

	private LinkedList<Integer>[] reactantDependants = null;
	private LinkedList<Integer>[] productDependants = null;
	
	@SuppressWarnings("unchecked")
	public ReactionDependencyGraph(ActiveAgent anAgent, Simulator sim) {
		reactantDependants = new LinkedList[anAgent.activeMolecularReactions.size()];
		productDependants = new LinkedList[anAgent.activeMolecularReactions.size()];
		
		for (int i=0; i<anAgent.activeMolecularReactions.size(); i++) {
			reactantDependants[i] = new LinkedList<Integer>();
			productDependants[i] = new LinkedList<Integer>();
//			if(sim.molecularReactionList[anAgent.activeMolecularReactions.get(i)].type.equalsIgnoreCase("compartmentDivision"))
//				continue;				
			for (int j=0; j<anAgent.activeMolecularReactions.size(); j++) {
				if (haveToCreateEdgeFromTo(i, j, anAgent, sim, "reactant"))
					reactantDependants[i].add(j);
				if (haveToCreateEdgeFromTo(i, j, anAgent, sim, "product"))
					productDependants[i].add(j);
			}
		}
	}

	private boolean haveToCreateEdgeFromTo(int i, int j, ActiveAgent anAgent, Simulator mySim, String dependencyType) {
//		if(i==j && mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)].type.equalsIgnoreCase("compartmentDivision"))
//			return true;  //It would be later used for considering lag time in division process
		if(i==j && dependencyType.equalsIgnoreCase("reactant"))
			return true;
		String[] source= dependencyType.equalsIgnoreCase("reactant")? mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)]._reactantMolecules:
			mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)]._productMolecules;
		if(mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)].type.equalsIgnoreCase("compartmentRemoval"))
			source=mySim.molecularReactionManager.getCompartmentMoleculeNames(anAgent,mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)].compartment);
		int counter;
		//Check to see if j reactant molecules depend on reaction i (either left side or right side of ith reaction) 
		for(String j_reactant:mySim.molecularReactionList[anAgent.activeMolecularReactions.get(j)]._reactantMolecules)
		{
			counter=-1;
			for(String i_reacProd:source)
			{
				counter++;
				if(dependencyType.equalsIgnoreCase("reactant") && !mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)].type.equalsIgnoreCase("compartmentRemoval")
						&& !mySim.molecularReactionList[anAgent.activeMolecularReactions.get(i)]._isMoleculeConsumed[counter] )
					continue;
				if(j_reactant.equalsIgnoreCase(i_reacProd))
					return true;
			}
		}
		//Check to see any of firing condition deciders of jth reaction depends on ith reaction 
		for( FiringCondition firingCondition: mySim.molecularReactionList[anAgent.activeMolecularReactions.get(j)].firingConditions)
		{
			for(String i_reacProd:source)
			{
				if(firingCondition.conditionDecider.equalsIgnoreCase(i_reacProd))
					return true;
			}
		}
		return false;
	}

	public LinkedList<Integer> getDependentsOnReaction(int reaction) {
		return reactantDependants[reaction];
	}
	
	public LinkedList<Integer> getDependentsOnProduct(int reaction) {
		return productDependants[reaction];
	}
}
