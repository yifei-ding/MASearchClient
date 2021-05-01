package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

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
        //for each agent, do
        for (Agent agent : allAgents.values()) {
            //1. get an uncompleted task of the agent with highest priority
            Task task = allTasks.get(data.getAllTasksByAgent(agent.getId()).get(0)); //TODO: improve; currently just get first task of the agent
            //2. Preprocess: check task type, whether it is with/without box
            to = task.getTargetLocation();
            if (task.getBoxId() == -1){ //task without box
                from = allAgents.get(agent.getId()).getLocation(); //starting position is agent location
                boxId = -1;
            }
            else { //task with box
                from = allBoxes.get(task.getBoxId()).getLocation(); //starting position is box location
                boxId = task.getBoxId();
            }

            //3. Preprocess: prepare map and constraints for LowLevelSolver.solve
            //TODO: maybe there's need to filter constraints
            //TODO: After getting all tasks in this round, treat all other non-moving agents and boxes as obstacles.
            //4. Call LowLevelSolver.solve
            action = solve(constraints, from, to, agent.getId(), boxId, agent.getLocation());
            plan[agent.getId()]= action; //TODO: for each timestep, return a pair of location instead of just one location for pushing/pulling the box

        }
        //print merged plan
//        System.err.println("[LowLevelSolver]Merged plan:");
//        for (int i=0; i<plan.length;i++)
//            System.err.println("Agent "+i+" : " + Arrays.toString(plan[i]));

        return plan;
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
            //if frontier is null return false
            if (frontier.isEmpty())
                return new LocationPair[]{};
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
                    //if m is not in frontier and m in not in expandedNodes then
                    //add child m to frontier
                    if (!frontier.contains(m) && !explored.contains(m)) {
                        frontier.add(m);
                    }
                }
            }
        }

    }




}
