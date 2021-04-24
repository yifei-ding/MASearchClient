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
            System.err.println("[-----Current constraints-----]: " + node.getConstraints().toString());
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
                System.err.println("[Vertex conflict] " + conflict.toString());
                Constraint newConstraint;
                HighLevelState child;
                for (int i=0; i<2; i++) {
                    if (i == 0) {
                        child = new HighLevelState(node.getConstraints());
                        newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation1());
                        System.err.println("[constraint 1] " + newConstraint.toString());
                        child.addConstraint(newConstraint);
                        child.calculateSolution();
                        child.updateCost();
                        if (child.getCost() > 0 && !tree.contains(child)) {
                            System.err.println("[Add child 1] " + child.toString());
                            tree.add(child);
                        } else {
                            child = new HighLevelState(node.getConstraints());

                            newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation2());
                            System.err.println("[constraint 2] " + newConstraint.toString());
                            child.addConstraint(newConstraint);
                            child.calculateSolution();
                            child.updateCost();
                            if (child.getCost() > 0 && !tree.contains(child)) {
                                System.err.println("[Add child 2] " + child.toString());
                                tree.add(child);

                            }


                        }
                    }
                }
            }
            else if (hasEdgeConflict(node)){

                Conflict conflict = getFirstEdgeConflict(node);
                System.err.println("*********Edge Conflict**********" + conflict.toString()); //TODO: 4/23 debug getFirstConflict, need to return edge conflict as well.
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                Constraint newConstraint;
                Constraint newConstraint2;
                if ((conflict.getAgentId_1() != -1) && (conflict.getAgentId_2() != -1)) {
                    HighLevelState child;
                    for (int i = 0; i < 2; i++) {
                        if (i == 0) {
                            child = new HighLevelState(node.getConstraints());

                            newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep() - 1, conflict.getLocation1());
                            newConstraint2 = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation2());
                            child.addConstraint(newConstraint);
                            child.addConstraint(newConstraint2);
                        } else {
                            child = new HighLevelState(node.getConstraints());

                            newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep() - 1, conflict.getLocation2());
                            newConstraint2 = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation1());
                            child.addConstraint(newConstraint);
                            child.addConstraint(newConstraint2);
                        }
                        child.calculateSolution();
                        child.updateCost();
                        if (child.getCost() > 0 && !tree.contains(child)) {
                            tree.add(child);

                        }
                    }
                }

                    else if ((conflict.getAgentId_1() == -1)){
                    HighLevelState child = new HighLevelState(node.getConstraints());
                    newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation2());
                    child.addConstraint(newConstraint);
                    child.calculateSolution();
                    child.updateCost();
                    if (child.getCost() > 0 && !tree.contains(child)) {
                        tree.add(child);

                        }

                    }
                    else if ((conflict.getAgentId_2() == -1)){
                    HighLevelState child = new HighLevelState(node.getConstraints());
                    newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation1());
                    child.addConstraint(newConstraint);
                    child.calculateSolution();
                    child.updateCost();
                    if (child.getCost() > 0 && !tree.contains(child)) {
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

                    if (route1[k].equals(route2[k+1]) && route1[k+1].equals(route2[k])) {
                            System.err.println("--------------------------" + "edege conflict 1");
                        return new Conflict(i, j, route1[k + 1], route2[k + 1], k);
                        }
                       else if (route1[k].equals(route2[k+1])){
                            System.err.println("--------------------------" + "edege conflict 2");

                            return new Conflict(-1, j, new Location(0,0), route2[k + 1], k+1);}
                        else if (route1[k+1].equals(route2[k])){
                            System.err.println("--------------------------" + "edege conflict 3");

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
