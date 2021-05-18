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
    private static final int w = 20; //replan every w steps
    private ArrayList<HighLevelState> tree = new ArrayList<>();
    private TaskHandler taskHandler = TaskHandler.getInstance();

    public HighLevelSolver(InMemoryDataSource data) {
        this.data = data;
    }

    public Action[][] solve() {
        System.err.println("[HighLevelSolver] Solving...");
        Action[][] finalSolution  = new Action[data.getAllAgents().size()][];
        while (data.countRemainingTask()>0) {
            tree = new ArrayList<>();
            HighLevelState initialState = new HighLevelState(new HashSet<>());
            initialState.calculateSolution();
            /**
             * For checking whether all tasks in low level are solvable
             */
//            LocationPair[][] solution = initialState.getSolution();
//            int i = -1;
//            for (LocationPair[] singeAgentSolution: solution){
//                i++;
//                if (singeAgentSolution == null)
//                    System.err.println("[HighLevelSolver] Agent " + i + " doesn't have solution in low level");
//                    return null;
//
//            }
            initialState.updateCost();
            tree.add(initialState);

            while (!tree.isEmpty()) {
                HighLevelState node = findBestNodeWithMinCost(tree);  //Heuristic: get a node with lowest cost; can replace with cardinal conflict (a conflict whose children has more cost)
                System.err.println("[-----------------Constraints of the current pop out node--------------]: " + node.getConstraints().toString());
                System.err.println("[------------------Current CT tree size--------------]: " + tree.size());
                if (!hasEdgeConflict(node) && !hasVertexConflict(node)  && !hasTargetConflict(node)) {
                    LocationPair[][] currentSolution = node.getSolution(); //current solution is the solution of each agent in a round of tasks
                    updateLocation(currentSolution); //given each agent's solution, get the last element, and update agent/box location in data accordingly
                    updateTask(currentSolution); //set the task as completed
                    Action[][] action = translate(currentSolution); //translate
                    action = addPadding(action); //add NoOp to the end of short solutions if each solution is of different length TODO: can switch to RHCR
                    finalSolution = concatenateSolution(finalSolution, action); //append current solution to final solution
                    break;
                } else {
                    dealWithFirstConflict(node);
                }
            }
        }
        //printSolution(finalSolution);
        return finalSolution;

    }

    private boolean hasTargetConflict(HighLevelState state) {
        return false; // 5/18 skip target conflict temporarily
//        LocationPair[][] solution = state.getSolution();
//        for(int i =0;i<solution.length;i++) { //i=agent 1
//            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
//                LocationPair[] route1 = solution[i];
//                LocationPair[] route2 = solution[j];
//                //now we have one path each for agent1 and agent2
//                if (hasTargetConflict(state, i,j, route1,route2)) {
//                    System.err.println("Has target conflict");
//                    return true;
//                }
//            }
//        }
//        return false;
    }

    private boolean hasTargetConflict(HighLevelState state, int i, int j, LocationPair[] route1, LocationPair[] route2) {
        if (route1.length != route2.length){
            if (route1.length < route2.length){
                LocationPair route1Destination = route1[route1.length-1];
                for (int k = route1.length; k < route2.length; k++) {
                    if (route2[k].overlaps(route1Destination))
                        return true;
                }
                return false;
            }
            else {
                LocationPair route2Destination = route2[route2.length-1];
                for (int k = route2.length; k < route1.length; k++) {
                    if (route1[k].overlaps(route2Destination))
                        return true;
                }
                return false;

            }
        }
        else return false;


    }


    public void dealWithFirstConflict(HighLevelState state){
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                dealWithFirstConflict(state, i,j, route1,route2);
            }
        }

    }

    private void dealWithFirstConflict(HighLevelState state, int agentId1, int agentId2, LocationPair[] route1, LocationPair[] route2) {
        if (route1 != null && route2 != null) { //each solution should not be null
            //Target conflict: when agent 1 need to go to agent2's goal location after agent2 reaches goal (and agent2 don't move anymore).
            int minIndex = Math.min(route1.length, route2.length) - 1;
            /**
            * Get box start location
            */
            Location boxStartLocation1 = route1[0].getBoxLocation();
            Location boxStartLocation2 = route2[0].getBoxLocation();

            Location agentCurrentLocation1;
            Location agentCurrentLocation2;
            Location agentNextLocation1;
            Location agentNextLocation2;
            Location boxCurrentLocation1;
            Location boxCurrentLocation2;
            Location boxNextLocation1;
            Location boxNextLocation2;

            Conflict conflict = null;
            Action action1;
            Action action2;
            Action previousAction1 = null;
            Action previousAction2 = null;

            if (boxStartLocation1 == null && boxStartLocation2 == null){ //two agents are both moving without box
                for (int k=0; k< minIndex; k++) { //timestep
                    agentCurrentLocation1 = route1[k].getAgentLocation();
                    agentCurrentLocation2 = route2[k].getAgentLocation();

                    if (agentCurrentLocation1.equals(agentCurrentLocation2)) { //vertex conflict
                        conflict = new AgentAgentConflict(agentId1, agentId2, agentCurrentLocation1, agentCurrentLocation2, k);
                        System.err.println("[HighLevelSolver] Find agent agent vertex conflict");
                        addChildrenOfConflictToTree(state, conflict,2);
                        break;
                    }
                    else{
                        agentNextLocation1 = route1[k+1].getAgentLocation();
                        agentNextLocation2 = route2[k+1].getAgentLocation();
                        if (agentCurrentLocation1.equals(agentNextLocation2) && agentCurrentLocation2.equals(agentNextLocation1)){  //mutual edge conflict
                            conflict = new AgentAgentConflict(agentId1, agentId2, agentNextLocation1, agentNextLocation2, k+1);
                            System.err.println("[HighLevelSolver] Find agent agent mutual edge conflict");
                            addChildrenOfConflictToTree(state, conflict,1);
                            break;
                        }
                        else if (agentCurrentLocation1.equals(agentNextLocation2)){ //single edge conflict
                            conflict = new AgentAgentConflict(-1, agentId2, new Location(-1,-1), agentNextLocation2, k+1);
                            System.err.println("[HighLevelSolver] Find agent2 single edge conflict");
                            addChildrenOfConflictToTree(state, conflict,1);
                            break;
                        }
                        else if (agentCurrentLocation2.equals(agentNextLocation1)){ //single edge conflict
                            conflict = new AgentAgentConflict(agentId1, -1, agentNextLocation1, new Location(-1,-1), k+1);
                            System.err.println("[HighLevelSolver] Find agent1 single edge conflict");
                            addChildrenOfConflictToTree(state, conflict,1);
                            break;
                        }
                    }
                }
            }
            else if (boxStartLocation1 != null && boxStartLocation2 == null) { //agent1 has box, agent2 doesn't have box
                //TODO
            }
            else if (boxStartLocation1 == null && boxStartLocation2 != null) { //agent2 has box, agent1 doesn't have box
                //TODO
            }
            else if (boxStartLocation1 != null && boxStartLocation2 != null) { //two agents are both moving with box
                previousAction1 = translateToAction(route1[0], route1[1]);
                previousAction2 = translateToAction(route2[0], route2[1]);

                for (int k = 0; k < minIndex; k++) { //timestep
                    agentCurrentLocation1 = route1[k].getAgentLocation();
                    agentCurrentLocation2 = route2[k].getAgentLocation();
                    boxCurrentLocation1 = route1[k].getBoxLocation();
                    boxCurrentLocation2 = route2[k].getBoxLocation();
                    agentNextLocation1 = route1[k+1].getAgentLocation();
                    agentNextLocation2 = route2[k+1].getAgentLocation();
                    boxNextLocation1 = route1[k+1].getBoxLocation();
                    boxNextLocation2 = route2[k+1].getBoxLocation();
//                    System.err.println("k= " + k);
                    if (translateToAction(route1[k],route1[k+1]).type != ActionType.NoOp) {
                        action1 = translateToAction(route1[k], route1[k+1]);
                        previousAction1 = translateToAction(route1[k], route1[k+1]);
                    }
                    else
                        action1 = previousAction1;

                    if (translateToAction(route2[k],route2[k+1]).type != ActionType.NoOp) {
                        action2 = translateToAction(route2[k], route2[k+1]);
                        previousAction2 = translateToAction(route2[k], route2[k+1]);
                    }
                    else
                        action2 = previousAction2;


//                    System.err.println("Action1= " + action1.name);
//                    System.err.println("Action2= " + action2.name);

                    if  ((action1.type == ActionType.Pull || action1.type == ActionType.Move) && (action2.type == ActionType.Pull || action2.type == ActionType.Move)){ //agent agent conflict
                        /**
                        * Conflict between heads of the combined object
                         */
//                        System.err.println("Searching for AgentAgentConflict");
                        if (getEdgeConflictType(agentCurrentLocation1,agentCurrentLocation2)){ //vertex conflict
                            conflict = new AgentAgentConflict(agentId1, agentId2, agentCurrentLocation1, agentCurrentLocation2, k);
                            addChildrenOfConflictToTree(state,conflict,3);
                            break;
                        }
                        else if (getEdgeConflictType(agentCurrentLocation1,agentCurrentLocation2,agentNextLocation1,agentNextLocation2) != -1) {
                            int type = getEdgeConflictType(agentCurrentLocation1,agentCurrentLocation2,agentNextLocation1,agentNextLocation2);
                            switch (type) {
                                case 0:
                                    conflict = new AgentAgentConflict(agentId1, agentId2, agentNextLocation1, agentNextLocation2,k+1 );
                                    break;
                                case 1:
                                    conflict = new AgentAgentConflict(agentId1, -1, agentNextLocation1, new Location(-1,-1),k+1 );
                                    break;
                                case 2:
                                    conflict = new AgentAgentConflict(-1, agentId2, new Location(-1,-1), agentNextLocation2,k+1 );
                                    break;
                            }
                            addChildrenOfConflictToTree(state,conflict,2);
                            break;
                        }
                        /**
                         * Conflict between head and tail
                         */
                        else if (agentNextLocation1.equals(boxCurrentLocation2)){
                            conflict = new AgentAgentConflict(agentId1, -1, agentNextLocation1, new Location(-1,-1), k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }
                        else if (agentNextLocation2.equals(boxCurrentLocation1)){
                            conflict = new AgentAgentConflict(-1, agentId2, new Location(-1,-1),agentNextLocation2 , k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }

                    }
                    else if ((action1.type == ActionType.Push) && (action2.type == ActionType.Push)){ //BoxBoxConflict
//                        System.err.println("Searching for BoxBoxConflict");
                        /**
                         * Conflict between heads of the combined object
                         */
                        if (getEdgeConflictType(boxCurrentLocation1,boxCurrentLocation2)){ //vertex conflict
                            conflict = new BoxBoxConflict(agentId1, agentId2, boxCurrentLocation1, boxCurrentLocation2, k);
                            addChildrenOfConflictToTree(state,conflict,3);
                            break;
                        }
                        else if (getEdgeConflictType(boxCurrentLocation1,boxCurrentLocation2,boxNextLocation1,boxNextLocation2) != -1) {
                            int type = getEdgeConflictType(boxCurrentLocation1,boxCurrentLocation2,boxNextLocation1,boxNextLocation2);
                            switch (type) {
                                case 0:
                                    conflict = new BoxBoxConflict(agentId1, agentId2, boxNextLocation1, boxNextLocation2,k+1 );
                                    break;
                                case 1:
                                    conflict = new BoxBoxConflict(agentId1, -1, boxNextLocation1, new Location(-1,-1),k+1 );
                                    break;
                                case 2:
                                    conflict = new BoxBoxConflict(-1, agentId2, new Location(-1,-1), boxNextLocation2,k+1 );
                                    break;
                            }
                            addChildrenOfConflictToTree(state,conflict,2);
                            break;
                        }
                        /**
                         * Conflict between head and tail
                         */
                        else if (boxNextLocation1.equals(agentCurrentLocation2)){
                            conflict = new BoxBoxConflict(agentId1, -1, boxNextLocation1, new Location(-1,-1), k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }
                        else if (boxNextLocation2.equals(agentCurrentLocation1)){
                            conflict = new BoxBoxConflict(-1, agentId2, new Location(-1,-1),boxNextLocation2 , k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }

                    }

                    else if ((action1.type == ActionType.Pull || action1.type == ActionType.Move) && action2.type == ActionType.Push){ //agent1box2 conflict
//                        System.err.println("Searching for agent1box2 conflict");
                        /**
                         * Conflict between heads of the combined object
                         */
                        if (getEdgeConflictType(agentCurrentLocation1,boxCurrentLocation2)){ //vertex conflict
                            conflict = new AgentBoxConflict(agentId1, agentId2, agentCurrentLocation1, boxCurrentLocation2, k);
                            addChildrenOfConflictToTree(state,conflict,3);
                            break;
                        }
                        else if (getEdgeConflictType(agentCurrentLocation1,boxCurrentLocation2,agentNextLocation1,boxNextLocation2) != -1) {
                            int type = getEdgeConflictType(agentCurrentLocation1,boxCurrentLocation2,agentNextLocation1,boxNextLocation2);
                            switch (type) {
                                case 0:
                                    conflict = new AgentBoxConflict(agentId1, agentId2, agentNextLocation1, boxNextLocation2,k+1 );
                                    break;
                                case 1:
                                    conflict = new AgentBoxConflict(agentId1, -1, agentNextLocation1, new Location(-1,-1),k+1 );
                                    break;
                                case 2:
                                    conflict = new AgentBoxConflict(-1, agentId2, new Location(-1,-1), boxNextLocation2,k+1 );
                                    break;
                            }
                            addChildrenOfConflictToTree(state,conflict,2);
                            break;
                        }
                        /**
                         * Conflict between head and tail
                         */
                        //TODO be careful
                        else if (agentNextLocation1.equals(agentCurrentLocation2)){
                            conflict = new AgentBoxConflict(agentId1, -1, agentNextLocation1, new Location(-1,-1), k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }
                        else if (boxNextLocation2.equals(boxCurrentLocation1)){ //note: when box1 is not moving yet, this could cause box2 wait for a long time
                            conflict = new AgentBoxConflict(-1, agentId2, new Location(-1,-1),boxNextLocation2 , k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }
                    }
                    else if ((action2.type == ActionType.Pull || action2.type == ActionType.Move) && action1.type == ActionType.Push){ //agent2box1 conflict
//                        System.err.println("Searching for agent2box1 conflict");
                        /**
                         * Conflict between heads of the combined object
                         */
                        if (getEdgeConflictType(agentCurrentLocation2,boxCurrentLocation1)){ //vertex conflict
                            conflict = new AgentBoxConflict(agentId2, agentId1, agentCurrentLocation2, boxCurrentLocation1, k);
                            addChildrenOfConflictToTree(state,conflict,3);
                            break;
                        }
                        else if (getEdgeConflictType(agentCurrentLocation2,boxCurrentLocation1,agentNextLocation2,boxNextLocation1) != -1) {
                            int type = getEdgeConflictType(agentCurrentLocation2,boxCurrentLocation1,agentNextLocation2,boxNextLocation1);
                            switch (type) {
                                case 0:
                                    conflict = new AgentBoxConflict(agentId2, agentId1, agentNextLocation2, boxNextLocation1,k+1 );
                                    break;
                                case 1:
                                    conflict = new AgentBoxConflict(agentId2, -1, agentNextLocation2, new Location(-1,-1),k+1 );
                                    break;
                                case 2:
                                    conflict = new AgentBoxConflict(-1, agentId1, new Location(-1,-1), boxNextLocation1,k+1 );
                                    break;
                            }
                            addChildrenOfConflictToTree(state,conflict,2);
                            break;
                        }
                        /**
                         * Conflict between head and tail
                         */
                        //TODO be careful
                        else if (boxNextLocation1.equals(boxCurrentLocation2)){
                            conflict = new AgentBoxConflict(-1, agentId1,new Location(-1,-1),  boxNextLocation1,  k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }
                        else if (agentNextLocation2.equals(agentCurrentLocation1)){
                            conflict = new AgentBoxConflict(agentId2, -1, agentNextLocation2 , new Location(-1,-1),k+1 );
                            addChildrenOfConflictToTree(state,conflict,1);
                            break;
                        }
                    }
                }
            }

        }
    }

    private int getEdgeConflictType(Location agentCurrentLocation1, Location agentCurrentLocation2, Location agentNextLocation1, Location agentNextLocation2) {
        if (agentCurrentLocation1.equals(agentNextLocation2) && agentCurrentLocation2.equals(agentNextLocation1))
            return 0;
        else if (agentCurrentLocation1.equals(agentNextLocation2))
            return 2;
        else if (agentCurrentLocation2.equals(agentNextLocation1))
            return 1;
        else return -1;
    }

    private boolean getEdgeConflictType(Location agentCurrentLocation1, Location agentCurrentLocation2) {
        return agentCurrentLocation1.equals(agentCurrentLocation2);
    }

    private Action translateToAction(LocationPair locationPair, LocationPair locationPair1) {
        LocationPair[] locationPairToTranslate = new LocationPair[2];
        locationPairToTranslate[0] = locationPair;
        locationPairToTranslate[1] = locationPair1;
        Action[] action = translateSingleSolution(locationPairToTranslate);
        return action[0];
    }

    //5/16 newly added function to generalize conflicts
    private void addChildrenOfConflictToTree(HighLevelState state, Conflict conflict, int constraintLength) {
        System.err.println("[HighLevel Conflict] " + conflict.toString());
        tree.remove(state);
        Constraint newConstraint;
        Constraint newConstraint2;
        int length = constraintLength;
        CorridorConflict corridorConflict = getCorridorConflict(state,conflict);
        if (corridorConflict != null) {
            for (int i = 0; i < 2; i++) {
                int timeStep = 0;
                HighLevelState childOfCorridorConflict = new HighLevelState(state.getConstraints());
                if (i == 0) {
                    timeStep = corridorConflict.getT2e2() + 1;
                    //agent1 cannot go to exit2 before agent 2 arrives at exit2; +1 is to avoid single edge conflict.
                    newConstraint = new Constraint(corridorConflict.getAgentId1(), false, timeStep, corridorConflict.getExit2());
                } else {
                    timeStep = corridorConflict.getT1e1() + 1;
                    newConstraint = new Constraint(corridorConflict.getAgentId2(), false, timeStep, corridorConflict.getExit1());
                }
                childOfCorridorConflict.addRangeConstraintsBackwards(newConstraint, timeStep);
                childOfCorridorConflict.calculateSolution();
                childOfCorridorConflict.updateCost();
                addToTree(childOfCorridorConflict);
            }
        }
        else {
            if (conflict.getLocation1().equals(conflict.getLocation2())) { //vertex conflict
                for (int i = 0; i < 2; i++) {
                    HighLevelState child = new HighLevelState(state.getConstraints());
                    if (i == 0) {
                        /**
                         * The first constraint can either be for a box or for an agent
                         */
                        if (conflict instanceof BoxBoxConflict) {
                            newConstraint = new Constraint(conflict.getId1(), true, conflict.getTimestep(), conflict.getLocation1());
                        } else {
                            newConstraint = new Constraint(conflict.getId1(), false, conflict.getTimestep(), conflict.getLocation1());
                        }
                    } else {
                        /**
                         * The second constraint can either be for a box or for an agent
                         */
                        if (conflict instanceof AgentAgentConflict) {
                            newConstraint = new Constraint(conflict.getId2(), false, conflict.getTimestep(), conflict.getLocation2());
                        } else {
                            newConstraint = new Constraint(conflict.getId2(), true, conflict.getTimestep(), conflict.getLocation2());
                        }

                    }
                    child.addRangeConstraints(newConstraint, constraintLength); // 5/13 update to range constraints
                    child.calculateSolution();
                    child.updateCost();
                    addToTree(child);
                }
            } else if (conflict.getLocation1().hasNeighbour(conflict.getLocation2())) { //mutual edge conflict
                for (int i = 0; i < 2; i++) {
                    HighLevelState child = new HighLevelState(state.getConstraints());
                    if (i == 0) {
                        /**
                         * The first child (add constraints for agent1)
                         */
                        if (conflict instanceof BoxBoxConflict) {
                            newConstraint = new Constraint(conflict.getId1(), true, conflict.getTimestep() - 1, conflict.getLocation2()); //TODO: note this could be wrong
                            newConstraint2 = new Constraint(conflict.getId1(), true, conflict.getTimestep(), conflict.getLocation1());  //TODO: note this could be wrong

                        } else {
                            newConstraint = new Constraint(conflict.getId1(), false, conflict.getTimestep() - 1, conflict.getLocation2());
                            newConstraint2 = new Constraint(conflict.getId1(), false, conflict.getTimestep(), conflict.getLocation1());

                        }
                    } else {
                        /**
                         * The second child (add constraints for agent2)
                         */
                        if (conflict instanceof AgentAgentConflict) {
                            newConstraint = new Constraint(conflict.getId2(), false, conflict.getTimestep() - 1, conflict.getLocation1());
                            newConstraint2 = new Constraint(conflict.getId2(), false, conflict.getTimestep(), conflict.getLocation2());

                        } else {
                            newConstraint = new Constraint(conflict.getId2(), true, conflict.getTimestep() - 1, conflict.getLocation1());
                            newConstraint2 = new Constraint(conflict.getId2(), true, conflict.getTimestep(), conflict.getLocation2());


                        }
                    }
                    child.addConstraint(newConstraint);
                    child.addRangeConstraints(newConstraint2, constraintLength); // 5/13 update to range constraints
                    child.calculateSolution();
                    child.updateCost();
                    this.addToTree(child);
                }
            } else if (conflict.getId2() == -1) { //single edge conflict for agent1
                HighLevelState child = new HighLevelState(state.getConstraints());
                if (conflict instanceof AgentAgentConflict)
                    newConstraint = new Constraint(conflict.getId1(), false, conflict.getTimestep(), conflict.getLocation1());
                else
                    newConstraint = new Constraint(conflict.getId1(), true, conflict.getTimestep(), conflict.getLocation1());

                child.addRangeConstraints(newConstraint, constraintLength); // 5/13 update to range constraints
                child.calculateSolution();
                child.updateCost();
                this.addToTree(child);
            } else if (conflict.getId1() == -1) { //single edge conflict for agent2
                HighLevelState child = new HighLevelState(state.getConstraints());
                if (conflict instanceof AgentAgentConflict)
                    newConstraint = new Constraint(conflict.getId2(), false, conflict.getTimestep(), conflict.getLocation2());
                else
                    newConstraint = new Constraint(conflict.getId2(), true, conflict.getTimestep(), conflict.getLocation2());

                child.addRangeConstraints(newConstraint, length); // 5/15 update to range constraints with corridor length
                child.calculateSolution();
                child.updateCost();
                this.addToTree(child);
            }
        }

    }



    private CorridorConflict getCorridorConflict(HighLevelState node, Conflict conflict) {
        LocationPair[][] solution = node.getSolution();
        LocationPair[] route1;
        LocationPair[] route2;
        Location location1;
        Location location2;
        Location exit1;
        Location exit2;
        int t1e1=0;
        int t2e2=0;
        int k = conflict.getTimestep();
        int length=0;
        CorridorConflict corridorConflict = null;
        HashSet<Location> corridorSet = new HashSet<>();
        if (conflict.getId1() != -1 && conflict.getId2() != -1) { //the corridor conflict can only happen with vertex conflict or mutual edge conflict
            route1 = solution[conflict.getId1()];
            route2 = solution[conflict.getId2()];
            if (conflict instanceof AgentAgentConflict) {
                location1 = route1[k].getAgentLocation();
                location2 = route2[k].getAgentLocation();

                if (degreeIs2(location1) || degreeIs2(location2)) {
                    while (degreeIs2(location1) && (k < route1.length-1)) {
                        corridorSet.add(location1);
                        k++;
                        location1 = route1[k].getAgentLocation();
                    }

                    exit1 = route1[k].getAgentLocation();
                    corridorSet.add(exit1);
                    t1e1 = k;

                    k = conflict.getTimestep();
                    while (degreeIs2(location2) && (k < route2.length-1)) {
                        corridorSet.add(location2);
                        k++;
                        location2 = route2[k].getAgentLocation();
                    }
                    exit2 = route2[k].getAgentLocation();
                    corridorSet.add(exit2);
                    t2e2 = k;
                    corridorConflict = new CorridorConflict(conflict.getId1(), conflict.getId2(), exit1, exit2, corridorSet.size(), t1e1, t2e2);
                    System.err.println("Current corridorConflict= " + corridorConflict.toString());
                }

            }
        }
        return corridorConflict;
    }

    private boolean degreeIs2(Location location){
        HashMap<Location,Integer> degreeMap = data.getDegreeMap();
        if (degreeMap.get(location) == 2)
            return true;
        else return false;
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
            //note: boxLocation cannot be null
            if (boxLocation2 !=null) {
                if (agentLocation1.equals(boxLocation2))
                    return true;
            }
            if (boxLocation1 !=null) {
                if (agentLocation2.equals(boxLocation1))
                    return true;
            }
            if (boxLocation1 !=null && boxLocation2 !=null){
                if (boxLocation1.equals(boxLocation2))
                    return true;
            }
        }
        return false;
    }

//

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


    private Action[][] addPadding(Action[][] action) {
        int max = getMaxLength(action);
        Action[][] result = new Action[action.length][max];
        for (int i = 0; i < action.length; i++) {
            for (int j = 0; j < max; j++) {
                if (j<action[i].length)
                    result[i][j] = action[i][j];
                else
                    result[i][j] = Action.NoOp;
            }
        }
        return result;
    }

    private int getMaxLength(Action[][] action){
        int max = 0;
        for (int i = 0; i < action.length; i++) {
            if (action[i].length > max)
                max = action[i].length;
        }
        return max;
    }

    private Action[][] concatenateSolution(Action[][] finalSolution, Action[][] currentSolution) {
        Action[][] result = new Action[data.getAllAgents().size()][];
        for (int i = 0; i < finalSolution.length; i++) {
            if (finalSolution[i] != null){
                Action[] previous = finalSolution[i];
                Action[] current = currentSolution[i];

                Action[] combinedAction = new Action[finalSolution[i].length + currentSolution[i].length];
                System.arraycopy(previous,0,combinedAction,0,previous.length);
                System.arraycopy(current,0,combinedAction,previous.length,current.length);
                result[i] = combinedAction;}
            else
                result[i] = currentSolution[i];

        }
        return result;
    }

    private void updateTask(LocationPair[][] currentSolution) {
        for (int i = 0; i < currentSolution.length; i++) { //i=agentId
            taskHandler.completeTask(i);
        }
    }

    private void updateLocation(LocationPair[][] currentSolution) {
        for (int i = 0; i < currentSolution.length; i++) { //i=agentId
            int lastIndex = currentSolution[i].length-1;
            Location agentLocation = currentSolution[i][lastIndex].getAgentLocation(); //get latest agent location
            Location boxLocation = currentSolution[i][lastIndex].getBoxLocation(); //get latest box location
            data.setAgentLocation(i,agentLocation); //update agent location in data
            Task task = taskHandler.pop(i);
            if (task != null) {
                int boxId = task.getBoxId(); //get boxId
                if (boxLocation!=null){
                    data.setBoxLocation(boxId,boxLocation);
                }
            }
        }
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
            finalSolution[i] = translateSingleSolution(solution[i]);
        }
        return finalSolution;
    }

    private Action[] translateSingleSolution(LocationPair[] path1) {
        LocationPair[] path = path1;
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

        return singleAgentSolution;
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



    }
