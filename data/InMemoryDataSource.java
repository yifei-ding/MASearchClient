package data;

import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryDataSource {
    private static final InMemoryDataSource dataSource = new InMemoryDataSource();

    private final HashMap<Integer, Agent> allAgents;
    private final HashMap<Integer, Box> allBoxes;
    private final HashMap<Integer, Goal> allGoals;
    //store 2D array for color-agent, name-box, name-goal to support query
    private final HashMap<Color, ArrayList<Agent>> allAgentsByColor;
    private final HashMap<String, ArrayList<Box>> allBoxesByName;
    private final HashMap<String, ArrayList<Goal>> allGoalsByName;

    //store map
    private final HashMap<Location, Object> staticMap;
    private final HashMap<Location, Object> dynamicMap;



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
        staticMap = new HashMap<Location, Object>();
        dynamicMap = new HashMap<Location, Object>();

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

    public HashMap<Integer, Goal> getAllGoals(){
        return allGoals;
    }

    public void setStaticMap(Location location, Object object){
        staticMap.put(location,object);
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





    public ArrayList<Box> getBoxByName(String name) {
       return allBoxesByName.get(name);
    }

    public HashMap<Location, Object> getStaticMap() {
        return staticMap;
    }

    public void setDynamicMap(Location location, Object object) {
        dynamicMap.put(location,object);
    }
    public HashMap<Location, Object> getDynamicMap() {
        return dynamicMap;
    }
}
