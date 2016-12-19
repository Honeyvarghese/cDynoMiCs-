package idyno;

/**
 *****************************************************************************
 Copyright

 * Laurent Lardon lardonl@supagro.inra.fr, DTU Environment, Technical University of Denmark (Denmark) & Laboratory of Environmental Biotechnology, INRA (France)
 * Brian Merkey, brim@env.dtu.dk (DTU Environment, Technical University of Denmark, Denmark) & bvm@northwestern.edu (Department of Engineering Sciences and Applied Mathematics, Northwestern University, USA)
 * Andreas Dötsch andreas.doetsch@helmholtz-hzi.de,  Chronic Pseudomonas Infections Group, Helmholtz Centre for Infection Research (Germany)
 * João Xavier xavierj@mskcc.org, Computational biology research, Memorial Sloan-Kettering Cancer Center, New York (USA)
 * Sónia Martins SCM808@bham.ac.uk, Centre for Systems Biology,School of Biosciences, University of Birmingham (UK)
 * Cristian Picioreanu C.Picioreanu@tudelft.nl, The Biofilm Research Group, Technical University of Delft (Netherlands)
 * Jan-Ulrich Kreft j.kreft@bham.ac.uk, Centre for Systems Biology, School of Biosciences, University of Birmingham (UK)
 * Barth Smets bfs@env.dtu.dk, DTU Environment, Technical University of Denmark (Denmark)
 * Sónia Martins SCM808@bham.ac.uk, Centre for Systems Biology,School of Biosciences, University of Birmingham (UK)



 This software is a computer program whose purpose is to model and simulate 
 microbial communities in an individual-based way. It is described in detail in this paper:

 Lardon LA, Merkey BV, Martins S, Dotsch A, Picioreanu C, Kreft JU, Smets BF (2010). 
 iDynoMiCS: Next Generation of Individual-based Modelling of Biofilms.

 This software is governed by the CeCILL license under French law and
 abiding by the rules of distribution of free software.  You can  use, 
 modify and/ or redistribute the software under the terms of the CeCILL
 license as circulated by CEA, CNRS and INRIA at the following URL
 "http://www.cecill.info". 

 As a counterpart to the access to the source code and  rights to copy,
 modify and redistribute granted by the license, users are provided only
 with a limited warranty  and the software's author,  the holder of the
 economic rights,  and the successive licensors  have only  limited
 liability. 

 In this respect, the user's attention is drawn to the risks associated
 with loading,  using,  modifying and/or developing or reproducing the
 software by the user in light of its specific status of free software,
 that may mean  that it is complicated to manipulate,  and  that  also
 therefore means  that it is reserved for developers  and  experienced
 professionals having in-depth computer knowledge. Users are therefore
 encouraged to load and test the software's suitability as regards their
 requirements in conditions enabling the security of their systems and/or 
 data to be ensured and,  more generally, to use and operate it in the 
 same conditions as regards security. 

 The fact that you are presently reading this means that you have had
 knowledge of the CeCILL license and that you accept its terms.

 Moreover authors ask to be acknowledged in any scientific publications based on 
 or using some part of that software.
 ******************************************************************************
 *
 * @since June 2006
 * @version 1.0
 * @author Andreas Dötsch (andreas.doetsch@helmholtz-hzi.de)
 * @author Laurent Lardon (lardonl@supagro.inra.fr)
 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu)
 * @author João Xavier (xavierj@mskcc.org)
 * @author Cristian Picioreanu (C.Picioreanu@tudelft.nl)
 * @author Jan-Ulrich Kreft (j.kreft@bham.ac.uk)
 */
//
//
import java.util.LinkedList;
import javax.swing.JFileChooser;

import SearchEngine.Constants;
import simulator.Simulator;
import utils.XMLParser;
import utils.ExtraMath;
import utils.LogFile;
import java.io.File;
import utils.ResultFile;

/**
 * Syntax : - To launch a single simulation idyno.Idynomics
 * \protocol\protocolFileURL - To launch a batch of simulations idyno.Idynomics
 * \protocol\FilesDirectory
 */

/**
 *comments by honey
 *
 *Use this as main class when you need to run single simulation
 *Make sure you change paths in SearchEngine.Constants
 */
public class Idynomics {
	// name of the protocol XML file where the simulation scenario is described
	private static String[] _protocolFile;

	// path of protocol file and where the results should be written
	private static String[] _activePath;
	private static String resultPath;
	
	private static boolean deterministicStart=true; 
	
	static //private static String pathName="D:\\Updated Workspace\\iDynoMiCS\\resultss\\Contact(20160218_0212)\\Contact.xml";
	
	String filepath =Constants.XML_PATH;
	

