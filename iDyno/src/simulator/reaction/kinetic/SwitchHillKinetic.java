package simulator.reaction.kinetic;

import org.jdom.Element;
import simulator.SpatialGrid;
import utils.ExtraMath;
import utils.XMLParser;

/**
 * Created by Chris on 4/12/2015.
 */
public class SwitchHillKinetic  extends IsKineticFactor implements IsCompositionFactor
    {


        // Serial version used for the serialisation of the class
        private static final long serialVersionUID = 1L;

        // parameters
        private double            _Ks;
        private double            _h;

        //the highest and lowest values that can be produced
        private double _max;
        private double _min;
        private double _threshold;

        // auxiliaries
        private double            _KsH, _KsPowH;

        public SwitchHillKinetic() {

            nParam = 5;

        }

        public SwitchHillKinetic(double Ks, double h, double max, double min, double threshold) {
            _Ks = Ks;
            _h = h;
            _max = max;
            _min = min;
            _threshold = threshold;
            _KsH = _Ks*_h;
            _KsPowH = Math.pow(_Ks, _h);
            nParam = 5;
        }

        public void init(Element defMarkUp) {
            _Ks = (new XMLParser(defMarkUp)).getParamDbl("Ks");
            _h = (new XMLParser(defMarkUp)).getParamDbl("h");
            _max  = (new XMLParser(defMarkUp)).getParamDbl("max");
            _min  = (new XMLParser(defMarkUp)).getParamDbl("min");
            _threshold  = (new XMLParser(defMarkUp)).getParamDbl("threshold");
            _KsH = Math.pow(_Ks, _h)*_h;
            _KsPowH = Math.pow(_Ks, _h);
            nParam = 5;
        }

        public void initFromAgent(Element defMarkUp, double[] kineticParam, int paramIndex) {



        }

        public double kineticValue(double solute, double[] paramTable, int index) {


//            <param name="Ks"></param>
//            <param name="h"></param>
//            <param name="max"></param>
//            <param name="min"></param>
//            <param name="threshold"></param>

        //    double sT= solute / _threshold;
          //  return (_max + _min * Math.pow(sT,_h) )/(_KsPowH+Math.pow(sT,_h));

            return 0;
        }

        public double kineticValue(double solute) {
            double sT= solute / _threshold;
            return (_max + _min * Math.pow(sT,_h) )/(_KsPowH+Math.pow(sT,_h));
        }

        public double kineticDiff(double solute) {
//            return _KsH*Math.pow(solute, _h-1)/(ExtraMath.sq(_KsPowH + Math.pow(solute, _h)));
            return 0;
        }

        public double kineticDiff(double solute, double[] paramTable, int index) {


//            return Math.pow(paramTable[index], paramTable[index+1])
//                    *paramTable[index+1]
//                    *Math.pow(solute, paramTable[index+1]-1)
//                    /(ExtraMath.sq(Math.pow(paramTable[index], paramTable[index+1])
//                    +Math.pow(solute, paramTable[index+1])));
            return 0;
        }

        public double kineticMax() {
            return _max;
        }

        public double kineticMax(double[] paramTable, int index) {
            return _max;
        }


        @Override
        public void compose(boolean noAccumulation, SpatialGrid output, SpatialGrid input, int i, int j, int k) {

            if(noAccumulation) output.grid[i][j][k] = kineticValue(input.grid[i][j][k]);
            else output.grid[i][j][k] += kineticValue(input.grid[i][j][k]);

        }
    }
