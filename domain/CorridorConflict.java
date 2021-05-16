package domain;

import java.util.Objects;

public class CorridorConflict {
    private int agentId1;
    private int agentId2;
    private Location exit1;
    private Location exit2;
    private int length;
    private int t1e1;
    private int t2e2;
    
    /**
    * @author Yifei
    * @description A corridor conflict happens when two agents traverse a corridor in the opposite direction, and their paths collide inside the corridor.
     * exit1: the location that agent1 leaves corridor
     * exit2: the location that agent2 leaves corridor
     * t1e1: the timestep that agent1 arrives at exit1 (leaves corridor)
     * t2e2: the timestep that agent2 arrives at exit2 (leaves corridor)
     * length: length of corridor, including exit1 and exit2
    * @date 2021/5/13
     */
    public CorridorConflict(int agentId1, int agentId2, Location exit1, Location exit2, int length, int t1e1, int t2e2) {
        this.agentId1 = agentId1;
        this.agentId2 = agentId2;
        this.exit1 = exit1;
        this.exit2 = exit2;
        this.length = length;
        this.t1e1 = t1e1;
        this.t2e2 = t2e2;
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

    public Location getExit1() {
        return exit1;
    }

    public void setExit1(Location exit1) {
        this.exit1 = exit1;
    }

    public Location getExit2() {
        return exit2;
    }

    public void setExit2(Location exit2) {
        this.exit2 = exit2;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public int getT1e1() {
        return t1e1;
    }

    public void setT1e1(int t1e1) {
        this.t1e1 = t1e1;
    }

    public int getT2e2() {
        return t2e2;
    }

    public void setT2e2(int t2e2) {
        this.t2e2 = t2e2;
    }

    @Override
    public String toString() {
        return "CorridorConflict{" +
                "agentId1=" + agentId1 +
                ", agentId2=" + agentId2 +
                ", exit1=" + exit1 +
                ", exit2=" + exit2 +
                ", length=" + length +
                ", t1e1=" + t1e1 +
                ", t2e2=" + t2e2 +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CorridorConflict that = (CorridorConflict) o;
        return agentId1 == that.agentId1 && agentId2 == that.agentId2 && length == that.length && t1e1 == that.t1e1 && t2e2 == that.t2e2 && Objects.equals(exit1, that.exit1) && Objects.equals(exit2, that.exit2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentId1, agentId2, exit1, exit2, length, t1e1, t2e2);
    }
}
