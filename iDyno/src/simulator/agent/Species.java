/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ______________________________________________________
 * top-level class of the simulation core. It is used to create and run a
 * simulation; this class is called by the class Idynomics.
 *
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 * @since June 2006
 */

/**
 *
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Sónia Martins (SCM808@bham.ac.uk), Centre for Systems Biology, University of Birmingham (UK)
 *
 */


//Modified by Farzin, replaced specializedAgent with Yeast
package simulator.agent;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Random;
import java.io.Serializable;
import java.awt.Color;

import org.jdom.Element;

import simulator.Simulator;
import simulator.agent.zoo.EntryCondition;
import simulator.agent.zoo.Yeast;
import simulator.geometry.*;
import utils.LogFile;
import utils.XMLParser;
import utils.ExtraMath;


public class Species implements Serializable {

    // Serial version used for the serialisation of the class
    private static final long serialVersionUID = 1L;

    public Simulator currentSimulator;
    public String speciesName;
    public int speciesIndex;
    public Color color;
    public boolean isVisible = true;
    //public boolean 			   canAttachToBoundary;
    // Computation domain of the species
    public Domain domain;

    public SpecialisedAgent _progenitor;
    protected int _population = 0;


    // FLANN
    // each located object has a mapping from species to its potential adhesive strength
    public Hashtable<String, Double> adhesionSpecies; //FLANN
    public Hashtable<String, Double> tightJunctionSpecies; // TJ Flann
    public Hashtable<String, Integer> switchingLags = new Hashtable<String, Integer>();        //Farzin
    public Hashtable<String, Double> chemotaxis = new Hashtable<String, Double>();        //Farzin
    public Hashtable<String, Integer> contactInhibition = new Hashtable<String, Integer>();        //Delin
    public ArrayList<EntryCondition> entryConditions = new ArrayList<EntryCondition>();    //Farzin


    //chris

    // whether or not the mass of the particles should be randomized when they are manually created
    public  boolean randomizeMassOnCreation;


    protected double tightJunctionToBoundaryStrength;
    protected double attachToBoundaryCreateFactor = 1;
    protected double attachToBoundaryDestroyFactor = 1;
    protected double attachCreateFactor = 1;
    protected double attachDestroyFactor = 1;


    /* ___________________ CONSTRUCTOR _____________________________________ */
    public Species(Simulator aSimulator, XMLParser aSpRoot) {
        speciesName = aSpRoot.getAttribute("name");
        String colorName = aSpRoot.getParam("color");
        if (colorName == null) colorName = "white";
        //	canAttachToBoundary = aSpRoot.getParamBool("attachedToBoundary");

        tightJunctionToBoundaryStrength = aSpRoot.getParamDblTry("tightJunctionToBoundaryStrength", 0);
        attachToBoundaryCreateFactor = aSpRoot.getParamDblTry("attachToBoundaryCreateFactor", 1.1);
        attachToBoundaryDestroyFactor = aSpRoot.getParamDblTry("attachToBoundaryDestroyFactor", 2);
        attachCreateFactor = aSpRoot.getParamDblTry("attachCreateFactor", 1.1);
        attachDestroyFactor = aSpRoot.getParamDblTry("attachDestroyFactor", 2);

        randomizeMassOnCreation = !aSpRoot.getParamBool("DoNotRandomizeMassOnCreation");


        color = utils.UnitConverter.getColor(colorName);
        if (aSpRoot.getParam("visible") != null)
            isVisible = aSpRoot.getParamBool("visible");

        // Register it
        speciesIndex = aSimulator.speciesList.size();
        currentSimulator = aSimulator;

        // Create the progenitor and tune its speciesParam object
        _progenitor = (SpecialisedAgent) aSpRoot.instanceCreator("simulator.agent.zoo");
        _progenitor.getSpeciesParam().init(aSimulator, aSpRoot);
        _progenitor.setSpecies(this);

        domain = aSimulator.world.getDomain(aSpRoot.getParam("computationDomain"));
        //Added by Farzin
        entryConditions = new ArrayList<EntryCondition>();
        adhesionSpecies = new Hashtable<String, Double>();
        tightJunctionSpecies = new Hashtable<String, Double>(); //TJ Flann


    }

    public Species(Yeast aProgenitor) {
        _progenitor = aProgenitor;
        aProgenitor.setSpecies(this);
    }

