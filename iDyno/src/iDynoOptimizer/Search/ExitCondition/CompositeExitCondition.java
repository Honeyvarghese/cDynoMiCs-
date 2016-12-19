package iDynoOptimizer.Search.ExitCondition;

/**
 * Created by Chris on 11/15/2014.
 */
public class CompositeExitCondition implements IExitCondition {

    private IExitCondition one;
    private IExitCondition two;
    private Operand operand;


    public CompositeExitCondition(IExitCondition cond1, IExitCondition cond2, Operand operand) {
        this.one = cond1;
        this.two = cond2;
        this.operand = operand;
    }

    @Override
    public boolean met(double value) {

       return met(value, 0);
    }

    @Override
    public boolean met(double value, double epsilon) {
        boolean cond1Met = true;

        boolean cond2Met = true;


        if (one != null) cond1Met = one.met(value, epsilon);
        if (two != null) cond2Met = two.met(value, epsilon);

        if (operand == Operand.and) return cond1Met && cond2Met;
        else if (operand == Operand.or) return cond1Met || cond2Met;
        else if (operand == Operand.xor) return (cond1Met || cond2Met) && !(cond1Met && cond2Met);


        return false;
    }


}
