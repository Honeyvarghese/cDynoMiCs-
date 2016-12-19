/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * ______________________________________________________
 * Class for containing chemical solutes, that are represented by a grid. The
 * grid is padded, 3D grid
 * Diffusivity is expressed in the local time unit
 *
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 * @since June 2006
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de), Helmholtz Centre for Infection Research (Germany)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu), Department of Engineering Sciences and Applied Mathematics, Northwestern University (USA)
 */

package simulator;

import farzin.Variable;
import org.jdom.Element;
import simulator.geometry.Domain;
import simulator.geometry.boundaryConditions.AllBC;
import utils.ExtraMath;
import utils.LogFile;
import utils.UnitConverter;
import utils.XMLParser;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class SoluteGrid extends SpatialGrid
    {
        // Serial version used for the serialisation of the class
        private static final long serialVersionUID = 1L;

	/* ______________________ PROPERTIES OF THE SOLUTE ______________________ */

        // Identifier
        public int soluteIndex;

        // Diffusivity in water
        public double diffusivity;

        // Diffusivity in air
        public double airDiffusivity;

        // Decay rate
    	public double decayRate;
    	
    	// Contact inhibition
    	public boolean contactInhibition;
    			
        public boolean writeOutput;
        public int[] outputXY;
        public int[] outputYZ;
        public int[] outputXZ;
        private final String XY = "xy";
        private final String YZ = "yz";
        private final String XZ = "xz";

        public boolean useRandomInit;
        public double  maxConc;
        public double  minConc;


        // Description of diffusion, carrier and bulk domains for this solute
        private Domain _domain;

	/* _____________________ CONSTRUCTOR ___________________________________ */

        /**
         * Constructor based on the XML file
         * @param aSim
         * @param xmlRoot
         */
        public SoluteGrid(Simulator aSim, XMLParser xmlRoot)
            {
                double value;
                StringBuffer unit = new StringBuffer("");

                gridName = xmlRoot.getAttribute("name");

                soluteIndex = aSim.getSoluteIndex(gridName);

                //chris johnnson 4/9/2015
                //record the index of the density solute
                if (gridName.equalsIgnoreCase(Variable.densityName)) Variable.densitySolute = soluteIndex;


                _domain = aSim.world.getDomain(xmlRoot.getAttribute("domain"));

                // Set the decay rate.
        		decayRate = xmlRoot.getParamDbl("decayRate");
        		
        		if(Double.isNaN(decayRate)){
        			decayRate = 0.0;
        		}
        		
        		// Set the contactInhibition.
            	contactInhibition = xmlRoot.getParamBool("contactInhibition");
            	
		/* Set the resolution and create the grid ________________ */
                value = xmlRoot.getParamLength("resolution");
                if (Double.isNaN(value))
                    {
                        useDomaingrid();
                    }
                else
                    {
                        specifyResolution(value);
                    }
                initGrids();

		/* Set the diffusivity ____________________________________ */
                value = xmlRoot.getParamDbl("diffusivity", unit);
                value *= UnitConverter.time(unit.toString());
                value *= UnitConverter.length(unit.toString());
                value *= UnitConverter.length(unit.toString());
                diffusivity = value;

                //Added by Farzin
                value = xmlRoot.getParamDbl("airDiffusivity", unit);
                value *= UnitConverter.time(unit.toString());
                value *= UnitConverter.length(unit.toString());
                value *= UnitConverter.length(unit.toString());
                if (Double.isNaN(value))
                    value = 0;
                airDiffusivity = value;

		/* Set the initial concentration __________________________ */
                value = xmlRoot.getParamDbl("concentration");
                // If no value specified, use the maximal concentration of the bulks
                if (Double.isNaN(value))
                    if (aSim.world.getAllBulkValue(soluteIndex).length > 0)
                        value = ExtraMath.max(aSim.world.getAllBulkValue(soluteIndex));
                    else
                        value = 0;
                setAllValueAt(value);


                //Added by Farzin to let random initialization
                useRandomInit = xmlRoot.getParamBool("randomInit");
                if (!Double.isNaN(value) && useRandomInit)
                    {
                        minConc = xmlRoot.getParamDbl("rndMinConcentration");
                        maxConc = xmlRoot.getParamDbl("rndMaxConcentration");
                    }
                else
                    useRandomInit = false;

                //added by chris
                writeOutput = xmlRoot.getParamBool("writeOutput");
                if(writeOutput) {
                    Element crossSectionsMarkup = xmlRoot.getParamMarkUp("crosssections");
                    outputXY = soluteCrossSection(crossSectionsMarkup, XY);
                    if (_nK > 1) outputYZ = soluteCrossSection(crossSectionsMarkup, YZ);
                    if (_nK > 1) outputXZ = soluteCrossSection(crossSectionsMarkup, XZ);
                }

            }


	/* The 2 next functions are used when creating the multigrids */

        public SoluteGrid(int nI, int nJ, int nK, double res)
            {
                super(nI, nJ, nK, res);
            }

        public SoluteGrid(int nI, int nJ, int nK, double res, String aName, Domain aDomain)
            {
                super(nI, nJ, nK, res);
                gridName = aName;
                _domain = aDomain;
            }

        public SoluteGrid(int nI, int nJ, int nK, double res, SoluteGrid aSolG)
            {
                super(nI, nJ, nK, res);
                useExternalSoluteGrid(aSolG);
            }

        public SoluteGrid(SoluteGrid aSolG)
            {
                gridName = aSolG.gridName;
                diffusivity = aSolG.diffusivity;
                _domain = aSolG._domain;

                _reso = aSolG.getResolution();
                _nI = aSolG.getGridSizeI();
                _nJ = aSolG.getGridSizeJ();
                _nK = aSolG.getGridSizeK();

                initGrids();

            }

        public void useExternalSoluteGrid(SoluteGrid aSolG)
            {
                gridName = aSolG.gridName;
                soluteIndex = aSolG.soluteIndex;
                diffusivity = aSolG.diffusivity;
                _domain = aSolG._domain;
            }


        /**
         * Use the size and the resolution used to define the computation domain to
         * define the solute grid
         */
        public void useDomaingrid()
            {
                _reso = _domain.getGrid().getResolution();
                _nI = _domain.getGrid().getGridSizeI();
                _nJ = _domain.getGrid().getGridSizeJ();
                _nK = _domain.getGrid().getGridSizeK();
            }

        /**
         * Give size of grid for the given resolution, based on length defined in
         * the domain
         *
         * @param reso
         */
        public void specifyResolution(double reso)
            {
                _reso = reso;
                _nI = (int) Math.ceil(_domain.getGrid().getGridLength(1) / _reso);
                _nJ = (int) Math.ceil(_domain.getGrid().getGridLength(2) / _reso);
                _nK = (int) Math.ceil(_domain.getGrid().getGridLength(3) / _reso);
            }

	/* ________________________ MAIN METHODS ______________________________ */

        public void refreshBoundary()
            {
                for (AllBC aBC : _domain.getAllBoundaries())
                    {
                        aBC.refreshBoundary(this);
                    }
            }



	/* ________________________ GET & SET __________________________________ */

        public String getName()
            {
                return gridName;
            }

        public double getDiffusivity()
            {
                return diffusivity;
            }


        public double getAirDiffusivity()
            {
                return airDiffusivity;
            }

        public Domain getDomain()
            {
                return _domain;
            }


        private int[] soluteCrossSection(Element crossSectionsCoordsElement, String crossSectionString)
        {
            int[] crossSectionsCoords = null;
            String crossSectionsCoordsString = null;
            if(crossSectionsCoordsElement != null) crossSectionsCoordsString = crossSectionsCoordsElement.getAttributeValue(crossSectionString);

            boolean isEmpty = crossSectionsCoordsString == null || crossSectionsCoordsString.length() == 0;


            if(_nK > 1)
            {
                if(isEmpty)
                {
//                    crossSectionsCoords = new int[1];
//                    if(crossSectionsCoordsString.equals(XY)) crossSectionsCoords[0] = (_nK + 1) /2;
//                    if(crossSectionsCoordsString.equals(YZ)) crossSectionsCoords[0] = (_nI + 1) /2;
//                    if(crossSectionsCoordsString.equals(XZ)) crossSectionsCoords[0] = (_nJ + 1) /2;
                }

                else
                {
                    String[] tmp = crossSectionsCoordsString.split(" ");
                    crossSectionsCoords = new int[tmp.length];
                    for(int i = 0; i <crossSectionsCoords.length;i++)
                    {
                        crossSectionsCoords[i] = Integer.parseInt(tmp[i]);
                    }
                }
            }
            else if(isEmpty || crossSectionsCoordsString.equals(XY))
            {
                crossSectionsCoords = new int[1];
                crossSectionsCoords[0] = (_nK + 1) / 2;
            }

            return crossSectionsCoords;

        }

        public void writeOutput(String resultPath, int currentIter) {
            if (writeOutput) {
                StringBuilder output = new StringBuilder();


                if (outputXY != null && outputXY.length > 0) {
                    for (int k : outputXY) {
                        for (int i = 1; i < grid.length -1; i++) {
                            for (int j = 1; j < grid[0].length -1; j++) {

                                output.append(Double.toString((grid[i][j][k+1]))).append("\t");
                            }
                            output.append("\n");
                        }

                        writeToFile(resultPath, currentIter, output.toString(), k, XY);
                        output = new StringBuilder();


                    }
                }

                if (outputYZ != null && outputYZ.length > 0) {
                    for (int i : outputYZ) {
                        for (int k = 1; k < grid[0][0].length-1; k++) {
                            for (int j = 1; j < grid[0].length-1; j++) {

                                output.append(Double.toString((grid[i+1][j][k]))).append("\t");
                            }
                            output.append("\n");
                        }

                        writeToFile(resultPath, currentIter, output.toString(), i, YZ);
                        output = new StringBuilder();

                    }
                }

                if (outputXZ != null && outputXZ.length > 0) {
                    for (int j : outputXZ) {
                        for (int k = 1; k < grid[0][0].length-1; k++) {
                            for (int i = 1; i < grid[0].length-1; i++) {

                                output.append(Double.toString((grid[i][j+1][k]))).append("\t");
                            }
                            output.append("\n");
                        }

                        writeToFile(resultPath, currentIter, output.toString(), j, XZ);
                        output = new StringBuilder();

                    }
                }
            }
        }

        private void writeToFile(String resultPath, int currentIter, String output, int secCoord, String crossSectionName)
        {
            StringBuffer fileName = new StringBuffer();
            try {
                fileName.append(resultPath).append(File.separator).append("SoluteConcentration").append(File.separator)
                        .append(crossSectionName).append("-").append(secCoord).append(File.separator).append(gridName).append(" solute ").append(LogFile.numberToString(currentIter)).append(".txt");
                File f = new File(fileName.toString());
                f.getParentFile().mkdir();
                FileWriter fs = new FileWriter(f);
                fs.write(output);
                fs.close();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

    }