    /**
     * Register the created species to speciesManager
     * @param aSpecies
     */
    public void register(Simulator aSimulator, XMLParser aSpRoot) {
        currentSimulator = aSimulator;
        speciesName = aSpRoot.getAttribute("name");

        speciesIndex = aSimulator.speciesList.size();
        aSimulator.speciesList.add(this);

        domain = aSimulator.world.getDomain(aSpRoot.getAttribute("computationDomain"));
    }

    //Added by Farzin
    public void registerEntryCondition(EntryCondition EC) {
        entryConditions.add(EC);
    }

    public ArrayList<EntryCondition> getEntryConditions() {
        return entryConditions;
    }

    /**
     * Create clones of the progenitor within the birth area specified before
     * @param XML markup
     */
    public void createPop(XMLParser spRoot) {

        double howMany = spRoot.getAttributeDbl("number");
        if (howMany == 0) return;

        // Define the birth area
        ContinuousVector[] _initArea = defineSquareArea(spRoot);

        // Create all the required agents
        ContinuousVector cc;
        for (int i = 0; i < howMany; i++) {
            if (_progenitor instanceof LocatedAgent) {
                cc = new ContinuousVector();
                //sonia:chemostat
                if (Simulator.isChemostat) {
                    //do not randomly generate coordinates for the agent within the iniArea given in the protocol file
                } else {
                    shuffleRectangleCoordinates(cc, _initArea);
                }
                ((LocatedAgent) _progenitor).createNewAgent(cc);
            } else {
                _progenitor.createNewAgent();
            }
        }

        LogFile.writeLog(howMany + " agents of species " + speciesName + " successfully created");
    }

    public void createPop2(XMLParser spRoot) {
        Random R = new Random();

        double howMany = spRoot.getAttributeDbl("number");
        String shape = spRoot.getAttributeStr("shape");
        if (shape == null)
            shape = "default";
        if (howMany == 0) return;

        // Define the birth area
        ContinuousVector[] _initArea = new ContinuousVector[2];
        if (shape.equalsIgnoreCase("unfilledBlock") || shape.equalsIgnoreCase("default"))
            _initArea = defineSquareArea(spRoot);

        else if (shape.equalsIgnoreCase("outpatch"))
            _initArea = defineSquareAreawithpatch(spRoot);


        else if (shape.equalsIgnoreCase("filledBlock"))
            _initArea = defineParamSquareArea(spRoot);

        else if (shape.equalsIgnoreCase("filledCircle") || shape.equalsIgnoreCase("ring") || shape.equalsIgnoreCase("regring"))
            _initArea = defineCircularArea(spRoot);
        else if (shape.equalsIgnoreCase("duct"))
            _initArea = defineSquareArea(spRoot);
        else if (shape.equalsIgnoreCase("randuct"))
            _initArea = defineSquareArea(spRoot);


        ContinuousVector[] locations = generateLocations(howMany, shape, _initArea);

        // Create all the required agents
        ContinuousVector cc;
        for (int i = 0; i < locations.length; i++) {
            if (_progenitor instanceof LocatedAgent) {
                //Written by Qanita
                int roll = R.nextInt(100);


                if (shape.equalsIgnoreCase("randuct")) {

                    if ((roll % 5) == 0) {
                        cc = locations[i];
                        //sonia:chemostat
                        if (Simulator.isChemostat) {
                            //do not randomly generate coordinates for the agent within the iniArea given in the protocol file
                        } else {
                            if (cc != null)
                                ((LocatedAgent) _progenitor).createNewAgent(cc);
                        }

                    }

                } else {

                    cc = locations[i];
                    //sonia:chemostat
                    if (Simulator.isChemostat) {
                        //do not randomly generate coordinates for the agent within the iniArea given in the protocol file
                    } else {
                        if (cc != null)
                            ((LocatedAgent) _progenitor).createNewAgent(cc);
                    }


                }//
            } // end first if
            else {
                _progenitor.createNewAgent();
            }

        }// end for

        LogFile.writeLog(locations.length + " agents of species " + speciesName + " successfully created");
    }


    public void notifyBirth() {
        _population++;
    }

    public void notifyDeath() {
        _population--;
    }

    /**
     * @return a clone of the progenitor
     * @throws CloneNotSupportedException
     */
    public SpecialisedAgent sendNewAgent() throws CloneNotSupportedException {
        return _progenitor.sendNewAgent();
    }

	/* ______________ TOOLS ____________________________________________ */

    public int getPopulation() {
        return _population;
    }

