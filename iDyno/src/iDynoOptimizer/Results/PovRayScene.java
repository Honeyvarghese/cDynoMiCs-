package iDynoOptimizer.Results;

import iDynoOptimizer.Results.Agent.Grid;
import povray.*;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.Map;


import iDynoOptimizer.Results.Agent.Agent;

/**
 * Created by Chris on 9/27/2015.
 */
public class PovRayScene {


    final private static String INCHEADER = "sceneheader.inc";
    final private static String INCFOOTER = "scenefooter.inc";
    private VectorProperty translate, rotate;


    private Camera _camera;
    private Background _background;
    private LightSource[] _lightSource;


    private double _x, _y, _z;
    private double _scaling;
    private boolean _is3D;
    private VectorProperty corner1;
    private VectorProperty corner2;



    private Map<String, List<Agent>> _agents;

    private String _name;

    double _resolution;


    public PovRayScene(Map<String, List<Agent>> agents, Grid grid) {

        _scaling = Math.max(Math.max(grid.getnI(), grid.getnJ()), grid.getnK()) * grid.getResolution() ;
        _x = grid.getnI() * grid.getResolution()  / _scaling;
        _y = grid.getnJ() * grid.getResolution()  / _scaling;
        _z = grid.getnK()* grid.getResolution()  / _scaling;
        _resolution = grid.getResolution();
        _is3D = grid.getnK() > 1;
        initializeScene();
        initializeTranslatRotate();
        initializeBoundary();
        _agents = agents;

        _name ="biocellionParts";
    }





