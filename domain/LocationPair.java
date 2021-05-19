package domain;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Objects;

public class LocationPair {
    private Location agentLocation;
    private Location boxLocation;

    public LocationPair(Location agentLocation, Location boxLocation) {
        this.agentLocation = agentLocation;
        this.boxLocation = boxLocation;
    }

    public Location getAgentLocation() {
        return agentLocation;
    }

    public void setAgentLocation(Location agentLocation) {
        this.agentLocation = agentLocation;
    }

    public Location getBoxLocation() {
        return boxLocation;
    }

    public void setBoxLocation(Location boxLocation) {
        this.boxLocation = boxLocation;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocationPair that = (LocationPair) o;
        return Objects.equals(agentLocation, that.agentLocation) && Objects.equals(boxLocation, that.boxLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(agentLocation, boxLocation);
    }

    @Override
    public String toString() {
        return "{" +
                "" + agentLocation +
                ", " + boxLocation +
                '}';
    }

    public boolean overlaps(LocationPair anotherPair) {
        ArrayList<Location> list = this.toList();
        ArrayList<Location> list2 = anotherPair.toList();
        for (Location temp :list) {
            if (list2.contains(temp))
                return true;
        }
        return false;
    }

    public ArrayList<Location> getOverlapLocation(LocationPair anotherPair){
        ArrayList<Location> location = new ArrayList<>();
        ArrayList<Location> list = this.toList();
        ArrayList<Location> list2 = anotherPair.toList();
        for (Location temp :list) {
            if (list2.contains(temp))
                location.add(temp);
        }
        return location;
    }

    public int getOverlapSize(LocationPair anotherPair){
        ArrayList<Location> location = this.getOverlapLocation(anotherPair);
        return location.size();
    }

    public ArrayList<Location> toList(){
        ArrayList<Location> list = new ArrayList<>();
       list.add(this.agentLocation);
       if (this.boxLocation != null && !list.contains(boxLocation))
           list.add(this.boxLocation);
       return list;
    }
}
