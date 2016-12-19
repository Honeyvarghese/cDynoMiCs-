package simulator.reaction.molecularReaction;

import idyno.SimTimer;

import java.awt.geom.Area;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.ListIterator;

import org.jdom.filter.ContentFilter;

import cern.jet.random.Exponential;
import cern.jet.random.engine.MersenneTwister;
import simulator.Simulator;
import simulator.agent.ActiveAgent;
import simulator.agent.LocatedAgent;
import simulator.agent.SpecialisedAgent;
import simulator.agent.zoo.Molecule;
import simulator.geometry.ContinuousVector;
import simulator.reaction.molecularReaction.MolecularReaction.FiringCondition;
import utils.ExtraMath;
import utils.XMLParser;

public class molecularReactionManager {
	
	public class ReactionProductFiringEvent
	{
		double fireTime;
		int reactionIndex;
		ActiveAgent anAgent;
		
		public ReactionProductFiringEvent(double fireTime, ActiveAgent anAgent, int reactionIndex)
		{
			this.fireTime=fireTime;
			this.anAgent=anAgent;
			this.reactionIndex=reactionIndex;
		}
	}
	
	public static Exponential exponential;
	public static final int ReactantSide=1;
	public static final int ProductSide=2;
	Simulator aSim;
	public String solverName;
	int steps;
	double timer;
	double lastOutputTime=0;
	public static final double outputPeriod=1;
	public static final double writingPeriod=100;
	private int nextOutputTime;
	private int globalNextOutputTime;
	private int globalNextWritingTime;
	private int nextWritingTime;
	HashMap<String, StringBuffer> fileContents;

	public void init(Simulator aSim, XMLParser xmlRoot)
	{
		this.aSim=aSim;
		exponential=new Exponential(0, new MersenneTwister((int) Simulator.randSeed));

		solverName = xmlRoot.getAttribute("name");
		steps = (int) xmlRoot.getParamDbl("steps");
		nextOutputTime=0;
		nextWritingTime=0;
		fileContents=new HashMap<String, StringBuffer>();
	}
	
	public void initializeAllReactions()
	{
		ListIterator<SpecialisedAgent> agentIter;
		ActiveAgent anAgent;
		for (agentIter = aSim.agentGrid.agentList.listIterator(); agentIter.hasNext();) 
		{
			anAgent = (ActiveAgent) agentIter.next();
			initializeReactionsForAgent(anAgent);
		}
	}
	
	public void initializeReactionsForAgent(ActiveAgent anAgent)
	{
		if(anAgent.activeMolecularReactions.size()==0)
			return;
		anAgent.a=new double[anAgent.activeMolecularReactions.size()];
		anAgent.taus=new double[anAgent.activeMolecularReactions.size()];
		anAgent.lastNonInfTau=new double[anAgent.activeMolecularReactions.size()];
		anAgent.lastNonZeroA=new double[anAgent.activeMolecularReactions.size()];
		for (int i=0; i<anAgent.a.length; i++)
		{
			anAgent.a[i] = calculatePropensity(aSim.molecularReactionList[anAgent.activeMolecularReactions.get(i)], anAgent);
			if(anAgent.a[i]==0)
				anAgent.taus[i]=Double.POSITIVE_INFINITY;
			else
				anAgent.taus[i] = exponential.nextDouble(anAgent.a[i])+ SimTimer.getCurrentTimeInSec();
		}
		anAgent.tauQueue=new IndexedPriorityQueue(anAgent.taus.clone());
	}

