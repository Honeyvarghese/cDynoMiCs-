package iDynoOptimizer.Global;
import iDynoOptimizer.Results.IterationResult;

import java.io.*;
import java.nio.file.CopyOption;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by Chris Johnson on 1/9/2015.
 */
public class FileReaderWriter {






    public static List<File> getFilesInDir(String dir)
    {
        List<File> fileList = new ArrayList<File>();
        File resultDir = new File(dir);
        if(resultDir.isFile()) fileList.add(resultDir);
        else
        {
            for (File f : resultDir.listFiles())  fileList.add(f);
        }

        return fileList;


    }

    public static List<String> readLines(String fileName) {

        try {
            return Files.readAllLines(Paths.get(fileName));
        } catch (IOException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static void writeLines(String fileName, List<String> lines) {
        try {
            File f = new File(fileName);

            f.createNewFile();

            FileWriter fw = new FileWriter(fileName);
            for (String s : lines) {


                fw.write(s);
                fw.write(System.getProperty("line.separator"));


            }
            fw.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }



    public static void writeSingleFile(String text, String filePath, String fileName, boolean append)
    {
        try {
            File dir = new File(filePath);
            if (!dir.exists()) dir.mkdirs();
            FileWriter  writer = new FileWriter(filePath + File.separator + fileName, append);
            BufferedWriter bw = new BufferedWriter(writer);
            bw.write(text);
            bw.close();
        }
        catch (IOException e)
        {
            MyPrinter.Printer().printErrorln("Error in writing to file in FileReaderWriter");
            e.printStackTrace();
        }
    }

}
