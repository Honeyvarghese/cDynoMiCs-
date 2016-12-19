package SearchEngine;

import java.io.FileWriter;
import java.io.IOException;

public class WriteToFile {

	
	   //public static void main(String[] args) throws IOException
	public static void write(String pathname, String text) throws IOException
	
	   {
		//String filename= "C:\\Users\\Honey\\Desktop\\Out.txt";
	    FileWriter fw = new FileWriter(pathname,true); //the true will append the new data
	    fw.write(text);//appends the string to the file
	    fw.close();
		
	}

}
