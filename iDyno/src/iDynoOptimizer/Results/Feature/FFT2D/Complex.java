package iDynoOptimizer.Results.Feature.FFT2D;
import iDynoOptimizer.Global.PrintfFormat;
/**
 * SOURCE (publicly avilable): https://www.cs.virginia.edu/~wm2a/applets/Transformation/Complex.java
 */



/**
 class implementing basic stuff needed for complex numbers
 */
public class Complex {

    /** real part of a number */
    public double real;

    /** imaginary part of a number */
    public double imag;

    /** constructor generates a complex number with value 0.0 */
    public Complex() {
        real = 0.0;
        imag = 0.0;
    }
    /** constructor generates of complex number iwth real and imaginary part */
    public Complex(double r, double i) {
        real = r;
        imag = i;
    }
    /** constructor copies complex number z */
    public Complex(Complex z) {
        real = z.real;
        imag = z.imag;
    }


    // ----------- Addition ---------------

    /** add z to complex object */
    public Complex add(Complex z) {
        real += z.real ; imag += z.imag ;
        return this ;
    }

    /** add x and y and return the result */
    public static Complex add(Complex x, Complex y) {
        return new Complex( x.real+y.real , x.imag+y.imag ) ;
    }

    // ----------- Subtraction ------------

    /** Subtracte z from Complex-Objekt */
    public Complex sub(Complex z) {
        real -= z.real ; imag -= z.imag ;
        return this ;
    }

    /** Subtracte y from x and return the result */
    public static Complex sub(Complex x, Complex y) {
        return new Complex( x.real-y.real , x.imag-y.imag ) ;
    }

    // ----------- Multiplikation ----------

    /** Multiply complex object with z */
    public Complex mult(Complex z) {
        double r = real ;
        real = r*z.real - imag*z.imag ;
        imag = imag*z.real + r*z.imag ;
        return this ;
    }

    /** Multiply complex object with scalar */
    public Complex mult(double x) {
        real *= x ;
        imag *= x ;
        return this ;
    }

    /** multiply x and y and return the result */
    public static Complex mult(Complex x, Complex y) {
        return new Complex( x.real*y.real - x.imag*y.imag , x.imag*y.real + x.real*y.imag) ;
    }

    /** multiply z with scalar a and return the result */
    public static Complex mult(double a, Complex z){
        return new Complex( a*z.real, a*z.imag );
    }

    /** complex exponential function, e^{ix}, x reell */
    public static Complex expi(double x) {
        return new Complex(Math.cos(x),Math.sin(x)) ;
    }

    // ---------- polar coordinates

    /** return the value of the complex object */
    public double Betrag() {
        return Math.sqrt(this.real*this.real + this.imag*this.imag);
    }

    /** return the vlaue of the complex number z */
    public static double Betrag(Complex z) {
        return Math.sqrt(z.real*z.real + z.imag*z.imag);
    }

    /** return the phase (angle in polar coordinates) */
    public double Phase() {
        double phase;
        phase = Math.acos(this.real/Betrag());
        if ( this.imag < 0.0 )
            phase = 2.0*Math.PI - phase;
        return phase;
    }

    /** return the phase of the number z */
    public static double Phase(Complex z) {
        double phase;
        phase = Math.acos(z.real/Betrag(z));
        if ( z.imag < 0.0 )
            phase = 2.0*Math.PI - phase;
        return phase;
    }


    // ---------- additional functions --------

    /** set the complex object to the value of a number */
    public Complex setTo(Complex x) {
        real = x.real ;
        imag = x.imag ;
        return this ;
    }

    /** set x to the value of y and y to the value of x */
    public static void swap(Complex x, Complex y) {
        double h ;
        h = x.real ; x.real = y.real ; y.real = h ;
        h = x.imag ; x.imag = y.imag ; y.imag = h ;
    }

    // ------------ output ------------

    /** return a formatted string of the complex number */
    public String toString() {
        StringBuffer erg = new StringBuffer() ;

        erg.append(new PrintfFormat("%9.2e").sprintf(real)+"+"+new PrintfFormat("%9.2e").sprintf(imag)+"i") ;

        return erg.toString() ;
    }
}