	private static String pathName=filepath;
	//private static String pathName="C:\\Users\\Farzin\\Dropbox (Personal)\\Works at ISB\\Peroxisomes\\iDyno and XMLs\\Glioma_simplifiedForThePaper.xml";
	
	//private static String pathName="C:\\Users\\farzin\\Dropbox\\Works at ISB\\Peroxisomes\\iDyno and XMLs\\perox_v3.2_simplified.xml";
	//private static String pathName="G:\\Dropbox\\Works at ISB\\Peroxisomes\\iDyno and XMLs\\perox_v3.2_simplified.xml";
//	private static String pathName="C:\\Users\\Farzin\\Downloads\\2d with AgarTest.xml";
//	private static String pathName="E:\\cellMatrix3D128_10WithCDPMap.xml";
//	private static String pathName="D:\\xmls\\cellMatrix3D128_10WithCDPMap.xml";
//	private static String pathName="G:\\Dropbox\\Works at ISB\\Peroxisomes\\iDyno and XMLs\\peroxisomes_v7.xml";
	public static double    begin;
	public static Simulator aSimulator;

	public static String currentPath; //added so that the simulator knows the current path to read/write the random number file - Chinmay 11/8/2009

	/**
	 * Process arguments, create a Simulator object and launch its execution
	 */
	
	//uncomment this when you start your search engine
	//public static void iDyno() throws Exception {
	
	//comment this when you start your search engine
	public static void main(String[] args) throws Exception {
	String[] args1={};
		
		processArguments(args1);
		currentPath = _activePath[0];

		for (int iSimul = 0; iSimul<_protocolFile.length; iSimul++)
		{
			if (initSimulation(iSimul)) {

				launchSimulation(iSimul);				
				/* The following lines write out the random number state file at the end of each simulation
				 * The reason this is done here, is because it is guaranteed to be the absolute last thing that's done before
				 * the next simulation is called. Chinmay 11/8/2009
				*/
				try {
					//resultPathFull = aSimulator.getResultPath();
//					FileOutputStream randomFileOutputStream = new FileOutputStream(currentPath+File.separator+"random.state");
//					ObjectOutputStream randomObjectOutputStream = new ObjectOutputStream(randomFileOutputStream);
//					randomObjectOutputStream.writeObject(ExtraMath.random);
//					randomObjectOutputStream.close();
//					randomFileOutputStream.close();
//					LogFile.writeLog("Wrote random number generator");
					LogFile.closeFile();
				}
				catch(Exception e) {
					LogFile.writeLog("Idynomics.main(): error met while writing out random number state file" + e);
					LogFile.closeFile();
					System.exit(-1);					
				}
			}
		}
		aSimulator = null;
	}

	/**
	 * Open a dialog to select directory of or a single protocol file
	 * @return a file or a directory
	 */
	public static LinkedList<File> chooseFile() {
		LinkedList<File> protocolFiles = new LinkedList<File>();

		if(deterministicStart)
		{
			File aFile=new File(pathName);
			listDirectory(aFile, "xml", protocolFiles);
			return protocolFiles;
			
		}
		
		// Open a FileChooser window in the current directory
		JFileChooser chooser = new JFileChooser(""+System.getProperty("user.dir")+"/protocol");
		chooser.setFileSelectionMode(JFileChooser.FILES_AND_DIRECTORIES);
		chooser.setMultiSelectionEnabled(true);

		if (chooser.showOpenDialog(null)==JFileChooser.APPROVE_OPTION) {
			for (java.io.File aFile : chooser.getSelectedFiles()) {
				listDirectory(aFile, "xml", protocolFiles);
			}
		}

		return protocolFiles;
	}

	/**
	 * Build a list of path and filename of protocol file(s)
	 */
	static public void processArguments(String[] args) {
		LinkedList<File> allFiles;

		// Build a list of Files only made of protocol files
		switch (args.length) {
		case 0:
			allFiles = chooseFile();
			break;
		default:
			allFiles = new LinkedList<File>();
			for (int iFile = 0; iFile<args.length; iFile++) {
				listDirectory(new File(args[iFile]), "xml", allFiles);
			}
		}

		// this part removes any agent_State and env_Sum files from the list
		int nfile = allFiles.size();
		int nchecked = 0;
		while (nchecked < nfile) {
			if (allFiles.get(nchecked).getName().contains("agent_State") ||
					allFiles.get(nchecked).getName().contains("env_Sum")) {
				allFiles.remove(nchecked);
				nfile = allFiles.size();
			} else {
				nchecked = nchecked + 1;
			}
		}

		// Build the list of path and protocol files
		int nProtocol = allFiles.size();
		_activePath = new String[nProtocol];
		_protocolFile = new String[nProtocol];

		for (int iFile = 0; iFile<nProtocol; iFile++) {
			_activePath[iFile] = allFiles.get(iFile).getParent()+java.io.File.separator;
			//Edd: Added check for null _activePath, which was occuring when the launch scripts
			//were invoked directly from the folder containing the protocol files in use.
			if(_activePath[iFile].equals("null"+java.io.File.separator)){
				_activePath[iFile]="."+java.io.File.separator;
			}
			_protocolFile[iFile] = allFiles.get(iFile).getName();

			System.out.println("Initializing with protocol file: "+_activePath[iFile]
			                                                                   +_protocolFile[iFile]);
		}
	}

