package domain;

import java.util.Objects;

public class Conflict2 {
    private int id1;
    private int id2;
    private LocationPair location1;
    private LocationPair location2;
    private ConflictType type;
    private int timestep;

    public Conflict2(int id1, int id2, LocationPair location1, LocationPair location2, ConflictType type, int timestep) {
        this.id1 = id1;
        this.id2 = id2;
        this.location1 = location1;
        this.location2 = location2;
        this.type = type;
        this.timestep = timestep;
    }

    public int getId1() {
        return id1;
    }

    public void setId1(int id1) {
        this.id1 = id1;
    }

    public int getId2() {
        return id2;
    }

    public void setId2(int id2) {
        this.id2 = id2;
    }

    public LocationPair getLocation1() {
        return location1;
    }

    public void setLocation1(LocationPair location1) {
        this.location1 = location1;
    }

    public LocationPair getLocation2() {
        return location2;
    }

    public void setLocation2(LocationPair location2) {
        this.location2 = location2;
    }

    public int getTimestep() {
        return timestep;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    public ConflictType getType() {
        return type;
    }

    public void setType(ConflictType type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conflict2 conflict2 = (Conflict2) o;
        return id1 == conflict2.id1 && id2 == conflict2.id2 && timestep == conflict2.timestep && Objects.equals(location1, conflict2.location1) && Objects.equals(location2, conflict2.location2) && type == conflict2.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2, location1, location2, type, timestep);
    }

    @Override
    public String toString() {
        return "Conflict2{" +
                "id1=" + id1 +
                ", id2=" + id2 +
                ", location1=" + location1 +
                ", location2=" + location2 +
                ", type=" + type +
                ", timestep=" + timestep +
                '}';
    }
}