    public SpecialisedAgent getProgenitor() {
        return _progenitor;
    }

    public SpeciesParam getSpeciesParam() {
        return _progenitor.getSpeciesParam();
    }

    public Species getSpecies(String speciesName) {
        return currentSimulator.speciesList.get(currentSimulator.getSpeciesIndex(speciesName));
    }

    public ContinuousVector[] defineSquareArea(XMLParser spRoot) {
        List<Element> area = spRoot.getChildren("coordinates");
        ContinuousVector cc1 = new ContinuousVector((Element) area.get(0));
        ContinuousVector cc2 = new ContinuousVector((Element) area.get(1));

        ContinuousVector[] initArea = new ContinuousVector[3];
        initArea[0] = new ContinuousVector();
        initArea[1] = new ContinuousVector();

        initArea[0].x = Math.min(cc1.x, cc2.x);
        initArea[0].y = Math.min(cc1.y, cc2.y);
        initArea[0].z = Math.min(cc1.z, cc2.z);
        initArea[1].x = Math.max(cc1.x, cc2.x);
        initArea[1].y = Math.max(cc1.y, cc2.y);
        initArea[1].z = Math.max(cc1.z, cc2.z);

        // In the case of 2D simulation, the agent's z-coordinate is 0.
        if (!domain.is3D) {
            initArea[0].z = 0;
            initArea[1].z = 0;
        }
        return initArea;
    }


    public ContinuousVector[] defineSquareAreawithpatch(XMLParser spRoot) {
        List<Element> area = spRoot.getChildren("coordinates");
        ContinuousVector cc1 = new ContinuousVector((Element) area.get(0));
        ContinuousVector cc2 = new ContinuousVector((Element) area.get(1));
        //	ContinuousVector cc3 = new ContinuousVector((Element) area.get(2));

        double radius = Double.parseDouble(((Element) area.get(1)).getAttributeValue("r"));

        ContinuousVector[] initArea = new ContinuousVector[3];
        initArea[0] = new ContinuousVector();
        initArea[1] = new ContinuousVector();
        initArea[2] = new ContinuousVector();
        initArea[0].x = Math.min(cc1.x, cc2.x);
        initArea[0].y = Math.min(cc1.y, cc2.y);
        initArea[0].z = Math.min(cc1.z, cc2.z);
        initArea[1].x = Math.max(cc1.x, cc2.x);
        initArea[1].y = Math.max(cc1.y, cc2.y);
        initArea[1].z = Math.max(cc1.z, cc2.z);
        initArea[2].x = radius;
        // In the case of 2D simulation, the agent's z-coordinate is 0.
        if (!domain.is3D) {
            initArea[0].z = 0;
            initArea[1].z = 0;
        }
        return initArea;
    }


    private ContinuousVector[] defineParamSquareArea(XMLParser spRoot) {
        ContinuousVector[] initArea = defineSquareArea(spRoot);
        List<Element> rowsCols = spRoot.getChildren("blocks");
        int rows = Integer.parseInt(((Element) rowsCols.get(0)).getAttributeValue("rows"));
        int cols = Integer.parseInt(((Element) rowsCols.get(0)).getAttributeValue("cols"));
        int bars = 1;
        if (Simulator.is3D) bars = Integer.parseInt(((Element) rowsCols.get(0)).getAttributeValue("bars"));


        initArea[2] = new ContinuousVector(rows, cols, bars);
        return initArea;
    }

    public ContinuousVector[] defineCircularArea(XMLParser spRoot) {
        List<Element> area = spRoot.getChildren("coordinates");
        ContinuousVector cc1 = new ContinuousVector((Element) area.get(0));
        //ContinuousVector cc2 = new ContinuousVector((Element) area.get(1));
        double radius = Double.parseDouble(((Element) area.get(1)).getAttributeValue("r"));

        ContinuousVector[] initArea = new ContinuousVector[2];
        initArea[0] = new ContinuousVector();
        initArea[1] = new ContinuousVector();

        initArea[0].x = cc1.x;
        initArea[0].y = cc1.y;
        initArea[0].z = cc1.z;
        initArea[1].x = radius;


        // In the case of 2D simulation, the agent's z-coordinate is 0.
        if (!domain.is3D) {
            initArea[0].z = 0;
        }
        return initArea;
    }


