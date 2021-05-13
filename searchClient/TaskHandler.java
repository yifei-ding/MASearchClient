package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class TaskHandler {
    private static final TaskHandler taskHandler = new TaskHandler();

    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Integer, Goal> allGoals = new HashMap<Integer, Goal>();
    private HashMap<Integer, Agent> allAgents = new HashMap<Integer, Agent>();
    private HashMap<Integer, Box> allBoxes = new HashMap<Integer, Box>();

    private HashMap<Location, Object> staticMap = new HashMap<Location, Object>();
    private HashMap<Location, Object> dynamicMap = new HashMap<Location, Object>();
    private HashMap<Location, Integer> StaticdegreeMap = new HashMap<Location, Integer>();
    private HashMap<Integer, ArrayList<Location>> corridor = new HashMap<Integer, ArrayList<Location>>();
    private static InMemoryDataSource data = InMemoryDataSource.getInstance();
    private HashMap<Integer, ArrayList<Location>> subMap = new HashMap<Integer, ArrayList<Location>>();


    private HashMap<Location, Integer> costMap = new HashMap<Location, Integer>();
    private static final int INFINITY = 2147483647;
    private static final int PRIORITY_SCALING_PARAM = 10;

    private TaskHandler() {

    }

    public static TaskHandler getInstance() {

        return taskHandler;
    }

    public HashMap<Integer, ArrayList<Location>> getSubMap() {
        HashMap<Location, Wall> wallMap = data.getWallMap();
//        System.err.println("wall map print"+wallMap.toString());
//        System.err.println(("goal map"+data.getAllGoals().toString()));
        int num = 0;
        ArrayList<Location> explored = new ArrayList<>();
        ArrayList<Location> unExplored = new ArrayList<>();
        ArrayList<Location> frontier = new ArrayList<>();
        Iterator<Location> iterator = wallMap.keySet().iterator();
        while (iterator.hasNext()) {
            unExplored.add(iterator.next());
        }
//        System.err.println("49"+unExplored.toString());
        for (int i = 0; i < unExplored.size(); i++) {

//            System.err.println("explored size" + explored.size());

//                Location currentlocation = unExplored.get(i);
            if (wallMap.get(unExplored.get(i)).isWall()) {
                unExplored.remove(i);
            } else {
                Location currentlocation = unExplored.get(i);
                frontier.add(currentlocation);
//                    unExplored.remove(currentlocation);
//                    System.err.println("60"+frontier.toString());
//                    System.err.println("61"+unExplored.toString());
                while (!frontier.isEmpty()) {
                    unExplored.remove(currentlocation);
                    ArrayList<Location> neighbours = currentlocation.getNeighbours();
                    for (int j = 0; j < neighbours.size(); j++) {
                        Location walllocation = neighbours.get(j);
                        if (wallMap.get(walllocation) != null) {
                            if (!wallMap.get(walllocation).isWall() && unExplored.contains(walllocation)) {
                                frontier.add(walllocation);
                            }
                        }
                    }
                    neighbours.clear();
                    frontier.remove(currentlocation);
                    explored.add(currentlocation);
//                        System.err.println(frontier.size());
                    if (frontier.size() > 0) {
                        currentlocation = frontier.get(frontier.size() - 1);
                        // System.err.println("76" + unExplored.size());
                    }
                }
//                System.err.println("82 submap num=" + num + ": " + explored.toString());

                subMap.put(num, explored);
                explored = new ArrayList<>();
                frontier = new ArrayList<>();

//                    System.err.println("82 submap num="+num+": "+subMap.toString());
                num++;
                //explored.clear();
                //frontier.clear();
            }
        }

//        System.err.println("90: "+ subMap);
        return subMap;
    }








    public String getMapType() { //return which value??
        HashMap<Location, Integer> StaticdegreeMap = data.getDegreeMap();
        Iterator<Location> iterator = StaticdegreeMap.keySet().iterator();
        while (iterator.hasNext()){
            Location key = iterator.next();
            int degreenum = StaticdegreeMap.get(key);
            if( degreenum == 2){
                int i = key.getRow();
                int j = key.getCol();
                Location locationup = new Location(i,j+1); //嵌套循环然后返回走廊的location list？？
                Location locationdown = new Location(i,j+1);
                Location locationleft = new Location(i-1,j);
                Location locationright = new Location(i + 1,j);
                if(StaticdegreeMap.get(locationup) == 2 || StaticdegreeMap.get(locationdown) == 2 || StaticdegreeMap.get(locationleft) == 2 ||StaticdegreeMap.get(locationright) == 2){
                    return "corridoroccur"; }

                //check adjacent
                }
            else if (degreenum == 3){
                return "deadendoccur";
            }
            else{
            }
        }
        return  "normalmap";
    }



    /**
    * @author Xiangao Liu
    * @description assign Tasks
    * @date 2021/4/27
    * @param
    * @return void
     */


    private int getManhattanDistance(Location location1, Location location2){
        return Math.abs(location1.getCol()- location2.getCol())
                + Math.abs(location1.getRow())- location2.getRow();
    };

    public void assignTask(){
        allGoals = data.getAllGoals();
        allAgents = data.getAllAgents();
        allBoxes = data.getAllBoxes();
        int taskId = 0;
        int priority = 0;
        int subNum = 0;
        subMap = getSubMap();
//        System.err.println(subMap.toString());
        HashSet<Integer> matchedBoxes = new HashSet<>();
        for (Goal goal : allGoals.values()){
            Location goallocation = goal.getLocation();
            //get the number of submap which goal belong to
            Iterator<Integer> iterator = subMap.keySet().iterator();
            while (iterator.hasNext()){
                Integer key = iterator.next();
                ArrayList<Location> locations = subMap.get(key);
                if(locations.contains(goallocation)){
                    subNum = key;
                }
            }
            //for each goal, find a same name box. Currently use the first matching box.
            ArrayList<Integer> boxList = data.getBoxByName(goal.getName());

            //if boxes in boxlist not at the submap same to goal,remove it from boxlist

            if(boxList == null){// No box match, so goal match agent by name ? or + color?
                //agent to goal without box
                int goalName = Integer.parseInt(goal.getName());
                Agent agent = allAgents.get(goalName);// according to goalName match agent
                Location agentlocation = agent.getLocation();
//                System.err.println("sun number"+ subMap.get(subNum).toString());
                if(!subMap.get(subNum).contains(agentlocation)){

                    System.err.println("This map is not solvable!");
                    //conditions: box problem, but all box removed

                }else {

                    priority = getManhattanDistance(agent.getLocation(), goal.getLocation()) * 10;
                    priority = priority + 10000;//considering normally the agent need done box tasks
                    Task task = new Task(taskId, agent.getId(), goal.getLocation(), priority);
                    taskId++;
                    data.addTask(task);
                }
            }
            // TODO: box-goal matching
            else { // if there is any box
//                Box box = allBoxes.get(boxList.get(0)); //TODO: only one box? improve goal-box matching. Remove box after pairing
//                System.err.println("sub number"+ subMap.get(subNum).toString());
                for(int i = 0; i < boxList.size(); i++){
                    int boxId = boxList.get(i);
                    Location boxLocation = allBoxes.get(boxId).getLocation();
                    if(!subMap.get(subNum).contains(boxLocation)){
                        boxList.remove(i);
                    }
                }
                int min_distance = -1; // match the nearest box
                Box matchedBox = null; //
                ArrayList<Integer>  matchAgentIds= new ArrayList<>();
                for (int boxId : boxList){
                    if(matchedBoxes.contains(boxId)){
                        continue;
                    }
                    Box box = allBoxes.get(boxId);
                    ArrayList<Integer> agentList = data.getAgentByColor(box.getColor());
                    if(agentList == null){
                        continue;
                    }
                    int distance = getManhattanDistance(goal.getLocation(),box.getLocation());
                    if (min_distance == -1 || distance < min_distance){
                        min_distance = distance;
                        matchedBox = box;
                        matchAgentIds = agentList;
                    }
                    //TODO: check if min_distance is correct
                }

                if (matchedBox == null){
                    System.err.println("This map is not solvable!");
                    continue;
                }
                //then for the box, find a same color agent. Currently use the first matching agent.
                //TODO: improve box-agent matching
                int min_score = -1;
                int min_distance2 = -1;
                Agent matchedAgent = null;
                for(int agentId : matchAgentIds) {
                    if (!subMap.get(subNum).contains(allAgents.get(agentId).getLocation())) {
                        System.err.println("This agent is not applicable!"+ agentId+" : "+goal.getName());
                    } else {
                        Agent agent = allAgents.get(agentId);
                        ArrayList<Integer> tasks = data.getAllTasksByAgent(agentId);
                        int task_score = 0;
                        if (tasks != null) {
                            task_score = tasks.size() * 10;
                        }
                        int distance = getManhattanDistance(agent.getLocation(), matchedBox.getLocation());
                        int score = distance + task_score;
                        if (min_score == -1 || score < min_score) {
                            min_score = score;
                            min_distance2 = distance;
                            matchedAgent = agent;
                        }
                    }
                }

                //for each goal, create 2 tasks. Tasks of the first goal has highest priority.
                //task1 is to find box. Task1 has higher priority than task2.
//                priority = goalCount*PRIORITY_SCALING_PARAM;
                priority = (min_distance + min_distance2)*10; // agent-box-goal sum distance x10
               //Task task1 = new Task(taskId,matchedAgent.getId(),matchedBox.getLocation(),priority); // 5/5 skip task1
                taskId++;
                //task2 is to push/pull box to goal.
                //TODO: When planning task2, need to check if agent is beside the box. Otherwise create a new task of task1 to let agent find box.
//                System.err.println("267"+ matchedAgent.getId());
                if(matchedAgent!=null){
                    Task task2 = new Task(taskId,matchedAgent.getId(),matchedBox.getId(), goal.getLocation(),priority-1);
                    matchedBoxes.add(matchedBox.getId()); // add matched box into restriction
                    taskId++;
                    //data.addTask(task1);
                    data.addTask(task2);
                }
//                goalCount--;
            }
        }
        System.err.println("[TaskHandler] Initial tasks: "+data.getAllTasks().toString());
    }

    // TODO: input a task which no solution, return the sub-tasks for helping solving it
    // TODO: if the box/agent already has its task to move out of the path?
    // if the task is move the box/agent to the goal, just use the box.location.
    // if it's a sub  task for move box/agent out of the path, use the orginal location, so we can check the task is still valid or not.
    public void taskHelper(Task task){
        // find the path in static map
        HashMap<Location, Integer> staticdegreeMap = data.getStaticdegreeMap();
        HashMap<Location, Object> dynamicMap = data.getDynamicMap();
        LocationPair[] locationPairs;
        Location from;
        Location to;
        int boxId;
        to = task.getTargetLocation();
        int agentId = task.getAgentId();
        Agent agent = allAgents.get(agentId);
        Location location = agent.getLocation();
        Constraint constraint = null;
        if (task.getBoxId() == -1) { //task without box
            from = allAgents.get(agent.getId()).getLocation(); //starting position is agent location
            boxId = -1;
        } else { //task with box
            from = allBoxes.get(task.getBoxId()).getLocation(); //starting position is box location
            boxId = task.getBoxId();
        }
        locationPairs = LowLevelSolver.staticSolve(from, to, agent.getId(), boxId, agent.getLocation()); // locations
        //find obstacles
        HashSet<Location> obstacles = new HashSet<>(); // the mandatory locations
//        HashSet<Location> pathLocations = new HashSet<>();
        boolean isInCorridor = false;
        for(LocationPair locationPair : locationPairs){
            Location location_temp = locationPair.getAgentLocation();
//            pathLocations.add(location_temp);
            if(staticdegreeMap.get(location_temp)==2){// recognize the corridor, -the mandatory locations
                isInCorridor = true;
                obstacles.add(location_temp);
            }else if(isInCorridor){
                obstacles.add(location_temp);
                isInCorridor = false;
            }
        }
        for(Location location_temp: obstacles){
            if(dynamicMap.get(location_temp)!=null){ // check if there is object
                Object obj = dynamicMap.get(location);
                if (obj instanceof Box) {
                    int newBoxId = ((Box) obj).getId();
                    int taskId = allTasks.size();
                    Location targetLocation = findTargetLocation(obstacles,location_temp);//from the original cell to find a nearest cell match the requirements
                    // Find the best match agent
                    int newAgentId = findBestAgent(newBoxId,targetLocation);
                    int distance = getManhattanDistance(allAgents.get(newAgentId).getLocation(),allBoxes.get(newBoxId).getLocation())+getManhattanDistance(allBoxes.get(newBoxId).getLocation(),targetLocation);
                    int priority = distance*10;
                    Task newTask_1 = new Task(taskId,newAgentId,-1,allBoxes.get(boxId).getLocation(),priority);
                    Task newTask_2 = new Task(taskId,newAgentId,newBoxId,targetLocation,priority+1); // TOdo: set the priority
                    data.addTask(newTask_1);
                    data.addTask(newTask_2);
                }else if(obj instanceof Agent){
                    int newAgentId = ((Agent) obj).getId();
                    int taskId = allTasks.size();
                    Location targetLocation = findTargetLocation(obstacles,location_temp);
                    Task newTask = new Task(taskId,newAgentId,-1,targetLocation,0);
                    data.addTask(newTask);
                }
            }
        }
    }



    public Location findTargetLocation(HashSet<Location> obstacles,Location startLocation){ //find the goal according to given obstacles
        Location currentLocation = startLocation;
        HashSet<Location> exploredPath = new HashSet<Location>();
        do{
            ArrayList<Location> fourDirections = new ArrayList<Location>(); // to explore the 4 neighbors
            int i = currentLocation.getRow();
            int j = currentLocation.getCol();
            Location locationUp = new Location(i, j + 1);
            Location locationDown = new Location(i, j - 1);
            Location locationLeft = new Location(i - 1, j);
            Location locationRight = new Location(i + 1, j);
            fourDirections.add(locationUp);
            fourDirections.add(locationDown);
            fourDirections.add(locationLeft);
            fourDirections.add(locationRight);
            // choose the next cell
            for (Location location : fourDirections) {
                if (StaticdegreeMap.get(location) != null && !exploredPath.contains(location)) {//not wall, and new
                    currentLocation = location; //update location
                    exploredPath.add(currentLocation);
                }
            }

        } while (obstacles.contains(currentLocation)==true || dynamicMap.get(currentLocation)!=null); //end requirements: not in obstacle positions and no object in that location

        return currentLocation;
    }

    public int findBestAgent(int boxId, Location targetLocation){
        //according to box color and location find match agents
        Box box = allBoxes.get(boxId);
        ArrayList<Integer> agentList = data.getAgentByColor(box.getColor());
        int min_distance = -1;
        int matchedAgentId = -1;
        for(int agentId : agentList){
            Agent agent = allAgents.get(agentId);
            int distance = getManhattanDistance(box.getLocation(), agent.getLocation());
            if (min_distance == -1 || distance < min_distance){
                min_distance = distance;
                matchedAgentId = agentId;
            }
        }
        return matchedAgentId;
    }



    public Task pop(int agentId){
        /**
        * @author Yifei
        * @description Pop a task of the given agentId that is not completed and with highest priority
        * @date 2021/4/27
        * @param [agentId]
        * @return Task
         */
        Task task = null;
        ArrayList<Integer> taskList = data.getAllTasksByAgent(agentId); //already in descending order
        if (taskList != null){
            for (Integer taskId:taskList){
                if (!data.getTaskById(taskId).isCompleted()) //check whether the task is completed
                    task = data.getTaskById(taskId);
            }
        }
        return task;
    }

    public void completeTask(int agentId) {
      Task task = pop(agentId);
      if (task != null){
          //update task status to complete
          data.setTaskAsComplete(task.getId());
      }

    }
}
