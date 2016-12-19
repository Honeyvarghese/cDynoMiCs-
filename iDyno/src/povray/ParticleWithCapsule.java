
/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 */

/**
 * @since Feb 2007
 * @version 1.0
 * @author João Xavier (xavierj@mskcc.org), Memorial Sloan-Kettering Cancer Center (NY, USA)
 * @author Laurent Lardon (lardonl@supagro.inra.fr), INRA, France
 */


package povray;

import farzin.Logger;
import idyno.SimTimer;
import simulator.agent.LocatedAgent;
import simulator.agent.zoo.Yeast;
import simulator.geometry.ContinuousVector;

import java.awt.*;
import java.io.Serializable;


public class ParticleWithCapsule implements Serializable {
    // Serial version used for the serialisation of the class
    private static final long serialVersionUID = 1L;

    private VectorProperty center;

    private double _radiusCore, _radiusCapsule;
    private VectorProperty _colorCore, _colorCapsule;
    private String _nameCore, _nameCapsule;
    private boolean _hasCapsule;
    private double _activeFrac;
    public int id;

    /* _________________ CONSTRUCTOR _________________________ */
    public ParticleWithCapsule() {
        center = new VectorProperty("");
        _colorCore = new VectorProperty("color rgb");
    }

    public ParticleWithCapsule(LocatedAgent p) {
        id = p._birthId;
        center = new VectorProperty("");
        setCenter(p.getLocation());

        _colorCore = new VectorProperty("color rgb");

      setColorCore(p.getColor());
        setCoreRadius(p.getRadius(true));

        // bvm 27.1.2009 for using color definitions

        setNameCore(p.getName());
        setActiveFrac(p.getActiveFrac());

        _hasCapsule = p.hasEPS();
        //TODO
        // NOTE: if this is set to true, need to modify the agent.Species routine
        // that creates color definitions so that the '-capsule' colors are defined
        _hasCapsule = false;
        if (_hasCapsule) {
            _radiusCapsule = p.getRadius(true) / Povray3DScene.getScaling();
            _colorCapsule = new VectorProperty("rgbf");
            if(p.getBirthday()== SimTimer.getCurrentIter())setColorCapsule(Color.RED);
            else setColorCapsule(p.getColor());
            // bvm 27.1.2009 for using color definitions
            setNameCapsule(p.getSpecies().speciesName + "-capsule");
        }
    }

    //Revision 2. Farzin

    public ParticleWithCapsule(Yeast p) {
        id = p._birthId;
        center = new VectorProperty("");
        setCenter(p.getLocation());

        _colorCore = new VectorProperty("color rgb");
        if (p.getTuringValue() > 0) {
            //setColorCore(Color.red);
            _nameCore = "Activated";
        } else {
            _nameCore = "Inhibited";
            //setColorCore(Color.blue);
        }

        setCoreRadius(p.getRadius(true));

       setColorCore(p.getColor());

        // bvm 27.1.2009 for using color definitions

        //setNameCore(p.getName());
        setActiveFrac(p.getActiveFrac());

        _hasCapsule = p.hasEPS();
        //TODO
        // NOTE: if this is set to true, need to modify the agent.Species routine
        // that creates color definitions so that the '-capsule' colors are defined
        _hasCapsule = false;
        if (_hasCapsule) {
            _radiusCapsule = p.getRadius(true) / Povray3DScene.getScaling();
            _colorCapsule = new VectorProperty("rgbf");
            if(p.getBirthday()== SimTimer.getCurrentIter())setColorCapsule(Color.RED);
            else setColorCapsule(p.getColor());
            // bvm 27.1.2009 for using color definitions
            setNameCapsule(p.getSpecies().speciesName + "-capsule");
        }
    }

    //End Revision 2


    /**
     * @param color
     */
    public void setColorCore(Color c) {
        _colorCore.setValues(((float) c.getRed()) / 255,
                ((float) c.getGreen()) / 255, ((float) c.getBlue()) / 255);
    }

    /**
     * For now sets capsule to gray
     *
     * @param fs
     */
    public void setColorCapsule(Color c) {
        float r = ColorMaps.brightenValue(((float) c.getRed()) / 255, 0.5f);
        float g = ColorMaps.brightenValue(((float) c.getGreen()) / 255, 0.5f);
        float b = ColorMaps.brightenValue(((float) c.getBlue()) / 255, 0.5f);
        _colorCapsule.setValues(r, g, b, 0.999f);
    }

    /**
     * @param theName
     */
    public void setNameCore(String theName) {
        _nameCore = theName;
    }

    /**
     * @param theName
     */
    public void setNameCapsule(String theName) {
        _nameCapsule = theName;
    }

    /**
     * @param activeFrac
     */
    public void setActiveFrac(double activeFrac) {
        _activeFrac = activeFrac;
    }

    /**
     * @param fs
     */
    public void setCenter(ContinuousVector c) {
        double s = Povray3DScene.getScaling();
        center.setValues(c.x / s, c.y / s, c.z / s);
    }

    /**
     * @param fs
     */
    public void setCoreRadius(double fs) {
        _radiusCore = fs / Povray3DScene.getScaling();
    }

