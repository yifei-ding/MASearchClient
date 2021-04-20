package searchClient;

import data.InMemoryDataSource;
import domain.Action;
import domain.Constraint;
import domain.Location;
import domain.Wall;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

public class State {
    private int timeStep;
    private Location location;
    private Location goalLocation;
    public final State parent;
    private final int g;
    private int hash = 0;
    private int agentId;
    private ArrayList<Constraint> constraints;
    private InMemoryDataSource data = InMemoryDataSource.getInstance();
    private HashMap<Location, Object> map = data.getStaticMap();


    public State(int timeStep, Location location, Location goalLocation, int agentId) {
        this.timeStep = timeStep;
        this.location = location;
        this.goalLocation = goalLocation;
        this.agentId = agentId;
        this.parent = null;
        this.g = 0;
    }


    public State(State parent, Location location) {
        this.timeStep = parent.timeStep+1;
        this.location = location;
        this.goalLocation = parent.goalLocation;
        this.agentId = parent.agentId;
        this.parent = parent;
        this.g = parent.g +1;

        //TODO:debug
        //Apply action
        data.setAgentLocation(agentId,location);
    }

    public boolean isGoalState() {
        return this.location.equals(this.goalLocation);
    }

    public Action[] extractPlan() {
//        System.err.println("Agent location: " + data.getAgent(agentId).getLocation().toString());
        //IMPORTANT.When finding box, extract plan starting from the parent of goal state. (Not the goal state)
        // Because goal location is the box. We need the neighbouring location of the box.
        //Else use State state = this;
        State state = this.parent;
        int size = state.g;
        Action[] plan = new Action[size];
        while (state.parent != null){
            Action action = translateLocationChange2Action(state.parent.location, state.location);
            plan[state.g-1] = action;
            state = state.parent;
        }

        return plan;
    }

    private Action translateLocationChange2Action(Location parentLocation, Location location) {
        if (parentLocation.getUpNeighbour().equals(location))
            return Action.MoveN;
        else if (parentLocation.getDownNeighbour().equals(location))
            return Action.MoveS;
        else if (parentLocation.getLeftNeighbour().equals(location))
            return Action.MoveW;
        else if (parentLocation.getRightNeighbour().equals(location))
            return Action.MoveE;
        else return null;
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
        ArrayList<Location> locations = this.location.getNeighbours();
        //TODO: need to get current timestep and check constraints
        for (Location location : locations) {
            if (this.isApplicable(location)) {
                expandedStates.add(new State(this,location));
            }
        }
        return expandedStates;
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

    @Override
    public String toString() {
        return "State{" +
                "timeStep=" + timeStep +
                ", location=" + location +
                ", goalLocation=" + goalLocation +
                //", parent=" + parent +
                ", agentId=" + agentId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return timeStep == state.timeStep && agentId == state.agentId && location.equals(state.location) && goalLocation.equals(state.goalLocation) && Objects.equals(parent, state.parent) && Objects.equals(constraints, state.constraints);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStep, location, goalLocation, parent, agentId, constraints);
    }
}
