package SearchEngine;

import java.io.File;
import java.io.IOException;

public class LatestFileprocessor {

//public static void main (String[] args) throws IOException
	public static  void func() throws IOException

{
String path =Constants.RESULT_PATH;
File file = new File(path);
String[] names = file.list();


   // if (new File(name).isDirectory())
    
      String latest= names[names.length-1];
      System.out.println("......>"+latest);
    
   // String last=path+latest;
  //  System.out.println(last);
    SoluteAverage.func(path,latest);
}

		// TODO Auto-generated method stub
		
	}
