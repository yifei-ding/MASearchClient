package domain;

public class Agent {
    private int id;
    private Color color;
    private Location location;

    public Agent(int id, Color color, Location location) {
        this.id = id;
        this.color = color;
        this.location = location;
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