	/**
	 * Create the result directories and a well-defined Simulator object
	 * @param iSimul
	 */
	public static boolean initSimulation(int iSimul) {

		// bvm 23.01.09: added code
		// the restart code works by reading in the "restartPreviousRun" parameter
		// from the protocol file; if true, then the run is continued from the
		// previous state.
		//
		// this approach means a user must edit the output protocol file in the
		// results folder and then choose it (FROM THE RESULT DIRECTORY) when 
		// starting a new run
		// 

		String resultDir;
		String resultFullURL;

		if (checkForRestart(_activePath[iSimul]+_protocolFile[iSimul])) {
			resultDir = _activePath[iSimul];
			resultFullURL = resultDir+File.separator+_protocolFile[iSimul];

			// Create a log file
			LogFile.openFile(resultDir);

			LogFile.writeLog("Restarting run from previous results.");
		} else {
			// default, non-restarting method

			// bvm 10.2.2009
			// create the output file name using the protocol file title too
			resultDir = _activePath[iSimul].replace("protocol", "results")
			+ _protocolFile[iSimul].replace(".xml", "(")
			+ LogFile.getDateFileName() + ")";

			new File(resultDir+File.separator+"lastIter").mkdirs();
			new File(resultDir+File.separator+"SoluteConcentration").mkdirs();
			new File(resultDir+File.separator+"Molecules").mkdirs();
			new File(resultDir+File.separator+"MovedCells").mkdirs();
			new File(resultDir+File.separator+"povray").mkdirs();
	//		new File(resultDir+File.separator+"VEGFPercell").mkdirs();
			// Copy the protocol file into the result directory
			resultFullURL = resultDir+File.separator+_protocolFile[iSimul];
			ResultFile.copyFile(_activePath[iSimul]+_protocolFile[iSimul], resultFullURL);

			// Create a log file
			LogFile.openFile(resultDir);
		}

		// Create the simulator
		try {
			aSimulator = new Simulator(_activePath[iSimul]+_protocolFile[iSimul], resultDir, true, true);
			LogFile.writeLog("Initialization ("+resultFullURL+"):OK");
			return true;
		} catch (Exception e) {
			LogFile.writeLog("Initialization ("+resultFullURL+"):ERROR");
			return false;
		}
	}

	/**
	 * Checks the protocol file to see whether we want to
	 * restart a previous simulation
	 * 
	 * @added 26.1.2009
	 * @author Brian Merkey (brim@env.dtu.dk, bvm@northwestern.edu)
	 */
	public static boolean checkForRestart(String protocolFile) {
		XMLParser inputFile = new XMLParser(protocolFile);
		XMLParser restartInfo = new XMLParser(inputFile.getChildElement("simulator"));

		return restartInfo.getParamBool("restartPreviousRun");
	}

	/**
	 * Execute the Simulator object
	 * @param iSimul
	 */
	public static void launchSimulation(int iSimul) {
		try {
			begin = System.currentTimeMillis();
			aSimulator.run();
			begin = Math.round(System.currentTimeMillis()-begin);

			String time = ExtraMath.toString(begin/1e3/60, false);
			LogFile.writeLog("Simulation succesfully achieved in "+time+" minutes.");

		} catch (Exception e) {
			System.out.println("At Idynomics:launch simulation error met :" + e);
			e.printStackTrace();
			LogFile.writeLog("Simulation failed. " + e);
		}
	}

	/**
	 * 
	 * @param aDirectory
	 * @param filter
	 * @param fileList
	 */
	public static void listDirectory(java.io.File aDirectory, String filter, LinkedList<File> fileList) {
		if (!aDirectory.isDirectory() && aDirectory.getName().contains("."+filter)) {
			// if it was just a file, add the file as the only member in the list
			fileList.addLast(aDirectory);
		} else {
			// for a directory, add all contained files to the list
			java.io.File[] list = aDirectory.listFiles(new utils.Jfilter(filter));
			for (int i = 0; i<list.length; i++) {
				fileList.addLast(list[i]);
			}
		}
	}
}
