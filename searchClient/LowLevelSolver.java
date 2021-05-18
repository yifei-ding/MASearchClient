package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.*;

public class LowLevelSolver {
    private static HashMap<Integer, Agent> allAgents;
    private static HashMap<Integer, Box> allBoxes;
    private static HashMap<Integer, Task> allTasks;
    private static InMemoryDataSource data = InMemoryDataSource.getInstance();

    public static LocationPair[][] solveForAllAgents( HashSet<Constraint> constraints)
    {
//        System.err.println("[LowLevelSolver]: solve for all agents");

        allAgents = data.getAllAgents();
        allBoxes = data.getAllBoxes();
        allTasks = data.getAllTasks();
        Location from;
        Location to;
        LocationPair[][] plan = new LocationPair[allAgents.size()][];
        LocationPair[] action;
        int boxId;
        TaskHandler taskHandler = TaskHandler.getInstance();
        data.initializeObstacleMap(); //treat all walls, agents and boxes as obstacles.
        ArrayList<Task> taskList = new ArrayList<>();
        //for each agent, do
        for (Agent agent : allAgents.values()) {
            //1. get an uncompleted task of the agent with highest priority
            Task task = taskHandler.pop(agent.getId());
            if (task != null) {
//                System.err.println("Agent "+agent.getId() + " have task " + task.toString());
                taskList.add(task);
            }
            else{ //agent has no task, then don't move
//                System.err.println("Agent "+agent.getId() + " don't have task");

                action = new LocationPair[1];
                action[0] = new LocationPair(agent.getLocation(),null);
                plan[agent.getId()] = action;
            }
        }
        for (Task task:taskList){
            //2. After getting all tasks in this round, treat agents and boxes who has a task as non-obstacle.
            Agent agent = data.getAgent(task.getAgentId());
            data.setObstacleMap(agent.getLocation(),false);
            if (task.getBoxId() != -1){
                Box box = data.getBox(task.getBoxId());
                data.setObstacleMap(box.getLocation(),false);
            }
        }
//        System.err.println("[LowLevelSovler] Created new obstacle map: " + data.getObstacleMap().toString());
        for (Task task:taskList){
            //3. Preprocess: check task type, whether it is with/without box
            to = task.getTargetLocation();
            if (task.getBoxId() == -1) { //task without box
                from = allAgents.get(task.getAgentId()).getLocation(); //starting position is agent location
                boxId = -1;
            } else { //task with box
                from = allBoxes.get(task.getBoxId()).getLocation(); //starting position is box location
                boxId = task.getBoxId();
            }
            //TODO: maybe there's need to filter constraints
            //4. Call LowLevelSolver.solve
            if (solvable(task)){
//                System.err.println("[LowLevelSolver]Solvable: " + task.toString());
                int agentId = task.getAgentId();
                action = solve(constraints, from, to, task.getAgentId(), boxId, data.getAgent(agentId).getLocation());
                plan[agentId] = action;
            }
            else{
//                System.err.println("[LowLevelSolver]Not solvable: " + task.toString());
                taskHandler.taskHelper(task); //call helper to generate new tasks to help remove obstacles
                return LowLevelSolver.solveForAllAgents(constraints); //replan
            }
        }
        //print merged plan
//        System.err.println("[LowLevelSolver]Merged plan:");
//        for (int i=0; i<plan.length;i++)
//            System.err.println("Agent "+i+" : " + Arrays.toString(plan[i]));

        return plan;
    }

    private static boolean solvable(Task task) {
//        System.err.println("Call solvable");
        Location from;
        Location goalLocation = task.getTargetLocation();
        Location agentInitialLocation = allAgents.get(task.getAgentId()).getLocation();
        //do a low level search, with no constraints
        if (task.getBoxId() == -1) { //task without box
            return IsConnected(agentInitialLocation,goalLocation);
        } else { //task with box
            Location boxInitialLocation = allBoxes.get(task.getBoxId()).getLocation();
            if (IsConnected(boxInitialLocation,goalLocation)){
                return IsConnected(agentInitialLocation,boxInitialLocation);
            }
            else return false;
        }
    }