    /**
     * Generate random but valid continuous coordinates inside a volume
     * @param cc
     * @param area
     */
    public void shuffleRectangleCoordinates(ContinuousVector cc, ContinuousVector[] area) {
        boolean test = true;
        while (test) {
            cc.x = area[0].x + ExtraMath.getUniRand() * (area[1].x - area[0].x);
            cc.y = area[0].y + ExtraMath.getUniRand() * (area[1].y - area[0].y);
            cc.z = area[0].z + ExtraMath.getUniRand() * (area[1].z - area[0].z);
            test = !(domain.testCrossedBoundary(cc) == null);

        }
    }

    public void shuffleCircleCoordinates(ContinuousVector cc, ContinuousVector[] area) {
        boolean test = true;
        double radius = area[1].x;
        while (test) {
            double angle = ExtraMath.getUniRand(0, 360);
            double angleinRadians = Math.toRadians(angle);
            /*x1 = x0 + (Math.cos(angle) * radius);
			y1 = y0 + (Math.sin(angle) * radius);*/
            cc.x = area[0].x + (Math.cos(angleinRadians) * radius);
            cc.y = area[0].y + (Math.sin(angleinRadians) * radius);

            //cc.x = area[0].x+ExtraMath.getUniRand()*(area[1].x-area[0].x);
            //cc.y = area[0].y+ExtraMath.getUniRand()*(area[1].y-area[0].y);
            //cc.z = area[0].z+ExtraMath.getUniRand()*(area[1].z-area[0].z);
            test = !(domain.testCrossedBoundary(cc) == null);

        }
    }


