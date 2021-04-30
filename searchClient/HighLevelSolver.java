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

        HighLevelState initialState = new HighLevelState(new HashSet<>());
        initialState.calculateSolution();
        initialState.updateCost();

        tree.add(initialState);

        while (!tree.isEmpty()){
            HighLevelState node = findBestNodeWithMinCost(tree);  //Heuristic: get a node with lowest cost; can replace with cardinal conflict (a conflict whose children has more cost)
//            System.err.println("[----------Best Node----------]: " + node.toString());
            System.err.println("[-----------------Constraints of the current pop out node--------------]: " + node.getConstraints().toString());
            System.err.println("[------------------Current CT tree size--------------]: " + tree.size());

            if (!hasVertexConflict(node) && !hasEdgeConflict(node)) {
                LocationPair[][] solution = node.getSolution();
                Action[][] finalSolution = translate(solution);
                //printSolution(finalSolution);
                return finalSolution;
            }
            else if (hasVertexConflict(node)){
                Conflict conflict = getFirstVertexConflict(node);
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                System.err.println("[Vertex conflict] " + conflict.toString());
                // expand the node
                addChildrenOfVertexConflictToTree(node, conflict); // new on 4/30
            }
//            else if (hasEdgeConflict(node)){
//                Conflict conflict = getFirstEdgeConflict(node);
////                System.err.println("[Edge conflict] " + conflict.toString());
//                // Remove current node from tree because it has conflicts.
//                tree.remove(node);
//                Constraint newConstraint;
//                Constraint newConstraint2;
//                if ((conflict.getAgentId_1() != -1) && (conflict.getAgentId_2() != -1)) {
//                    for (int i = 0; i < 2; i++) {
//                        HighLevelState child = new HighLevelState(node.getConstraints());
//                        if (i == 0) {
//                            newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep() - 1, conflict.getLocation1());
//                            newConstraint2 = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation2());
//                        } else {
//                            newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep() - 1, conflict.getLocation2());
//                            newConstraint2 = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation1());
//                        }
//                        child.addConstraint(newConstraint);
//                        child.addConstraint(newConstraint2);
//                        child.calculateSolution();
//                        child.updateCost();
//                        this.addToTree(child);
//                    }
//                }
//                else if ((conflict.getAgentId_1() == -1)){
//                    HighLevelState child = new HighLevelState(node.getConstraints());
//                    newConstraint = new Constraint(conflict.getAgentId_2(), conflict.getTimestep(), conflict.getLocation2());
//                    child.addConstraint(newConstraint);
//                    child.calculateSolution();
//                    child.updateCost();
//                    this.addToTree(child);
//                }
//                else if ((conflict.getAgentId_2() == -1)){
//                    HighLevelState child = new HighLevelState(node.getConstraints());
//                    newConstraint = new Constraint(conflict.getAgentId_1(), conflict.getTimestep(), conflict.getLocation1());
//                    child.addConstraint(newConstraint);
//                    child.calculateSolution();
//                    child.updateCost();
//                    this.addToTree(child);
//                }
//            }
        }
        return null;
    }

    /**
    * @author Yifei
    * @description create 2 children; then for each child, use conflict to generate a constraint, add constraint, calculate solution, update cost, add child to tree.
    * @date 2021/4/30
    * @param node, conflict
     * @return void
     */
    private void addChildrenOfVertexConflictToTree(HighLevelState node, Conflict conflict) {
        for (int i = 0; i < 2; i++) {
            HighLevelState child = new HighLevelState(node.getConstraints());
            Constraint newConstraint;
            if (i==0) {
                /**
                * The first constraint can either be for a box or for an agent
                 */
                if (conflict instanceof BoxBoxConflict)
                    newConstraint = new Constraint(conflict.getId1(), true, conflict.getTimestep(), conflict.getLocation1());
                else
                    newConstraint = new Constraint(conflict.getId1(), false, conflict.getTimestep(), conflict.getLocation1());
            }
            else {
                /**
                 * The second constraint can either be for a box or for an agent
                 */
                if (conflict instanceof AgentAgentConflict)
                    newConstraint = new Constraint(conflict.getId2(), false, conflict.getTimestep(), conflict.getLocation2());
                else
                    newConstraint = new Constraint(conflict.getId2(), true, conflict.getTimestep(), conflict.getLocation2());

            }
            child.addConstraint(newConstraint);
            child.calculateSolution();
            child.updateCost();
            addToTree(child);
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


    private boolean hasVertexConflict(HighLevelState state) {  // TODO: add boxes into the Pathes
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                Conflict conflict = getFirstVertexConflict(i,j,route1,route2);
                if (conflict != null) {
//                    System.err.println("getFirstVertexConflict "+ conflict.toString());
                    return true;
                }
            }
        }
        return false;
    }

    private Conflict getFirstVertexConflict(HighLevelState state){
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                System.err.println("i= "+i + " j="+j);
                //now we have one path each for agent1 and agent2
                Conflict conflict = getFirstVertexConflict(i,j,route1,route2);
                System.err.println("FirstVertexConflict: "+ conflict.toString());
                System.err.println("Conflict type: "+ conflict.getClass().getName());

                if (conflict != null)
                    return conflict;
            }
        }
        return null;
    }

    private Conflict getFirstVertexConflict(int agentId1, int agentId2, LocationPair[] route1, LocationPair[] route2) {
        int minIndex = Math.min(route1.length, route2.length);
        Location agentLocation1;
        Location agentLocation2;
        Location boxLocation1;
        Location boxLocation2;
        for (int k=0; k< minIndex; k++){ //timestep
            agentLocation1 = route1[k].getAgentLocation();
            agentLocation2 = route2[k].getAgentLocation();
            boxLocation1 = route1[k].getBoxLocation();
            boxLocation2 = route2[k].getBoxLocation();
//            System.err.println("Check conflict at timestep "+ k);
            //vertex conflict could be 3 types of conflict: agent-agent conflict, agent-box conflict, box-box conflict
            if (agentLocation1.equals(agentLocation2))
                return new AgentAgentConflict(agentId1,agentId2,agentLocation1,agentLocation2,k);
            //note: boxLocation can be null
            if (boxLocation1 !=null) {
                if (agentLocation1.equals(boxLocation2))
                    return new AgentBoxConflict(agentId1, agentId2, agentLocation1, boxLocation2, k);
                else if (agentLocation2.equals(boxLocation1))
                    return new AgentBoxConflict(agentId2, agentId1, agentLocation2, boxLocation1, k);
                else if (boxLocation1.equals(boxLocation2))
                    return new BoxBoxConflict(agentId1, agentId2, boxLocation1, boxLocation2, k);
            }
          }
         return null;
    }

    private Conflict getFirstConflict(HighLevelState state) {
        LocationPair[][] allPaths = state.getSolution();
        int max = getMaxPathLength(allPaths);
        LocationPair locationPair;
        Location agentLocation;
        Location boxLocation;
        for (int i = 0; i < max; i++) { //i = timestep
            HashMap<Location, Integer> agentLocations = new HashMap<>();
            HashMap<Location, Integer> boxLocations = new HashMap<>();

            for (int j = 0; j < allPaths.length; j++) { //j = agent
                if (i < allPaths[j].length)  //4/25 bug fixed: because each agent has different length of solution, need to check length while getting an element in solution[][]
                {
                    locationPair = allPaths[j][i];
                } else {
                    locationPair = allPaths[j][allPaths[j].length - 1];
                }
                agentLocation = locationPair.getAgentLocation();
                boxLocation = locationPair.getBoxLocation();
                if (agentLocations.get(agentLocation) == null && agentLocations.get(boxLocation) == null && boxLocations.get(agentLocation) == null && boxLocations.get(boxLocation) == null) {
                    agentLocations.put(agentLocation, j);
                    boxLocations.put(boxLocation, j);
                }
                else if (agentLocations.get(agentLocation) != null && boxLocations.get(agentLocation) == null ) { //agentLocation conflicts with other agent: agent agent conflict
                    int agentId1 = j;
                    int agentId2 = agentLocations.get(agentLocation);
                    System.err.println("Vertex agent agent conflict at timestep " + i);
                    Conflict conflict = new AgentAgentConflict(agentId1, agentId2, agentLocation, agentLocation, i);
                    return conflict;
                }
                else if (boxLocations.get(agentLocation) != null && agentLocations.get(agentLocation) == null) { //agentLocation conflicts with other box:agent box conflict
                    int agentId_1 = j;
                    int agentId_2 = boxLocations.get(agentLocation);
                    System.err.println("Vertex agent box conflict at timestep " + i);
                    Conflict conflict = new AgentBoxConflict(agentId_1, agentId_2, agentLocation, agentLocation, i);
                    return conflict;
                }
            }
        }
        return new AgentAgentConflict(0,0,new Location(0,0),new Location(0,0),0);

    }
