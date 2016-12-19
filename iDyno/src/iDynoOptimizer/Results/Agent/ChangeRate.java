package iDynoOptimizer.Results.Agent;


/**
 * Created by Chris on 11/15/2014.
 */
public class ChangeRate implements IAgentBuilder {


    private double growthRate;
    private double volumeRate;


    public ChangeRate(double growthRate, double volumeRate) {

        this.growthRate = growthRate;
        this.volumeRate = volumeRate;
    }

    public ChangeRate() {

    }

    public double getGrowthRate() {

        return this.growthRate;
    }

    public double getVolumeRate() {
        return this.volumeRate;
    }

    @Override
    public void set(AgentPartName apn, String value) {
        try {
            double iValue = Double.parseDouble(value);

            if (apn == AgentPartName.growthRate) growthRate = iValue;
            else if (apn == AgentPartName.volumeRate) volumeRate = iValue;
            else {
                throw new IllegalArgumentException(apn + "is not valid. Expecting " + AgentPartName.volumeRate + " or " + AgentPartName.growthRate);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(-5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(-5);
        }
    }
}
