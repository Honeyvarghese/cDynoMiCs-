package iDynoOptimizer.Protocol.ProtocolFile;

import iDynoOptimizer.Global.Global;
import iDynoOptimizer.Global.MyPrinter;
import iDynoOptimizer.Protocol.Parameters.ParamNum;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ProtocolFile {


    private        String         protocolFileName;
    private static ProtocolFile   classRep;
    private        Tag            root;
    private        List<ParamNum> changingParams;
    private static final String rootTagName = "idynomics";

    public ProtocolFile() {
        root = new Tag(rootTagName);
        changingParams = new ArrayList<ParamNum>();
    }


    public static ProtocolFile getClassRep() {
        if (classRep == null) {
            classRep = new ProtocolFile();
        }
        return classRep;
    }


    public static void copyTargetProtocol() {
        String   sourceTarget = Global.getGlobal().getSourceTargetProtocolFileName();
        String   targetTarget = Global.getGlobal().getTargetProtocolFileNamePathFull();
        String[] sourcesT     = new String[]{sourceTarget};
        String[] targetsT     = new String[]{targetTarget};
        copyFiles(sourcesT, targetsT);
    }


    public static void copyTestProtocols() {

        String sourceTestLoc = Global.getGlobal().getSourceTestProtocolFolder();
        String targetTestLoc = Global.getGlobal().getTestProtocolFolder();

        MyPrinter.Printer().printTier1ln("Copying test protocols from " + sourceTestLoc + " to " + targetTestLoc);


        File   sourceFolder  = new File(sourceTestLoc);





        File[] listOfSources = sourceFolder.listFiles();

        String[] sources = new String[listOfSources.length];
        String[] targets = new String[listOfSources.length];


        for (int i = 0; i < listOfSources.length; i++) {
            if (listOfSources[i].isFile()) {

                sources[i] = sourceTestLoc + File.separator + listOfSources[i].getName();
                targets[i] = targetTestLoc + File.separator + listOfSources[i].getName();

            }

        }

        copyFiles(sources, targets);
    }


    /*
    copies the source target and test protocol files to their results folders
 */
    public static void copyProtocols() {


        //copy the target protocol file
        copyTargetProtocol();
        //copy the test protocols
        copyTestProtocols();

    }

    /*
       copies the specified protocol file to the specified location
     */


    public static void copyMiscFilesToResults(String resultsPath, String[] fileFullPaths) {

        int count = fileFullPaths.length;




        String[] targets = new String[count];

        for(int i = 0; i < count;i++)
        {
            String[] fullFilePathParts = fileFullPaths[i].split(File.separator + File.separator);
            //get just the file name
            targets[i] = resultsPath + File.separator + fullFilePathParts[fullFilePathParts.length - 1];
        }
        copyFiles(fileFullPaths, targets);

    }

    public static String copyProtocolToResults(String resultsPath, String protocolFileName, String protocolFileNamePathFull) {


        String target = resultsPath + File.separator + protocolFileName;

        String[] sources = new String[]{protocolFileNamePathFull};
        String[] targets = new String[]{target};

        copyFiles(sources, targets);

        return target;
    }

    /*
        Copies the files located at "sources" to the location at "targets"
        Copies the first source to the first target, the second source to the second target, etc.
     */
    private static void copyFiles(String[] sources, String[] targets) {


        try {

            for (int i = 0; i < sources.length; i++) {
                FileUtils.copyFile(new File(sources[i]), new File(targets[i]));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (IndexOutOfBoundsException e) {
            e.printStackTrace();
        }
    }


    public Tag getRoot() {
        return root;
    }

    public void setRoot(Tag r) {
        root = r;
    }

    public List<ParamNum> getChangingParams() {

        return changingParams;
    }

    public void setChangingParams(List<ParamNum> params) {
        changingParams = params;
    }


    public void getChangingParamsRecursively() {
        changingParams.clear();
        getChangingParamsRecursively(root);
    }

    private void getChangingParamsRecursively(Tag t) {

        for (ParamNum pn : t.getChangingParams()) {
            changingParams.add(pn);
        }

        if (t.hasChildren()) {

            List<Tag> children = t.getAllChildren();

            for (Tag child : children) {

                getChangingParamsRecursively(child);
            }
        }
    }

    public String getProtocolFileName() {
        return protocolFileName;
    }

    public void setProtocolFileName(String protocolFileName) {
        this.protocolFileName = protocolFileName;
    }
}
