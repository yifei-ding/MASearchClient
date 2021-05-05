package domain;

public class AgentBoxConflict extends Conflict {
    public AgentBoxConflict(int agentId, int agentIdThatHasBoxConflict, Location agentLocation, Location boxLocation, int timestep) {
        super(agentId, agentIdThatHasBoxConflict, agentLocation, boxLocation, timestep);
    }
}
