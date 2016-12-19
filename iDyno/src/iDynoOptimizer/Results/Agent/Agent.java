package iDynoOptimizer.Results.Agent;


public class Agent {


    private Family family;

    private double birthday;

    private Mass mass;

    private Size size;


    private Location location;

    private ChangeRate changeRate;


    private int state;

    public Agent(AgentBuilder ab) {
        this.family = ab.getFamily();
        this.birthday = ab.getBirthday();
        this.mass = ab.getMass();
        this.size = ab.getSize();
        this.location = ab.getLocation();
        this.changeRate = ab.getChangeRate();
        this.state = ab.getState();

    }

    public Agent(Family f, double birthday, Mass m, Size s, ChangeRate cr, Location l, int state) {

        this.family = f;
        this.birthday = birthday;
        this.mass = m;
        this.size = s;
        this.location = l;
        this.changeRate = cr;

        this.state = state;
    }


    public Family getFamily() {
        return family;
    }


    public double getBirthday() {
        return birthday;
    }


    public Mass getMass() {
        return mass;
    }


    public Size getSize() {
        return size;
    }


    public ChangeRate getChangeRate() {
        return this.changeRate;
    }


    public Location getLocation() {
        return location;
    }


      public int getState() {
         return state;
      }


    public void delete() {
        family = null;
        mass = null;
        size = null;
        location = null;
        changeRate = null;
    }


}
