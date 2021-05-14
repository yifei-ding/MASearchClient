package domain;

import java.util.Objects;

public class Constraint {
    private int agentId;
    private boolean isBoxConstraint;
    private int timeStep;
    private Location location;

    /**
    * @author Yifei
    * @description If isBoxConstraint = true, it means this constraint is for the corresponding box if the agent (agentId), not agent itself; If isBoxConstraint = false, it is an agent constraint.
    * @date 2021/4/30
    * @param
    * @return
     */
    public Constraint(int agentId, boolean isBoxConstraint, int timeStep, Location location) {
        this.agentId = agentId;
        this.isBoxConstraint = isBoxConstraint;
        this.timeStep = timeStep;
        this.location = location;
    }

    public Constraint(int agentId, int timeStep, Location location) {
        this.agentId = agentId;
        this.isBoxConstraint = false;
        this.timeStep = timeStep;
        this.location = location;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public boolean isBoxConstraint() {
        return isBoxConstraint;
    }

    public void setBoxConstraint(boolean boxConstraint) {
        this.isBoxConstraint = boxConstraint;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Constraint that = (Constraint) o;
        return agentId == that.agentId && isBoxConstraint == that.isBoxConstraint && timeStep == that.timeStep && Objects.equals(location, that.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId, isBoxConstraint, timeStep, location);
    }

    @Override
    public String toString() {
        return "Constraint{" +
                "agentId=" + agentId +
                ", isBoxConstraint=" + isBoxConstraint +
                ", timeStep=" + timeStep +
                ", location=" + location +
                '}';
    }
}
