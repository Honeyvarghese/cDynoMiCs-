package iDynoOptimizer.Results.Agent;

/**
 * Created by Chris on 4/16/2015.
 */
public class Grid
    {

        private double resolution;
        private double nI;
        private double nJ;
        private double nK;

        public Grid(double resolution, int nI, int nJ, int nK)
            {
                this.resolution = resolution;
                this.nI = nI;
                this.nJ = nJ;
                this.nK = nK;
            }

        public Grid(String resolution, String nI, String nJ, String nK)
            {
                this.resolution = Double.parseDouble(resolution);
                this.nI = Double.parseDouble(nI);
                this.nJ = Double.parseDouble(nJ);
                this.nK = Double.parseDouble(nK);
            }


        public double getResolution()
            {
                return resolution;
            }

        public double getnI()
            {
                return nI;
            }

        public double getnJ()
            {
                return nJ;
            }

        public double getnK()
            {
                return nK;
            }
    }

