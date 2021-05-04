package domain;

import java.util.Objects;

public abstract class Conflict {
    private int id1;
    private int id2;
    private Location location1;
    private Location location2;
    private int timestep;

    public Conflict(int id1, int id2, Location location1, Location location2, int timestep) {
        this.id1 = id1;
        this.id2 = id2;
        this.location1 = location1;
        this.location2 = location2;
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

    public Location getLocation1() {
        return location1;
    }

    public void setLocation1(Location location1) {
        this.location1 = location1;
    }

    public Location getLocation2() {
        return location2;
    }

    public void setLocation2(Location location2) {
        this.location2 = location2;
    }

    public int getTimestep() {
        return timestep;
    }

    public void setTimestep(int timestep) {
        this.timestep = timestep;
    }

    @Override
    public String toString() {
        return "Conflict{" +
                "id1=" + id1 +
                ", id2=" + id2 +
                ", location1=" + location1 +
                ", location2=" + location2 +
                ", timestep=" + timestep +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Conflict conflict = (Conflict) o;
        return id1 == conflict.id1 && id2 == conflict.id2 && timestep == conflict.timestep && location1.equals(conflict.location1) && location2.equals(conflict.location2);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id1, id2, location1, location2, timestep);
    }
}
