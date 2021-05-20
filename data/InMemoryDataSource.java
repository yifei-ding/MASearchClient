package data;

import domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

public class InMemoryDataSource {
    private static final InMemoryDataSource dataSource = new InMemoryDataSource();
    private int row;
    private int col;
    private int[][] goalMap; //store id of goal on the map
    private int[][] agentMap; //id of agent
    private int[][] boxMap;//id of box

    private HashMap<Integer, Agent> allAgents;
    private HashMap<Integer, Box> allBoxes;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks;
    //store 2D array for color-agent, name-box, name-goal to support query
    private HashMap<Color, ArrayList<Integer>> allAgentsByColor;
    private HashMap<String, ArrayList<Integer>> allBoxesByName;
    private HashMap<String, ArrayList<Integer>> allGoalsByName;

    //store task list for agents
    private HashMap<Integer, ArrayList<Integer>> allTasksByAgent;

    //store map
    private HashMap<Location, Object> staticMap; //goal+wall
    private HashMap<Location, Wall> wallMap; //single wall map

    private HashMap<Location, Object> dynamicMap; //agent+box
    private HashMap<Location, Boolean> obstacleMap; //for low level to check if the cell is free

    private HashMap<Location, Integer> staticdegreeMap; // location+degree four direction arraylist?


