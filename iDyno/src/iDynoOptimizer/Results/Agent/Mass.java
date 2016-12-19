package iDynoOptimizer.Results.Agent;


public class Mass implements IAgentBuilder {

    private double biomass;
    private double inert;
    private double capsule;

    public Mass(double biomass, double inert, double capsule) {
        this.biomass = biomass;
        this.inert = inert;
        this.capsule = capsule;
    }

    public Mass() {

    }

    public double getBiomass() {
        return biomass;
    }

    public double getInert() {
        return inert;
    }

    public double getCapsule() {
        return capsule;
    }


    @Override
    public void set(AgentPartName apn, String value) {

        try {
            double iValue = Double.parseDouble(value);

            if (apn == AgentPartName.biomass || apn == AgentPartName.extra1) biomass = iValue;
            else if (apn == AgentPartName.inert || apn == AgentPartName.extra2) inert = iValue;
            else if (apn == AgentPartName.capsule) capsule = iValue;
            else {
                throw new IllegalArgumentException(apn + " is not valid. Expecting " + AgentPartName.biomass + ", " + AgentPartName.inert + " or " + AgentPartName.capsule);
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
