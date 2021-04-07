package domain;

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

}
