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

            LocationPair[][] solution1 = node.getSolution();
            System.err.println("[HighLevelSolver] Get solution of current node:");
            for (int i=0; i<solution1.length;i++)
                System.err.println("Agent "+i+" : " + Arrays.toString(solution1[i]));

            if (!hasEdgeConflict(node) && !hasVertexConflict(node) ) {
                LocationPair[][] solution = node.getSolution();

                Action[][] finalSolution = translate(solution);
                //printSolution(finalSolution);
                return finalSolution;
            }
            else if (hasEdgeConflict(node)){
                Conflict edgeConflict = getFirstEdgeConflict(node);
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                //System.err.println("[Edge conflict] " + edgeConflict.toString());
                addChildrenOfEdgeConflictToTree(node, edgeConflict); // new on 5/1

            }
            else if (hasVertexConflict(node)){
                Conflict vertexConflict = getFirstVertexConflict(node);
                // Remove current node from tree because it has conflicts.
                tree.remove(node);
                //System.err.println("[Vertex conflict] " + vertexConflict.toString());
                // expand the node
                addChildrenOfVertexConflictToTree(node, vertexConflict); // new on 4/30
            }

        }
        return null;
    }

    /**
     * @author Yifei
     * @description create 2 children or 1 child depends on whether it is mutual edge conflict or single edge conflict;
     * for each child, use conflict to generate one (or two) constraint, add constraint, calculate solution, update cost, add child to tree.
     * @date 2021/5/1
     * @param node, conflict
     * @return void
     */
    private void addChildrenOfEdgeConflictToTree(HighLevelState node, Conflict conflict) {
        Constraint newConstraint;
        Constraint newConstraint2;
        /**
        * First case: mutual edge conflict.
        * This will add two children to the tree.
        */
        if ((conflict.getId1() != -1) && (conflict.getId2() != -1)) {
            for (int i = 0; i < 2; i++) {
                HighLevelState child = new HighLevelState(node.getConstraints());
                if (i == 0) {
                    /**
                     * The first child (add constraints for agent1)
                     */
                    if (conflict instanceof BoxBoxConflict) {
                        newConstraint = new Constraint(conflict.getId1(), true,conflict.getTimestep() - 1, conflict.getLocation2()); //TODO: note this could be wrong
                        newConstraint2 = new Constraint(conflict.getId1(), true, conflict.getTimestep(), conflict.getLocation1());  //TODO: note this could be wrong
                    }
                    else {
                        newConstraint = new Constraint(conflict.getId1(), false,conflict.getTimestep() - 1, conflict.getLocation2());
                        newConstraint2 = new Constraint(conflict.getId1(), false, conflict.getTimestep(), conflict.getLocation1());
                    }
                } else {
                    /**
                     * The second child (add constraints for agent2)
                     */
                    if (conflict instanceof AgentAgentConflict) {
                        newConstraint = new Constraint(conflict.getId2(), false,conflict.getTimestep() - 1, conflict.getLocation1());
                        newConstraint2 = new Constraint(conflict.getId2(), false, conflict.getTimestep(), conflict.getLocation2());
                    }
                    else {
                        newConstraint = new Constraint(conflict.getId2(), true,conflict.getTimestep() - 1, conflict.getLocation1());
                        newConstraint2 = new Constraint(conflict.getId2(), true, conflict.getTimestep(), conflict.getLocation2());
                    }
                }
                child.addConstraint(newConstraint);
                child.addConstraint(newConstraint2);
                child.calculateSolution();
                child.updateCost();
                this.addToTree(child);
            }
        }
        /**
         * Second case: single edge conflict, agentId1 of the conflict==-1 means there will be no constraints for agent1, the conflict is only for agent2.
         * This only add one child to the tree.
         */
        else if (conflict.getId1() == -1) {
            HighLevelState child = new HighLevelState(node.getConstraints());
            if (conflict instanceof AgentAgentConflict)
                newConstraint = new Constraint(conflict.getId2(), false,conflict.getTimestep(), conflict.getLocation2());
            else
                newConstraint = new Constraint(conflict.getId2(), true,conflict.getTimestep(), conflict.getLocation2());
            child.addConstraint(newConstraint);
            child.calculateSolution();
            child.updateCost();
            this.addToTree(child);
        }
        /**
         * Third case: single edge conflict, agentId2 of the conflict==-1 means there will be no constraints for agent2, the conflict is only for agent1.
         * This only add one child to the tree.
         */

        else if ((conflict.getId2() == -1)){
                    HighLevelState child = new HighLevelState(node.getConstraints());
            if (conflict instanceof AgentAgentConflict)
                newConstraint = new Constraint(conflict.getId1(), false, conflict.getTimestep(), conflict.getLocation1());
            else
                newConstraint = new Constraint(conflict.getId1(), true, conflict.getTimestep(), conflict.getLocation1());

            child.addConstraint(newConstraint);
            child.calculateSolution();
            child.updateCost();
            this.addToTree(child);
            }

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
                if (hasVertexConflict(route1,route2)) {
//                    System.err.println("getFirstVertexConflict "+ conflict.toString());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasVertexConflict(LocationPair[] route1, LocationPair[] route2) {
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
                return true;
            //note: boxLocation can be null
            if (boxLocation1 !=null) {
                if (agentLocation1.equals(boxLocation2))
                    return true;
                else if (agentLocation2.equals(boxLocation1))
                    return true;
                else if (boxLocation1.equals(boxLocation2))
                    return true;
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
                //now we have one path each for agent1 and agent2
                Conflict conflict = getFirstVertexConflict(i,j,route1,route2);
                if (conflict != null) {
                    System.err.println("FirstVertexConflict: "+ conflict.toString());
                    System.err.println("Conflict type: "+ conflict.getClass().getName());
                    return conflict;
                }
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

//
    private boolean hasEdgeConflict(HighLevelState state){
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                if (hasEdgeConflict(route1,route2))
                    return true;
            }
        }
        return false;
    }

    private boolean hasEdgeConflict(LocationPair[] route1, LocationPair[] route2) {
        int minIndex = Math.min(route1.length, route2.length)-1;
        Location agentCurrentLocation1;
        Location agentCurrentLocation2;
        Location agentNextLocation1;
        Location agentNextLocation2;
        Location boxCurrentLocation1;
        Location boxCurrentLocation2;
        Location boxNextLocation1;
        Location boxNextLocation2;
        for (int k=0; k< minIndex; k++){ //timestep
            agentCurrentLocation1 = route1[k].getAgentLocation();
            agentCurrentLocation2 = route2[k].getAgentLocation();
            agentNextLocation1 = route1[k+1].getAgentLocation();
            agentNextLocation2 = route2[k+1].getAgentLocation();
            boxCurrentLocation1 = route1[k].getBoxLocation();
            boxCurrentLocation2 = route2[k].getBoxLocation();
            boxNextLocation1 = route1[k+1].getBoxLocation();
            boxNextLocation2 = route2[k+1].getBoxLocation();
//            System.err.println("Check conflict at timestep "+ k);

            /**
             * First case: AgentAgent Conflict of Edge conflict:
             * 1. two agents switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             */
            if (agentCurrentLocation1.equals(agentNextLocation2) && agentCurrentLocation2.equals(agentNextLocation1))
                return true;
            else if (agentCurrentLocation1.equals(agentNextLocation2))
                return true;
            else if (agentCurrentLocation2.equals(agentNextLocation1))
                return true;
            /**
             * Second case: BoxBox Conflict of Edge conflict:
             * 1. two boxes switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             * note: both boxLocations cannot be null
             */
            if (boxCurrentLocation1 !=null && boxCurrentLocation2 != null) {
                if (boxCurrentLocation1.equals(boxNextLocation2) && boxCurrentLocation2.equals(boxNextLocation1))
                    return true;
                else if (boxCurrentLocation1.equals(boxNextLocation2))
                    return true;
                else if (boxCurrentLocation2.equals(boxNextLocation1))
                    return true;
            }

            /**
             * Third case: AgentBox Conflict of Edge conflict:
             * 1. Agent1 and Agent2’s box switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             * note: Agent2's boxLocation cannot be null
             */
            if (boxCurrentLocation2 != null) {
                if (agentCurrentLocation1.equals(boxNextLocation2) && boxCurrentLocation2.equals(agentNextLocation1))
                    return true;
                else if (agentCurrentLocation1.equals(boxNextLocation2))
                    return true;
                else if (boxCurrentLocation2.equals(agentNextLocation1))
                    return true;
            }
            /**
             * Fourth case: AgentBox Conflict of Edge conflict:
             * 1. Agent2 and Agent1’s box switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             * note: Agent1's boxLocation cannot be null
             */
            if (boxCurrentLocation1 != null) {
                if (agentCurrentLocation2.equals(boxNextLocation1) && boxCurrentLocation1.equals(agentNextLocation2))
                    return true;
                else if (agentCurrentLocation2.equals(boxNextLocation1))
                    return true;
                else if (boxCurrentLocation1.equals(agentNextLocation2))
                    return true;
            }
        }
        return false;
    }

    private Conflict getFirstEdgeConflict(HighLevelState state){
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                Conflict conflict = getFirstEdgeConflict(i,j,route1,route2);
                if (conflict != null) {
                    System.err.println("FirstEdgeConflict: "+ conflict.toString());
                    System.err.println("Conflict type: "+ conflict.getClass().getName());
                    return conflict;
                }
            }
        }
        return null;
    }

    private Conflict getFirstEdgeConflict(int agentId1, int agentId2, LocationPair[] route1, LocationPair[] route2) {
        int minIndex = Math.min(route1.length, route2.length)-1;
        Location agentCurrentLocation1;
        Location agentCurrentLocation2;
        Location agentNextLocation1;
        Location agentNextLocation2;
        Location boxCurrentLocation1;
        Location boxCurrentLocation2;
        Location boxNextLocation1;
        Location boxNextLocation2;
        for (int k=0; k< minIndex; k++){ //timestep
            agentCurrentLocation1 = route1[k].getAgentLocation();
            agentCurrentLocation2 = route2[k].getAgentLocation();
            agentNextLocation1 = route1[k+1].getAgentLocation();
            agentNextLocation2 = route2[k+1].getAgentLocation();
            boxCurrentLocation1 = route1[k].getBoxLocation();
            boxCurrentLocation2 = route2[k].getBoxLocation();
            boxNextLocation1 = route1[k+1].getBoxLocation();
            boxNextLocation2 = route2[k+1].getBoxLocation();
//            System.err.println("Check conflict at timestep "+ k);

            /**
            * First case: AgentAgent Conflict of Edge conflict:
             * 1. two agents switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             */
            if (agentCurrentLocation1.equals(agentNextLocation2) && agentCurrentLocation2.equals(agentNextLocation1))
                return new AgentAgentConflict(agentId1,agentId2,agentNextLocation1,agentNextLocation2,k+1);
            else if (agentCurrentLocation1.equals(agentNextLocation2))
                return new AgentAgentConflict(-1,agentId2,new Location(-1,-1),agentNextLocation2,k+1);
            else if (agentCurrentLocation2.equals(agentNextLocation1))
                return new AgentAgentConflict(agentId1,-1, agentNextLocation1, new Location(-1,-1),k+1);
            /**
             * Second case: BoxBox Conflict of Edge conflict:
             * 1. two boxes switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             * note: both boxLocations cannot be null
             */
            if (boxCurrentLocation1 !=null && boxCurrentLocation2 != null) {
                if (boxCurrentLocation1.equals(boxNextLocation2) && boxCurrentLocation2.equals(boxNextLocation1))
                    return new BoxBoxConflict(agentId1,agentId2,boxNextLocation1,boxNextLocation2,k+1);
                else if (boxCurrentLocation1.equals(boxNextLocation2))
                    return new BoxBoxConflict(-1,agentId2,new Location(-1,-1),boxNextLocation2,k+1);
                else if (boxCurrentLocation2.equals(boxNextLocation1))
                    return new BoxBoxConflict(agentId1,-1, boxNextLocation1, new Location(-1,-1),k+1);
            }

            /**
             * Third case: AgentBox Conflict of Edge conflict:
             * 1. Agent1 and Agent2’s box switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             * note: Agent2's boxLocation cannot be null
             */
            if (boxCurrentLocation2 != null) {
                if (agentCurrentLocation1.equals(boxNextLocation2) && boxCurrentLocation2.equals(agentNextLocation1))
                    return new AgentBoxConflict(agentId1, agentId2, agentNextLocation1, boxNextLocation2, k + 1);
                else if (agentCurrentLocation1.equals(boxNextLocation2))
                    return new AgentBoxConflict(-1, agentId2, new Location(-1, -1), boxNextLocation2, k + 1);
                else if (boxCurrentLocation2.equals(agentNextLocation1))
                    return new AgentBoxConflict(agentId1, -1, agentNextLocation1, new Location(-1, -1), k + 1);
            }
            /**
             * Fourth case: AgentBox Conflict of Edge conflict:
             * 1. Agent2 and Agent1’s box switch location (mutual edge conflict)
             * 2 & 3 one tries to go to the other's current location (single edge conflict)
             * note: Agent1's boxLocation cannot be null
             */
            if (boxCurrentLocation1 != null) {
                if (agentCurrentLocation2.equals(boxNextLocation1) && boxCurrentLocation1.equals(agentNextLocation2))
                    return new AgentBoxConflict(agentId1, agentId2, agentNextLocation2, boxNextLocation1, k + 1);
                else if (agentCurrentLocation2.equals(boxNextLocation1))
                    return new AgentBoxConflict(agentId1, -1, boxNextLocation1, new Location(-1, -1),k + 1);
                else if (boxCurrentLocation1.equals(agentNextLocation2))
                    return new AgentBoxConflict(-1, agentId2, new Location(-1, -1), agentNextLocation2,  k + 1);
            }
        }
        return null;
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

    /**
    * @author Yifei
    * @description add child to tree, valid the child before adding: whether it has a solution and whether it's already in the tree
    * @date 2021/5/1
    * @param child
    * @return void
     */
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


    //print merged plan
    private void printSolution(Action[][] solution){
        System.err.println("[HighLevelState] Found solution: ");
        for (int i=0; i<solution.length;i++)
            System.err.println("Agent "+i+" : " + Arrays.toString(solution[i]));
    }

    /**
    * @author Yifei
    * @description old method, not in use
    * @date 2021/5/1
    * @param state
    * @return conflict
     */
//    private Conflict getFirstConflict(HighLevelState state) {
//        LocationPair[][] allPaths = state.getSolution();
//        int max = getMaxPathLength(allPaths);
//        LocationPair locationPair;
//        Location agentLocation;
//        Location boxLocation;
//        for (int i = 0; i < max; i++) { //i = timestep
//            HashMap<Location, Integer> agentLocations = new HashMap<>();
//            HashMap<Location, Integer> boxLocations = new HashMap<>();
//
//            for (int j = 0; j < allPaths.length; j++) { //j = agent
//                if (i < allPaths[j].length)  //4/25 bug fixed: because each agent has different length of solution, need to check length while getting an element in solution[][]
//                {
//                    locationPair = allPaths[j][i];
//                } else {
//                    locationPair = allPaths[j][allPaths[j].length - 1];
//                }
//                agentLocation = locationPair.getAgentLocation();
//                boxLocation = locationPair.getBoxLocation();
//                if (agentLocations.get(agentLocation) == null && agentLocations.get(boxLocation) == null && boxLocations.get(agentLocation) == null && boxLocations.get(boxLocation) == null) {
//                    agentLocations.put(agentLocation, j);
//                    boxLocations.put(boxLocation, j);
//                }
//                else if (agentLocations.get(agentLocation) != null && boxLocations.get(agentLocation) == null ) { //agentLocation conflicts with other agent: agent agent conflict
//                    int agentId1 = j;
//                    int agentId2 = agentLocations.get(agentLocation);
//                    System.err.println("Vertex agent agent conflict at timestep " + i);
//                    Conflict conflict = new AgentAgentConflict(agentId1, agentId2, agentLocation, agentLocation, i);
//                    return conflict;
//                }
//                else if (boxLocations.get(agentLocation) != null && agentLocations.get(agentLocation) == null) { //agentLocation conflicts with other box:agent box conflict
//                    int agentId_1 = j;
//                    int agentId_2 = boxLocations.get(agentLocation);
//                    System.err.println("Vertex agent box conflict at timestep " + i);
//                    Conflict conflict = new AgentBoxConflict(agentId_1, agentId_2, agentLocation, agentLocation, i);
//                    return conflict;
//                }
//            }
//        }
//        return new AgentAgentConflict(0,0,new Location(0,0),new Location(0,0),0);
//
//    }

        /**
         * @author Yifei
         * @description old method, not in use
         * @date 2021/5/1
         * @param state
         * @return conflict
         */
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
//                        return new Conflict(i, j, route1[k + 1], route2[k + 1], k); //TODO: note this could be wrong. Should be k+1
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
//        private int getMinPathLength(LocationPair[][] solution){
//            int min = INFINITY;
//            for (int i = 0; i < solution.length; i++) {
//                if (solution[i].length < min)
//                    min = solution[i].length;
//            }
//            return min;
//        }
//    private int getMaxPathLength(LocationPair[][] solution){
//        int max = 0;
//        for (int i = 0; i < solution.length; i++) {
//            if (solution[i].length > max)
//                max = solution[i].length;
//        }
//        return max;
//    }



    }