    private ContinuousVector[] generateLocations(double howMany, String shape,
                                                 ContinuousVector[] _initArea) {

        ContinuousVector[] locs = new ContinuousVector[(int) howMany];


        if (shape.equalsIgnoreCase("default"))
            for (int i = 0; i < howMany; i++) {
                locs[i] = new ContinuousVector();
                shuffleRectangleCoordinates(locs[i], _initArea);
            }


        else if (shape.equalsIgnoreCase("outpatch")) {
            //The Start of the domain is  _initArea[0].x=0   _initArea[0].y=0
            //The End of the domain is    _initArea[1].x=2400   _initArea[1].y=2400;
            //The diumeter is  _initArea[2].x= 500


            int i = 0;
            ContinuousVector[] Checks = new ContinuousVector[(int) howMany];
            while (i < howMany) {
                Checks[i] = new ContinuousVector();// Great Random points X,Y

                boolean test = true;
                double CheckXcenter = 0;
                double CheckYcenter = 0;
                CheckXcenter = _initArea[0].x + _initArea[2].x;//Intial center x value
                CheckYcenter = _initArea[0].y + _initArea[2].x;//Intial center y value


                while (test) {
                    //Assign a random value for Checks in side the domain
                    Checks[i].x = _initArea[0].x + ExtraMath.getUniRand() * (_initArea[1].x - _initArea[0].x);
                    Checks[i].y = _initArea[0].y + ExtraMath.getUniRand() * (_initArea[1].y - _initArea[0].y);
                    Checks[i].z = _initArea[0].z + ExtraMath.getUniRand() * (_initArea[1].z - _initArea[0].z);
                    test = !(domain.testCrossedBoundary(Checks[i]) == null);

                }

                //Here we will write the code that check[i].x and check[i].y that this point is out of patch
                int Flagn = 0;


                while (CheckXcenter <= (_initArea[1].x - _initArea[2].x)) {
                    CheckYcenter = _initArea[0].y + _initArea[2].x;

                    while (CheckYcenter <= (_initArea[1].y - _initArea[2].x)) {

                        double temp = Math.pow((Checks[i].x - CheckXcenter), 2.0) + Math.pow((Checks[i].y - CheckYcenter), 2.0);
                        double distance = Math.sqrt(temp);
                        if (distance > _initArea[2].x / 2 && Flagn != 3) Flagn = 1;
                        else Flagn = 3;
                        CheckYcenter = CheckYcenter + (2 * _initArea[2].x);
                    }

                    CheckXcenter = CheckXcenter + (2 * _initArea[2].x);
                }


                if (Flagn == 1) {
                    locs[i] = new ContinuousVector();
                    locs[i] = Checks[i];
                    i++;
                }


            }// end howmany loop
        }// end of else if(shape.equalsIgnoreCase("outpatch"))

        else if (shape.equalsIgnoreCase("filledBlock")) {
            double firstSide = Math.abs(_initArea[0].x - _initArea[1].x);
            double secondSide = Math.abs(_initArea[0].y - _initArea[1].y);
            double thirdSide = Math.abs(_initArea[0].z - _initArea[1].z);
            int rows = (int) _initArea[2].x;
            int cols = (int) _initArea[2].y;
            int bars = (int) _initArea[2].z;
            double xStep = firstSide / rows;
            double yStep = secondSide / cols;
            double zStep = thirdSide / bars;
            int counter = 0;
            for (int i = 0; i < rows; i++)
                for (int j = 0; j < cols; j++) {
                    for (int k = 0; k < bars; k++) {
                        double x = _initArea[0].x + i * xStep;
                        double y = _initArea[0].y + j * yStep;
                        double z = _initArea[0].z + k * zStep;
                        if (i % 2 == 1)
                            y += yStep / 2.0;
                        if (counter < howMany)
                            locs[counter] = new ContinuousVector(x, y, z);
                        counter++;
                    }
                }

        } else if (shape.equalsIgnoreCase("unfilledBlock")) {
            double sides = _initArea[0].rectangleSide(_initArea[1]);
            double step = sides / howMany;
            double currentPoint = 0;
            int counter = 0;
            while (currentPoint < sides && counter < howMany) {
                locs[counter] = mapToRectPoint(currentPoint, _initArea);
                currentPoint += step;
                counter++;
            }
        } else if (shape.equalsIgnoreCase("ring")) {
            for (int i = 0; i < howMany; i++) {
                locs[i] = new ContinuousVector();
                shuffleCircleCoordinates(locs[i], _initArea);
            }
        }


        // Written by Qanita
        else if (shape.equalsIgnoreCase("Regring")) {


            for (int i = 0; i < howMany; i++) {
                locs[i] = new ContinuousVector();
                // shuffleCircleCoordinates(locs[i], _initArea);


                //	boolean test = true;
                double radius = _initArea[1].x;
                double angleinRadians = 2 * Math.PI * i / howMany;
					/*x1 = x0 + (Math.cos(angle) * radius);
					y1 = y0 + (Math.sin(angle) * radius);*/
                locs[i].x = _initArea[0].x + (Math.cos(angleinRadians) * radius);
                locs[i].y = _initArea[0].y + (Math.sin(angleinRadians) * radius);
            }
        } else if (shape.equalsIgnoreCase("filledCircle")) {


            for (int i = 0; i < howMany; i++) {
					
					/*
					//Randomly generate an angle 
					double angle=ExtraMath.getUniRand(0,360);
					double radius=_initArea[1].x;
					
					double randomR=ExtraMath.getUniRand(0,radius);
					double angleinRadians = Math.toRadians(angle);
					locs[i]=new ContinuousVector();
					locs[i].x = _initArea[0].x+(Math.cos(angleinRadians) * randomR);
					locs[i].y = _initArea[0].y+(Math.sin(angleinRadians) * randomR);
					*/


                //Randomly generate an angle
                double angle = Math.random() * 360.0 + 0.0;
                double radius = _initArea[1].x;

                double randomR = Math.sqrt(Math.random()) * radius;
                //	double angleinRadians = Math.toRadians(angle);
                locs[i] = new ContinuousVector();
                locs[i].x = _initArea[0].x + (Math.cos(angle) * randomR);
                locs[i].y = _initArea[0].y + (Math.sin(angle) * randomR);

                // shuffleCircleCoordinates(locs[i], _initArea);

                //	double angleinRadians = Math.toRadians(angle);
                //	double x=ExtraMath.getUniRand(0,1);
                //	double xx=Math.random();

                //	boolean test = true;

                //	double angleinRadians = 2*Math.PI*i/howMany;
					/*x1 = x0 + (Math.cos(angle) * radius);
					y1 = y0 + (Math.sin(angle) * radius);*/


                // double length = generator.nextDouble() * 40.0 + 10.0;
            }
        }


        //Added by Farzin;
        else if (shape.equalsIgnoreCase("duct") || shape.equalsIgnoreCase("randuct")) {
            double halfCircleR = Math.abs(_initArea[0].x - _initArea[1].x) / 2;
            double particleRadius = ((LocatedAgent) getProgenitor())._radius;
            int rectCount = (int) (Math.abs(_initArea[0].y - _initArea[1].y) / particleRadius);
            int circleCount = (int) ((Math.PI * halfCircleR) / (2 * particleRadius));
            double newR = circleCount * 2 * particleRadius / Math.PI;

            if (domain.is3D()) {
                locs = new ContinuousVector[(rectCount + circleCount) * circleCount * 2];
                int index = 0;
                double recY = Math.min(_initArea[0].y, _initArea[1].y);
                for (int i = 0; i < rectCount / 2; i++) {

                    index = ringCreatorForDuct((_initArea[0].x + _initArea[1].x) / 2, recY + i * 2 * particleRadius,
                            (_initArea[0].z + _initArea[1].z) / 2, halfCircleR, locs, index, circleCount * 2);

                }


                //circleCount=circleCount/2;
                double arcStep = 180 / circleCount;
                double curArc = 90 + arcStep;
                double mainR = halfCircleR;
                int temp = circleCount;
                recY = Math.max(_initArea[0].y, _initArea[1].y);
                double curY = recY;
                for (int i = 0; i < temp / 2 + 1; i++) {
                    //halfCircleR=halfCircleR-2*particleRadius;

                    halfCircleR = mainR * Math.abs(Math.sin(Math.toRadians(curArc)));
                    circleCount = (int) ((Math.PI * halfCircleR) / (2 * particleRadius));

                    index = ringCreatorForDuct((_initArea[0].x + _initArea[1].x) / 2, curY,
                            (_initArea[0].z + _initArea[1].z) / 2, halfCircleR, locs, index, circleCount * 2);
                    curY = recY + mainR * Math.abs(Math.cos(Math.toRadians(curArc)));
                    curArc += arcStep;
                }
            } else {
                locs = new ContinuousVector[2 * rectCount + circleCount];

                for (int i = 0; i < rectCount; i++) {
                    double downX = Math.min(_initArea[0].x, _initArea[1].x);
                    double recY = Math.min(_initArea[0].y, _initArea[1].y);
                    double curX = downX;
                    locs[i] = new ContinuousVector(curX, recY + i * 2 * particleRadius, 0);
                }
                for (int i = 0; i < rectCount; i++) {
                    double upX = Math.max(_initArea[0].x, _initArea[1].x);
                    double recY = Math.min(_initArea[0].y, _initArea[1].y);
                    double curX = upX;
                    locs[i + rectCount] = new ContinuousVector(curX, recY + i * 2 * particleRadius, 0);
                }
                double arcStep = 180 / circleCount;
                double centerX = (Math.min(_initArea[0].x, _initArea[1].x) + Math.max(_initArea[0].x, _initArea[1].x)) / 2;
                double centerY = Math.min(_initArea[0].y, _initArea[1].y);
                double curArc = 90 + arcStep;
                for (int i = 0; i < circleCount; i++) {
                    locs[i + rectCount * 2] = new ContinuousVector(centerX + newR * Math.sin(Math.toRadians(curArc)),
                            centerY + newR * Math.cos(Math.toRadians(curArc)), 0);

                    curArc += arcStep;
                }
            }
        }


        return locs;
    }

