package iDynoOptimizer.Results;

import iDynoOptimizer.Global.FileReaderWriter;
import iDynoOptimizer.Global.XmlParser;
import iDynoOptimizer.Results.Agent.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

import java.io.File;
import java.text.MessageFormat;
import java.util.*;

import iDynoOptimizer.Results.Feature.SpatialIterationFeature.AgentTypeChoice;

public class IterationResult {


    private static final String agentStateExt2 = "csv";
    private static final String povExt = "pov";
    private static final String agentStateExt = "xml";
    private static final String lastIterationName = "last";
    private static final int lastIterationPH = -1;
    private static final int allIterationPH = -2;
    private static final String agentTag = "species";
    private static final String gridTag = "grid";
    private static final String[] agentTagAttributes = new String[]{"name", "header"};
    private static final String splitter = ",";
    private static final String lineEnd = ";";

    private static final String agentFolder = "agent_State";
    private static final String agentDeathFolder = "agent_StateDeath";

    private static final String agentFileNamePartial = "agent_State";
    private static final String agentFileNamePartial2 = "cells.";
    private static final String agentDeathFileNamePartial = "agent_StateDeath";

    private static final String agentFileNamePattern = "{0}{1}.{2}";

    // private static final String agentFileName = agentFolder + "({0}).xml";
    // private static final String agentDeathFileName = agentDeathFolder + "({0})" + agentStateExt;

    private static final String agentFolderLastIter = "lastIter";
    private static final String povRayFolder = "povray";

    private String resultPath;
    private int iteration;
    private int lastIteration;
    private double time;

    //represents the state of all the members of each species of this simulation result
    private Map<String, List<Agent>> speciesLists;
    private Map<String, List<Agent>> deadSpeciesLists;

    private Map<String, List<Agent>> allSpeciesLists = null;

    private Grid grid;

    public IterationResult(String resultPath, int iteration, AgentTypeChoice agentTypeChoice, int lastIteration) {
        this.resultPath = resultPath;
        this.iteration = iteration;
        this.lastIteration = lastIteration;
        if (agentTypeChoice == AgentTypeChoice.Alive) generateLivingAgents();
        else if (agentTypeChoice == AgentTypeChoice.Dead) generateDeadAgents();
        else {
            generateLivingAgents();
            generateDeadAgents();
        }




    }




    public static int getIterationCount(String resultsPath) {
        File f = new File(resultsPath + File.separator + agentFolder);
        File[] files = null;
        if (f.exists()) {
            files = f.listFiles();
        } else {
            f = new File(resultsPath + File.separator + "1" + File.separator + agentFolder);
            if (f.exists())
                files = f.listFiles();

        }
        int count = 0;
        if (files != null) {
            for (File ags : files) {
                String fileName = ags.getName();
                if (fileName.endsWith(agentStateExt) || fileName.endsWith(agentStateExt2)) count++;
            }
        }

        return count;

    }


    private void generateGrid(String fileName) {
        Element gridNode = (Element) XmlParser.getElements(fileName, gridTag).item(0);
        grid = new Grid(gridNode.getAttribute("resolution"), gridNode.getAttribute("nI"), gridNode.getAttribute("nJ"), gridNode.getAttribute("nK"));
    }


    private void generateCDynamicAgents(String fileName, AgentType at) {

        NodeList agentsTag = XmlParser.getElements(fileName, agentTag);

        generateGrid(fileName);


        Map<String, List<Agent>> sl = new HashMap<String, List<Agent>>();


        for (int i = 0; i < agentsTag.getLength(); i++) {

            Node n = agentsTag.item(i);
            Element e = (Element) n;
            Text t = (Text) e.getFirstChild();

            List<Agent> agents = new ArrayList<Agent>();


            //Family f, double birthday, Mass m, Size s, double growthRate, double volumeRate, Location l, int state

            String name = e.getAttribute(agentTagAttributes[0]);

            String header = e.getAttribute(agentTagAttributes[1]);

            String[] headerParts = header.split(splitter);

            String contents = t.getNodeValue();

            if (contents.contains(lineEnd)) {
                String[] contentLines = contents.split(lineEnd);


                AgentBuilder ab = new AgentBuilder();
                for (String cl : contentLines) {

                    cl = cl.replace("\n", "").trim();

                    if (!cl.equals("")) {
                        String[] cPart = cl.split(splitter);


                        for (int j = 0; j < headerParts.length; j++) {
                            ab.set(AgentPartName.valueOf(headerParts[j].trim()), cPart[j]);

                        }
                        Agent agent = ab.build();
                        agents.add(agent);
                    }
                }
                sl.put(name, agents);
            }


        }
        if (at == AgentType.living) speciesLists = sl;
        else deadSpeciesLists = sl;

    }

