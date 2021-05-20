package searchClient;

import data.InMemoryDataSource;
import domain.*;
import java.lang.Math;
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
            initialState.updateCost();
            tree.add(initialState);
            System.err.println("[-------**********----------Current remaining tasks-----******---------]: " + data.countRemainingTask());

            while (!tree.isEmpty()) {
                HighLevelState node = findBestNodeWithMinCost(tree);  //Heuristic: get a node with lowest cost; can replace with cardinal conflict (a conflict whose children has more cost)
                System.err.println("[-------Constraints of the current pop out node-----]: " + node.getConstraints().size());
                System.err.println("[------------------Current CT tree size--------------]: " + tree.size());
//                node.printSolution();
                if (!hasVertexConflict(node) && !hasEdgeConflict(node) && !hasTargetConflict(node)) {
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

    public void dealWithFirstConflict2(HighLevelState state, int agentId1, int agentId2, LocationPair[] route1, LocationPair[] route2) {
//        state.printSolution();
        int minIndex = Math.min(route1.length, route2.length) - 1;
        Conflict2 conflict2 = null;
        LocationPair location1 = null;
        LocationPair location2 = null;
        Location boxStartLocation1 = route1[0].getBoxLocation();
        Location boxStartLocation2 = route2[0].getBoxLocation();
        String scenario = null;
        Location agent2Head;
        Location agent1Head;
        Location collision;

        if (boxStartLocation1 == null && boxStartLocation2 == null){
            scenario = "NoBox";
        }
        else if (boxStartLocation1 != null && boxStartLocation2 != null){
            scenario = "BothWithBox";
        }
        else if (boxStartLocation1 != null){
            scenario = "Agent1HasBox";
        }
        else
            scenario = "Agent2HasBox";

        System.err.println("Current scenario: " + scenario);

        ArrayList<Location> overlap;
        ArrayList<Location> overlap1; //agent1 has edge conflict with agent2
        ArrayList<Location> overlap2; //agent2 has edge conflict with agent1
        int flag;
        int range1=0;
        int range2=0;
        switch (scenario) {
            case "NoBox":
                range1 = 2;
                range2 = 2;
                break;
            case "BothWithBox":
                range1 = 3;
                range2 = 3;
                break;
            case "Agent1HasBox":
                range1 = 2;
                range2 = 3;
                break;
            case "Agent2HasBox":
                range1 = 3;
                range2 = 2;
                break;
        }
        for (int k = 0; k < minIndex; k++) { //timestep
            if (!route1[k].overlaps(route2[k])) {
                overlap = route1[k + 1].getOverlapLocation(route2[k + 1]);
                overlap1 = route1[k + 1].getOverlapLocation(route2[k]);
                overlap2 = route1[k].getOverlapLocation(route2[k + 1]);
                if (overlap.size() == 1 && overlap1.size() == 0 && overlap2.size() == 0) { //vertex conflict
                    location1 = route1[k + 1];
                    location2 = route2[k + 1];
                    collision = overlap.get(0);
                    if (location1.getAgentLocation().equals(collision))
                        location1 = new LocationPair(collision,null);
                    else
                        location1 = new LocationPair(null,collision);

                    if (location2.getAgentLocation().equals(collision))
                        location2 = new LocationPair(collision,null);
                    else
                        location2 = new LocationPair(null,collision);

                    conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.Vertex, range1, range2);
                    addConflict2(state, conflict2);
                    break;

                }
                else if (overlap1.size() == 1 && overlap2.size() == 1) { //mutual edge conflict
                    location1 = route1[k + 1];
                    location2 = route2[k + 1];

                    switch (scenario) {
                        case "NoBox":
                            conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.MutualEdge);
                            addConflict2(state, conflict2);
                            break;
                        case "BothWithBox", "Agent1HasBox", "Agent2HasBox":
                            agent2Head = overlap1.get(0);
                            agent1Head = overlap2.get(0);

                            if (agent2Head.equals(route2[k].getAgentLocation()))
                                location2 = new LocationPair(agent1Head,null);
                            else
                                location2 = new LocationPair(null, agent1Head);


                            if (agent1Head.equals(route1[k].getAgentLocation()))
                                location1 = new LocationPair(agent2Head,null);
                            else
                                location1 = new LocationPair(null, agent2Head);

                            conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.MutualEdge);
                            addConflict2(state, conflict2);
                            break;
//                        case "Agent1HasBox":
//                            agent1Head = overlap2.get(0);
//                            if (agent1Head.equals(route1[k].getAgentLocation()))
//                                location1 = new LocationPair(agent1Head,null);
//                            else
//                                location1 = new LocationPair(null, agent1Head);
//
//                            conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.MutualEdge);
//                            addConflict2(state, conflict2);
//                            break;
//                        case "Agent2HasBox":
//                            agent2Head = overlap1.get(0);
//                            if (agent2Head.equals(route2[k].getAgentLocation()))
//                                location2 = new LocationPair(agent2Head,null);
//                            else
//                                location2 = new LocationPair(null, agent2Head);
//
//                            conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.MutualEdge);
//                            addConflict2(state, conflict2);
//                            break;
                    }

                }
                else if (overlap.size() ==1 && (overlap1.size()==1 || overlap2.size() ==1)) {

                    //Head-tail collision
                    if (overlap1.size() == 1) { //agent1 bump into agent2
                        collision = overlap1.get(0);
                        location1 = route1[k + 1];
                        location2 = route2[k];
                        if (collision.equals(location2.getAgentLocation()))
                            location2 = new LocationPair(collision,null);
                        else
                            location2 = new LocationPair(null, collision);

                        if (collision.equals(location1.getAgentLocation()))
                            location1 = new LocationPair(collision,null);
                        else
                            location1 = new LocationPair(null, collision);

                        conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k , ConflictType.Vertex,range1, range2);
                        addConflict2(state, conflict2);
                        break;
                    }
                    else if (overlap2.size() == 1) { //agent2 bump into agent1
                        collision = overlap2.get(0);
                        location1 = route1[k];
                        location2 = route2[k + 1];
                        if (collision.equals(location2.getAgentLocation()))
                            location2 = new LocationPair(collision,null);
                        else
                            location2 = new LocationPair(null, collision);

                        if (collision.equals(location1.getAgentLocation()))
                            location1 = new LocationPair(collision,null);
                        else
                            location1 = new LocationPair(null, collision);
                        conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k, k + 1, ConflictType.Vertex,range1, range2);
                        addConflict2(state, conflict2);
                        break;
                    }

                }
                else if (overlap.size()==0 && (overlap1.size()==1 || overlap2.size() ==1)) {
                    //Rear-end collision
                    if (overlap1.size() == 1) {
                        collision = overlap1.get(0);
                        location1 = route1[k + 1];
                        if (collision.equals(location1.getAgentLocation()))
                            location1 = new LocationPair(collision,null);
                        else
                            location1 = new LocationPair(null, collision);

                        conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.SingleEdge1);
                        addConflict2(state, conflict2);
                        break;
                    } else if (overlap2.size() == 1) {
                        location2 = route2[k + 1];
                        collision = overlap2.get(0);

                        if (collision.equals(location2.getAgentLocation()))
                            location2 = new LocationPair(collision,null);
                        else
                            location2 = new LocationPair(null, collision);

                        conflict2 = new Conflict2(agentId1, agentId2, location1, location2, k + 1, k + 1, ConflictType.SingleEdge2);
                        addConflict2(state, conflict2);
                        break;
                    }
                    break;
                }

            }
            }
        }


    private void addConflict2(HighLevelState state, Conflict2 conflict2) {
        tree.remove(state);
        HighLevelState child = null;
        ArrayList<Constraint> constraints = new ArrayList<>();
        System.err.println("[HighLevelSolver]Find conflict " + conflict2.toString());

        switch (conflict2.getType()) {
            case SingleEdge1:
                child = new HighLevelState(state.getConstraints());
                constraints = conflictToConstraints(conflict2,1);
                child.addConstraints(constraints);
                child.calculateSolution();
                child.updateCost();
                addToTree(child);

                break;
            case SingleEdge2:
                child = new HighLevelState(state.getConstraints());
                constraints = conflictToConstraints(conflict2,2);
                child.addConstraints(constraints);
                child.calculateSolution();
                child.updateCost();
                addToTree(child);

                break;
            case Vertex,MutualEdge:
                CorridorConflict corridorConflict = getCorridorConflict(state,conflict2);
                Constraint newConstraint;
                if (corridorConflict != null) {
                    for (int i = 0; i < 2; i++) {
                        int timeStep = 0;
                        HighLevelState childOfCorridorConflict = new HighLevelState(state.getConstraints());
                        if (i == 0) {
                            timeStep = corridorConflict.getT2e2() + 2;
                            //agent1 cannot go to exit2 before agent 2 arrives at exit2; +2 is to avoid bumping into tail and rear-end collision.
                            if (conflict2.getLocation1().getAgentLocation() != null)
                                newConstraint = new Constraint(corridorConflict.getAgentId1(), false, timeStep, corridorConflict.getExit2());
                            else
                                newConstraint = new Constraint(corridorConflict.getAgentId1(), true, timeStep, corridorConflict.getExit2());

                        } else {
                            timeStep = corridorConflict.getT1e1() + 2;
                            if (conflict2.getLocation2().getAgentLocation() != null)
                                newConstraint = new Constraint(corridorConflict.getAgentId2(), false, timeStep, corridorConflict.getExit1());
                            else
                                newConstraint = new Constraint(corridorConflict.getAgentId2(), true, timeStep, corridorConflict.getExit1());

                        }
                        childOfCorridorConflict.addRangeConstraintsBackwards(newConstraint, timeStep);
                        childOfCorridorConflict.calculateSolution();
                        childOfCorridorConflict.updateCost();
                        addToTree(childOfCorridorConflict);
                    }
                } else {
                    for (int i = 1; i<2; i++) {
                        child = new HighLevelState(state.getConstraints());
                        constraints = conflictToConstraints(conflict2, i);
                        child.addConstraints(constraints);
                        child.calculateSolution();
                        child.updateCost();
                        addToTree(child);
                    }
                }
                break;

        }
    }

    private ArrayList<Constraint> conflictToConstraints(Conflict2 conflict2, int agentSequence) {
//        System.err.println("Convert conflict to constraints for agent " + agentSequence);
        ArrayList<Constraint> constraints = new ArrayList<>();
        LocationPair locationPair;
        LocationPair locationPair2;
        Location temp;
        int agentId;
        int timeStep;
        int range;
        if (agentSequence==1){
            agentId = conflict2.getAgentId1();
            locationPair = conflict2.getLocation1();
            locationPair2 = conflict2.getLocation2();
            timeStep = conflict2.getTimestep1();
            range = conflict2.getRange1();
        }
        else {
            agentId = conflict2.getAgentId2();
            locationPair = conflict2.getLocation2();
            locationPair2 = conflict2.getLocation1();
            timeStep = conflict2.getTimestep2();
            range = conflict2.getRange2();

        }
        if (conflict2.getType() != ConflictType.MutualEdge) {
            if (locationPair.getAgentLocation() != null) {
                for (int i = 0; i < range; i++) {
//                    System.err.println("Add constraint1 " + i);
                    constraints.add(new Constraint(agentId, false, timeStep + i, locationPair.getAgentLocation()));
                }
            }
            if (locationPair.getBoxLocation() != null) {
                for (int i = 0; i < range; i++) {
//                    System.err.println("Add constraint2 " + i);
                    constraints.add(new Constraint(agentId, true, timeStep + i, locationPair.getBoxLocation()));
                }
            }
        }
        else {
            if (locationPair2.getAgentLocation() != null)
                temp = locationPair2.getAgentLocation();
            else
                temp = locationPair2.getBoxLocation();
            if (locationPair.getAgentLocation() != null) {
//                System.err.println("Add constraint 3");
                constraints.add(new Constraint(agentId, false, timeStep-1, temp));
                constraints.add(new Constraint(agentId, false, timeStep, locationPair.getAgentLocation()));
            }
            else if (locationPair.getBoxLocation() != null) {
//                System.err.println("Add constraint 4");
                constraints.add(new Constraint(agentId, true, timeStep-1, temp));
                constraints.add(new Constraint(agentId, true, timeStep, locationPair.getBoxLocation()));
            }
        }
        return constraints;

    }

    public void dealWithFirstConflict(HighLevelState state){
        LocationPair[][] solution = state.getSolution();
        outerLoop: for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                if (route1 != null && route2 != null && route1.length>1 && route2.length>1) {
                    if (hasVertexConflict(route1,route2) || hasEdgeConflict(route1,route2) ) {
                        System.err.println("Agent " + i +" and Agent " + j +" has conflict");
                        dealWithFirstConflict2(state, i, j, route1, route2);
                        break outerLoop;
                    }
                }
            }
        }

    }



    private CorridorConflict getCorridorConflict(HighLevelState node, Conflict2 conflict) {
        LocationPair[][] solution = node.getSolution();
        LocationPair[] route1;
        LocationPair[] route2;
        Location location1;
        Location location2;
        Location exit1;
        Location exit2;
        int t1e1=0;
        int t2e2=0;
        int k1;
        int k2;
        int length=0;
        CorridorConflict corridorConflict = null;
        HashSet<Location> corridorSet = new HashSet<>();
        route1 = solution[conflict.getAgentId1()];
        route2 = solution[conflict.getAgentId2()];
        k1 = conflict.getTimestep1();
        k2 = conflict.getTimestep2();
        int flag1;
        int flag2;
        if (conflict.getType() == ConflictType.Vertex || conflict.getType() == ConflictType.MutualEdge) {
            if (conflict.getLocation1().getAgentLocation() != null) {
            flag1 = 0; //agent
            location1 = conflict.getLocation1().getAgentLocation();
        }
        else {
            flag1 = 1; //box
            location1 = conflict.getLocation1().getBoxLocation();

        }
        if (conflict.getLocation2().getAgentLocation() != null){
            flag2 = 0;
            location2 = conflict.getLocation2().getAgentLocation();

        }
        else {
            flag2 = 1;
            location2 = conflict.getLocation2().getBoxLocation();
        }
            if (degreeIs2(location1) || degreeIs2(location2)) {
                while (degreeIs2(location1) && (k1 < route1.length - 1)) {
                    corridorSet.add(location1);
                    k1++;
                    if (flag1 == 0)
                        location1 = route1[k1].getAgentLocation();
                    else
                        location1 = route1[k1].getBoxLocation();

                }
                exit1 = location1;
                corridorSet.add(exit1);
                t1e1 = k1;


                while (degreeIs2(location2) && (k2 < route2.length - 1)) {
                    corridorSet.add(location2);
                    k2++;
                    if (flag2 == 0)
                        location2 = route2[k2].getAgentLocation();
                    else
                        location2 = route2[k2].getBoxLocation();
                }
                exit2 = location2;
                corridorSet.add(exit2);
                t2e2 = k2;
                corridorConflict = new CorridorConflict(conflict.getAgentId1(), conflict.getAgentId2(), exit1, exit2, corridorSet.size(), t1e1, t2e2);
                System.err.println("Current corridorConflict= " + corridorConflict.toString());
            }
        }
        return corridorConflict;
    }

    private boolean degreeIs2(Location location){
        HashMap<Location,Integer> degreeMap = data.getDegreeMap();
        if (location!= null) {
            if (degreeMap.get(location) == 2)
                return true;
            else return false;
        }
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

    private boolean hasVertexConflict(HighLevelState state) {
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                if ((route1 != null) && (route2 != null) && hasVertexConflict(route1,route2)){
//                    System.err.println("getFirstVertexConflict "+ conflict.toString());
                    return true;
                }
            }
        }
        return false;
    }

    private boolean hasVertexConflict(LocationPair[] route1, LocationPair[] route2) {
        int minIndex = Math.min(route1.length, route2.length);
        for (int k=0; k< minIndex; k++){ //timestep
            if (route1[k].overlaps(route2[k])) {
//                System.err.println("Vertex Route overlap at " + (k));
                return true;
            }
        }
        return false;
    }


//
    private boolean hasEdgeConflict(HighLevelState state){
        LocationPair[][] solution = state.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                if ((route1 != null) && (route2 != null) && hasEdgeConflict(route1,route2))
                    return true;
            }
        }
        return false;
    }


    private boolean hasEdgeConflict(LocationPair[] route1, LocationPair[] route2) {
        int minIndex = Math.min(route1.length, route2.length)-1;
        for (int k=0; k< minIndex; k++) { //timestep
            if (route1[k].overlaps(route2[k+1])) {
//                System.err.println("Route overlap at " + k + " and "+ (k+1));
                return true;
            }
            else if (route1[k+1].overlaps(route2[k])) {
//                System.err.println("Route overlap2 at " + k + " and "+ (k+1));
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
        if (child.getSolution() != null) {
            if (child.getCost() > 0 && !tree.contains(child)) {
                System.err.println("[Add child]");
                tree.add(child);
            }
        }
        else
            System.err.println("[Pruned]");

    }


    //print merged plan
    private void printSolution(Action[][] solution){
        System.err.println("[HighLevelState] Found solution: ");
        for (int i=0; i<solution.length;i++)
            System.err.println("Agent "+i+" : " + Arrays.toString(solution[i]));
    }



    }