    private static boolean IsConnected(Location from, Location to) {
        HashMap<Location, Boolean> obstacleMap = data.getObstacleMap();
        ArrayList<Location> explored = new ArrayList<>();
        ArrayList<Location> frontier = new ArrayList<>();
        Location initial = from;
        Location goal = to;
        frontier.add(initial);
        while (!frontier.isEmpty()){
            Location node = frontier.get(0);
            frontier.remove(0);
//            System.err.println("Frontier.size: " + frontier.size());
            explored.add(node);
            if (node.equals(goal)) {
//            System.err.println("Connected");
                return true;
            }
            else{
                ArrayList<Location> neighbours = node.getNeighbours();
                for (Location neighbour:neighbours){
                    if (obstacleMap.containsKey(neighbour)){
                        if (!obstacleMap.get(neighbour)) {
                            if (!frontier.contains(neighbour) && !explored.contains(neighbour)){
                                frontier.add(neighbour);
                            }
                        }
                    }
                }
            }

        }
//        System.err.println("Not connected");

        return false;
    }

    public static LocationPair[] solve(HashSet<Constraint> constraints, Location from, Location to, int agentId, int boxId, Location agentLocation)
    {
        //Use graph search to find a solution
//        System.err.println("[LowLevelSolver]: Graph Search from " + from.toString() + " to " + to.toString() + " Agent Location " + agentLocation.toString());
        State initialState = new State(0, from, to, agentId, boxId, agentLocation, constraints);
        Frontier frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
//        Frontier frontier = new FrontierDFS();

        frontier.add(initialState);
        HashSet<State> explored = new HashSet<>();
        while (true) {
//            System.err.println("[LowLevelSolver] Frontier size: " + frontier.size());
            //if frontier is null return false
//            if (frontier.isEmpty()) { //TODO: Note: frontier will never be empty
//                System.err.println("[LowLevelSolver]Frontier is empty");
//                return new LocationPair[]{};
//            }
            //choose a node n from frontier (and remove)
            State node = frontier.pop();
            //if n is a goal state then return solution
            if (node.isGoalState()) {
//                System.err.println("[LowLevelSolver] Found goal state " + node.toString());
                return node.extractPlan();
            }
            else {
                //add n to expandedNodes
                explored.add(node);
                //get children of n
                ArrayList<State> children = node.getExpandedStates();
                //for each child m of n // we expand n
                for (State m : children){
//                    System.err.println("[LowLevelSolver] Find children: " + m.toString());

                    //if m is not in frontier and m in not in expandedNodes then
                    //add child m to frontier
                    if (!frontier.contains(m) && !explored.contains(m)) {
//                        System.err.println("[LowLevelSolver] Add children: " + m.toString());

                        frontier.add(m);
                    }
                }
            }
        }

    }

    public static LocationPair[] staticSolve(Location from, Location to, int agentId, int boxId, Location agentLocation)
    {
        HashSet<Constraint> constraints = null;
        State initialState = new State(0, from, to, agentId, boxId, agentLocation, constraints);
        Frontier frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
//        Frontier frontier = new FrontierDFS();

        frontier.add(initialState);
        HashSet<State> explored = new HashSet<>();
        while (true) {
//            System.err.println("[LowLevelSolver] Frontier size: " + frontier.size());
            //if frontier is null return false
            if (frontier.isEmpty()) { //TODO: implement solvable(task) instead
                System.err.println("[LowLevelSolver]Frontier is empty");
                return new LocationPair[]{};
            }
            //choose a node n from frontier (and remove)
            State node = frontier.pop();
            //if n is a goal state then return solution
            if (node.isGoalState()) {
//                System.err.println("[LowLevelSolver] Found goal state " + node.toString());
                return node.extractPlan();
            }
            else {
                //add n to expandedNodes
                explored.add(node);
                //get children of n
                ArrayList<State> children = node.getStaticExpandedStates();
                //for each child m of n // we expand n
                for (State m : children){
//                    System.err.println("[LowLevelSolver] Find children: " + m.toString());

                    //if m is not in frontier and m in not in expandedNodes then
                    //add child m to frontier
                    if (!frontier.contains(m) && !explored.contains(m)) {
//                        System.err.println("[LowLevelSolver] Add children: " + m.toString());

                        frontier.add(m);
                    }
                }
            }
        }

    }


}
