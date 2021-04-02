package data;

import domain.Agent;
import domain.Box;
import domain.Color;
import domain.Goal;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryDataSource {
    private static final InMemoryDataSource dataSource = new InMemoryDataSource();
    private HashMap<Integer, Agent> allAgents;
    private HashMap<Integer, Box> allBoxes;
    private HashMap<Integer, Goal> allGoals;
    //store 2D array for color-agent to support query
    private HashMap<Color, ArrayList<Agent>> allAgentsByColor;
    //TODO: add 2D array for name-box, name-goal


    public static InMemoryDataSource getInstance(){
        return dataSource;
    }

    public InMemoryDataSource() {
        allAgents = new HashMap<Integer, Agent>();
        allBoxes = new HashMap<Integer, Box>();
        allGoals = new HashMap<Integer, Goal>();
        allAgentsByColor = new HashMap<Color, ArrayList<Agent>>();
    }

    public void addAgent(Agent agent){
        //when adding agent, add to both maps
        allAgents.put(agent.getId(),agent);
        ArrayList<Agent> list = allAgentsByColor.get(agent.getColor());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(agent);
    }

    public Agent getAgent(int id){
        return allAgents.get(id);

    }

    /*
    * @Author Yifei
    * @Description Search agent by color, for goal-box-agent matching
    * @Date 17:14 2021/4/2
    * @Param [color]
    * @return java.util.ArrayList<domain.Agent>
     **/
    public ArrayList<Agent> getAgent(Color color){
        return allAgentsByColor.get(color);
    }

}
