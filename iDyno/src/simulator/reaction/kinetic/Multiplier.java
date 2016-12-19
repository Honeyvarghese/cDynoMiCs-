package simulator.reaction.kinetic;

import org.jdom.Element;
import simulator.SpatialGrid;
import utils.XMLParser;

/**
 * Created by chris on 6/25/2015.
 */
public class Multiplier extends IsKineticFactor implements IsCompositionFactor {


    private double _multiplier;

    public Multiplier()
    {
        nParam = 1;
    }


    @Override
    public void init(Element defMarkUp) {

        _multiplier  = (new XMLParser(defMarkUp)).getParamDbl("multiplier");
    }

    @Override
    public void initFromAgent(Element aReactionRoot, double[] kineticParam, int paramIndex) {

    }

    @Override
    public double kineticValue(double solute) {

        return solute * _multiplier;

    }

    @Override
    public double kineticDiff(double solute) {
        return 0;
    }

    @Override
    public double kineticMax() {
        return 0;
    }

    @Override
    public double kineticValue(double solute, double[] paramTable, int index) {
        return 0;
    }

    @Override
    public double kineticDiff(double solute, double[] paramTable, int index) {
        return 0;
    }

    @Override
    public double kineticMax(double[] paramTable, int index) {
        return 0;
    }


    @Override
    public void compose(boolean noAccumulation, SpatialGrid output, SpatialGrid input, int i, int j, int k) {

        if(noAccumulation) output.grid[i][j][k] = kineticValue(input.grid[i][j][k]);
        else output.grid[i][j][k] += kineticValue(input.grid[i][j][k]);

    }
}
