package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Objects;

public class State {
    private int timeStep;
    private Location location;
    private Location goalLocation;
    public final State parent;
    private final int g;
    private int hash = 0;
    private int agentId;
    private int boxId;
    private Location agentLocation;
    private HashSet<Constraint> constraints;
    private InMemoryDataSource data = InMemoryDataSource.getInstance();
    private HashMap<Location, Object> map = data.getStaticMap();


    public State(int timeStep, Location location, Location goalLocation, int agentId, int boxId, Location agentLocation, HashSet<Constraint> constraints) {
        this.timeStep = timeStep;
        this.location = location;
        this.goalLocation = goalLocation;
        this.agentId = agentId;
        this.boxId = boxId;
        this.agentLocation = agentLocation;
        this.parent = null;
        this.constraints = constraints;
        this.g = 0;
    }

    //for without box
    public State(State parent, Location location) {
        this.timeStep = parent.timeStep+1;
        this.location = location;
        this.goalLocation = parent.goalLocation;
        this.agentId = parent.agentId;
        this.boxId = parent.boxId;
        this.agentLocation = location;
        this.parent = parent;
        this.constraints = parent.constraints;
        this.g = parent.g +1;

        //Apply action TODO:debug
        //Maybe there's no need to update things to data.
        //data.setAgentLocation(agentId,location);
    }

    //for with box
    public State(State parent, Location agentDestination, Location boxDestination) {
        this.timeStep = parent.timeStep+1;
        this.location = boxDestination;
        this.goalLocation = parent.goalLocation;
        this.agentId = parent.agentId;
        this.boxId = parent.boxId;
        this.agentLocation = agentDestination;
        this.parent = parent;
        this.constraints = parent.constraints;
        this.g = parent.g +1;
    }

    public boolean isGoalState() {
        if (this.location.equals(this.goalLocation)){
//            System.err.println("[State] goal state.g=" + this.g);
            return true;
        }
        else return false;
//        return this.location.equals(this.goalLocation);
    }

    public LocationPair[] extractPlan() {
//        System.err.println("Agent location: " + data.getAgent(agentId).getLocation().toString());
        //IMPORTANT.When finding box, extract plan starting from the parent of goal state. (Not the goal state)
        // Because goal location is the box. We need the neighbouring location of the box.
        //Else use State state = this;
        //        if (this.boxId > -1)
//            state = this.parent;

        if ( boxId== -1) {
            State state = this;
            int size = state.g+1;
            LocationPair[] plan = new LocationPair[size];
            while (state.parent != null) {
                plan[state.g] = new LocationPair(state.location,null);
                state = state.parent;
            }
            plan[0] = new LocationPair(state.location,null);; //this is the initial location
            System.err.println("[State] plan.length=" + plan.length);
            return plan;

        }
        else //TODO: return agent location and box location. Use a pair of location
        {    State state = this;
            int size = state.g+1;
            LocationPair[] plan = new LocationPair[size];
            while (state.parent != null) {
                plan[state.g] = new LocationPair(state.agentLocation,state.location);
                state = state.parent;
            }
            plan[0] = new LocationPair(state.agentLocation,state.location); //this is the initial location
            System.err.println("[State] plan.length=" + plan.length);
            return plan;


        }

    }


    /**
    * @author Yifei
    * @description Try all actions, see which are applicable. Get all next step states (equivalent to a node's children)
    * @date 2021/4/20
    * @param
    * @return ArrayList<State>
     */
    public ArrayList<State> getExpandedStates() {
        ArrayList<State> expandedStates = new ArrayList<>(16);
        //Action[] actions = new Action[]{Action.MoveE, Action.MoveN, Action.MoveS, Action.MoveW};
        Location agentDestination;
        Location boxDestination;

        ArrayList<Location> locations = this.location.getNeighbours(); //equivalent to Move E, W, S, N
        locations.add(location); //equivalent to NoOp
        if (boxId == -1){
            for (Location location : locations) {
                if (this.isApplicable(location) && !isConstraint(timeStep+1,location)) { //fixed issue 4/27: low level also returns initial location, so that timestep is consistent
                    expandedStates.add(new State(this,location));
                }
            }
        }
        else{
            //check all possible actions
            for (Action action:Action.values()){
                if (this.isApplicable(action)){

                    agentDestination = new Location(this.agentLocation.getRow() + action.agentRowDelta, this.agentLocation.getCol() + action.agentColDelta);

                    if (action.type == ActionType.Push || action.type == ActionType.Pull) {
                        System.err.println("Applicable action: "+ action.name);
                        boxDestination = new Location(this.location.getRow() + action.boxRowDelta, this.location.getCol() + action.boxColDelta);
                    }

                    else boxDestination = this.location;
                    if (!isConstraint(timeStep+1,agentDestination,boxDestination)) {
                            System.err.println("Agent next location: " + agentDestination.toString());
                            System.err.println("Box next location: " + boxDestination.toString());
                            if (!agentDestination.equals(boxDestination))
                                expandedStates.add(new State(this, agentDestination, boxDestination));
                    }
                }
            }
        }
        return expandedStates;
    }

