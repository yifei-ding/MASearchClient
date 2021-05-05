package domain;

public class BoxBoxConflict extends Conflict {
    public BoxBoxConflict(int agentId1ThatHasBoxConflict, int agentId2ThatHasBoxConflict, Location boxLocation1, Location boxLocation2, int timestep) {
        super(agentId1ThatHasBoxConflict, agentId2ThatHasBoxConflict, boxLocation1, boxLocation2, timestep);
    }
}
