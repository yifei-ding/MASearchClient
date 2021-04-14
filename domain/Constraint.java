package domain;

public class Constraint {
    private int agentId;
    private int timeStep;
    private Location location;

    public Constraint(int agentId, int timeStep, Location location) {
        this.agentId = agentId;
        this.timeStep = timeStep;
        this.location = location;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public int getTimeStep() {
        return timeStep;
    }

    public void setTimeStep(int timeStep) {
        this.timeStep = timeStep;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }
}
