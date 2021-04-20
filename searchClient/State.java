package searchClient;

import data.InMemoryDataSource;
import domain.Action;
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
    private InMemoryDataSource data = InMemoryDataSource.getInstance();
    private HashMap<Location, Object> map = data.getStaticMap();


//    public State(int timeStep, Location location) {
//        this.timeStep = timeStep;
//        this.location = location;
//        this.parent = null;
//        this.g = 0;
//    }

    public State(int timeStep, Location location, Location goalLocation, int agentId) {
        this.timeStep = timeStep;
        this.location = location;
        this.goalLocation = goalLocation;
        this.agentId = agentId;
        this.parent = null;
        this.g = 0;
    }

    public boolean isGoalState() {
        return this.location.equals(this.goalLocation);
    }

    public Action[] extractPlan() {
        return null;
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
        for (Location location : locations) {
            if (this.isApplicable(location)) {
                expandedStates.add(new State(this.timeStep+1, location,this.goalLocation,this.agentId));
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

    @Override
    public String toString() {
        return "State{" +
                "timeStep=" + timeStep +
                ", location=" + location +
                ", goalLocation=" + goalLocation +
                ", parent=" + parent +
                ", agentId=" + agentId +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        State state = (State) o;
        return timeStep == state.timeStep && g == state.g && hash == state.hash && agentId == state.agentId && location.equals(state.location) && goalLocation.equals(state.goalLocation) && Objects.equals(parent, state.parent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(timeStep, location, goalLocation, parent, g, hash, agentId);
    }
}
