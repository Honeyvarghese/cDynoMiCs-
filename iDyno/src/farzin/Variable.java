package farzin;

public class Variable {
	public double value;
	public String name;

	public static final String densityName = "density";
	public static int densitySolute=-999;
	public static final int pressureSolute=-998;
	public static final int molecularReactionIndexInitiator=-10000;
	public static final int _8neighborHood=8;
	public static final double turingValuebound=1E12;

	public Variable(String name, double value)
	{
		this.value=value;
		this.name=name;
	}

}
