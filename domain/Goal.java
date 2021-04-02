package domain;


public class Goal {
    private int id;
    private String name;
    private Location location;

    public Goal(int id, String name, Location location) {
        this.id = id;
        this.name = name;
        this.location = location;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
    }

    public Location getLocation() {
        return location;
    }
    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Box{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location.toString() +
                '}';
    }
}