    //TJ Flann
    public String toTightJunctionString(ParticleWithCapsule aNeighbor) {
//	    String cylinderString = "cylinder  {\n"
//			+ "\t "	+ center + ",\n"
//			+ "\t " + aNeighbor.center + ",\n"
//			+ "\t "	+ _radiusCore*0.2 + "\n";
//	    if(farzin.Logger.coloringTightJunctions)
//	    {
//	    	if(farzin.Logger.tightJunctions[id][aNeighbor.id]>0)
//	    		cylinderString+= "\t pigment { " + "color rgb <"+ Math.min(1.0, Math.max(farzin.Logger.tightJunctions[id][aNeighbor.id],farzin.Logger.tightJunctions[aNeighbor.id][id])/200) +", 0.0 , 0.0 >"+ " }\n";
//	    	else if(farzin.Logger.tightJunctions[id][aNeighbor.id]<0)
//	    		cylinderString+= "\t pigment { " + "color rgb <0.0, "+ Math.min(1.0, Math.max(-farzin.Logger.tightJunctions[id][aNeighbor.id],-farzin.Logger.tightJunctions[aNeighbor.id][id])/100) +" , 0.0 >"+ " }\n";
//	    	else
//	    		cylinderString+= "\t pigment { color rgb < 0.0 , 0.0 , 1.0 > }\n";
//	    		//cylinderString+= "\t pigment { color rgb < 1.0 , 0.7843137254901961 , 0.0 > }\n";
//	    	farzin.Logger.tightJunctions[id][aNeighbor.id]=0;
//	    	farzin.Logger.tightJunctions[aNeighbor.id][id]=0;
//	    }
//	    else
//			cylinderString+= "\t pigment { " + _nameCore + "*" + _activeFrac + " }\n";
        if (!farzin.Logger.drawingTightJunctions)
            return "";
        String cylinderString = "cylinder  {\n"
                + "\t " + center + ",\n"
                + "\t " + aNeighbor.center + ",\n";
        if (farzin.Logger.coloringTightJunctions) {
            double width = Math.max(4e-4, Math.abs(farzin.Logger.tightJunctions.get(id).get(aNeighbor.id) / 200) * _radiusCore * 0.2);

            // cylinderString+= "\t "	+  Math.abs(farzin.Logger.tightJunctions[id][aNeighbor.id]/200)*_radiusCore*0.2 + "\n";
            cylinderString += "\t " + width + "\n";


            //if(farzin.Logger.tightJunctions[id][aNeighbor.id] > 0)
            if (farzin.Logger.tightJunctions.get(id).get(aNeighbor.id) > 0)
                cylinderString += "\t pigment { " + "color rgb <1.0, 0.0 , 0.0 >" + " }\n";  //color a positive (repellant) force red

                // else if(farzin.Logger.tightJunctions[id][aNeighbor.id]<0)
            else if (farzin.Logger.tightJunctions.get(id).get(aNeighbor.id) < 0) //color a negative (attractive) force green
                cylinderString += "\t pigment { " + "color rgb <0.0, 1.0 , 0.0 >" + " }\n";
            else
                cylinderString += "\t pigment { color rgb < 0.0 , 0.0 , 1.0 > }\n"; //color no force blue
            //cylinderString+= "\t pigment { color rgb < 1.0 , 0.7843137254901961 , 0.0 > }\n";



            cylinderString += "}\n";

            cylinderString += "cylinder  {\n"
                    + "\t " + center + ",\n"
                    + "\t " + aNeighbor.center + ",\n";
            cylinderString += "\t " + 4e-4 + "\n";
            cylinderString += "\t pigment { " + "color rgb <0.0, 0.0 , 0.0 >" + " }\n";
            cylinderString += "}\n";


            // farzin.Logger.tightJunctions.get(id).get(aNeighbor.id)=0;
            // farzin.Logger.tightJunctions.get(aNeighbor.id).get(id)=0;
            Logger.setTightJunctionRecord(id, aNeighbor.id, 0);
            Logger.setTightJunctionRecord(aNeighbor.id, id, 0);
        } else {
            cylinderString += "\t " + _radiusCore * 0.2 + "\n";
            cylinderString += "\t pigment { " + _nameCore + "*" + _activeFrac + " }\n";
            cylinderString += "}\n";
        }


        return cylinderString;
    }

    //FLANN make the sphere shiny and 3D
    public String toString() {

        // bvm 27.1.2009: modified this output to use color definitions and
        // textures rather than pigments
        String core = "sphere {\n"
                + "\t " + center + "\n"
                + "\t " + _radiusCore + "\n"
                //+ "\t pigment { " + _nameCore + "*" + _activeFrac + " }\n"
                + "\t FinishMacro ( " + center + "," + _nameCore + "Finish," + _nameCore + "*" + _activeFrac + ")\n"
                //+ "\t finish { " + _nameCore+"Finish }\n"
                //+ "\t finish { reflection 0.3 phong 1 }\n"
               // +"\t pigment { " + _colorCore + " }\n"
                + "}\n";

        if (_hasCapsule) {
            String capsule = "sphere {\n"
                    + "\t " + center + "\n"
                    + "\t " + _radiusCapsule + "\n"
                    //+ "\t pigment { " + _nameCapsule + "*" + _activeFrac + " }\n"
                    + "\t FinishMacro ( " + center + "," + _nameCore + "Finish," + _nameCapsule + "*" + _activeFrac + ")\n"
                    //+ "\t finish { " + _nameCore+"Finish }\n"
                    //+ "\t finish { reflection 0.3 phong 1 }\n"
                   // +"\t pigment { " + _colorCapsule + " }\n"
                    + "}\n";
            return core + capsule;
        }

        return core;
    }
}
