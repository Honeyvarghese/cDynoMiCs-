package iDynoOptimizer.Results.Agent;


public class Size implements IAgentBuilder {

    private double radius;
    private double totalRadius;

    public Size(double radius, double totalRadius) {

        this.radius = radius;
        this.totalRadius = totalRadius;
    }

    public Size() {

    }

    public double getRadius() {
        return radius;
    }

    public double getTotalRadius() {
        return totalRadius;
    }

    @Override
    public void set(AgentPartName apn, String value) {
        try {
            double iValue = Double.parseDouble(value);

            if (apn == AgentPartName.radius) radius = iValue;
            else if (apn == AgentPartName.totalRadius) totalRadius = iValue;
            else {
                throw new IllegalArgumentException(apn + "is not valid. Expecting " + AgentPartName.radius + " or " + AgentPartName.totalRadius);
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
