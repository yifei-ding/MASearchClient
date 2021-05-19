package domain;

import java.util.HashSet;
import java.util.Objects;

public class Conflict2 {
    private int agentId1;
    private int agentId2;
    private LocationPair location1;
    private LocationPair location2;
    private int timestep1;
    private int timestep2;
    private ConflictType type;

    public Conflict2(int agentId1, int agentId2, LocationPair location1, LocationPair location2, int timestep1, int timestep2, ConflictType type) {
        this.agentId1 = agentId1;
        this.agentId2 = agentId2;
        this.location1 = location1;
        this.location2 = location2;
        this.timestep1 = timestep1;
        this.timestep2 = timestep2;
        this.type = type;
    }

    public int getAgentId1() {
        return agentId1;
    }

    public void setAgentId1(int agentId1) {
        this.agentId1 = agentId1;
    }

    public int getAgentId2() {
        return agentId2;
    }

    public void setAgentId2(int agentId2) {
        this.agentId2 = agentId2;
    }

    public LocationPair getLocation1() {
        return location1;
    }

    public void setLocation1(LocationPair location1) {
        this.location1 = location1;
    }

    public LocationPair getLocation2() {
        return location2;
    }

    public void setLocation2(LocationPair location2) {
        this.location2 = location2;
    }

    public int getTimestep1() {
        return timestep1;
    }

    public void setTimestep1(int timestep1) {
        this.timestep1 = timestep1;
    }

    public int getTimestep2() {
        return timestep2;
    }

    public void setTimestep2(int timestep2) {
        this.timestep2 = timestep2;
    }

    public ConflictType getType() {
        return type;
    }

    public void setType(ConflictType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conflict2 conflict2 = (Conflict2) o;
        return agentId1 == conflict2.agentId1 && agentId2 == conflict2.agentId2 && timestep1 == conflict2.timestep1 && timestep2 == conflict2.timestep2 && Objects.equals(location1, conflict2.location1) && Objects.equals(location2, conflict2.location2) && type == conflict2.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId1, agentId2, location1, location2, timestep1, timestep2, type);
    }

    @Override
    public String toString() {
        return "Conflict2{" +
                "agentId1=" + agentId1 +
                ", agentId2=" + agentId2 +
                ", location1=" + location1 +
                ", location2=" + location2 +
                ", timestep1=" + timestep1 +
                ", timestep2=" + timestep2 +
                ", type=" + type +
                '}';
    }
}