package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.*;

public class HighLevelSolver {

    private final InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Location, Boolean> map;
    private static final int INFINITY = 2147483647;
    private ArrayList<HighLevelState> tree = new ArrayList<>();


    public HighLevelSolver(InMemoryDataSource data) {
        this.data = data;
    }

    public Action[][] solve() {
        System.err.println("[HighLevelSolver] Solving...");

        HighLevelState initialState = new HighLevelState(new ArrayList<>());
        initialState.calculateSolution();
        initialState.updateCost();

        tree.add(initialState);

        while (!tree.isEmpty()){
            HighLevelState node = findBestNodeWithMinCost(tree);  //Heuristic: get a node with lowest cost; can replace with cardinal conflict (a conflict whose children has more cost)
//            System.err.println("[----------Best Node----------]: " + node.toString());
            System.err.println("[------------------Current constraints--------------]: " + node.getConstraints().size());
            System.err.println("[------------------Current tree--------------]: " + tree.size());

            if (!hasConflict(node) && !hasEdgeConflict(node)) {
                Location[][] solution = node.getSolution();
                Action[][] finalSolution = translate(solution);
                printSolution(finalSolution);
                return finalSolution;
            }
            else if (hasConflict(node)){
                Conflict conflict = getFirstConflict(node);
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
//                System.err.println("[Vertex conflict] " + conflict.toString());
                for (int i = 0; i < 2; i++) {
                    HighLevelState child = new HighLevelState(node.getConstraints());
//                    System.err.println("[--------------------]: "+i +"th child" + node.getConstraints());
//                    System.err.println("[New child]" + child.toString()); //4/25 debug solved by create new ArrayList for each child
                    Constraint newConstraint;
                    if (i==0) {
                        newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation1());
                    }else {
                        newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation2());
                    }
                    child.addConstraint(newConstraint);
                    child.calculateSolution();
                    child.updateCost();
                    this.addToTree(child);
                }
            }
            else if (hasEdgeConflict(node)){
                Conflict conflict = getFirstEdgeConflict(node);
//                System.err.println("[Edge conflict] " + conflict.toString());
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                Constraint newConstraint;
                Constraint newConstraint2;
                if ((conflict.getAgentId_1() != -1) && (conflict.getAgentId_2() != -1)) {
                    for (int i = 0; i < 2; i++) {
                        HighLevelState child = new HighLevelState(node.getConstraints());
                        if (i == 0) {
                            newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep() - 1, conflict.getLocation1());
                            newConstraint2 = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation2());
                        } else {
                            newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep() - 1, conflict.getLocation2());
                            newConstraint2 = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation1());
                        }
                        child.addConstraint(newConstraint);
                        child.addConstraint(newConstraint2);
                        child.calculateSolution();
                        child.updateCost();
                        this.addToTree(child);
                    }
                }
                else if ((conflict.getAgentId_1() == -1)){
                    HighLevelState child = new HighLevelState(node.getConstraints());
                    newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation2());
                    child.addConstraint(newConstraint);
                    child.calculateSolution();
                    child.updateCost();
                    this.addToTree(child);
                }
                else if ((conflict.getAgentId_2() == -1)){
                    HighLevelState child = new HighLevelState(node.getConstraints());
                    newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation1());
                    child.addConstraint(newConstraint);
                    child.calculateSolution();
                    child.updateCost();
                    this.addToTree(child);
                }
            }
        }
        return null;
    }

    private void addToTree(HighLevelState child) {
//        System.err.println("[Check child] " + child.toString());

//        if (child.getCost()>0)
//            System.err.println("[Check child] cost>0");
//        if (!tree.contains(child))
//            System.err.println("[Check child] tree doesn't contain this child");

        if (child.getCost() > 0 && !tree.contains(child)) {
            System.err.println("[Add child]");
            tree.add(child);
        }
    }


    private HighLevelState findBestNodeWithMinCost(ArrayList<HighLevelState> tree) {
        int min = INFINITY;
        HighLevelState bestNode = null;
        for (HighLevelState node: tree){
            if (node.getCost() < min){
                min = node.getCost();
                bestNode = node;
            }

        }
        return bestNode;
    }


    private boolean hasConflict(HighLevelState state) {  // TODO: add boxes into the Pathes
        Location[][] allPaths = state.getSolution();
        int max = getMaxPathLength(allPaths);
        Location location;
        for (int i = 0; i < max; i++) { //i = timestep
            HashMap<Location, Integer> locations = new HashMap<>();
            for (int j = 0; j < allPaths.length; j++) { //j = agent
                if(allPaths[j]==null){ // TODO: check the reason
                    return false;
                }
                if (i < allPaths[j].length)  //4/25 debug fix: because each agent has different length of solution, need to check length while getting an element in solution[][]
                    location = allPaths[j][i];
                else
                    location = allPaths[j][allPaths[j].length-1];
                if (locations.get(location) == null) {
                    locations.put(location,j);
                } else {
                    return true;
                }
            }
        }
        return false;
    }
    private int getMinPathLength(Location[][] solution){
        int min = INFINITY;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i].length < min)
                min = solution[i].length;
        }
        return min;
    }
    private int getMaxPathLength(Location[][] solution){
        int max = 0;
        for (int i = 0; i < solution.length; i++) {//TODO: Check why no solution
            if(solution[i]==null){
                continue;
            }
            if (solution[i].length > max)
                max = solution[i].length;
        }
        return max;
    }

    private Conflict getFirstConflict(HighLevelState state) {
        Location[][] allPaths = state.getSolution();
        int max = getMaxPathLength(allPaths);
        Location location;
        for (int i = 0; i < max; i++) { //i = timestep
            HashMap<Location, Integer> locations = new HashMap<>();
            for (int j = 0; j < allPaths.length; j++) { //j = agent
                if(allPaths[j]==null){ // TODO:Check
                    return null;
                }
                if (i < allPaths[j].length)  //4/25 debug fix: because each agent has different length of solution, need to check length while getting an element in solution[][]
                    location = allPaths[j][i];
                else
                    location = allPaths[j][allPaths[j].length-1];
                if (locations.get(location) == null) {
                    locations.put(location,j);
                } else {
                    int agentId_1 = j;
                    int agentId_2 = locations.get(location);
                    Conflict conflict = new Conflict(agentId_1,agentId_2, location, location, i);
                    return conflict;
                }
            }
        }
        return new Conflict(0,0,new Location(0,0),new Location(0,0),0);

    }


    private Action[][] translate(Location[][] solution) {
        Action[][] finalSolution = new Action[solution.length][];
        //for each agent, get a list of its path
        for (int i=0; i<solution.length; i++){
            Location[] path = solution[i];
            Action[] singleAgentSolution = new Action[path.length];
            for (int j=0; j< path.length -1; j++){
                if (j==0){
                    if (data.getAgent(i).getLocation().getUpNeighbour().equals(path[j]))
                        singleAgentSolution[j] = Action.MoveN;
                    else if (data.getAgent(i).getLocation().getDownNeighbour().equals(path[j]))
                        singleAgentSolution[j] = Action.MoveS;
                    else if (data.getAgent(i).getLocation().getLeftNeighbour().equals(path[j]))
                        singleAgentSolution[j] = Action.MoveW;
                    else if (data.getAgent(i).getLocation().getRightNeighbour().equals(path[j]))
                        singleAgentSolution[j] = Action.MoveE;
                    else if (data.getAgent(i).getLocation().equals(path[j]))
                        singleAgentSolution[j] = Action.NoOp;
                }

                if (path[j].getUpNeighbour().equals(path[j + 1]))
                    singleAgentSolution[j+1] = Action.MoveN;
                else if (path[j].getDownNeighbour().equals(path[j + 1]))
                    singleAgentSolution[j+1] = Action.MoveS;
                else if (path[j].getLeftNeighbour().equals(path[j + 1]))
                    singleAgentSolution[j+1] = Action.MoveW;
                else if (path[j].getRightNeighbour().equals(path[j + 1]))
                    singleAgentSolution[j+1] = Action.MoveE;
                else if (path[j].equals(path[j + 1]))
                    singleAgentSolution[j+1] = Action.NoOp;

            }
            finalSolution[i] = singleAgentSolution;
         }

        return finalSolution;
    }

    //print merged plan
    private void printSolution(Action[][] solution){
        System.err.println("[HighLevelState] Found solution: ");
        for (int i=0; i<solution.length;i++)
            System.err.println("Agent "+i+" : " + Arrays.toString(solution[i]));
    }


    private Conflict getFirstEdgeConflict(HighLevelState state) {
        Location[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++){
            for(int j = i+1;j<solution.length;j++){
                int minIndex = Math.min(solution[i].length,solution[j].length) ;
                for (int k=0; k< minIndex; k++){
                    Location[] route1 = solution[i];
                    Location[] route2 = solution[j];
                    if (route1[k].equals(route2[k+1]) && route1[k+1].equals(route2[k])){
                            System.err.println("Edge conflict type 1");
                            return new Conflict(i, j, route1[k + 1], route2[k + 1], k);
                    }
                    else if (route1[k].equals(route2[k+1])){
                        System.err.println("Edge conflict type 2");
                        return new Conflict(-1, j, new Location(0,0), route2[k + 1], k+1);
                    }
                    else if (route1[k+1].equals(route2[k])){
                        System.err.println("Edge conflict type 3");
                        return new Conflict(i, -1, route1[k + 1], new Location(0,0), k+1);
                    }
                }
            }
        }
        return new Conflict(0,0,new Location(0,0),new Location(0,0),0);
    }
    private boolean hasEdgeConflict(Location[] locations_1, Location[] locations_2) {
        int min_route_size = Math.min(locations_1.length,locations_2.length) - 1;
        for (int i = 0; i<min_route_size;i++){
            if(locations_1[i].equals(locations_2[i+1]) || locations_1[i+1].equals(locations_2[i])){
                return true;
            }
        }
        return false;
    }
    private boolean hasEdgeConflict(HighLevelState state){
        Location[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++){
            for(int j = i+1;j<solution.length;j++){
                if(hasEdgeConflict(solution[i],solution[j])){
                    return true;
                }
            }
        }
        return false;
    }


}
