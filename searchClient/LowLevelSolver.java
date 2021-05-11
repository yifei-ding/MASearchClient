package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.lang.reflect.Array;
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
        TaskHandler taskHandler = TaskHandler.getInstance();
        //for each agent, do
        for (Agent agent : allAgents.values()) {
            //1. get an uncompleted task of the agent with highest priority
            Task task = taskHandler.pop(agent.getId());
            if (task != null) {
                //2. Preprocess: check task type, whether it is with/without box
                to = task.getTargetLocation();
                if (task.getBoxId() == -1) { //task without box
                    from = allAgents.get(agent.getId()).getLocation(); //starting position is agent location
                    boxId = -1;
                } else { //task with box
                    from = allBoxes.get(task.getBoxId()).getLocation(); //starting position is box location
                    boxId = task.getBoxId();
                }

                //3. Preprocess: prepare map and constraints for LowLevelSolver.solve
                //TODO: maybe there's need to filter constraints
                //TODO: After getting all tasks in this round, treat all other non-moving agents and boxes as obstacles.
                //4. Call LowLevelSolver.solve
                if (solvable(task)){
                    action = solve(constraints, from, to, agent.getId(), boxId, agent.getLocation());
                    plan[agent.getId()] = action;
                }
                else{ //TODO: 5/10 new methods to be implemented
                    taskHandler.taskHelper(task); //add new task
                return LowLevelSolver.solveForAllAgents(constraints); //replan
                }
            }
            else{ //agent has no task, then don't move
                action = new LocationPair[1];
                action[0] = new LocationPair(agent.getLocation(),null);
                plan[agent.getId()] = action;
            }

        }
        //print merged plan
//        System.err.println("[LowLevelSolver]Merged plan:");
//        for (int i=0; i<plan.length;i++)
//            System.err.println("Agent "+i+" : " + Arrays.toString(plan[i]));

        return plan;
    }

    private static boolean solvable(Task task) {
        //TODO: to identify whether the task is solvable
        return true;
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




}
