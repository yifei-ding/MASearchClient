package domain;

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
        HashSet<Location> set = this.toSet();
        HashSet<Location> set2 = anotherPair.toSet();
        for (Location location:set){
            if (set2.contains(location))
                return true;
        }
    return false;
    }

    public HashSet<Location> toSet(){
       HashSet<Location> set = new HashSet<>();
       set.add(this.agentLocation);
       if (this.boxLocation != null)
           set.add(this.boxLocation);
       return set;
    }
}