	public void stepMolecularReactions(MolecularReaction[] mReactions) {		
		ListIterator<SpecialisedAgent> agentIter;
		ActiveAgent anAgent;
		MolecularReaction aReaction;
		Molecule molecule;
		ContinuousVector loc;
		double tau, productNextTau;
		int reactionIndex;
		@SuppressWarnings("unused")
		int check=0;
		for (agentIter = aSim.agentGrid.agentList.listIterator(); agentIter.hasNext();) 
		{
			check++;
			anAgent = (ActiveAgent) agentIter.next();
			if(anAgent.activeMolecularReactions.size()==0)
				continue;
			timer=SimTimer.getCurrentTimeInSec();
			nextOutputTime=globalNextOutputTime;
			nextWritingTime=globalNextWritingTime;
			while(timer<SimTimer.getCurrentTimeInSec()+SimTimer.getCurrentTimeStepInSec())
			{
				manageQueueProducts(anAgent, timer);
				tau=anAgent.tauQueue.getMinKey();
				productNextTau=getNextProductQueueFireTime(anAgent);
				if(Double.isInfinite(tau))
				{
					throw new RuntimeException("Infinite tau reached...");
				} 
				
				timer= Math.min(Math.min(tau, productNextTau), nextOutputTime);
				if(timer>SimTimer.getCurrentTimeInSec()+SimTimer.getCurrentTimeStepInSec() || timer==productNextTau || timer==nextOutputTime)
				{
					if ( timer>=nextOutputTime)
						createMolecularReport(anAgent, timer);
					continue;
				}

				reactionIndex=nextReaction(anAgent);
				aReaction= aSim.molecularReactionList[anAgent.activeMolecularReactions.get(reactionIndex)];
				aReaction.numOccurence++;
//				if(ExtraMath.getUniRand()>aReaction.rate)
//					continue;

				if(aReaction.type.equalsIgnoreCase("sensing"))
				{
					loc=((LocatedAgent)anAgent)._location;
					double divider=aSim.agentGrid.getResolution();
					if(aSim.getSolute(aReaction.correspondningSolute).grid[(int)(loc.x/divider)][(int)(loc.y/divider)][(int)(loc.z/divider)]>aReaction.sensingThreshold)
					{
						molecule=anAgent.molecules.get(aReaction.player);
						molecule.addInstance(molecule.defaultInitialValue);
					}
					//continue;
				}

				else if(aReaction.type.equalsIgnoreCase("signalling"))
				{
					loc=((LocatedAgent)anAgent)._location;
					double divider=aSim.agentGrid.getResolution();
					molecule=anAgent.molecules.get(aReaction.player);
					if(molecule==null)
					{
						throw new RuntimeException("Unknown reaction player: "+ aReaction.player);
					}
					if(molecule.instances.size()>0)
						aSim.getSolute(aReaction.correspondningSolute).grid[(int)(loc.x/divider+1)][(int)(loc.y/divider+1)][(int)(loc.z/divider)+1]+=aReaction.signallingRate;//This needs justification
					//continue;
				}
				else if(aReaction.type.equalsIgnoreCase("compartmentRemoval"))
				{
					String compartmentName=aReaction.compartment;
					Compartment comp=anAgent.compartments.get(compartmentName);
					int indexToRemove=comp.numberOfInstances==1?0:ExtraMath.getUniRandInt(0, comp.numberOfInstances);
					for(String moleculeName:comp.containedMolecules)
					{
//						if(moleculeName.startsWith("count"))
//							continue;
						anAgent.molecules.get(moleculeName).amount-=anAgent.molecules.get(moleculeName).instances.get(indexToRemove);
						if(anAgent.molecules.get(moleculeName).amount<0)
							throw new RuntimeException("amount<0 at compartmentRemoval");
						anAgent.molecules.get(moleculeName).instances.remove(indexToRemove);
						
//						if(anAgent.molecules.get(moleculeName).amount!=summation(anAgent.molecules.get(moleculeName).instances))
//							throw new RuntimeException("Unequality :( ");
					}
					comp.instancesName.remove(indexToRemove);
					//Make Sure it is working correctly
					comp.numberOfInstances--;
				}
				else if(aReaction.type.equalsIgnoreCase("compartmentDivision"))
				{
					String compartmentName=aReaction.compartment;
					Compartment comp=anAgent.compartments.get(compartmentName);
					int indexToDivide=ExtraMath.getUniRandInt(0, comp.numberOfInstances);
					double amount, portion;
					for(String moleculeName:comp.containedMolecules)
					{
//						if(moleculeName.startsWith("count"))
//							continue;
						portion=ExtraMath.getUniRand(0.40, 0.60);
						amount=anAgent.molecules.get(moleculeName).instances.get(indexToDivide);
						int newAmount=(int)(amount*(1.0-portion));
						anAgent.molecules.get(moleculeName).instances.set(indexToDivide,newAmount);
						anAgent.molecules.get(moleculeName).instances.add((int)amount-newAmount);
					}
					//Make Sure it is working correctly
					comp.numberOfInstances++;
					comp.createdInstancesCounter++;
					comp.instancesName.add(compartmentName+comp.createdInstancesCounter);
					if(aReaction.productDelay>0)
						throw new RuntimeException("Delay in compartment division is not supported");
				}
				
//				if(!checkFeasibility(aReaction, anAgent))
//					continue;

				if(!aReaction.type.equalsIgnoreCase("sensing") && !aReaction.type.equalsIgnoreCase("signalling"))
					changeAmounts(aReaction, anAgent, ReactantSide);
				updateTaus(reactionIndex, anAgent, ReactantSide);
		
				//changeAmounts(aReaction, anAgent, ProductSide);
				if(aReaction._productMolecules.length>0 && !aReaction.type.equalsIgnoreCase("sensing"))
					addToQueue(anAgent, new ReactionProductFiringEvent(timer+aReaction.productDelay, anAgent, reactionIndex));
				
				if (timer-lastOutputTime>= outputPeriod)
					createMolecularReport(anAgent, timer);
			}
		}
		globalNextOutputTime=nextOutputTime;
		globalNextWritingTime=nextWritingTime;
		updateMolecularRegulatorsConcentration();
	}
	
	
	private void updateMolecularRegulatorsConcentration() {
		double resolution;
		int x,y,z;
		for(String key:aSim.molecularKineticRegulators.keySet())
		{
			aSim.molecularKineticRegulators.put(key, new double[aSim.world.domainList.get(0)._nI]
					[aSim.world.domainList.get(0)._nJ][aSim.world.domainList.get(0)._nK]);
		}
		ListIterator<SpecialisedAgent> agentIter;
		ActiveAgent anAgent;
		for (agentIter = aSim.agentGrid.agentList.listIterator(); agentIter.hasNext();) 
		{
			anAgent = (ActiveAgent) agentIter.next();
			for(String moleculeName:anAgent.molecules.keySet())
			{
				if(aSim.molecularKineticRegulators.containsKey(moleculeName))
				{
					resolution=aSim.world.domainList.get(0)._resolution;
					x=(int)(((LocatedAgent)anAgent)._location.x/resolution);
					y=(int)(((LocatedAgent)anAgent)._location.y/resolution);
					z=(int)(((LocatedAgent)anAgent)._location.z/resolution);
					aSim.molecularKineticRegulators.get(moleculeName)[x][y][z]+=anAgent.molecules.get(moleculeName).amount;
				}
			}
		}
	}

	private double getNextProductQueueFireTime(ActiveAgent anAgent) {
		if(anAgent.reactionProductQueue.size()==0)
			return Double.POSITIVE_INFINITY;
		else
			return anAgent.reactionProductQueue.get(0).fireTime;
	}

