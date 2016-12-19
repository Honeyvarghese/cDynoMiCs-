package iDynoOptimizer.Global;

import iDynoOptimizer.Protocol.Parameters.ParamNum;
import iDynoOptimizer.Search.ExitCondition.Operator;

import java.util.List;

/**
 * Created by Chris on 8/3/2015.
 */
public class ExtraMath {



    public static double average (List<Double> numbers)
    {
        double sum = 0;
        for(Double d : numbers)
        {
            sum+= d;
        }
        return sum / numbers.size();
    }

    public static double[] avgStd(List<Double> numbers)
    {
        double[] avgStd = new double[2];
        avgStd[0] = average(numbers);
        for(Double d : numbers)
        {
            avgStd[1] += Math.pow(d -  avgStd[0], 2);
        }
        avgStd[1]= Math.sqrt( avgStd[1] / numbers.size());
        return avgStd;
    }

    public static double normalize(double num, double avg, double std)
    {
        return (num - avg) / std;
    }



    public static double round(double num, int decimals)
    {

        float places = 1;

        while(decimals-- > 0)
        {
            places *= 10;
        }

        return  Math.round(num*places)/places;
    }



    public static boolean approxComp(double a, double b, double epsilon, Operator operator)
    {
        if (operator == Operator.equal) {
            return ExtraMath.approxEqual(a, b,epsilon);
        } else if (operator == Operator.less) {
            return ExtraMath.approxLess(a, b, epsilon);
        } else if (operator == Operator.greater) {
            return ExtraMath.approxGreater(a, b, epsilon);
        }
        else if(operator == Operator.lessThanOrEqual)
        {
            return ExtraMath.approxEqualorLess(a, b, epsilon);
        }
        else if(operator == Operator.greaterThanOrEqual)
        {
            return ExtraMath.approxEqualorGreater(a, b,epsilon);
        }
        return false;
    }

    public static boolean approxEqual(double a, double b, double epsilon)
    {
        return a == b ? true : Math.abs(a - b) < epsilon;
    }



    public static boolean approxGreater(double a, double b, double epsilon)
    {
        return a - epsilon > b;
    }
    public static boolean approxLess(double a, double b, double epsilon)
    {
        return a + epsilon < b;
    }

    public static boolean approxEqualorGreater(double a, double b, double epsilon)
    {
        return approxEqual(a, b, epsilon) || a > b;
    }

    public static boolean approxEqualorLess(double a, double b, double epsilon)
    {
        return approxEqual(a, b, epsilon) || a < b;
    }



}
