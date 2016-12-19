package iDynoOptimizer.Search.ExitCondition;

/**
 * Created by Chris on 11/15/2014.
 */
public interface IExitCondition {

    boolean met(double value);
    boolean met(double value, double epsilon);


}