	private void createMolecularReport(ActiveAgent anAgent, double timer) {
		Compartment comp;
		lastOutputTime=timer;
		File F;
		FileWriter fw=null;
		StringBuffer str;
		for(String compartmentName:anAgent.compartments.keySet())
		{
			comp=anAgent.compartments.get(compartmentName);
			
			for(int i=0;i<comp.numberOfInstances;i++)
			{
				try {
					String fileName=aSim.getResultPath() + File.separator + "Molecules" + File.separator+ anAgent.getSpecies().speciesName+"_"+ anAgent._birthId+"_"+ comp.instancesName.get(i)+".txt";
					//F= new File(fileName);
					if(!fileContents.containsKey(fileName))
					{
						str=createHeaderFile(comp, anAgent);
					}
					else
					{
						str=fileContents.get(fileName);
					}
					str.append(String.valueOf(Math.round(timer*10)/10.0));
					//	fw= new FileWriter(fileName, true);
					
					for(String moleculeName:comp.containedMolecules)
					{	
						str.append("\t"+anAgent.molecules.get(moleculeName).instances.get(i));
					}
					if(comp.isMainCompartment)
						for(String containedCompartmentName:anAgent.compartments.keySet())
						{
							Compartment containedComp=anAgent.compartments.get(containedCompartmentName);
							if(!containedComp.isEssential)
								str.append("\t"+containedComp.numberOfInstances);
						}
					str.append("\n");
					fileContents.put(fileName, str);
					if(timer>nextWritingTime)
					{
						fw= new FileWriter(fileName, true);
						fw.write(str.toString());
						fileContents.get(fileName).setLength(0);
						fw.close();
						nextWritingTime+=writingPeriod;
					}
					//fw.write(str+"\n");
					//fw.close();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		nextOutputTime+=outputPeriod;		

	}

	public void writeMolecularReport()
	{
		for(String fileName:fileContents.keySet())
		{
			try {
				FileWriter fw= new FileWriter(fileName, true);
				fw.write(fileContents.get(fileName).toString());
				fileContents.get(fileName).setLength(0);
				fw.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	private StringBuffer createHeaderFile(Compartment comp, ActiveAgent anAgent) {
		StringBuffer str=new StringBuffer();
		str.append("time");
		for(String moleculeName:comp.containedMolecules)
		{	
			str.append("\t"+ moleculeName.split("@")[0]);
		}
		if(comp.isMainCompartment)
			for(String containedCompartmentName:anAgent.compartments.keySet())
			{
				Compartment containedComp=anAgent.compartments.get(containedCompartmentName);
				if(!containedComp.isEssential)
					str.append("\tcount@"+containedCompartmentName);
			}
		str.append("\n");
		return str;
	}

	private void updateTaus(int reactionIndex, ActiveAgent anAgent, int reactionSide) {
		
		double oldA, depTau;
		MolecularReaction depReac;
		LinkedList<Integer> dependants= reactionSide==ReactantSide?anAgent.reactionDependencyGraph.getDependentsOnReaction(reactionIndex): 
			anAgent.reactionDependencyGraph.getDependentsOnProduct(reactionIndex);
		for(int dep_index:dependants)
		{
			oldA=anAgent.a[dep_index];
			depReac= aSim.molecularReactionList[anAgent.activeMolecularReactions.get(dep_index)];
			anAgent.a[dep_index]=calculatePropensity(depReac, anAgent);
			if(anAgent.a[dep_index]==0)
			{
				depTau=Double.POSITIVE_INFINITY;
				if(oldA>0)
				{
					anAgent.lastNonZeroA[dep_index]=(dep_index==reactionIndex?0: oldA);
					anAgent.lastNonInfTau[dep_index]= anAgent.taus[dep_index]-timer;
				}
			}
			else
			{
				if(oldA==0 && anAgent.a[dep_index]>0)
				{
					if(anAgent.lastNonZeroA[dep_index]==0)
						depTau= exponential.nextDouble(anAgent.a[dep_index])+timer;
					else
						depTau= (anAgent.lastNonZeroA[dep_index]/anAgent.a[dep_index])
						*anAgent.lastNonInfTau[dep_index]+timer;
				}
				else
				{
					if(dep_index!=reactionIndex)
						depTau=(oldA/anAgent.a[dep_index])*(anAgent.taus[dep_index]-timer)+timer;
					else
						depTau=exponential.nextDouble(anAgent.a[dep_index])+timer;
				}
			}
			anAgent.taus[dep_index]=depTau;
			anAgent.tauQueue.update(dep_index, depTau);
		}
	}

	private void addToQueue(ActiveAgent anAgent,ReactionProductFiringEvent reactionProductFiringEvent) {
		for(int i=0;i<anAgent.reactionProductQueue.size();i++)
		{
			if(anAgent.reactionProductQueue.get(i).fireTime>reactionProductFiringEvent.fireTime)
			{
				anAgent.reactionProductQueue.add(i, reactionProductFiringEvent);
				return;
			}
		}
		anAgent.reactionProductQueue.add(reactionProductFiringEvent);
	}

	private void manageQueueProducts(ActiveAgent anAgent, double timer) {
		int check=0;
		while(anAgent.reactionProductQueue.size()>0 && anAgent.reactionProductQueue.get(0).fireTime<=timer) 
		 {
			 changeAmounts(aSim.molecularReactionList[anAgent.activeMolecularReactions.get(anAgent.reactionProductQueue.get(0).reactionIndex)], anAgent, ProductSide);
			 updateTaus(anAgent.reactionProductQueue.get(0).reactionIndex, anAgent, ProductSide);
			 anAgent.reactionProductQueue.remove(0);
			 check++;
		 }
		
	}

	private int nextReaction(ActiveAgent anAgent) {
		return anAgent.tauQueue.getMin();
	}

	private boolean checkFeasibility(MolecularReaction aReaction, ActiveAgent anAgent) 
	{
		Molecule molecule;
		Boolean hasEnoughMolecule, isReactionPossible=true;
		for(int i=0;i<aReaction._reactantMolecules.length;i++)
		{
			molecule=anAgent.molecules.get(aReaction._reactantMolecules[i]);
			
			if(molecule==null)
			{
				utils.LogFile.writeError("Undefined Molecule"+aReaction._reactantMolecules[i]+ " in reaction:" + aReaction.reactionName, "MolecularReactionClass");
				return false;
			}
			hasEnoughMolecule=false;		
			for(int ri=0;ri<molecule.instances.size();ri++)
			{
				if(molecule.instances.get(ri)>0)
				{
					hasEnoughMolecule=true;
					break;
				}
			}
			
			if(!hasEnoughMolecule)
			{
				isReactionPossible=false;
				break;
			}
		}
		return isReactionPossible;
	}

	protected void changeAmounts(MolecularReaction aReaction, ActiveAgent anAgent, int YieldType)
	{
		int randomIndex=0;
		Molecule molecule;
		Boolean isConsumed, isNewComplex=false;
		int yield;
		int reacSize=(YieldType==ReactantSide)? aReaction._reactantMoleculeYield.length:aReaction._productMoleculeYield.length;
		for(int i=0;i<reacSize;i++)
		{
			randomIndex=0;
			if(YieldType==ReactantSide)
			{
				isConsumed=aReaction._isMoleculeConsumed[i];
				if(!isConsumed)
					continue;
				molecule=anAgent.molecules.get(aReaction._reactantMolecules[i]);
				if(molecule.amount<=0)
					continue;
//					throw new RuntimeException("Molecule Amount equal to zero");
				yield=(int)aReaction._reactantMoleculeYield[i];
			}
			else
			{
				if(aReaction._productMolecules[i].startsWith("@"))
				{
					isNewComplex=true;
					String compartmentName=aReaction._productMolecules[i].substring(1);
					Compartment comp=anAgent.compartments.get(compartmentName);
					comp.numberOfInstances++;
					comp.createdInstancesCounter++;
					comp.instancesName.add(compartmentName+comp.createdInstancesCounter);
					for(String moleculeName:comp.containedMolecules)
					{
//						if(moleculeName.startsWith("count"))
//							continue;
						anAgent.molecules.get(moleculeName).addInstance(0);
					}
					continue;
				}
				molecule=anAgent.molecules.get(aReaction._productMolecules[i]);
				if(molecule==null)
					throw new RuntimeException("Undefined molecule: "+ aReaction._productMolecules[i]);
				yield=(int)aReaction._productMoleculeYield[i];
				if(molecule.instances.size()==0)
				{
					molecule.addInstance(yield);
					//molecule.amount+=yield;
					continue;
				}
//				if(molecule.amount!=summation(molecule.instances))
//					throw new RuntimeException("Unequality :( ");
			}
			
			if(isNewComplex)
				randomIndex=molecule.instances.size()-1;
			else if(molecule.instances.size()>1)
				randomIndex=ExtraMath.getUniRandInt(0, molecule.instances.size());
			//if the reactant instant at randomIndex has no molecule then look for the first instant that has
			
			if(YieldType==ReactantSide && molecule.instances.get(randomIndex)==0)
			{
				int newRandIndex=-1;
				for(int ri=randomIndex+1;ri<molecule.instances.size();ri++)
				{
					if(molecule.instances.get(ri)>0)
					{
						newRandIndex= ri;
						break;
					}
				}
				if(newRandIndex!=-1)
					randomIndex=newRandIndex;
				else
				{
					for(int ri=randomIndex-1;ri>=0;ri--)
					{
						if(molecule.instances.get(ri)>0)
						{
							newRandIndex= ri;
							break;
						}
					}
				}
				randomIndex=newRandIndex;
			}
			molecule.instances.set(randomIndex, molecule.instances.get(randomIndex)+ yield);
			molecule.amount+=yield;
//			if(molecule.amount!=summation(molecule.instances))
//				throw new RuntimeException("Unequality :( ");
			if (molecule.amount<-1) 
				throw new RuntimeException("Propensity < 0 **"+molecule.amount+"**"+yield);
			molecule.amount=Math.max(0, molecule.amount);
		}
	}
	

	
//	private double summation(ArrayList<Integer> instances) {
//		double result=0;
//		for(int i=0;i<instances.size();i++)
//			result+=instances.get(i);			
//		return result;
//	}

	public double calculatePropensity(MolecularReaction aReaction, ActiveAgent anAgent) {
		if(!aReaction.rateFunction.equalsIgnoreCase("regular"))
		{
			return rateFunctionEvaluator(aReaction,anAgent);
		}
		double re=aReaction.rates[0], yield, amount;
		if(aReaction.type.equalsIgnoreCase("compartmentRemoval") || aReaction.type.equalsIgnoreCase("compartmentDivision"))
		{
			aReaction.compartmentIndicesReadyForReacting.clear();
			if(aReaction.firingConditions.length==0)
			{
				for(int i=0;i<anAgent.compartments.get(aReaction.compartment).numberOfInstances;i++)
				{
					aReaction.compartmentIndicesReadyForReacting.add(i);
				}
				return re*anAgent.compartments.get(aReaction.compartment).numberOfInstances;
			}
			for(int i=0;i<re*anAgent.compartments.get(aReaction.compartment).numberOfInstances;i++)
			{
				boolean allConditionsHold=true;
				for(FiringCondition firingCondtition:aReaction.firingConditions)
				{
					if(!isReactionFiringConditionStatisfied(firingCondtition,aReaction,anAgent,i))
					{
						allConditionsHold=false;
						break;
					}
				}
				if(allConditionsHold)
					aReaction.compartmentIndicesReadyForReacting.add(i);
			}
			return re*aReaction.compartmentIndicesReadyForReacting.size();
		}
		for (int i=0; i<aReaction._reactantMolecules.length; i++) {
			yield = Math.abs(aReaction._reactantMoleculeYield[i]);
			if(aReaction._reactantMolecules[i].startsWith("@"))
				amount=anAgent.compartments.get(aReaction._reactantMolecules[i].substring(1)).numberOfInstances;
			else if(anAgent.molecules.get(aReaction._reactantMolecules[i])==null)
			{
				utils.LogFile.writeError("Undefined Molecule in reaction:" + aReaction.reactionName, "MolecularReactionManager");
				throw new RuntimeException("Undefined Molecule "+aReaction._reactantMolecules[i]+ " in reaction:" + aReaction.reactionName);
			}
			else
				amount= anAgent.molecules.get(aReaction._reactantMolecules[i]).amount;
			if(yield>amount)
				re=0;
			re*= Math.pow(amount,yield);
			re/=ExtraMath.factorial((int)yield);
		}
		return Math.abs(re);
	}
	private double rateFunctionEvaluator(MolecularReaction aReaction, ActiveAgent anAgent) {
		//Rate functions in this method are specifically implemented for peroxisome project and can 
		// be disregarded in other projects 

		if(aReaction.rateFunction.equalsIgnoreCase("OleateImport") || aReaction.rateFunction.equalsIgnoreCase("R_OleateImport"))
		{
			double E1=aReaction.rates[0];
			double Km1=aReaction.rates[1];
			double E2=aReaction.rates[2];
			double Km2=aReaction.rates[3];
			double v1meas=aReaction.rates[4];
			double Oexmeas=aReaction.rates[5];
			double r=aReaction.rates[6];
			double Oex= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
			double Oic= aReaction.rateFunction.equalsIgnoreCase("R_OleateImport")?anAgent.molecules.get(aReaction._reactantMolecules[1]).amount:
				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
			
			double k1=v1meas*Km1/(E1*Oexmeas);
			double rate1= k1*E1*(Oex-Oic)/(Km1+Oex+Oic);
			double k2=k1*E1*(1-r)/(E2*(1+r));
			double rate2= k2*E2*(Oic)/(Km2+Oic);
			return aReaction.rateFunction.equalsIgnoreCase("OleateImport")?Math.max(0,rate1-rate2):Math.max(0,rate2-rate1);
		}
		else if(aReaction.rateFunction.equalsIgnoreCase("fractionalDirect") || aReaction.rateFunction.equalsIgnoreCase("R_fractionalDirect"))
		{//ADR1, OAF1, OAF3
			double r_xx=aReaction.rates[0], r_gx=aReaction.rates[1], r_ox=aReaction.rates[2];
			double K_ms=aReaction.rates[3], max_transcription=aReaction.rates[4];
			double degradationRate=aReaction.rates[5];
			
			double O_ic= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
			double product=aReaction.rateFunction.equalsIgnoreCase("R_fractionalDirect")?anAgent.molecules.get(aReaction._reactantMolecules[1]).amount:
				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
			
			
			double S=O_ic/(K_ms+O_ic);
			double forwardRate=(r_gx+ S*(r_ox-r_gx))/r_xx;
			forwardRate*=max_transcription;
			
			double reverseRate=product*degradationRate;
			double rate= aReaction.rateFunction.equalsIgnoreCase("R_fractionalDirect")?Math.max(reverseRate-forwardRate, 0):Math.max(forwardRate-reverseRate, 0);
			return rate;
		}
		else if(aReaction.rateFunction.equalsIgnoreCase("fractional1A1R") || aReaction.rateFunction.equalsIgnoreCase("R_fractional1A1R"))
		{// POT1
			
			double epsilon=aReaction.rates[0], A=aReaction.rates[1], K1=aReaction.rates[2];
			double K2=aReaction.rates[3],max_transcription=aReaction.rates[4];
			
			double K_do=aReaction.rates[5], oaf1_minActivity=aReaction.rates[6];
			double K_dy=aReaction.rates[7], oaf3_minActivity=aReaction.rates[8];
			double K_dh=aReaction.rates[9], Q=aReaction.rates[10];
			double degradationRate=aReaction.rates[11];
			
			double oaf1p=anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
			double pip2p=anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
			double oaf3p=anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
			double oleate=anAgent.molecules.get(aReaction._reactantMolecules[3]).amount;
			double product=aReaction.rateFunction.equalsIgnoreCase("R_fractional1A1R")?anAgent.molecules.get(aReaction._reactantMolecules[4]).amount:
				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
						
			//protein activation
			double oaf1pa= oaf1p*(oaf1_minActivity+(1-oaf1_minActivity)*oleate/(K_do+oleate));
			double oaf3pa= oaf3p*(oaf3_minActivity+(1-oaf3_minActivity)*oleate/(K_dy+oleate));
			
			
			//heterodimerization
			double kq=K_dh/Q;
			double x1x2_kq = 1+(oaf1pa+pip2p)/kq;
			double h =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*oaf1pa*pip2p/(kq*kq)));
			

			
			double x1= h;
			double x2= oaf3pa;
			
			double forwardRate= (A*epsilon+x1/K1)/(A+x1/K1+x2/K2+(x1*x2)/(K1*K2));
			forwardRate*=max_transcription;
			
			double reverseRate=product*degradationRate;
			double rate= aReaction.rateFunction.equalsIgnoreCase("R_fractional1A1R")?Math.max(reverseRate-forwardRate, 0):Math.max(forwardRate-reverseRate, 0);
			return rate;
		}
		
		else if(aReaction.rateFunction.equalsIgnoreCase("pot1m"))
		{// pot1 test
			
			double epsilon=aReaction.rates[0], A=aReaction.rates[1], K1=aReaction.rates[2];
			double K2=aReaction.rates[3],max_transcription=aReaction.rates[4];
			
			double K_do=aReaction.rates[5], oaf1_minActivity=aReaction.rates[6];
			double K_dy=aReaction.rates[7], oaf3_minActivity=aReaction.rates[8];
			double K_dh=aReaction.rates[9], Q=aReaction.rates[10];
			double degradationRate=aReaction.rates[11];
			
			double oaf1p=anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
			double pip2p=anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
			double oaf3p=anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
			double oleate=anAgent.molecules.get(aReaction._reactantMolecules[3]).amount;
			double product=aReaction.rateFunction.equalsIgnoreCase("R_fractional1A1R")?anAgent.molecules.get(aReaction._reactantMolecules[4]).amount:
				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
						
			//protein activation
			double oaf1pa= oaf1p*(oaf1_minActivity+(1-oaf1_minActivity)*oleate/(K_do+oleate));
			double oaf3pa= oaf3p*(oaf3_minActivity+(1-oaf3_minActivity)*oleate/(K_dy+oleate));
			
			
			//heterodimerization
			double kq=K_dh/Q;
			double x1x2_kq = 1+(oaf1pa+pip2p)/kq;
			double h =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*oaf1pa*pip2p/(kq*kq)));
			

			
			double x1= h;
			double x2= oaf3pa;
			
			double forwardRate= (A*epsilon+x1/K1)/(A+x1/K1+x2/K2+(x1*x2)/(K1*K2));
			forwardRate*=max_transcription;
			
//			double reverseRate=product*degradationRate;
//			double rate= aReaction.rateFunction.equalsIgnoreCase("R_fractional1A1R")?Math.max(reverseRate-forwardRate, 0):Math.max(forwardRate-reverseRate, 0);
			return forwardRate;
		}
		
		else if(aReaction.rateFunction.equalsIgnoreCase("cta1m") || aReaction.rateFunction.equalsIgnoreCase("pmpm") || 
				aReaction.rateFunction.equalsIgnoreCase("pex11m") || aReaction.rateFunction.equalsIgnoreCase("pex19m"))
		{ //For CTA1, PIP2
			
			double epsilon=aReaction.rates[0], A=aReaction.rates[1], q=aReaction.rates[2];
			double K1=aReaction.rates[3],K2=aReaction.rates[4],K3=aReaction.rates[5], max_transcription=aReaction.rates[6];
			
			
			double K_do=aReaction.rates[7], oaf1_minActivity=aReaction.rates[8];
			double K_dy=aReaction.rates[9], oaf3_minActivity=aReaction.rates[10];
			double K_Ma=aReaction.rates[11], adr1_minActivity=aReaction.rates[12];
			double K_dh=aReaction.rates[13], Q=aReaction.rates[14];
			double degradationRate=aReaction.rates[15];
			
			double oaf1p=anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
			double pip2p=anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
			double oaf3p=anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
			double adr1p=anAgent.molecules.get(aReaction._reactantMolecules[3]).amount;
			double oleate=anAgent.molecules.get(aReaction._reactantMolecules[4]).amount;
			double product=aReaction.rateFunction.equalsIgnoreCase("R_fractional2A1R")?anAgent.molecules.get(aReaction._reactantMolecules[5]).amount:
				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
						
			//protein activation
			double oaf1pa= oaf1p*(oaf1_minActivity+(1-oaf1_minActivity)*oleate/(K_do+oleate));
			double oaf3pa= oaf3p*(oaf3_minActivity+(1-oaf3_minActivity)*oleate/(K_dy+oleate));
			double adr1pa= adr1p*(adr1_minActivity+(1-adr1_minActivity)*oleate/(K_Ma+oleate));
			
			//heterodimerization
			double kq=K_dh/Q;
			double x1x2_kq = 1+(oaf1pa+pip2p)/kq;
			double h =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*oaf1pa*pip2p/(kq*kq)));
			
			double x1= h;
			double x2= adr1pa;
			double x3= oaf3pa;

			
			double forwardRate = (epsilon*A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2))/(A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2)+x3/K3+x1*x3/(K1*K3)+
					x2*x3/(K2*K3)+x1*x2*x3/(K1*K2*K3));
			forwardRate*=max_transcription;
			
//			double reverseRate=product*degradationRate;
//			double rate= aReaction.rateFunction.equalsIgnoreCase("R_fractional2A1R")?Math.max(reverseRate-forwardRate, 0):Math.max(forwardRate-reverseRate, 0);
			return forwardRate;
		}
		
		else if(aReaction.rateFunction.equalsIgnoreCase("fractional2A1R") || aReaction.rateFunction.equalsIgnoreCase("R_fractional2A1R"))
		{ //For CTA1, PIP2
			
			double epsilon=aReaction.rates[0], A=aReaction.rates[1], q=aReaction.rates[2];
			double K1=aReaction.rates[3],K2=aReaction.rates[4],K3=aReaction.rates[5], max_transcription=aReaction.rates[6];
			
			
			double K_do=aReaction.rates[7], oaf1_minActivity=aReaction.rates[8];
			double K_dy=aReaction.rates[9], oaf3_minActivity=aReaction.rates[10];
			double K_Ma=aReaction.rates[11], adr1_minActivity=aReaction.rates[12];
			double K_dh=aReaction.rates[13], Q=aReaction.rates[14];
			double degradationRate=aReaction.rates[15];
			
			double oaf1p=anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
			double pip2p=anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
			double oaf3p=anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
			double adr1p=anAgent.molecules.get(aReaction._reactantMolecules[3]).amount;
			double oleate=anAgent.molecules.get(aReaction._reactantMolecules[4]).amount;
			double product=aReaction.rateFunction.equalsIgnoreCase("R_fractional2A1R")?anAgent.molecules.get(aReaction._reactantMolecules[5]).amount:
				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
						
			//protein activation
			double oaf1pa= oaf1p*(oaf1_minActivity+(1-oaf1_minActivity)*oleate/(K_do+oleate));
			double oaf3pa= oaf3p*(oaf3_minActivity+(1-oaf3_minActivity)*oleate/(K_dy+oleate));
			double adr1pa= adr1p*(adr1_minActivity+(1-adr1_minActivity)*oleate/(K_Ma+oleate));
			
			//heterodimerization
			double kq=K_dh/Q;
			double x1x2_kq = 1+(oaf1pa+pip2p)/kq;
			double h =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*oaf1pa*pip2p/(kq*kq)));
			
			double x1= h;
			double x2= adr1pa;
			double x3= oaf3pa;

			
			double forwardRate = (epsilon*A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2))/(A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2)+x3/K3+x1*x3/(K1*K3)+
					x2*x3/(K2*K3)+x1*x2*x3/(K1*K2*K3));
			forwardRate*=max_transcription;
			
			double reverseRate=product*degradationRate;
			double rate= aReaction.rateFunction.equalsIgnoreCase("R_fractional2A1R")?Math.max(reverseRate-forwardRate, 0):Math.max(forwardRate-reverseRate, 0);
			return rate;
		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("protein") || aReaction.rateFunction.equalsIgnoreCase("R_protein"))
