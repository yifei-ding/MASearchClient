package domain;

import java.util.Objects;

public class Conflict {
    private int agentId_1;
    private int agentId_2;
    private Location location;
    private int timestep;

    public Conflict(int agentId_1, int agentId_2, Location location, int timestep) {
        this.agentId_1 = agentId_1;
        this.agentId_2 = agentId_2;
        this.location = location;
        this.timestep = timestep;
    }

    public int getAgentId_1() {
        return agentId_1;
    }

    public void setAgentId_1(int agentId_1) {
        this.agentId_1 = agentId_1;
    }

    public int getAgentId_2() {
        return agentId_2;
    }

    public void setAgentId_2(int agentId_2) {
        this.agentId_2 = agentId_2;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getTimestep() {
        return timestep;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conflict conflict = (Conflict) o;
        return agentId_1 == conflict.agentId_1 && agentId_2 == conflict.agentId_2 && timestep == conflict.timestep && location.equals(conflict.location);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId_1, agentId_2, location, timestep);
    }
}
