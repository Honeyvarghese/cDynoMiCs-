/**
 * Project iDynoMiCS (copyright -> see Idynomics.java)
 * <p>
 * ___________________________________________________________________________
 * BoundaryBulk : the concentration on the boundary is fixed by a dynamical
 * bulk, the agents crossing this line die
 *
 * @version 1.0
 * @author Ahmadreza Ghaffarizadeh (ghaffarizadeh@aggiemail.usu.edu)
 * @since June 2006
 */

/**
 * @since June 2006
 * @version 1.0
 * @author Ahmadreza Ghaffarizadeh (ghaffarizadeh@aggiemail.usu.edu)
 */

package simulator.geometry.boundaryConditions;

import utils.XMLParser;

import simulator.*;
import simulator.geometry.*;
import simulator.agent.LocatedAgent;
import simulator.agent.LocatedGroup;

public class BoundaryAgar extends AllBC {

    /* _____________________________ FIELDS _______________________________ */
    // Serial version used for the serialisation of the class
    private static final long serialVersionUID = 6L;

    // At which bulk the liquid phase is connected
    protected Agar _Agar;
    simulator.Simulator mySim;

    /* ______________ INTERNAL TEMPORARY VARIABLES ____________________ */
    protected static ContinuousVector vectorIn;

    /* ___________________________ CONSTRUCTOR _____________________________ */
    public BoundaryAgar() {
        hasAgar = true;
    }

    public void init(Simulator aSim, Domain aDomain, XMLParser aBoundCondMarkUp) {
        // Load the geometry of the boundary
        _isSupport = true;

        _canAttachTo = aBoundCondMarkUp.getParamBool("canAttachTo");
        mySim = aSim;
        readGeometry(aBoundCondMarkUp, aDomain);

        aDomain.addBoundary(this);

        // Load description of the connected bulk
        String agarName = aBoundCondMarkUp.getParam("agar");
        _Agar = aSim.world.getAgar(agarName);
    }

	/* ____________________________ SOLVER _________________________________ */

    public void refreshBoundary(SoluteGrid aSoluteGrid) {
        // Store the concentration in the bulk
        //bulkValue = _connectedBulk.getValue(aSoluteGrid.soluteIndex);
        // bulkValue = 1;
        // Initialise the course along the shape of the boundary
        _myShape.readyToFollowBoundary(aSoluteGrid);
        if (_Agar.hasBeenSolved())
            while (_myShape.followBoundary(dcIn, dcOut, aSoluteGrid)) {
                aSoluteGrid.setValueAt(getAgarValue(dcIn), dcOut);
            }
    }

    // bvm note 15.12.08: modified inputs to allow reaction or flux-based bulk treatment
    public void updateAgar(SoluteGrid[] soluteGrid, SoluteGrid[] reacGrid, double timeStep) {
        _Agar.updateAgar(soluteGrid, reacGrid, timeStep);
    }

    public void updateAgar(SoluteGrid soluteGrid, double timeStep) {
        _Agar.updateAgar(soluteGrid, timeStep);
    }

    public Agar getAgar() {
        return _Agar;
    }

    public double getAgarValue(DiscreteVector dc) {
        //This dc is dcOut that is -1 of the actual grid location,
        // so must +1 to access agar grid because the agar and grid use the same coordaintes
        return _Agar.getValue(dc.j + 1, dc.k + 1);
    }

	/* _______________________ LOCATED Agents ______________________________ */

    public ContinuousVector lookAt(ContinuousVector cc) {
        return cc;
    }

    /**
     * Label a LocatedGroup which has been identified being outside this
     * boundary
     */
    public void setBoundary(LocatedGroup aGroup) {
        aGroup.status = 44;
        // status 3 -> agar
    }

    /**
     * Modify the movement vector : the new position is the orthognal projection
     * of the outside point on the boundary surface
     *
     * @see LocatedAgent.move();
     */
    public void applyBoundary(LocatedAgent anAgent, ContinuousVector target) {
        // Define coordinates of the corrected position
        _myShape.orthoProj(target, target);

        // Build a vector normal to the boundary and starting from the
        // orthogonal projection
        vectorIn = new ContinuousVector(_myShape.getNormalInside(target));

        // The whole cell has to be inside, so make a translation equal to the
        // total radius
        vectorIn.times(anAgent.getRadius(true));

        // Compute the new position
        target.add(vectorIn);

        // Compute and update the movement vector leading to this new position
        anAgent.getMovement().sendDiff(anAgent.getLocation(), target);
    }


    public String toString() {
        return new String("Bulk:" + this._mySide);
    }
}
