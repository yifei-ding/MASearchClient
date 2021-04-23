package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class HighLevelSolver {

    private final InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Location, Boolean> map;
    private static final int INFINITY = 2147483647;

    public HighLevelSolver(InMemoryDataSource data) {
        this.data = data;
    }

    public Action[][] solve() {
        System.err.println("[HighLevelSolver] Solving...");
        ArrayList<HighLevelState> tree = new ArrayList<>();

        HighLevelState initialState = new HighLevelState(new ArrayList<Constraint>());
        initialState.calculateSolution();
        initialState.updateCost();

        tree.add(initialState);

        while (!tree.isEmpty()){
            HighLevelState node = findBestNode(tree);  //Heuristic: get a node with lowest cost; can replace with cardinal conflict (a conflict whose children has more cost)
            if (!hasConflict(node) && !hasEdgeConflict(node)) {
                Location[][] solution = node.getSolution();
                Action[][] finalSolution = translate(solution);
                return finalSolution;
            }
            else if (hasConflict(node)){
                Conflict conflict = getFirstConflict(node);
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                System.err.println("[1231231] " + conflict.toString());
                Constraint newConstraint;
                for (int i=0; i<2; i++){
                    HighLevelState child = new HighLevelState(node.getConstraints());
                    if (i==0){
                        newConstraint= new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation1());
                        System.err.println("[constraint 1] " + newConstraint.toString());
                        child.addConstraint(newConstraint);
                    }
                    else {
                        newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation2());
                        System.err.println("[constraint 2] " + newConstraint.toString());
                        child.addConstraint(newConstraint);

                    }
                    child.calculateSolution();
                    child.updateCost();
                    if (child.getCost() >0) {
                        tree.add(child);

                }
                }

            }
            else if (hasEdgeConflict(node)){

                Conflict conflict = getFirstConflict(node);
                System.err.println("Conflict" + conflict.toString()); //TODO: 4/23 debug getFirstConflict, need to return edge conflict as well.
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                Constraint newConstraint;
                Constraint newConstraint2;
                for (int i=0; i<2; i++){
                    HighLevelState child = new HighLevelState(node.getConstraints());
                    if (i==0){
                        newConstraint= new Constraint(conflict.getAgentId_1(), conflict.getTimestep()-1, conflict.getLocation1());
                        newConstraint2 =  new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation2());
                        child.addConstraint(newConstraint);
                        child.addConstraint(newConstraint2);
                    }
                    else {
                        newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep()-1, conflict.getLocation2());
                        newConstraint2 = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation1());
                        child.addConstraint(newConstraint);
                        child.addConstraint(newConstraint2);
                    }

                    child.calculateSolution();
                    child.updateCost();
                    if (child.getCost() >0) {
                        tree.add(child);

                    }
                }

            }
        }

        return null;
    }

    private HighLevelState findBestNode(ArrayList<HighLevelState> tree) {
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
        for (int i = 0; i < allPaths[0].length; i++) { //i = timestep
//            ArrayList<Location> locations = new ArrayList<>();
            HashMap<Location, Integer> locations = new HashMap<>();
            for (int j = 0; j < allPaths.length; j++) { //j = agent
                Location location = allPaths[j][i];
//                Set<Integer> indexes = locations.get(location);
                if (locations.get(location) == null) {
                    locations.put(location,j);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private Conflict getFirstConflict(HighLevelState state) {
        Location[][] allPaths = state.getSolution();
//        ArrayList<Constraint> constraints = new ArrayList<>();
        for (int i = 0; i < allPaths[0].length; i++) { //i = timestep
//            ArrayList<Location> locations = new ArrayList<>();
            HashMap<Location, Integer> locations = new HashMap<>();
            for (int j = 0; j < allPaths.length; j++) { //j = agent
                Location location = allPaths[j][i];
//                Set<Integer> indexes = locations.get(location);
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
        return null;
    }


    private Action[][] translate(Location[][] solution) {
        Action[][] finalSolution = new Action[solution.length][];
        //for each agent, get a list of its path
        for (int i=0; i<solution.length; i++){
            Location[] path = solution[i];
            Action[] singleAgentSolution = new Action[path.length-1];
            for (int j=0; j< path.length -1; j++){
                if (path[j+1].getUpNeighbour().equals(path[j]))
                    singleAgentSolution[j] = Action.MoveN;
                else if (path[j+1].getDownNeighbour().equals(path[j]))
                    singleAgentSolution[j] = Action.MoveS;
                else if (path[j+1].getLeftNeighbour().equals(path[j]))
                    singleAgentSolution[j] = Action.MoveW;
                else if (path[j+1].getRightNeighbour().equals(path[j]))
                    singleAgentSolution[j] = Action.MoveE;
            }
            finalSolution[i] = singleAgentSolution;
         }

        return finalSolution;
    }



    private boolean hasEdgeConflict(Location[] locations_1, Location[] locations_2) {
        int min_route_size = Math.min(locations_1.length,locations_2.length) - 1;
        for (int i = 0; i<min_route_size;i++){
            if(locations_1[i].equals(locations_2[i+1]) && locations_1[i+1].equals(locations_2[i])){
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
