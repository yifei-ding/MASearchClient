package data;

import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class InMemoryDataSource {
    private static final InMemoryDataSource dataSource = new InMemoryDataSource();

    private final HashMap<Integer, Agent> allAgents;
    private final HashMap<Integer, Box> allBoxes;
    private final HashMap<Integer, Goal> allGoals;
    private final HashMap<Integer, Task> allTasks;
    //store 2D array for color-agent, name-box, name-goal to support query
    private final HashMap<Color, ArrayList<Integer>> allAgentsByColor;
    private final HashMap<String, ArrayList<Integer>> allBoxesByName;
    private final HashMap<String, ArrayList<Integer>> allGoalsByName;

    //store map
    private final HashMap<Location, Object> staticMap;
    private final HashMap<Location, Object> dynamicMap;



    public static InMemoryDataSource getInstance(){
        System.err.println("[InMemoryDataSource] getInstance");
        return dataSource;
    }

    private InMemoryDataSource() {
        allAgents = new HashMap<Integer, Agent>();
        allBoxes = new HashMap<Integer, Box>();
        allGoals = new HashMap<Integer, Goal>();
        allTasks = new HashMap<Integer, Task>();
        allAgentsByColor = new HashMap<Color, ArrayList<Integer>>();
        allBoxesByName = new HashMap<String, ArrayList<Integer>>();
        allGoalsByName = new HashMap<String, ArrayList<Integer>>();
        staticMap = new HashMap<Location, Object>();
        dynamicMap = new HashMap<Location, Object>();

    }

    public void addAgent(Agent agent){
        //when adding agent, add to both maps
        allAgents.put(agent.getId(),agent);
        ArrayList<Integer> list = allAgentsByColor.get(agent.getColor());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(agent.getId());
        allAgentsByColor.put(agent.getColor(),list);
    }

    public void addTask(Task task){
        allTasks.put(task.getId(),task);
    }

    public HashMap<Integer, Task> getAllTasks() {
        return allTasks;
    }

    public Agent getAgent(int id){
        return allAgents.get(id);

    }

    public HashMap<Integer, Agent> getAllAgents() {
        return allAgents;
    }
    public HashMap<Integer, Box> getAllBoxes() {
        return allBoxes;
    }


    /*
    * @Author Yifei
    * @Description Search agent by color, for goal-box-agent matching
    * @Date 17:14 2021/4/2
    * @Param [color]
    * @return AgentId list
     **/
    public ArrayList<Integer> getAgentByColor(Color color){
        return allAgentsByColor.get(color);
    }

    public void addBox(Box box) {
        //when adding box, add to both maps
        allBoxes.put(box.getId(),box);
        ArrayList<Integer> list = allBoxesByName.get(box.getName());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(box.getId());
        allBoxesByName.put(box.getName(),list);
    }

    public void addGoal(Goal goal) {
        //when adding goal, add to both maps
        allGoals.put(goal.getId(), goal);
        ArrayList<Integer> list = allGoalsByName.get(goal.getName());
        if (list==null){
            list = new ArrayList<>();
        }
        list.add(goal.getId());
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





    public ArrayList<Integer> getBoxByName(String name) {
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