//    private Conflict getFirstEdgeConflict(HighLevelState state) {
//        LocationPair[][] solution = state.getSolution();
//        for(int i =0;i<solution.length;i++){
//            for(int j = i+1;j<solution.length;j++){
//                int minIndex = Math.min(solution[i].length,solution[j].length) ;
//                for (int k=0; k< minIndex; k++){
//                    LocationPair[] route1 = solution[i];
//                    LocationPair[] route2 = solution[j];
//                    if (route1[k].equals(route2[k+1]) && route1[k+1].equals(route2[k])){
//                        System.err.println("Edge conflict type 1 at timestep " + k);
//                        return new Conflict(i, j, route1[k + 1], route2[k + 1], k);
//                    }
//                    else if (route1[k].equals(route2[k+1])){
//                        System.err.println("Edge conflict type 2 at timestep " + k);
//                        return new Conflict(-1, j, new Location(0,0), route2[k + 1], k+1);
//                    }
//                    else if (route1[k+1].equals(route2[k])){
//                        System.err.println("Edge conflict type 3 at timestep " + k);
//                        return new Conflict(i, -1, route1[k + 1], new Location(0,0), k+1);
//                    }
//                }
//            }
//        }
//        return new Conflict(0,0,new Location(0,0),new Location(0,0),0);
//    }
//    private boolean hasEdgeConflict(Location[] locations_1, Location[] locations_2) {
//
//        int min_route_size = Math.min(locations_1.length,locations_2.length) - 1;
//        for (int i = 0; i<min_route_size;i++){
//            if(locations_1[i].equals(locations_2[i+1]) || locations_1[i+1].equals(locations_2[i])){
//                return true;
//            }
//        }
//        return false;
//    }
    private boolean hasEdgeConflict(HighLevelState state){
        return false; //4/28 for skipping conflict

//        LocationPair[][] solution = state.getSolution();
//
//        for(int i =0;i<solution.length;i++){
//            for(int j = i+1;j<solution.length;j++){
//                if(hasEdgeConflict(solution[i],solution[j])){
//                    return true;
//                }
//            }
//        }
//        return false;
    }

    /**
    * @author Yifei
    * @description translate location change of two consecutive timesteps to an action
    * @date 2021/4/30
    * @param solution
    * @return Action[][]
     */
    private Action[][] translate(LocationPair[][] solution) {
        Action[][] finalSolution = new Action[solution.length][];
        //for each agent, get a list of its path
        for (int i=0; i<solution.length; i++){
            LocationPair[] path = solution[i];
            Action[] singleAgentSolution = new Action[path.length-1];
            for (int j = 0; j < path.length - 1; j++) {
                //if LocationPair doesn't have boxLocation, or if agent is not next to box or box doesn't move, the action is agent action only
                if ( path[0].getBoxLocation() == null || !path[j].getBoxLocation().getNeighbours().contains(path[j].getAgentLocation()) || (path[j].getBoxLocation().equals(path[j+1].getBoxLocation())) ){
                    if (path[j].getAgentLocation().getUpNeighbour().equals(path[j + 1].getAgentLocation()))
                        singleAgentSolution[j] = Action.MoveN;
                    else if (path[j].getAgentLocation().getDownNeighbour().equals(path[j + 1].getAgentLocation()))
                        singleAgentSolution[j] = Action.MoveS;
                    else if (path[j].getAgentLocation().getLeftNeighbour().equals(path[j + 1].getAgentLocation()))
                        singleAgentSolution[j] = Action.MoveW;
                    else if (path[j].getAgentLocation().getRightNeighbour().equals(path[j + 1].getAgentLocation()))
                        singleAgentSolution[j] = Action.MoveE;
                    else if (path[j].getAgentLocation().equals(path[j + 1].getAgentLocation()))
                        singleAgentSolution[j] = Action.NoOp;
                }
                else if (path[j].getBoxLocation().getDownNeighbour().equals(path[j+1].getBoxLocation())) { //1. if box goes down
                    if (path[j].getBoxLocation().getDownNeighbour().equals(path[j].getAgentLocation())) { //if currently agent under box
                        if (path[j].getAgentLocation().getDownNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes down
                            singleAgentSolution[j] = Action.PullSS;
                        if (path[j].getAgentLocation().getLeftNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes left
                            singleAgentSolution[j] = Action.PullWS;
                        if (path[j].getAgentLocation().getRightNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes right
                            singleAgentSolution[j] = Action.PullES;
                    }
                    else if (path[j].getBoxLocation().getUpNeighbour().equals(path[j].getAgentLocation()))  //if currently agent up to box
                        singleAgentSolution[j] = Action.PushSS;
                    else if (path[j].getBoxLocation().getLeftNeighbour().equals(path[j].getAgentLocation()))  //if currently agent left to box
                        singleAgentSolution[j] = Action.PushES;
                    else if (path[j].getBoxLocation().getRightNeighbour().equals(path[j].getAgentLocation()))  //if currently agent right to box
                        singleAgentSolution[j] = Action.PushWS;
                }
                else if (path[j].getBoxLocation().getUpNeighbour().equals(path[j+1].getBoxLocation())) { //2. if box goes up
                    if (path[j].getBoxLocation().getUpNeighbour().equals(path[j].getAgentLocation())) { //if currently agent up to box
                        if (path[j].getAgentLocation().getUpNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes up
                            singleAgentSolution[j] = Action.PullNN;
                        if (path[j].getAgentLocation().getLeftNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes left
                            singleAgentSolution[j] = Action.PullWN;
                        if (path[j].getAgentLocation().getRightNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes right
                            singleAgentSolution[j] = Action.PullEN;
                     }
                    else if (path[j].getBoxLocation().getDownNeighbour().equals(path[j].getAgentLocation()))  //if currently agent down to box
                        singleAgentSolution[j] = Action.PushNN;
                    else if (path[j].getBoxLocation().getLeftNeighbour().equals(path[j].getAgentLocation()))  //if currently agent left to box
                        singleAgentSolution[j] = Action.PushEN;
                    else if (path[j].getBoxLocation().getRightNeighbour().equals(path[j].getAgentLocation()))  //if currently agent right to box
                        singleAgentSolution[j] = Action.PushWN;
                }
                else if (path[j].getBoxLocation().getLeftNeighbour().equals(path[j+1].getBoxLocation())) { //3. if box goes left
                    if (path[j].getBoxLocation().getLeftNeighbour().equals(path[j].getAgentLocation())) { //if currently agent left to box
                        if (path[j].getAgentLocation().getLeftNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes left
                            singleAgentSolution[j] = Action.PullWW;
                        if (path[j].getAgentLocation().getUpNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes up
                            singleAgentSolution[j] = Action.PullNW;
                        if (path[j].getAgentLocation().getDownNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes down
                            singleAgentSolution[j] = Action.PullSW;
                    }
                    else if (path[j].getBoxLocation().getRightNeighbour().equals(path[j].getAgentLocation()))  //if currently agent right to box (go west)
                        singleAgentSolution[j] = Action.PushWW;
                    else if (path[j].getBoxLocation().getUpNeighbour().equals(path[j].getAgentLocation()))  //if currently agent up to box (go south)
                        singleAgentSolution[j] = Action.PushSW;
                    else if (path[j].getBoxLocation().getDownNeighbour().equals(path[j].getAgentLocation()))  //if currently agent down to box (go north)
                        singleAgentSolution[j] = Action.PushNW;


                }
                else if (path[j].getBoxLocation().getRightNeighbour().equals(path[j+1].getBoxLocation())) { //4. if box goes right
                    if (path[j].getBoxLocation().getRightNeighbour().equals(path[j].getAgentLocation())) { //if currently agent right to box
                        if (path[j].getAgentLocation().getRightNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes right
                            singleAgentSolution[j] = Action.PullEE;
                        if (path[j].getAgentLocation().getUpNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes up
                            singleAgentSolution[j] = Action.PullNE;
                        if (path[j].getAgentLocation().getDownNeighbour().equals(path[j + 1].getAgentLocation())) //...and agent goes down
                            singleAgentSolution[j] = Action.PullSE;
                    }
                    else if (path[j].getBoxLocation().getLeftNeighbour().equals(path[j].getAgentLocation()))  //if currently agent left to box (go east)
                        singleAgentSolution[j] = Action.PushEE;
                    else if (path[j].getBoxLocation().getUpNeighbour().equals(path[j].getAgentLocation()))  //if currently agent up to box (go south)
                        singleAgentSolution[j] = Action.PushSE;
                    else if (path[j].getBoxLocation().getDownNeighbour().equals(path[j].getAgentLocation()))  //if currently agent down to box (go north)
                        singleAgentSolution[j] = Action.PushNE;
                }
            }
            finalSolution[i] = singleAgentSolution;
         }

        return finalSolution;
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

    private int getMinPathLength(LocationPair[][] solution){
        int min = INFINITY;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i].length < min)
                min = solution[i].length;
        }
        return min;
    }
    private int getMaxPathLength(LocationPair[][] solution){
        int max = 0;
        for (int i = 0; i < solution.length; i++) {
            if (solution[i].length > max)
                max = solution[i].length;
        }
        return max;
    }
    //print merged plan
    private void printSolution(Action[][] solution){
        System.err.println("[HighLevelState] Found solution: ");
        for (int i=0; i<solution.length;i++)
            System.err.println("Agent "+i+" : " + Arrays.toString(solution[i]));
    }





}
