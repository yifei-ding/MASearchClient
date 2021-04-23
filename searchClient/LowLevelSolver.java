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

    public static Location[][] solveForAllAgents( ArrayList<Constraint> constraints)
    {
        System.err.println("[LowLevelSolver]: solve for all agents");

        allAgents = data.getAllAgents();
        allBoxes = data.getAllBoxes();
        allTasks = data.getAllTasks();
        Location from;
        Location to;
        Location[][] plan = new Location[allAgents.size()][];
        Location[] action;
        //for each agent, do
        for (Agent agent : allAgents.values()) {
            //1. get an uncompleted task of the agent with highest priority TODO: improve
            Task task = allTasks.get(data.getAllTasksByAgent(agent.getId()).get(1)); //get first task of the agent
            //2. Preprocess: check task type, whether it is with/without box
            to = task.getTargetLocation();
            if (task.getBoxId() == -1){ //task without box
                from = allAgents.get(agent.getId()).getLocation(); //starting position is agent location
                action = solve(constraints, from, to, agent.getId(), -1);
                //After single agent planning, add action list to plan[][]
                plan[agent.getId()]= action;
                System.err.println("[LowLevelSolver]: Agent " + agent.getId() + " " + Arrays.toString(action));

            }
            else { //task with box
                from = allBoxes.get(task.getBoxId()).getLocation(); //starting position is box location
                action = solve(constraints, from, to, agent.getId(), task.getBoxId());
                plan[agent.getId()]= action;
                System.err.println("[LowLevelSolver]: Box " + task.getBoxId()+ " " + Arrays.toString(action));

            }

            //3. Preprocess: prepare map and constraints for LowLevelSolver.solve
            //TODO: maybe there's need to re-design map data structure
            //4. Call LowLevelSolver.solve

        }
        //print merged plan
//        System.err.println("[LowLevelSolver]Merged plan:");
//        for (int i=0; i<plan.length;i++)
//            System.err.println("Agent "+i+" : " + Arrays.toString(plan[i]));

        return plan;
    }


    public static Location[] solve(ArrayList<Constraint> constraints, Location from, Location to, int agentId, int boxId)
    {
        //Use graph search to find a solution
        System.err.println("[LowLevelSolver]: Graph Search from " + from.toString() + " to " + to.toString());
        State initialState = new State(0, from, to, agentId, boxId);
        Frontier frontier = new FrontierBestFirst(new HeuristicAStar(initialState));
        frontier.add(initialState);
        HashSet<State> explored = new HashSet<>();
        while (true) {
            //if frontier is null return false
            if (frontier.isEmpty())
                return new Location[]{};
            //choose a node n from frontier (and remove)
            State node = frontier.pop();
            //if n is a goal state then return solution
            if (node.isGoalState()) {
                System.err.println("[LowLevelSolver] Found goal state " + node.toString());
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
                    if (!frontier.contains(m) && !explored.contains(m))
                        frontier.add(m);
                }
            }
        }

    }




}
