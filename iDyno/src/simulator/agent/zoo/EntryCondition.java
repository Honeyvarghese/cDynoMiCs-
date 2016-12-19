package simulator.agent.zoo;

import simulator.geometry.ContinuousVector;

public class EntryCondition {
	public String switchType;
	public int speciesIndex;
	// the component controlling the switch and the value and
	// state that set the transition
	public String switchControl;
	public int switchControlIndex;
	public int switchControlIndex2;
	public String switchCondition; // lessThan or greaterThan
	public double switchValue;
	public String fromSpecies;  //Allowed transition from (use "Any" keyword for allowing all transitions)
	public double switchValue2; //if you want to determine the range
	public ContinuousVector locationCorner1; //used for "location" condition
	public ContinuousVector locationCorner2; //used for "location" condition
	public ContinuousVector Center; //used for "distance" condition
}
