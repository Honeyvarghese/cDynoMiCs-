package iDynoOptimizer.Results.Feature.Temporal;

/**
 * Created by Chris on 4/16/2015.
 * Keeps track of a vector, a momentum vector (vector times mass), mass, and the vector magnitude, and the momentum vector magnitude
 * Methods operate on all of these to keep them consistent
 */
public class VectorSums
    {


        //these two are used to calculate convergence
        //this value for the incoming minus this value for the outgoing is convergence
        //it's not really a vector, but I'm using this class so that I can keep track of the different components separately
        private Vector scalarConvDiv; //does not take into account direction
        private Vector momScalarConvDiv; //same as above but multiplied by mass

        private Vector v; //the vector
        private Vector vMom; //the momentum vector
        private double mass; //mass
        private double mag; //v's magnitude
        private double magMom; //vMom's magnitude
        private int count;

        public VectorSums()
            {
                vMom = new Vector();
                v = new Vector();
                scalarConvDiv = new Vector();
                momScalarConvDiv = new Vector();
                mass = 0;
                mag = 0;
                magMom = 0;
                count = 0;
            }


        public void add(VectorSums vs)
            {
                add(vs.v, vs.scalarConvDiv, vs.vMom, vs.momScalarConvDiv, vs.mass, vs.getCount());
            }

        public void add(Vector v, Vector sv, Vector mom, Vector momSV, double mass, double count)
            {
                this.v.add(v);
                this.vMom.add(mom);
                this.scalarConvDiv.add(sv);
                this.momScalarConvDiv.add(momSV);
                this.mass += mass;
                this.mag += v.getMagnitude();
                this.magMom += mom.getMagnitude();
                this.count += count;

            }

        public void add(Vector v, Vector sv, Vector mom,  Vector momSV, double mass, double mag, double magMom, int count)
            {
                this.v.add(v);
                this.vMom.add(mom);
                this.scalarConvDiv.add(sv);
                this.momScalarConvDiv.add(momSV);
                this.mass += mass;
                this.mag += mag;
                this.magMom += magMom;
                this.count += count;
            }

        public void subtract(Vector v, Vector sv, Vector mom, Vector momSV, double mass, double count)
        {
            this.v.subtract(v);
            this.vMom.subtract(mom);
            this.scalarConvDiv.subtract(sv);
            this.momScalarConvDiv.subtract(momSV);
            this.mass -= mass;
            this.mag -= v.getMagnitude();
            this.magMom -= mom.getMagnitude();
            this.count -= count;
        }

        public void subtract(VectorSums vs)
        {
            subtract(vs.v, vs.scalarConvDiv, vs.vMom, vs.momScalarConvDiv, vs.mass, vs.getCount());
        }

        public void divide(double divisor)
            {
                v.divide(divisor);
                vMom.divide(divisor);
                scalarConvDiv.divide(divisor);
                momScalarConvDiv.divide(divisor);
                mass /= divisor;
                mag /= divisor;
                magMom /= divisor;
                count /= divisor;
            }


//        public double addMass(double d)
//            {
//                return mass += d;
//            }
//        public double addmag(double d)
//            {
//                return mag +=d;
//            }
//        public double addMagMom(double d)
//            {
//                return magMom +=d;
//            }

        public Vector getV()
            {
                return v;
            }

        public Vector getvMom()
            {
                return vMom;
            }

        public Vector getScalarConvDiv() {
            return scalarConvDiv;
        }

        public double getJKConvergence()
        {
            double convg = scalarConvDiv.getJ() + scalarConvDiv.getK();

            if (convg < 0) convg = 0;

            return convg;
        }

        public Vector getMomScalarConvDiv() {
            return momScalarConvDiv;
        }

        public double getMass()
            {
                return mass;
            }

        public double getMag()
            {
                return mag;
            }

        public double getMagMom()
            {
                return magMom;
            }

        public int getCount() {
            return count;
        }

        public String toString()
        {
            StringBuilder gp = new StringBuilder();
            gp.append('[');
            gp.append(v.getI()).append(' ');
            gp.append(v.getJ()).append(' ');
            gp.append(v.getK()).append(' ');
            gp.append(scalarConvDiv.getI()).append(' ');
            gp.append(scalarConvDiv.getJ()).append(' ');
            gp.append(scalarConvDiv.getK()).append(' ');
            gp.append(vMom.getI()).append(' ');
            gp.append(vMom.getJ()).append(' ');
            gp.append(vMom.getK()).append(' ');
            gp.append(momScalarConvDiv.getI()).append(' ');
            gp.append(momScalarConvDiv.getJ()).append(' ');
            gp.append(momScalarConvDiv.getK()).append(' ');
            gp.append(mass).append(' ');
            gp.append(mag).append(' ');
            gp.append(magMom).append(' ');
            gp.append(']');
            return gp.toString();
        }

        public String toStringJKConvergence()
        {
            return String.valueOf(getJKConvergence());
        }
    }