    private void generateBiocellionAgents(String fileName) {

        grid = new Grid(8.0, 36, 36, 1);


        List<Agent> agents = new ArrayList<Agent>();

        List<String> lines = FileReaderWriter.readLines(fileName);
        String line;

        String header = lines.get(0);
        String[] headerParts = header.split(splitter);
        String[] cParts;
        //the first line is the header and can be skipped

        AgentBuilder ab = new AgentBuilder();
        for (int i = 1; i < lines.size(); i++) {
            line = lines.get(i);

            cParts = line.split(splitter);

            for (int j = 0; j < headerParts.length; j++) {
                ab.set(AgentPartName.valueOf(headerParts[j].toLowerCase().replace(":", "").replace("\"", "").trim()), cParts[j].trim());
            }


            Agent agent = ab.build();
            agents.add(agent);

        }

        Map<String, List<Agent>> sl = new HashMap<String, List<Agent>>();
        sl.put("BIOCELLION AGENTS", agents);
        speciesLists = sl;


        PovRayScene pvr = new PovRayScene(getAliveandDeadSpeciesLists(), grid);
        pvr.writeAll(resultPath + File.separator + "POVRAYS", iteration);

    }

    private void generateLivingAgents() {
        generateAgents(AgentType.living);
    }

    private void generateDeadAgents() {
        generateAgents(AgentType.dead);
    }

    private void generateAgents(AgentType at) {
        String aFileName = "";
        String var = "";
//        if (iteration == lastIteration) {
//            var = lastIterationName;
//            aFileName = agentFolderLastIter + File.separator;
//
//            if (at == AgentType.living) aFileName += agentFileName;
//            else aFileName += agentDeathFileName;
//        } else {
        var = String.valueOf(iteration);


        aFileName = agentFolder + File.separator + agentFileNamePattern;
        if (at == AgentType.living)
            aFileName = MessageFormat.format(aFileName, agentFileNamePartial, "(" + iteration + ")", agentStateExt);

        else
            aFileName = MessageFormat.format(aFileName, agentDeathFileNamePartial, "(" + iteration + ")", agentStateExt);


        aFileName = resultPath + File.separator + aFileName;
        if ((new File(aFileName)).exists()) {
            generateCDynamicAgents(aFileName, at);
            return;
        }

        aFileName = agentFolder + File.separator + agentFileNamePattern;
        aFileName = MessageFormat.format(aFileName, agentFileNamePartial2, iteration, agentStateExt2);
        aFileName = resultPath + File.separator + aFileName;

        if ((new File(aFileName)).exists())
            generateBiocellionAgents(aFileName);

        //}


    }

    private Map<String, List<Agent>> getSpeciesLists() {

        return speciesLists;
    }


    private Map<String, List<Agent>> getDeadSpeciesLists() {
        return deadSpeciesLists;
    }


    public Map<String, List<Agent>> getAliveandDeadSpeciesLists() {


        if(allSpeciesLists == null)
        {
            allSpeciesLists = new HashMap<>();
        }

        if(allSpeciesLists.isEmpty())
        {
            if (speciesLists != null) allSpeciesLists.putAll(speciesLists);
            if (deadSpeciesLists != null) allSpeciesLists.putAll(deadSpeciesLists);
        }


        return allSpeciesLists;

    }


    public Grid getGrid() {
        return grid;
    }





    public void delete()
    {

        if(speciesLists != null && speciesLists.size() > 0) IterationResult.delete(speciesLists);
        if(deadSpeciesLists != null && deadSpeciesLists.size() > 0) IterationResult.delete(deadSpeciesLists);
        speciesLists = null;
        deadSpeciesLists = null;
        grid = null;

    }

    public static void delete(Map<String, List<Agent>> m)
    {

        for(String s : m.keySet())
        {
            List<Agent> agents = m.get(s);

            for(Agent a : agents)
            {
                a.delete();
            }
        }
    }


    public static String getPovRayFolder() {
        return povRayFolder;
    }

    public static String getPovExt() {
        return povExt;
    }

    public static String getAgentStateExt() {
        return agentStateExt;
    }

    public static String getAgentStateExt2() {
        return agentStateExt2;
    }

    public static int getAllIterationPH() {
        return allIterationPH;
    }

    public static int getLastIterationPH() {
        return lastIterationPH;
    }

    public static String getAgentFolder() {
        return agentFolder;
    }
}