    public static InMemoryDataSource getInstance() {
        //System.err.println("[InMemoryDataSource] getInstance");
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
        obstacleMap = new HashMap<Location, Boolean>();
        allTasksByAgent = new HashMap<Integer, ArrayList<Integer>>();
        wallMap = new HashMap<Location, Wall>();
        staticdegreeMap = new HashMap<Location, Integer>();

    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public void addAgent(Agent agent) {
        //when adding agent, add to both maps
        allAgents.put(agent.getId(), agent);
        ArrayList<Integer> list = allAgentsByColor.get(agent.getColor());
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(agent.getId());
        allAgentsByColor.put(agent.getColor(), list);
    }

    public void addTask(Task task) {
//        System.err.println("[Add task] " + task.toString());
        allTasks.put(task.getId(), task);

        int agentId = task.getAgentId();
        //group tasks by agent id
        ArrayList<Integer> list = allTasksByAgent.get(agentId);
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(task.getId());
        allTasksByAgent.put(agentId, list);
    }

    public HashMap<Integer, Task> getAllTasks() {
        return allTasks;
    }

    public Agent getAgent(int id) {
        return allAgents.get(id);

    }
    public Box getBox(int id) {
        return allBoxes.get(id);
    }

    public HashMap<Integer, Agent> getAllAgents() {
        return allAgents;
    }
    public HashMap<Integer, Box> getAllBoxes() {
        return allBoxes;
    }

    public HashMap<Location, Integer> getStaticdegreeMap() {
        return staticdegreeMap;
    }

    /*
     * @Author Yifei
     * @Description Search agent by color, for goal-box-agent matching
     * @Date 17:14 2021/4/2
     * @Param [color]
     * @return AgentId list
     **/
    public ArrayList<Integer> getAgentByColor(Color color) {
        return allAgentsByColor.get(color);
    }

    public void addBox(Box box) {
        //when adding box, add to both maps
        allBoxes.put(box.getId(), box);
        ArrayList<Integer> list = allBoxesByName.get(box.getName());
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(box.getId());
        allBoxesByName.put(box.getName(), list);
    }

    public void addGoal(Goal goal) {
        //when adding goal, add to both maps
        allGoals.put(goal.getId(), goal);
        ArrayList<Integer> list = allGoalsByName.get(goal.getName());
        if (list == null) {
            list = new ArrayList<>();
        }
        list.add(goal.getId());
        allGoalsByName.put(goal.getName(), list);
    }

    public HashMap<Integer, Goal> getAllGoals() {
        return allGoals;
    }

    public void setStaticMap(Location location, Object object) {
        staticMap.put(location, object);
    }

    public void setObstacleMap(Location location, Boolean isObstacle) {
        obstacleMap.put(location, isObstacle);
    }

    public HashMap<Location, Boolean> getObstacleMap(){
        return obstacleMap;
    }

    /**
    * @author Yifei
    * @description For each round of tasks, initialize obstacle map with the location of all walls, all agents and all boxes.
    * @date 2021/5/11
     */
    public void initializeObstacleMap() {
        this.obstacleMap = new HashMap<Location, Boolean>();

        for (Map.Entry<Location, Wall> entry : wallMap.entrySet()) {
            Location location = entry.getKey();
            Wall wall = entry.getValue();
            if (wall.isWall())
                obstacleMap.put(location, true);
            else
                obstacleMap.put(location, false);
        }
        for (Agent agent : allAgents.values()) {
            obstacleMap.put(agent.getLocation(), true);
        }
        for (Box box : allBoxes.values()) {
            obstacleMap.put(box.getLocation(), true);
        }
    }

    public HashMap<Location,Boolean> getInitialObstacleMap() {
        HashMap<Location,Boolean> map = new HashMap<Location, Boolean>();

        for (Map.Entry<Location, Wall> entry : wallMap.entrySet()) {
            Location location = entry.getKey();
            Wall wall = entry.getValue();
            if (wall.isWall())
                map.put(location, true);
            else
                map.put(location, false);
        }
        for (Agent agent : allAgents.values()) {
            map.put(agent.getLocation(), true);
        }
        for (Box box : allBoxes.values()) {
            map.put(box.getLocation(), true);
        }
        return map;
    }


    public void setWallMap(Location location, Wall wall) {
        wallMap.put(location, wall);
    }


    public HashMap<Location, Integer> getDegreeMap(){
        for (Map.Entry<Location,Wall> entry:wallMap.entrySet()){
            Location location = entry.getKey();
            Wall wall = entry.getValue();
            int k = 0;
            if (!wall.isWall()) { //if the current location is not wall, count its degree
                ArrayList<Location> neighbours = location.getNeighbours();
                for (Location neighbour : neighbours) {
                    if (wallMap.containsKey(neighbour) && !wallMap.get(neighbour).isWall())
                        k++;
                }
                staticdegreeMap.put(location, k);
            }

        }
        return staticdegreeMap;
    }

    public HashMap<Location, Wall> getWallMap(){

        return wallMap;


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


    public ArrayList<Integer> getAllTasksByAgent(int agentId) {
        return allTasksByAgent.get(agentId);
    }

    public ArrayList<Integer> getBoxByName(String name) {
        return allBoxesByName.get(name);
    }

    public HashMap<Location, Object> getStaticMap() {
        return staticMap;
    }

    public void setDynamicMap(Location location, Object object) {
        dynamicMap.put(location, object);
    }

    public HashMap<Location, Object> getDynamicMap() {
        return dynamicMap;
    }


    /**
     * to update agent location after a task is completed
     */
    public void setAgentLocation(int agentId, Location location) {
        //remove current agent in dynamic map
        dynamicMap.remove(allAgents.get(agentId).getLocation());

        //update agent map
        Agent agent = allAgents.get(agentId);
        agent.setLocation(location);
        allAgents.put(agentId, agent);

        //add new agent to dynamic map
        dynamicMap.put(location, agent);

    }
    /**
     * to update box location after a task is completed
     */
    public void setBoxLocation(int boxId, Location location) {
        //remove current box in dynamic map
        dynamicMap.remove(allBoxes.get(boxId).getLocation());

        //update box map
        Box box = allBoxes.get(boxId);
        box.setLocation(location);
        allBoxes.put(boxId, box);

        //add new box to dynamic map
        dynamicMap.put(location, box);
    }
    public void setTaskAsComplete(int taskId) {
        Task task = allTasks.get(taskId);
        System.err.println("[TaskCompleted] " + task.toString());
        task.setCompleted(true);
        allTasks.put(taskId,task);

    }


    public Task getTaskById(Integer taskId) {
        return allTasks.get(taskId);
    }


    public HashMap<Location, Integer> getDegreeMap(boolean[][] wallMap) {

    for (int i = 1; i <= wallMap.length; i++) {
        for (int j = 1; j <= wallMap[i].length; i++) {
            Location location = new Location(i, j);
            int k = 0;
            if (wallMap[i - 1][j]) {
                k = k + 1;
            }
            if (wallMap[i + 1][j]) {
                k = k + 1;
            }
            if (wallMap[i][j - 1]) {
                k = k + 1;
            }
            if (wallMap[i][j + 1]) {
                k = k + 1;
            }
            staticdegreeMap.put(location, k);

        }
    }
    return staticdegreeMap;
}

    public int countRemainingTask() {
        int count=0;
        for (Task task:allTasks.values()){
            if (task.isCompleted()==true){
                count++;
            }
        }
        return allTasks.size()-count;
    }



}


