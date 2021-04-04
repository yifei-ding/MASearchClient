package data;

import domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

public class InMemoryDataSource {
    private static final InMemoryDataSource dataSource = new InMemoryDataSource();

    private HashMap<Integer, Agent> allAgents;
    private HashMap<Integer, Box> allBoxes;
    private HashMap<Integer, Goal> allGoals;
    //store 2D array for color-agent, name-box, name-goal to support query
    private HashMap<Color, ArrayList<Agent>> allAgentsByColor;
    private HashMap<String, ArrayList<Box>> allBoxesByName;
    private HashMap<String, ArrayList<Goal>> allGoalsByName;

    //store map
    private HashMap<Location, Boolean> map;


    public static InMemoryDataSource getInstance(){
        System.err.println("InMemoryDataSource instance");
        return dataSource;
    }

    private InMemoryDataSource() {
        allAgents = new HashMap<Integer, Agent>();
        allBoxes = new HashMap<Integer, Box>();
        allGoals = new HashMap<Integer, Goal>();
        allAgentsByColor = new HashMap<Color, ArrayList<Agent>>();
        allBoxesByName = new HashMap<String, ArrayList<Box>>();
        allGoalsByName = new HashMap<String, ArrayList<Goal>>();
        map = new HashMap<Location, Boolean>();

    }

    public void addAgent(Agent agent){
        //when adding agent, add to both maps
        allAgents.put(agent.getId(),agent);
        ArrayList<Agent> list = allAgentsByColor.get(agent.getColor());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(agent);
        allAgentsByColor.put(agent.getColor(),list);
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
    public ArrayList<Agent> getAgentByColor(Color color){
        return allAgentsByColor.get(color);
    }

    public void addBox(Box box) {
        //when adding box, add to both maps
        allBoxes.put(box.getId(),box);
        ArrayList<Box> list = allBoxesByName.get(box.getName());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(box);
        allBoxesByName.put(box.getName(),list);
    }

    public void addGoal(Goal goal) {
        //when adding goal, add to both maps
        allGoals.put(goal.getId(), goal);
        ArrayList<Goal> list = allGoalsByName.get(goal.getName());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(goal);
        allGoalsByName.put(goal.getName(),list);
    }

    public void setMap(Location location, Boolean isWall){
        map.put(location,isWall);
    }

    @Override
    public String toString() {
        return "InMemoryDataSource{" +
                "allAgents=" + allAgents + "\n" +
                ", allBoxes=" + allBoxes + "\n" +
                ", allGoals=" + allGoals + "\n" +
                ", allAgentsByColor=" + allAgentsByColor + "\n" +
                ", allBoxesByName=" + allBoxesByName + "\n" +
                ", allGoalsByName=" + allGoalsByName + "\n" +
                '}';
    }


    public String toString2() {
        return "InMemoryDataSource{" +
                "map=" + map +
                '}';
    }
}
