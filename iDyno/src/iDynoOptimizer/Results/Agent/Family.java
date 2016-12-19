package iDynoOptimizer.Results.Agent;


public class Family implements IAgentBuilder {

    private int birthId;
    private int family;
    private int genealogy;
    private int generation;

    public Family(int birthId, int family, int genealogy, int generation) {
        this();
        this.birthId = birthId;
          this.family = family;
          this.genealogy = genealogy;
          this.generation = generation;
    }

    public Family() {


    }


    public int getFamily() {
        return family;
    }

    public int getGenealogy() {
        return genealogy;
    }

    public int getGeneration() {
        return generation;
    }

    public int getBirthId() {
        return birthId;
    }


    public void set(AgentPartName apn, String value) {
        try {
            int iValue = Integer.parseInt(value);

            if (apn == AgentPartName.family) ; //family = iValue;
            else if (apn == AgentPartName.genealogy) ; //genealogy = iValue;
            else if (apn == AgentPartName.generation) ;// generation = iValue;
            else if (apn == AgentPartName.birthid || apn == AgentPartName.extra0) birthId = iValue;
            else {
                throw new IllegalArgumentException(apn + "is not valid. Expecting " + AgentPartName.family + ", " + AgentPartName.genealogy + " or " + AgentPartName.generation);
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