    private int ringCreatorForDuct(double x, double y, double z, double r, ContinuousVector[] locs, int index, int howMany) {
        for (int i = 0; i < howMany; i++) {
            locs[index] = new ContinuousVector();
            double angleinRadians = 2 * Math.PI * i / howMany;
            locs[index].x = x + (Math.cos(angleinRadians) * r);
            locs[index].y = y;
            locs[index].z = z + (Math.sin(angleinRadians) * r);
            index++;
        }
        return index;

    }

    private ContinuousVector mapToRectPoint(double currentPoint, ContinuousVector[] initArea) {
        // Clockwise searching
        double firstSide = Math.abs(initArea[0].x - initArea[1].x);
        double secondSide = Math.abs(initArea[0].y - initArea[1].y);
        ContinuousVector point = new ContinuousVector();
        if (currentPoint < firstSide) {
            point.y = initArea[0].y;
            point.x = initArea[0].x + currentPoint;
        } else if (currentPoint < firstSide + secondSide) {
            point.y = initArea[0].y + (currentPoint - firstSide);
            point.x = initArea[1].x;
        } else if (currentPoint < 2 * firstSide + secondSide) {
            point.y = initArea[1].y;
            point.x = initArea[0].x + (2 * firstSide + secondSide - currentPoint);
        } else if (currentPoint < 2 * firstSide + 2 * secondSide) {
            point.y = initArea[0].y + (2 * firstSide + 2 * secondSide - currentPoint);
            point.x = initArea[0].x;
        } else {
            double a = 10;
        }

        return point;
    }
}
