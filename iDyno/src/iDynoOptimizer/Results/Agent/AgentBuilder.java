package iDynoOptimizer.Results.Agent;


/**
 * Created by Chris on 11/15/2014.
 */
public class AgentBuilder implements IAgentBuilder {


    private Family family;

    private double birthday;

    private Mass mass;
    private Size size;
    private Location location;
    private ChangeRate changeRate;
    private int state;


    public void set(AgentPartName apn, String value) {


        try {
            if (apn == AgentPartName.birthid || apn == AgentPartName.family || apn == AgentPartName.genealogy || apn == AgentPartName.generation || apn == AgentPartName.extra0) {

                if (family == null) family = new Family();
                family.set(apn, value);
            } else if (apn == AgentPartName.birthday) {

                birthday = Double.parseDouble(value);

            } else if (apn == AgentPartName.state) {

                state = Integer.parseInt(value);

            } else if (apn == AgentPartName.biomass || apn == AgentPartName.inert || apn == AgentPartName.capsule || apn == AgentPartName.extra1 || apn == AgentPartName.extra2) {
                if (mass == null) mass = new Mass();
                mass.set(apn, value);
            } else if (apn == AgentPartName.radius || apn == AgentPartName.totalRadius) {
                if (size == null) size = new Size();
                size.set(apn, value);
            } else if (apn == AgentPartName.locationX || apn == AgentPartName.locationY || apn == AgentPartName.locationZ ||
                    apn == AgentPartName.points0 || apn == AgentPartName.points1 || apn == AgentPartName.points2) {
                if (location == null) location = new Location();
                location.set(apn, value);
            } else if (apn == AgentPartName.growthRate || apn == AgentPartName.volumeRate) {
                if (changeRate == null) changeRate = new ChangeRate();
                changeRate.set(apn, value);
            } else if (apn == AgentPartName.death) {

            } else if (apn == AgentPartName.color) {

            } else {
                throw new IllegalArgumentException(apn + " is not valid");
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
            System.exit(-5);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            System.exit(-5);
        }


    }


    public Agent build() {

        Agent a = new Agent(this);
        family = null;
        mass = null;
        size = null;
        location = null;
        changeRate = null;
        return a;


    }

    public Family getFamily() {
        return family;
    }

      public double getBirthday()
        {
             return birthday;
         }

    public Mass getMass() {
        return mass;
    }

    public Size getSize() {
        return size;
    }

    public Location getLocation() {
        return location;
    }

      public ChangeRate getChangeRate()
       {
            return changeRate;
        }

     public int getState()
        {
            return state;
        }
}
