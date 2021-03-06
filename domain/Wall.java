package domain;

//represents a cell on the map
public class Wall {
    private Location location;
    private boolean isWall;

    public Wall(Location location, boolean isWall) {
        this.location = location;
        this.isWall = isWall;
    }

    public Location getLocation() {
        return location;
    }

    public boolean isWall() {
        return isWall;
    }

    @Override
    public String toString() {
        return "Wall{" +
                "location=" + location +
                ", isWall=" + isWall +
                '}';
    }
}
