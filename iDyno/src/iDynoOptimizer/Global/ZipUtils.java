package iDynoOptimizer.Global;


import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtils {

    private Map<String, List<String>> fileList;
    private String sourceParentDir;
    private String sourceDir;

    public ZipUtils(String sourceDirectory) {
        fileList = new HashMap<>();

        File toZip = new File(sourceDirectory);

        if (!toZip.exists() || !toZip.isDirectory()) {
            throw new IllegalArgumentException("The folder you're trying to zip does not exist or is not a directory");
        }


        sourceParentDir = toZip.getParent();
        sourceDir =toZip.getPath();
        generateFileList(toZip, "");
    }


    public void zipIt(String zipFileName, boolean deleteAfterZip) {


        zipFileName += "zippped";


        byte[]           buffer = new byte[1024];
        FileOutputStream fos;
        ZipOutputStream  zos    = null;
        try {

            fos = new FileOutputStream(zipFileName);
            zos = new ZipOutputStream(fos);

            FileInputStream in = null;

            for (String dir : this.fileList.keySet()) {
                {
                    for (String file : fileList.get(dir)) {

                        String innerZipFolderName=dir + File.separator + file;

                        String sourcePath = sourceParentDir+ File.separator + innerZipFolderName;

                        ZipEntry ze = new ZipEntry(innerZipFolderName);
                        zos.putNextEntry(ze);
                        try {
                            in = new FileInputStream(sourcePath);
                            int len;
                            while ((len = in.read(buffer)) > 0) {
                                zos.write(buffer, 0, len);
                            }
                        } finally {
                            in.close();
                        }
                    }
                }
            }

            zos.closeEntry();


        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        if(deleteAfterZip) delete();

    }


    private void delete()
    {
        try {
            FileUtils.deleteDirectory(new File(sourceDir));
        }
        catch (IOException e)
        {
            MyPrinter.Printer().printErrorln("Couldn't delete after zip:" + sourceDir);
        }
    }

    private void generateFileList(File node, String runningPath) {

        // add file only
        if (node.isFile()) {
            fileList.get(node.getParent().replace(sourceParentDir + File.separator, "")).add(node.getName());

        }

        if (node.isDirectory()) {
            String[] subNote = node.list();

            if(!runningPath.isEmpty()) runningPath += File.separator + node.getName();
            else  runningPath += node.getName();

            fileList.put(runningPath, new ArrayList<>());

            for (String filename : subNote) {
                generateFileList(new File(node, filename), runningPath);
            }
        }
    }


//    private String generateZipEntry(String file) {
//        return file.substring(SOURCE_FOLDER.length() + 1, file.length());
//    }
}