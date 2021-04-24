package domain;

import java.util.Objects;

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

    @Override
    public String toString() {
        return "Constraint{" +
                "agentId=" + agentId +
                ", timeStep=" + timeStep +
                ", location=" + location +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return agentId == that.agentId && timeStep == that.timeStep && location.equals(that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, timeStep, location);
    }
}
