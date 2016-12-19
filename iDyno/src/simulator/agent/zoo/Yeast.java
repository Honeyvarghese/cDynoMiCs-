/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ___________________________________________________________________________
 * Yeast: a species that can change its reaction based on local conditions, it has triple-state switches
 *
 * @version 1.1
 * @author Ahmadreza Ghaffarizadeh (ghaffarizadeh@aggiemail.usu.edu), Department of Computer Science, Utah State University (USA)
 * ____________________________________________________________________________
 * @since May 2011
 */

/**
 *
 * @since May 2011
 * @version 1.1
 * @author Ahmadreza Ghaffarizadeh (ghaffarizadeh@aggiemail.usu.edu), Department of Computer Science, Utah State University (USA)
 * ____________________________________________________________________________
 */

package simulator.agent.zoo;

import farzin.Logger;
import farzin.Variable;
import idyno.SimTimer;
import org.jdom.Element;
import simulator.Simulator;
import simulator.agent.LocatedAgent;
import simulator.agent.Species;
import simulator.geometry.ContinuousVector;
import utils.ExtraMath;
import utils.LogFile;
import utils.XMLParser;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.List;
import java.util.Scanner;

//import utils.LogFile;

public class Yeast extends BactEPS {

    protected String switchState;

    protected double ShovingInteractionRadiusMultiplier = 1.25;

    private int targetSpeciesIndex = -1;

    private       double   switchTimer          = -1;
    private       double   distance0Weight      = 1;
    private       double   distance1Weight      = 0.8;
    private       double   distance2Weight      = 0.595;
    private       double   distance3Weight      = -0.659;
    //private double bound=1E12;  Moved to Farzin.Variables
    private       double[] weights              = new double[]{distance0Weight, distance1Weight, distance2Weight, distance3Weight};
    private       double   oldTuringValue       = 1;
    public        double   newTuringValue       = 1;
    //	private int numberOfCheckedNeighborsForSkin=10;
    public static double   meanTuringValue      = 0;
    public        int      radiusToCheckForSkin = 4;
    private boolean reachedTuringSteadyState;

    public Yeast() {
        super();
        newTuringValue = ExtraMath.getUniRand();
        oldTuringValue = newTuringValue;
        _speciesParam = new YeastParam();
    }

    /**
     * Initialises the progenitor
     * (This code is borrowed from Bacterium class)
     */

