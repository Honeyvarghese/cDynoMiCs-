package simulator.reaction.kinetic;

import org.jdom.Element;
import simulator.SpatialGrid;
import utils.XMLParser;

/**
 * Created by chris on 6/25/2015.
 */
public class AreaMean extends IsKineticFactor implements IsCompositionFactor{


    private int _area;

    public AreaMean()
    {
        nParam = 1;
    }


    @Override
    public void init(Element defMarkUp) {

        _area = (new XMLParser(defMarkUp)).getParamInt("area");
    }

    @Override
    public void initFromAgent(Element aReactionRoot, double[] kineticParam, int paramIndex) {

    }

    @Override
    public double kineticValue(double solute) {

        return solute;
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


        double outValue = 0;
        int tempi;
        int tempj;
        int tempk;
        int count = 0;

        if(_area <= 0)  outValue = input.grid[i][j][k];
        else
        {
            for(int ii = i - _area; ii <= _area + i; ii++)
            {

                tempi = ii;
                for(int jj = j -_area; jj <= _area + j; jj++)
                {
                    tempj = jj;

                    if(output.grid[0][0].length == 3) //2D
                    {
                      //  if (i != ii && j != jj) {
                            if (ii < 1 || ii >= input.grid.length - 1) tempi = i;
                            if (jj < 1 || jj >= input.grid[0].length - 1) tempj = j;
                            outValue += input.grid[tempi][tempj][1];
                            count++;
                      //  }
                    }
                    else {
                        for (int kk = k - _area; kk <= _area + k; kk++) {
                            tempk = kk;
                           // if (i != ii && j != jj && k != kk) {
                                if (ii < 1 || ii >= input.grid.length - 1) tempi = i;
                                if (jj < 1 || jj >= input.grid[0].length - 1) tempj = j;
                                if (kk < 1 || kk >= input.grid[0][0].length - 1) tempk = k;
                                outValue += input.grid[tempi][tempj][tempk];
                                count++;
                           // }

                        }
                    }
                }
            }


            //count should be equal to the following:
            //for 3D Math.pow(2 * _area, 3) - 1
            //for 2D Math.pow(2 * _area, 2)

            outValue /= count;
        }

        if(noAccumulation) output.grid[i][j][k] =outValue;
        else output.grid[i][j][k] += outValue;

    }
}