//		{
//			double translationRate=aReaction.rates[0];
//			double degradationRate=aReaction.rates[1];
//			double mrna= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double protein= aReaction.rateFunction.equalsIgnoreCase("R_protein")?anAgent.molecules.get(aReaction._reactantMolecules[1]).amount:
//				anAgent.molecules.get(aReaction._productMolecules[0]).amount;
//			
//			double rate=translationRate*mrna-protein*degradationRate;
//			return aReaction.rateFunction.equalsIgnoreCase("protein")?Math.max(0,rate):Math.max(0,-rate);
//		}
		
		else
			throw new RuntimeException("Unimplemented rate functions: "+ aReaction.rateFunction);
			
	}
	
//	private double rateFunctionEvaluator(MolecularReaction aReaction, ActiveAgent anAgent) {
//		//Rate functions in this method are specifically implemented for peroxisome project and can 
//		// be disregarded in other projects 
//		if(aReaction.rateFunction.equalsIgnoreCase("MichaelisMenten"))
//		{
//			double K=aReaction.rates[0];
//			double minActivity=aReaction.rates[1];
//			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double x2= anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			return x1*(minActivity+(1-minActivity)*x2/(K+x2));
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("OleateImport"))
//		{
//			double E1=aReaction.rates[0];
//			double Km1=aReaction.rates[1];
//			double E2=aReaction.rates[2];
//			double Km2=aReaction.rates[3];
//			double v1meas=aReaction.rates[4];
//			double Oexmeas=aReaction.rates[5];
//			double r=aReaction.rates[6];
//			double Oex= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double Oic= anAgent.molecules.get(aReaction._productMolecules[0]).amount;
//			
//			double k1=v1meas*Km1/(E1*Oexmeas);
//			double rate1= k1*E1*(Oex-Oic)/(Km1+Oex+Oic);
//			double k2=k1*E1*(1-r)/(E2*(1+r));
//			double rate2= k2*E2*(Oic)/(Km2+Oic);
//			return Math.max(0,rate1-rate2);
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("OleateUtilization"))
//		{
//			double E1=aReaction.rates[0];
//			double Km1=aReaction.rates[1];
//			double E2=aReaction.rates[2];
//			double Km2=aReaction.rates[3];
//			double v1meas=aReaction.rates[4];
//			double Oexmeas=aReaction.rates[5];
//			double r=aReaction.rates[6];
//			
//			double Oic= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			//double Oex= anAgent.molecules.get(aReaction._productMolecules[1]).amount;
//			
//			double k1=v1meas*Km1/(E1*Oexmeas);
//			double k2=k1*E1*(1-r)/(E2*(1+r));
//			double rate= k2*E2*(Oic)/(Km2+Oic);
//			return rate;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("sshdimer"))
//		{
//			double K=aReaction.rates[0];
//			double Q=aReaction.rates[1];
//			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double x2= anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			double kq=K/Q;
//			double x1x2_kq = 1+(x1+x2)/kq;
//			double rate =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*x1*x2/(kq*kq)));
//			return rate;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("fractionalDirect"))
//		{//ADR1, OAF1, OAF3
//			double r_xx=aReaction.rates[0], r_gx=aReaction.rates[1], r_ox=aReaction.rates[2];
//			double K_ms=aReaction.rates[3], max_transcription=aReaction.rates[4];
////			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
////			if(x1==0)
////				return 0;
//			double O_ic= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double S=O_ic/(K_ms+O_ic);
//			double rate=(r_gx+ S*(r_ox-r_gx))/r_xx;
//			return rate*max_transcription;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("fractional1A1R"))
//		{// POT1
//			
//			double epsilon=aReaction.rates[0], A=aReaction.rates[1], K1=aReaction.rates[2];
//			double K2=aReaction.rates[3],max_transcription=aReaction.rates[4];
//			
//			double K_do=aReaction.rates[5], oaf1_minActivity=aReaction.rates[6];
//			double K_dy=aReaction.rates[7], oaf3_minActivity=aReaction.rates[8];
//			double K_dh=aReaction.rates[9], Q=aReaction.rates[10];
//			
//			double oaf1p=anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double pip2p=anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			double oaf3p=anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
//			double oleate=anAgent.molecules.get(aReaction._reactantMolecules[3]).amount;
//			
//						
//			//protein activation
//			double oaf1pa= oaf1p*(oaf1_minActivity+(1-oaf1_minActivity)*oleate/(K_do+oleate));
//			double oaf3pa= oaf3p*(oaf3_minActivity+(1-oaf3_minActivity)*oleate/(K_dy+oleate));
//			
//			//heterodimerization
//			double kq=K_dh/Q;
//			double x1x2_kq = 1+(oaf1pa+pip2p)/kq;
//			double h =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*oaf1p*pip2p/(kq*kq)));
//			
//
//			
//			double x1= h;
//			double x2= oaf3pa;
//			
//			double rate= (A*epsilon+x1/K1)/(A+x1/K1+x2/K2+(x1*x2)/(K1*K2));
//			return rate*max_transcription;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("fractional2A1R"))
//		{ //For CTA1, PIP2
//			
//			double epsilon=aReaction.rates[0], A=aReaction.rates[1], q=aReaction.rates[2];
//			double K1=aReaction.rates[3],K2=aReaction.rates[4],K3=aReaction.rates[5], max_transcription=aReaction.rates[6];
//			
//			
//			double K_do=aReaction.rates[7], oaf1_minActivity=aReaction.rates[8];
//			double K_dy=aReaction.rates[9], oaf3_minActivity=aReaction.rates[10];
//			double K_Ma=aReaction.rates[11], adr1_minActivity=aReaction.rates[12];
//			double K_dh=aReaction.rates[13], Q=aReaction.rates[14];
//			
//			double oaf1p=anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double pip2p=anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			double oaf3p=anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
//			double adr1p=anAgent.molecules.get(aReaction._reactantMolecules[3]).amount;
//			double oleate=anAgent.molecules.get(aReaction._reactantMolecules[4]).amount;
//			
//						
//			//protein activation
//			double oaf1pa= oaf1p*(oaf1_minActivity+(1-oaf1_minActivity)*oleate/(K_do+oleate));
//			double oaf3pa= oaf3p*(oaf3_minActivity+(1-oaf3_minActivity)*oleate/(K_dy+oleate));
//			double adr1pa= adr1p*(adr1_minActivity+(1-adr1_minActivity)*oleate/(K_Ma+oleate));
//			
//			//heterodimerization
//			double kq=K_dh/Q;
//			double x1x2_kq = 1+(oaf1pa+pip2p)/kq;
//			double h =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*oaf1p*pip2p/(kq*kq)));
//			
//			double x1= h;
//			double x2= adr1pa;
//			double x3= oaf3pa;
//
//			
//			double rate = (epsilon*A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2))/(A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2)+x3/K3+x1*x3/(K1*K3)+
//					x2*x3/(K2*K3)+x1*x2*x3/(K1*K2*K3));
//			return rate*max_transcription;
//		}
//		else
//			throw new RuntimeException("Unimplemented rate functions: "+ aReaction.rateFunction);
//			
//	}

