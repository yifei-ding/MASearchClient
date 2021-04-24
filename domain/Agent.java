package domain;

public class Agent {
    private final int id;
    private final Color color;
    private Location location;

    public Agent(int id, Color color, Location location) {

        this.id = id;
        this.color = color;
        this.location = location;
    }

    public Agent(int id, Color color) {
        this.id = id;
        this.color = color;
        this.location = new Location(0,0);
    }

    public int getId() {
        return id;
    }
    public Color getColor() {
        return color;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    @Override
    public String toString() {
        return "Agent{" +
                "id=" + id +
                ", color=" + color +
                ", location=" + location.toString() +
                '}';
    }
}
