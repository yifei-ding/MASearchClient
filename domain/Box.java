package domain;

public class Box {
    private final int id;
    private final String name;
    private final Color color;
    private Location location;

    public Box(int id, String name, Color color, Location location) {
        this.id = id;
        this.name = name;
        this.color = color;
        this.location = location;
    }

    public int getId() {
        return id;
    }
    public String getName() {
        return name;
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
        return "Box{" +
               // "id=" + id +
                ", name='" + name + '\'' +
                ", color=" + color +
                ", location=" + location.toString() +
                '}' + '\n';
    }
}