    public void initFromProtocolFile(Simulator aSim, XMLParser aSpeciesRoot) {
        // Reading of the molecules/proteins
        super.initFromProtocolFile(aSim, aSpeciesRoot);
        meanTuringValue = 0;
        String withSpecies;
        double strength;
        //Reading Adhesion Parameters
        XMLParser adhesionParser = new XMLParser(aSpeciesRoot.getChildElement("adhesions"));
        if (adhesionParser.get_localRoot() != null) { //TJ Flann stop error if no adhesion specified
            for (Element adhesionMarkUp : adhesionParser.buildSetMarkUp("adhesion")) {
                XMLParser parser = new XMLParser(adhesionMarkUp);
                withSpecies = parser.getAttribute("withSpecies");
                strength = parser.getAttributeDbl("strength");
                if (_species.adhesionSpecies.get(withSpecies) == null)
                    _species.adhesionSpecies.put(withSpecies, strength);
            }
        }
        //Reading Tight Junction Parameters TJ Flann
        double    stiffness           = 0.0;
        XMLParser tightJunctionParser = new XMLParser(aSpeciesRoot.getChildElement("tightJunctions"));
        if (tightJunctionParser.get_localRoot() != null) {
            for (Element tightJunctionMarkUp : tightJunctionParser.buildSetMarkUp("tightJunction")) {
                XMLParser parser = new XMLParser(tightJunctionMarkUp);
                withSpecies = parser.getAttribute("withSpecies");
                stiffness = parser.getAttributeDbl("stiffness");
                if (_species.tightJunctionSpecies.get(withSpecies) == null)
                    _species.tightJunctionSpecies.put(withSpecies, stiffness);
            }
        }
        XMLParser switchParser = new XMLParser(aSpeciesRoot.getChildElement("entryConditions"));
        if (switchParser.get_localRoot() != null) {
            for (Element ECMarkUp : switchParser.buildSetMarkUp("entryCondition")) {
                EntryCondition ec = new EntryCondition();
                XMLParser parser = new XMLParser(ECMarkUp);
                ec.speciesIndex = speciesIndex;
                ec.switchType = parser.getAttribute("type");
                ec.switchControl = parser.getAttribute("name");


                // get whether it's lessThan or greaterThan
                ec.switchCondition = parser.getParam("switch");
                ec.fromSpecies = parser.getParam("fromSpecies");
                if (ec.fromSpecies == null)
                    ec.fromSpecies = "Any";

                // get the concentration or mass for the switch
                if (ec.switchType.equalsIgnoreCase("solute")) {
                    ec.switchValue = parser.getParamConc("concentration");
                    ec.switchControlIndex = aSim.soluteDic.indexOf(ec.switchControl);
                    if (ec.switchControlIndex == -1)
                        System.out.println("WARNING: solute " + ec.switchControl +
                                " used in the <entryConditions> markup does not exist.");
                } else if (ec.switchType.equalsIgnoreCase("biomass")) {
                    ec.switchValue = parser.getParamDbl("mass");
                    ec.switchControlIndex = aSim.particleDic.indexOf(ec.switchControl);
                    if (ec.switchControlIndex == -1)
                        System.out.println("WARNING: particle " + ec.switchControl +
                                " used in the <entryConditions> markup does not exist.");
                } else if (ec.switchType.equalsIgnoreCase("aging")) {
                    ec.switchValue = parser.getParamDbl("age");
                } else if (ec.switchType.equalsIgnoreCase("timing")) {
                    ec.switchValue = parser.getParamDbl("time");
                } else if (ec.switchType.equalsIgnoreCase("distance")) {
                    ec.switchValue = parser.getParamDbl("distanceValue");
                    List<Element> area = ECMarkUp.getChildren("distanceTo");
                    ec.Center = new ContinuousVector((Element) area.get(0));
                } else if (ec.switchType.equalsIgnoreCase("location")) {
                    List<Element> area = ECMarkUp.getChildren("coordinates");
                    ec.locationCorner1 = new ContinuousVector((Element) area.get(0));
                    ec.locationCorner2 = new ContinuousVector((Element) area.get(1));
                } else if (ec.switchType.equalsIgnoreCase("random")) {
                    ec.switchValue = parser.getParamDbl("minRange");
                    ec.switchValue2 = parser.getParamDbl("maxRange");
                } else if (ec.switchType.equalsIgnoreCase("CDP")) { //Cell Death Pattern
                    if (farzin.Logger.CDPMap == null) {
                        initCDPMap(parser.getParam("CDPFile"));
                    }
                    List<Element> area = ECMarkUp.getChildren("coordinates");
                    ec.locationCorner1 = new ContinuousVector((Element) area.get(0));
                    ec.locationCorner2 = new ContinuousVector((Element) area.get(1));
                } else if (ec.switchType.equalsIgnoreCase("geometry")) {
                    //ec.switchValue = Double.parseDouble(parser.getParam(""));
                } else if (ec.switchType.equalsIgnoreCase("turingValue")) {
                    //ec.switchValue = Double.parseDouble(parser.getParam(""));
                } else if (ec.switchType.equalsIgnoreCase("soluteCompare")) {
                    ec.switchControlIndex = aSim.soluteDic.indexOf(parser.getParam("solute1"));
                    ec.switchControlIndex2 = aSim.soluteDic.indexOf(parser.getParam("solute2"));
                    if (ec.switchControlIndex == -1)
                        System.out.println("WARNING: solute " + parser.getParam("solute1") + " or " + parser.getParam("solute2") +
                                " used in the <entryConditions> markup does not exist.");
                } else
                    System.out.println("WARNING: at least one fo the entry conditions is not in the valid format.");
                //aSim.speciesList.get(speciesIndex).registerEntryCondition(ec);
                _species.entryConditions.add(ec);
            }
        }

        switchParser = new XMLParser(aSpeciesRoot.getChildElement("switchingLags"));

        if (switchParser.get_localRoot() != null) {
            for (Element sLag : switchParser.buildSetMarkUp("switchingLag")) {
                XMLParser parser = new XMLParser(sLag);
                String to = parser.getAttribute("toSpecies");
                int lagTime = (int) parser.getAttributeDbl("value");
                _species.switchingLags.put(to, lagTime);
            }
        }

        switchParser = new XMLParser(aSpeciesRoot.getChildElement("chemotaxis"));
        if (switchParser.get_localRoot() != null) {
            for (Element chemo : switchParser.buildSetMarkUp("chemotactic")) {
                XMLParser parser = new XMLParser(chemo);
                String withSolute = parser.getAttribute("withSolute");
                double chemoStrength = parser.getAttributeDbl("strength");
                int contactInhibition=0;
                if(parser.getAttribute("contactInhibition")!=null)
                 contactInhibition = Integer.parseInt(parser.getAttribute("contactInhibition"));
                int soluteIndex = aSim.soluteDic.indexOf(withSolute);
                _species.chemotaxis.put(String.valueOf(soluteIndex), chemoStrength);
                _species.contactInhibition.put(String.valueOf(soluteIndex), contactInhibition);
            }
        }
    }