    public void writeAll(String dir, int iteration)
    {
        try {
            File dirF = new File(dir);
            dirF.mkdir();
            writePovrayIncFiles(dir);
            writeModelState(dir + File.separator + iteration + "." + IterationResult.getPovExt());

        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }

    private void writeModelState(String fileName) throws IOException {


        File sysData = new File(fileName);
        sysData.delete();
        sysData.createNewFile();
        FileWriter fr = new FileWriter(sysData);

        fr.write("#include \"" + "colors.inc" + "\"\n");
        fr.write("#include \"" + INCHEADER + "\"\n");

        particlesToFile(fr);

        fr.write("#include \"" + INCFOOTER + "\"\n");
        fr.close();
    }


    private void particlesToFile(FileWriter f) throws IOException {
        for (List<Agent> agents : _agents.values()) {

            for (Agent anAgent : agents) {
                f.write(writeSingleParticle(anAgent.getLocation().getX(), anAgent.getLocation().getY(), anAgent.getLocation().getZ(), anAgent.getSize().getRadius()));
            }
        }
    }

    private String writeSingleParticle(double x, double y, double z, double radius) {

        VectorProperty center = new VectorProperty("");
        double radiusCore;
        center.setValues(x / _scaling, y / _scaling, z / _scaling);
        radiusCore = radius /_scaling;

        double activeFrac = 1;
        String core = "sphere {\n"
                + "\t " + center + "\n"
                + "\t " + radiusCore + "\n"
                //+ "\t pigment { " + _nameCore + "*" + _activeFrac + " }\n"
                 + "\t FinishMacro ( " + center + "," + _name + "Finish," + _name + "*" + activeFrac + ")\n"
                //+ "\t finish { " + _nameCore+"Finish }\n"
                //+ "\t finish { reflection 0.3 phong 1 }\n"
                // +"\t pigment { " + _colorCore + " }\n"
                + "}\n";
        return core;
    }

    private void biofilmFooterToFile(FileWriter f) throws IOException {
        f.write("\t" + translate + "\n");
        f.write("\t"+rotate+"\n");
        f.write("}");
    }
    private void biofilmHeaderToFile(FileWriter f) throws IOException {
        f.write("union {\n");
        f.write(writeBoundary());
    }

    private String writeBoundary() {
        return "box {\n"
                + "\t "
                + corner1
                + "\n"
                + "\t "
                + corner2
                + "\n"
                + "\t pigment { " +
                "color rgb <0.2,  0.2,  0.2>"
                + " }\n"
                + "\t\tfinish {\n"
                + "\t\t\t phong 0.9\n"
                + "\t\t\t phong_size 60\n"
                + "\t\t metallic }\n"
                + "}\n";
    }


    private void writePovrayIncFiles(String dir) throws IOException {


        // header include file
        File header = new File(dir+ File.separator + INCHEADER);
        FileWriter fr = new FileWriter(header);
        fr.write(_camera.toString());
        fr.write(_background.toString());
        fr.write(_lightSource[0].toString());
        fr.write(_lightSource[1].toString());
        biofilmHeaderToFile(fr);

        fr.write("#declare crossPlane= <1,1,1>;\n#declare visibleFinish = finish { reflection 0.3 phong 1 };\n#declare invisibleFinish = finish { phong 0 };\n");
//        for (Species aSpecies : mySim.speciesList)
//            //aSpecies.getProgenitor().writePOVColorDefinition(fr);
//            aSpecies.getProgenitor().writePOVColorDefinition(fr, aSpecies.isVisible);

        writePOVColorDefinition(fr, true);

        fr.write(macroWriter());
        fr.close();

        // footer include file
        File footer = new File(dir+File.separator+INCFOOTER);
        fr = new FileWriter(footer);
        biofilmFooterToFile(fr);
        fr.close();

    }

    private void writePOVColorDefinition(FileWriter fr, boolean visible) throws IOException {

        fr.write("#declare "+_name+" = color rgb < ");
        fr.write(((float) 0) / 255.0 + " , ");
        fr.write(((float)0) / 255.0 + " , ");
        fr.write(((float) 255) / 255.0 + " >");
        fr.write(";\n");
        fr.write("#declare "+_name+"Finish = visibleFinish;\n");
//        if(visible)
//        {
//            fr.write("#declare "+_species.speciesName+" = color rgb < ");
//            fr.write(((float) _species.color.getRed()) / 255.0 + " , ");
//            fr.write(((float) _species.color.getGreen()) / 255.0 + " , ");
//            fr.write(((float) _species.color.getBlue()) / 255.0 + " >");
//            fr.write(";\n");
//            fr.write("#declare "+_species.speciesName+"Finish = visibleFinish;\n");
//        }
//        else
//        {
//            fr.write("#declare "+_species.speciesName+" = color rgbt < ");
//            fr.write(((float) _species.color.getRed()) / 255.0 + " , ");
//            fr.write(((float) _species.color.getGreen()) / 255.0 + " , ");
//            fr.write(((float) _species.color.getBlue()) / 255.0 + " ,");
//            fr.write( " 1>");
//            fr.write(";\n");
//            fr.write("#declare "+_species.speciesName+"Finish = invisibleFinish;\n");
//        }

    }







    private String macroWriter() {
        return 	"#macro FinishMacro(loc, fin, col)\n" +
                "  #if(loc.x<=crossPlane.x & loc.y<=crossPlane.y & loc.z<=crossPlane.z)\n" +
                "	  pigment { col }\n" +
                "	  finish{ fin }\n" +
                "  #else\n" +
                "	  pigment { rgbt<1,1,1,1> }\n" +
                "	  finish { invisibleFinish }\n" +
                "  #end\n" +
                "#end";
    }



    private void initializeScene() {
        // Set the camera
        _camera = new Camera();
        _lightSource = new LightSource[2];

        if (_is3D) {
            _camera.setLocation(0, _y * 1.5, +_x * 1.5);

            _lightSource[0] = new LightSource();
            _lightSource[0].setLocation(_y, _x, -_z);
            _lightSource[0].setColor(1f, 1f, 1f);

            _lightSource[1] = new LightSource();
            _lightSource[1].setLocation(-_y, _x, -_z);
            _lightSource[1].setColor(1f, 1f, 1f);
        } else {
            _camera.setLocation(0, 0, 1.2);

            _lightSource[0] = new LightSource();
            _lightSource[0].setLocation(2.0, 1.0, +_x * 1.5);
            _lightSource[0].setColor(1f, 1f, 1f);

            _lightSource[1] = new LightSource();
            _lightSource[1].setLocation(2.0, 1.0, +_x * 1.5);
            _lightSource[1].setColor(1f, 1f, 1f);

        }

        _camera.setLook_at(0, 0, 0);

        // these set the aspect ratio of the view
        _camera.setUp(0, 1, 0);
        _camera.setRight(-1.33f, 0, 0);
        _camera.setAngle(60);

        // Set the background
        _background = new Background();
        _background.setColor(1f, 1f, 1f);

    }

    private void initializeTranslatRotate() {
        if (_is3D) {
            translate = new VectorProperty("translate");
            translate.setValues(-_x * 0.4f, -_y * 0.5f, -_z * 0.5f);

            rotate = new VectorProperty("rotate");
            rotate.setValues(0, 55, 90);
        } else {
            // just want to center the view
            translate = new VectorProperty("translate");
            translate.setValues(-_x * 0.5f, -_y * 0.5f, 0);

            rotate = new VectorProperty("rotate");
            rotate.setValues(0, 0, 90);
        }


    }

    private void initializeBoundary()
    {

        corner1 = new VectorProperty("");
        corner2 = new VectorProperty("");
        if(_is3D)
        {
            //fill in bottom boundary for 3d
        }
        else
        {
            //bottom boundary
            corner1.setValues(0, 0, 0);
            corner2.setValues(-_x * 0.01f, _y, _z);
        }

    }



}
