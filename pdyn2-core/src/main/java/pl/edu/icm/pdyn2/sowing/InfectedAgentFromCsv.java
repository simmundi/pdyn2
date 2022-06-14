package pl.edu.icm.pdyn2.sowing;

public class InfectedAgentFromCsv {
    private int agentId;
    private int elapsedDays;
    private boolean symptomatic;

    public InfectedAgentFromCsv(int agentId, int elapsedDays, int symptomatic) {
        this.agentId = agentId;
        this.elapsedDays = elapsedDays;
        if (symptomatic != 1 && symptomatic != 0) {
            throw new IllegalArgumentException("symptomatic != 1 || symptomatic != 0" +
                    ", symptomatic = " + String.valueOf(symptomatic));
        }
        if (symptomatic == 1) {
            this.symptomatic = true;
        } else {
            this.symptomatic = false;
        }
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public int getElapsedDays() {
        return elapsedDays;
    }

    public void setElapsedDays(int elapsedDays) {
        this.elapsedDays = elapsedDays;
    }

    public boolean isSymptomatic() {
        return symptomatic;
    }

    public void setSymptomatic(boolean symptomatic) {
        this.symptomatic = symptomatic;
    }
}