    //TODO: This function (initFromResultFile in Yeast class) needs to be rewritten, check it
    public void initFromResultFile(Simulator aSim, String[] singleAgentData) {

        meanTuringValue = 0;

        // find the position to start at by using length and number of values read
        int nValsRead  = 1;
        int iDataStart = singleAgentData.length - nValsRead;

        // read in info from the result file IN THE SAME ORDER AS IT WAS OUTPUT

        // now go up the hierarchy with the rest of the data
        String[] remainingSingleAgentData = new String[iDataStart];
        for (int i = 0; i < iDataStart; i++)
            remainingSingleAgentData[i] = singleAgentData[i];
        super.initFromResultFile(aSim, remainingSingleAgentData);
    }


    public double getTuringValue() {
        return newTuringValue;
    }

    /**
     * Called at each time step (under the control of the method Step of the
     * class Agent to avoid multiple calls
     */
    protected void internalStep() {


        //added by Farzin to reflect kind of type switching in Yeast model
        int speciesIndexToChange = findTargetSpecies();
        //checking switching lag; and reset timers if needed
        if (speciesIndexToChange != targetSpeciesIndex && speciesIndexToChange != speciesIndex && speciesIndexToChange != -1) {
            targetSpeciesIndex = speciesIndexToChange;
            switchTimer = SimTimer.getCurrentTime();
        }
        if (speciesIndexToChange != targetSpeciesIndex)
            targetSpeciesIndex = -1;

        if (speciesIndexToChange != -1 && speciesIndexToChange != speciesIndex
                && SimTimer.getCurrentTime() - switchTimer >= _species.switchingLags.get(getSpeciesParam().aSim.speciesList.get(targetSpeciesIndex).speciesName))
            try {
                changeSpeciesTo(speciesIndexToChange);
            } catch (CloneNotSupportedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        //TODO: decide to have super.internalStep() before type switching or after it
        // once the reactions are set up, everything else goes as normal

        //Added by Farzin, Turing Patterns Revision 1
        if (getSpeciesParam().useActivationInhibition && getSpeciesParam().startingTimeForActivationInhibition <= SimTimer.getCurrentTime())
            updateTuringValue();
        //end Farzin


//        if(_species.canAttachToBoundary && _location.x < getShoveRadius())
//        {
//            _frozenVertical = true;
//        }
//
//        if(_frozenVertical && _location.x > getShoveRadius())
//        {
//            _frozenVertical = false;
//        }


        // TJ Flann If this type is new, then initialize its tight junctions
        //if (_newInThisType) {
        initializeTightJunctions();
        //    _newInThisType = false;
        //  }
        super.internalStep();
    }

    //Farzin Revision1
    public void activationInhibitionOperation() {
        if (!getSpeciesParam().useActivationInhibition || getSpeciesParam().startingTimeForActivationInhibition > SimTimer.getCurrentTime())
            return;

        //double newValue=Math.min(newTuringValue,bound);
        //oldTuringValue=Math.max(newValue,-bound);
        oldTuringValue = newTuringValue;

        meanTuringValue += newTuringValue;
        if (newTuringValue > 0) {
            //this._hasEps=true;
//			this.getSpeciesParam().kHyd=0.2;
        } else {
            //this._hasEps=false;
//			this.getSpeciesParam().kHyd=0.007;
        }
    }

    //End Revision1
    private void updateTuringValue() {
        if (reachedTuringSteadyState)
            return;
        double actInhEffectiveRadiusShoving = _radius * getSpeciesParam().shoveFactor < 1 ? 1 : _radius * getSpeciesParam().shoveFactor;
        getNeighborhood((_radius * actInhEffectiveRadiusShoving) * getSpeciesParam().nieighborhoodRadiusCoefficient);
        double dist;

        for (LocatedAgent neighbor : _tempNeighbors) {
            if (neighbor instanceof Yeast && ((Yeast) neighbor).getSpeciesParam().useActivationInhibition) {
                dist = ExtraMath.pointDistance(this.getLocation(), neighbor.getLocation());
                //resolution = getSpeciesParam().aSim.world.domainList.get(0)._resolution;
                double indexParam = ((dist) / (_radius * getSpeciesParam().shoveFactor));
                double floorWeight = 1 - (indexParam - Math.floor(indexParam));
                double ceilingWeight = 1 - (Math.ceil(indexParam) - indexParam);
                int floorIndex = (int) Math.floor(indexParam);
                int ceilingIndex = (int) Math.ceil(indexParam);

                if (ceilingIndex < 4)
                    newTuringValue += ((Yeast) neighbor).oldTuringValue * ceilingWeight * weights[ceilingIndex];
                if (floorIndex < 4)
                    newTuringValue += ((Yeast) neighbor).oldTuringValue * floorWeight * weights[floorIndex];
            }

        }
        if (newTuringValue > farzin.Variable.turingValuebound || newTuringValue < -farzin.Variable.turingValuebound)
            reachedTuringSteadyState = true;
        double newValue = Math.min(newTuringValue, farzin.Variable.turingValuebound);
        newTuringValue = Math.max(newValue, -farzin.Variable.turingValuebound);
//		newTuringValue/=1000;
    }

    private void changeSpeciesTo(int speciesIndexToChange) throws CloneNotSupportedException {


        if (speciesIndexToChange == speciesIndex)
            return;

        int[] tempReactionActive = new int[this.reactionActive.size()];
        int   counter            = 0;
        for (int aReac : tempReactionActive) {
            tempReactionActive[counter++] = aReac;
        }
        for (int aReac : tempReactionActive) {
            switchOffreaction(allReactions[aReac]);
        }
        //TODO: check all needed parameters
        Yeast temp = (Yeast) _speciesParam.aSim.speciesList.get(speciesIndexToChange)._progenitor.clone();
        this.speciesIndex = speciesIndexToChange;
        this._species = _speciesParam.aSim.speciesList.get(speciesIndexToChange);
        this._speciesParam = temp._speciesParam;
        this.allReactions = temp.allReactions;

        this.reactionKnown = temp.reactionKnown;
        this._netGrowthRate = temp._netGrowthRate;
        this._netVolumeRate = temp._netVolumeRate;
        this.growthRate = temp.growthRate;
        this.soluteYield = temp.soluteYield;
        this.reactionKinetic = temp.reactionKinetic;
        this.particleYield = temp.particleYield;
        this._hasEps = temp._hasEps;
        this._hasInert = temp._hasInert;
        this._epsSpecies = temp._epsSpecies;
        this.setNewInThisType(); // TJ Flann flag as just changed
        targetSpeciesIndex = -1;

        for (int aReac : temp.reactionActive) {
            switchOnReaction(allReactions[aReac]);
        }
        this.reactionActive = temp.reactionActive;

//		this.clearTightJunctionsWithNeighbors();
//		this._myTightJunctions.clear();

    }

    //Added by Farzin to find the target species we want change to
    private int findTargetSpecies() {
        int counter           = 0;
        int passedConiditions = 0;
        for (Species aSpieces : _speciesParam.aSim.speciesList) {
            boolean allTrue = true;
            passedConiditions = 0;
            for (EntryCondition ec : aSpieces.entryConditions) {
                if (aSpieces.speciesIndex != ec.speciesIndex)
                    continue;
                if (!isCorrect(ec)) {
                    allTrue = false;
                    break;
                }
                passedConiditions++;
            }
            if (allTrue && aSpieces._progenitor.getClass().getName() == "simulator.agent.zoo.Yeast" && passedConiditions > 0)
                return counter;
            counter++;
        }
        return -1;
    }


    private boolean isCorrect(EntryCondition ec) {
        double localValue = -1;
        if (!ec.fromSpecies.toLowerCase().contains(getSpecies().speciesName.toLowerCase()) && !ec.fromSpecies.toLowerCase().contains("any"))
            return false;
        if (ec.switchType.equalsIgnoreCase("solute")) {
            localValue = getSpeciesParam()._soluteList[ec.switchControlIndex].getValueAround((LocatedAgent) this);
        } else if (ec.switchType.equalsIgnoreCase("biomass")) {
            // biomass
            localValue = getParticleMass(ec.switchControlIndex);
        }
        //chris johnson
        // 4/23/2015
        //compares the carrying capacity solute to the density solute
        else if (ec.switchType.equalsIgnoreCase("soluteCompare")) {
            localValue = getSpeciesParam()._soluteList[ec.switchControlIndex].getValueAround(this);
            ec.switchValue = getSpeciesParam()._soluteList[ec.switchControlIndex2].getValueAround(this);
        }

        //Written by Qanita, added by Farzin 08/24/2012
        else if (ec.switchType.equalsIgnoreCase("random")) {
            // biomass
            localValue = 1.0d - ExtraMath.getUniRand();
            localValue = -1 * Math.log(localValue);


            localValue = ExtraMath.getUniRand();
            //System.out.println("++++++++++++++++"+localValue);
            //	localValue = getParticleMass(ec.switchControlIndex);
        } else if (ec.switchType.equalsIgnoreCase("aging")) {
            localValue = SimTimer.getCurrentTime() - this._birthday;
        } else if (ec.switchType.equalsIgnoreCase("distance")) {
            localValue = ec.Center.distance(this._location);
        } else if (ec.switchType.equalsIgnoreCase("timing")) {
            localValue = SimTimer.getCurrentTime();
            if (ec.switchCondition.equals("equalTo"))
                if (Math.abs(localValue - ec.switchValue) < 0.001)
                    return true;
        } else if (ec.switchType.equalsIgnoreCase("location")) {
            if (_location.x >= ec.locationCorner1.x && _location.y >= ec.locationCorner1.y && _location.z >= ec.locationCorner1.z &&
                    _location.x <= ec.locationCorner2.x && _location.y <= ec.locationCorner2.y && _location.z <= ec.locationCorner2.z)
                return true;
            return false;

        } else if (ec.switchType.equalsIgnoreCase("CDP")) {
            if (farzin.Logger.CDPMap == null) {
                LogFile.writeLog("File not found to use as CDPMap\n");
                return false;
            }
            int cols = Logger.CDPMap.length - 1;
            int rows = Logger.CDPMap[0].length - 1;
            double yStep = rows / Math.abs(ec.locationCorner1.y - ec.locationCorner2.y);
            double zStep = cols / Math.abs(ec.locationCorner1.z - ec.locationCorner2.z);
            int mapYCoord = (int) Math.abs(Math.floor((_location.y - ec.locationCorner1.y) * yStep));
            int mapZCoord = (int) Math.abs(Math.floor((_location.z - ec.locationCorner1.z) * zStep));

            if (_location.x >= ec.locationCorner1.x && _location.x <= ec.locationCorner2.x &&
                    _location.y >= ec.locationCorner1.y && _location.z >= ec.locationCorner1.z &&
                    _location.y <= ec.locationCorner2.y && _location.z <= ec.locationCorner2.z &&
                    farzin.Logger.CDPMap[mapZCoord][rows - mapYCoord] == 1)
                return true;
            return false;
        } else if (ec.switchType.equalsIgnoreCase("turingValue")) {
            if (ec.switchCondition.equalsIgnoreCase("Activated"))
                return newTuringValue > 0;

            return newTuringValue < 0;

        } else if (ec.switchType.equals("geometry")) {
            if (ec.switchCondition.equalsIgnoreCase("atskin")) {
                if (false) //SimTimer.getCurrentIter()<5)
                    return false;
                return atSkin || hasOpenBoundary();

            }
            if (ec.switchCondition.equalsIgnoreCase("notAtskin")) //TJ Flann
            {
                return !hasOpenBoundary();
            }
        }
        boolean isTrue =
                (localValue == ec.switchValue && ec.switchCondition.equals("equalTo")) ||
                        (localValue <= ec.switchValue && ec.switchCondition.equals("lessThan")) ||
                        (localValue > ec.switchValue && ec.switchCondition.equals("greaterThan")) ||
                        (localValue >= ec.switchValue && localValue <= ec.switchValue2 && ec.switchCondition.equals("between"));

        return isTrue;
    }

    public YeastParam getSpeciesParam() {
        return (YeastParam) _speciesParam;
    }

    /* _______________ FILE INPUT _____________________ */
    private void initCDPMap(String address) {

        Scanner input;
        try {
            File file = new File(address);

            //try result path + address
            if(!file.exists())
            {
                address = _species.currentSimulator.getResultPath() +address;
                file = new File(address);
            }

            input = new Scanner(file);

            // pre-read in the number of rows/columns
            int rows = 0;
            int columns = 0;
            Boolean colPass = false;
            while (input.hasNextLine()) {
                ++rows;
                Scanner colReader = new Scanner(input.nextLine());
                while (colReader.hasNextDouble() && !colPass) {
                    double a = colReader.nextDouble();
                    ++columns;
                }
                colPass = true;
                colReader.close();
            }

            //for padding
            //so we don't get index of out bounds error
            rows = rows + 1;
            columns = columns + 1;
            farzin.Logger.CDPMap = new double[rows][columns];
            input.close();

            // read in the data
            input = new Scanner(new File(address));
            for (int i = 0; i < rows; ++i) {
                for (int j = 0; j < columns; ++j) {

                    //we're at the padding

                    if (i == rows - 1 && j == columns - 1) {
                        farzin.Logger.CDPMap[i][j] = farzin.Logger.CDPMap[i - 1][j - 1];
                    } else if (i == rows - 1) {
                        farzin.Logger.CDPMap[i][j] = farzin.Logger.CDPMap[i - 1][j];
                    } else if (j == columns - 1) {
                        farzin.Logger.CDPMap[i][j] = farzin.Logger.CDPMap[i][j - 1];
                    }
                    else if (input.hasNextInt()) {
                        farzin.Logger.CDPMap[i][j] = input.nextDouble();
                    }
                }
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LogFile.writeLog("File not found to load CDPMap\n");
        }
    }


	/* _______________ FILE OUTPUT _____________________ */

    public String sendHeader() {
        // return the header file for this agent's values after sending those for super
        StringBuffer tempString = new StringBuffer(super.sendHeader());
        tempString.append(",");

        // switch state and timing info
        tempString.append("state");
        return tempString.toString();
    }

    public void synchroniseTuringValues() {
        oldTuringValue = newTuringValue;

    }

    public String writeOutput() {
        // write the data matching the header file
        StringBuffer tempString = new StringBuffer(super.writeOutput());
        //tempString.append(",");

        // switch state
        tempString.append("1");

        return tempString.toString();
    }

}
