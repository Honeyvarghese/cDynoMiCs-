package iDynoOptimizer.Results.Feature.Temporal;

import iDynoOptimizer.Results.Agent.Location;

/**
 * Created by Chris on 4/16/2015.
 *
 * Represents a standard vector
 */
public class Vector
    {



        private double i;
        private double j;
        private double k;

        public double getI()
            {
                return i;
            }

        public double getJ()
            {
                return j;
            }

        public double getK()
            {
                return k;
            }


        public Vector()
            {
                i = 0;
                j = 0;
                k = 0;
            }


        public Vector(double i, double j, double k)
            {
                this.i = i;
                this.j = j;
                this.k = k;
            }

        public Vector(Location from, Location to, double d)
            {
                i = d * (to.getX() - from.getX());
                j = d * (to.getY() - from.getY());
                k = d * (to.getZ() - from.getZ());
            }

//        @Override
//        public Vector clone()
//            {
//                return new Vector(i, j, k, d);
//            }

//        public void addVector(Location from, Location to)
//            {
//                add(new Vector(from, to,d));
//            }


        public void add(Vector v)
            {
                i += v.getI();
                j += v.getJ();
                k += v.getK();
            }


        public void subtract(Vector v)
        {
            i -= v.getI();
            j -= v.getJ();
            k -= v.getK();
        }

        public static Vector abs(Vector v)
        {
            return  new Vector(Math.abs(v.getI()), Math.abs(v.getJ()), Math.abs(v.getK()));
        }

        public double getSum()
        {
            return i + j + k;
        }


        public static Vector add(Vector first, Vector second)
        {
            return new Vector(first.getI() + second.getI(), first.getJ() + second.getJ(), first.getK() + second.getK());
        }


        public static Vector subtract(Vector first, Vector second)
            {
                return new Vector(first.getI() - second.getI(), first.getJ() - second.getJ(), first.getK() - second.getK());
            }



        public void divide(double divisor)
            {
                i /= divisor;
                j /= divisor;
                k /= divisor;
            }


        public double getMagnitude()
            {
               return Math.sqrt(Math.pow(i, 2) + Math.pow(j, 2) + Math.pow(k, 2));
            }



    }
