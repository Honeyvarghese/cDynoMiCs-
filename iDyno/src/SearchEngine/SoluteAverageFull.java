package SearchEngine;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.comparator.LastModifiedFileComparator;

public class SoluteAverageFull {
	
	private static final String RESULT_PATH = "C:\\Users\\Honey\\Desktop\\cDynomicsV4\\iDyno\\iDyno\\resultss\\";
	private static String separator = "	";

	public static void main(String[] args) throws IOException {
		/*File file = new File(RESULT_PATH);
		String[] names = file.list();
		Arrays.sort(names);
		// reading the latest contact folder
		String name = names[names.length - 1];
		*/
		String name = "10";
		
		consolidateSoluteConcentrations(name);
	}

	private static void consolidateSoluteConcentrations(String name) throws IOException {
		boolean vascular = false;
		String[] names=new String[1000000];
		File lastResultDirectory = new File(RESULT_PATH + name + "\\SoluteConcentration\\xy-1\\");
	    File[] files = lastResultDirectory.listFiles();
		Arrays.sort(files, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);

		for(int p=0;p<files.length;p++)
		{
		names[p]=files[p].getPath();
		//System.out.println(""+"/n"+p);
		}
		
		

//Arrays.sort();
		//
//Collections.sort(names, LastModifiedFileComparator.LASTMODIFIED_COMPARATOR);
		List<Double> glucose = new ArrayList<Double>();
		List<Double> acetate = new ArrayList<Double>();
		List<Double> methane = new ArrayList<Double>();
		// if (files.length > 0) {
		        /** The newest file comes first **/
		       // 
		for (int i = 0; i < files.length; i++) {
			if (names[i].contains(".txt")) {
				String[] splits = names[i].split("\\.");
				int hour = Integer.parseInt(splits[0].substring(splits[0].length() - 3, splits[0].length()));
				//System.out.println(hour);
				String filePath = RESULT_PATH + name + "\\SoluteConcentration\\xy-1\\" + names[i];
				List<String> lines = Files.readAllLines(Paths.get(names[i]));

				double sum = 0;
				for (int k = lines.size() - 1; k >= 0; k--) {
					String[] digitStrings = lines.get(k).split(separator);
					
					for (int j = 0; j < digitStrings.length; j++) {
						sum += Double.parseDouble(digitStrings[j]);
					}
				}
				if (names[i].contains("Glucose")) {
					//System.out.println(names[i]+sum);
					glucose.add(sum);
				}
				
				if (names[i].contains("Acetate")) {
					//System.out.println(names[i]+sum);

					acetate.add(sum);
				}
				if (names[i].contains("Methane")) {
					methane.add(sum);
				}
			
			}
		}
		List<String> outputLines = new ArrayList<String>();
		for (int i = 0; i < glucose.size(); i++) {
			outputLines.add(i + "," + glucose.get(i) + ","+ acetate.get(i) + "," + methane.get(i));
			System.out.println(outputLines.get(i));
		}
		
		
		FileUtils.writeLines(new File(RESULT_PATH + name + "\\SoluteConcentration\\Consolidated.txt"), outputLines);
	}
}
