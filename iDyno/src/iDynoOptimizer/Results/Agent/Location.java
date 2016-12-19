package iDynoOptimizer.Results.Agent;




public class Location implements IAgentBuilder {

    private double x;
    private double y;
    private double z;


    public Location(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Location() {

    }


    public double getX() {
        return x;
    }


    public double getY() {
        return y;
    }


    public double getZ() {
        return z;
    }


    @Override
    public void set(AgentPartName apn, String value) {
        try {
            double iValue = Double.parseDouble(value);

            if (apn == AgentPartName.locationX || apn == AgentPartName.points0) x = iValue;
            else if (apn == AgentPartName.locationY || apn == AgentPartName.points1) y = iValue;
            else if (apn == AgentPartName.locationZ || apn == AgentPartName.points2) z = iValue;
            else {
                throw new IllegalArgumentException(apn + "is not valid. Expecting " + AgentPartName.locationX + ", " + AgentPartName.locationY + " or " + AgentPartName.locationZ);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(-5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(-5);
        }
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Location)) return false;

        Location location = (Location) o;

        if (Double.compare(location.getX(), getX()) != 0) return false;
        if (Double.compare(location.getY(), getY()) != 0) return false;
        return Double.compare(location.getZ(), getZ()) == 0;

    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        temp = Double.doubleToLongBits(getX());
        result = (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getY());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        temp = Double.doubleToLongBits(getZ());
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        return result;
    }
}
