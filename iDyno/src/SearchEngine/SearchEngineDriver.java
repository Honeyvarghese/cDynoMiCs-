package SearchEngine;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.Map;

import javax.xml.transform.TransformerException;


import idyno.Idynomics;

public class SearchEngineDriver {
	
	//To automatically run simulations for different levels of glucose(as specified by for loop interval
	
	public static void main(String args[]) throws Exception
	{
	
		Map<String,String> map=new HashMap<String,String>();
		XMLReader obj= new XMLReader();
		File file=new File("Out.txt");
		for(double glucose=0.1;glucose<=0.4;glucose=glucose+0.05)			
		{
			
			
					WriteToFile.write(Constants.OUTPUT_PATH, glucose+",");
                  ((HashMap<String, String>) map).put("seed",""+glucose);
                    XMLReader.updateParameter((map));
             //Uncoment this when you run your search Engine       
         // Idynomics.iDyno();
          LatestFileprocessor.func();
                                                                                                                                                                                                                                                                                                                                                                                                                                                                 
				
			}}}