package domain;

public class AgentAgentConflict extends Conflict {
    public AgentAgentConflict(int agentId1, int agentId2, Location location1, Location location2, int timestep) {
        super(agentId1, agentId2, location1, location2, timestep);

    }


    //    public static void main(String[] args) {
//    Conflict conflict1 = new AgentAgentConflict(1,2,new Location(1,1), new Location(2,2),3);
//    Conflict conflict2 = new AgentBoxConflict(1,2,new Location(1,1), new Location(2,2),3);
//    System.out.println(conflict1.equals(conflict2));
//    System.out.println(conflict1 instanceof AgentAgentConflict);
//    System.out.println(conflict1 instanceof AgentBoxConflict);
//    System.out.println(conflict1 instanceof Conflict);
//    System.out.println(conflict1.getId2());

//    }
}