    private boolean isConstraint(int timeStep, Location agentDestination, Location boxDestination) {
        return false;
    }

    private boolean isApplicable(Action action) {
        int agentRow;
        int agentCol;
        int boxRow;
        int boxCol;
        int destinationRow;
        int destinationCol;
        Location agentDestination;
        Location boxDestination;
        agentRow = this.agentLocation.getRow();
        agentCol = this.agentLocation.getCol();
        boxRow = this.location.getRow();
        boxCol = this.location.getCol();

        switch (action.type) {
            case NoOp:
                return true;
            case Move:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                agentDestination = new Location(destinationRow,destinationCol);
                return this.cellIsFree(agentDestination);

            case Push:
                destinationRow = agentRow + action.agentRowDelta;
                destinationCol = agentCol + action.agentColDelta;
                agentDestination = new Location(destinationRow,destinationCol);

                //box location should equal to agent destination
                if (this.location.equals(agentDestination)){
//                    System.err.println("box location equal to agent destination");
                    destinationRow = boxRow + action.boxRowDelta;
                    destinationCol = boxCol + action.boxColDelta;
                    boxDestination = new Location(destinationRow,destinationCol);
                    //box destination is free
                    if (this.cellIsFree(boxDestination)){
                        return true;
                    }
                    else return false;
                }
                else return false;
            case Pull:
                destinationRow = boxRow + action.boxRowDelta;
                destinationCol = boxCol + action.boxColDelta;
                boxDestination = new Location(destinationRow,destinationCol);
                //box destination should equal to agent location
                if (this.agentLocation.equals(boxDestination)){
//                    System.err.println("box destination equal to agent location");
                    destinationRow = agentRow + action.agentRowDelta;
                    destinationCol = agentCol + action.agentColDelta;
                    agentDestination = new Location(destinationRow,destinationCol);
                    //agent destination is free
                    if (this.cellIsFree(agentDestination)){
                        return true;
                    }
                    else return false;
                }
                else return false;
        }
        return false;
    }

    private boolean cellIsFree(Location location) {
        Object obj = data.getStaticMap().get(location);
        if (obj instanceof Wall) {
            if (((Wall)obj).isWall())
                return false;
        }

        //might also check whether there's a box at location?
        obj = data.getDynamicMap().get(location);
        if (obj instanceof Box) {
            return false;
        }
        return true;
    }

    private boolean isConstraint(int timeStep, Location location) {
//        System.err.println("isConstraint");
        for (Constraint constraint : this.constraints){
            if (constraint.getAgentId() == this.agentId){
                if (constraint.getTimeStep() == timeStep && constraint.getLocation().equals(location)){
//                            System.err.println("isConstraint");
                    return true;
                }
            }
        }

        return false;
    }

    private boolean isApplicable(Location location) {
        Object obj = map.get(location);
        if (obj instanceof Wall) {
            if (((Wall)obj).isWall())
                return false;
        }

        return true;
    }

    public int g()
    {
        return this.g;
    }

    public Location getLocation() {
        return location;
    }

    public Location getGoalLocation() {
        return goalLocation;
    }

    public int getBoxId() {
        return boxId;
    }

    public Location getAgentLocation() {
        return agentLocation;
    }

    @Override
    public String toString() {
        return "State{" +
                 "timeStep=" + timeStep +
                ", location=" + location +
                //", goalLocation=" + goalLocation +
                //", agentId=" + agentId +
                //", boxId=" + boxId +
                ", agentLocation=" + agentLocation +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return  location.equals(state.location) && agentLocation.equals(state.agentLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStep, location, g, agentLocation);
    }
}