//	private double rateFunctionEvaluator(MolecularReaction aReaction, ActiveAgent anAgent) {
//		//Rate functions in this method are specifically implemented for peroxisome project and can 
//		// be disregarded in other projects 
//		if(aReaction.rateFunction.equalsIgnoreCase("MichaelisMenten"))
//		{
//			double K=aReaction.rates[0];
//			double minActivity=aReaction.rates[1];
//			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double x2= anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			return x1*(minActivity+(1-minActivity)*x2/(K+x2));
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("OleateImport"))
//		{
//			double E1=aReaction.rates[0];
//			double Km1=aReaction.rates[1];
//			double E2=aReaction.rates[2];
//			double Km2=aReaction.rates[3];
//			double v1meas=aReaction.rates[4];
//			double Oexmeas=aReaction.rates[5];
//			double r=aReaction.rates[6];
//			double Oex= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double Oic= anAgent.molecules.get(aReaction._productMolecules[0]).amount;
//			
//			double k1=v1meas*Km1/(E1*Oexmeas);
//			double rate1= k1*E1*(Oex-Oic)/(Km1+Oex+Oic);
//			double k2=k1*E1*(1-r)/(E2*(1+r));
//			double rate2= k2*E2*(Oic)/(Km2+Oic);
//			return Math.max(0,rate1-rate2);
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("OleateUtilization"))
//		{
//			double E1=aReaction.rates[0];
//			double Km1=aReaction.rates[1];
//			double E2=aReaction.rates[2];
//			double Km2=aReaction.rates[3];
//			double v1meas=aReaction.rates[4];
//			double Oexmeas=aReaction.rates[5];
//			double r=aReaction.rates[6];
//			
//			double Oic= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			//double Oex= anAgent.molecules.get(aReaction._productMolecules[1]).amount;
//			
//			double k1=v1meas*Km1/(E1*Oexmeas);
//			double k2=k1*E1*(1-r)/(E2*(1+r));
//			double rate= k2*E2*(Oic)/(Km2+Oic);
//			return rate;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("sshdimer"))
//		{
//			double K=aReaction.rates[0];
//			double Q=aReaction.rates[1];
//			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double x2= anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			double kq=K/Q;
//			double x1x2_kq = 1+(x1+x2)/kq;
//			double rate =0.5 * kq * (x1x2_kq - Math.sqrt(x1x2_kq*x1x2_kq - 4*x1*x2/(kq*kq)));
//			return rate;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("fractionalDirect"))
//		{//ADR1, OAF1, OAF3
//			double r_xx=aReaction.rates[0], r_gx=aReaction.rates[1], r_ox=aReaction.rates[2];
//			double K_ms=aReaction.rates[3], max_transcription=aReaction.rates[4];
////			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
////			if(x1==0)
////				return 0;
//			double O_ic= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double S=O_ic/(K_ms+O_ic);
//			double rate=(r_gx+ S*(r_ox-r_gx))/r_xx;
//			return rate*max_transcription;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("fractional1A1R"))
//		{// POT1
//			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double x2= anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			double epsilon=aReaction.rates[0], A=aReaction.rates[1], K1=aReaction.rates[2];
//			double K2=aReaction.rates[3],max_transcription=aReaction.rates[4];
//			double rate= (A*epsilon+x1/K1)/(A+x1/K1+x2/K2+(x1*x2)/(K1*K2));
//			return rate*max_transcription;
//		}
//		else if(aReaction.rateFunction.equalsIgnoreCase("fractional2A1R"))
//		{ //For CTA1, PIP2
//			
//			double x1= anAgent.molecules.get(aReaction._reactantMolecules[0]).amount;
//			double x2= anAgent.molecules.get(aReaction._reactantMolecules[1]).amount;
//			double x3= anAgent.molecules.get(aReaction._reactantMolecules[2]).amount;
//
//			double epsilon=aReaction.rates[0], A=aReaction.rates[1], q=aReaction.rates[2];
//			double K1=aReaction.rates[3],K2=aReaction.rates[4],K3=aReaction.rates[5], max_transcription=aReaction.rates[6];
//			
//			double rate = (epsilon*A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2))/(A+x1/K1+x2/K2+(q*x1*x2)/(K1*K2)+x3/K3+x1*x3/(K1*K3)+
//					x2*x3/(K2*K3)+x1*x2*x3/(K1*K2*K3));
//			return rate*max_transcription;
//		}
//		else
//			throw new RuntimeException("Unimplemented rate functions: "+ aReaction.rateFunction);
//			
//	}

	private boolean isReactionFiringConditionStatisfied(FiringCondition firingCondition, MolecularReaction aReaction, ActiveAgent anAgent, int instanceIndex) {
		if(!firingCondition.conditionType.equalsIgnoreCase("amount"))
			throw new RuntimeException("Unimplemented condition type: "+firingCondition.conditionType);
		if(firingCondition.conditionMeasure.equalsIgnoreCase("greaterThan"))
		{
			if(anAgent.molecules.get(firingCondition.conditionDecider)==null)
				throw new RuntimeException("Undefined conditionDecider: "+firingCondition.conditionDecider);
			if(anAgent.molecules.get(firingCondition.conditionDecider).instances.get(instanceIndex)>firingCondition.conditionValue1)
				return true;
			else
				return false;
		}
		else
		{
			if(anAgent.molecules.get(firingCondition.conditionDecider)==null)
				throw new RuntimeException("Undefined conditionDecider: "+firingCondition.conditionDecider);
			if(anAgent.molecules.get(firingCondition.conditionDecider).amount<firingCondition.conditionValue1)
				return true;
			else
				return false;
			
		}
	}

	public String[] getCompartmentMoleculeNames(ActiveAgent anAgent, String key) {
		String[] result= new String[anAgent.compartments.get(key).containedMolecules.size()];
		for(int i=0;i<anAgent.compartments.get(key).containedMolecules.size();i++)
			result[i]=anAgent.compartments.get(key).containedMolecules.get(i);
		return result;
	}

	public void registerToCompartment(ActiveAgent anAgent, String compartment, String name) {
		if(!anAgent.compartments.containsKey(compartment))
			throw new RuntimeException("Undefined compartment: "+compartment);
		anAgent.compartments.get(compartment).containedMolecules.add(name);
	}

	public void registerCompartment(ActiveAgent anAgent, String name, int number, Boolean isEssential, Boolean isMainCompartment) {
		if(anAgent.compartments.containsKey(name))
			throw new RuntimeException("Duplicate compartment: "+name);
		anAgent.compartments.put(name, new Compartment(name,number, isEssential, isMainCompartment));
		
	}	
}
