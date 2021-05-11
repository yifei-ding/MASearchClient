package domain;


public class Goal {
    private final int id;
    private final String name;
    private Location location;


    private int perviousGoalId;
    private boolean isCompleted;

    public int getPerviousGoalId() {
        return perviousGoalId;
    }

    public void setPerviousGoalId(int perviousGoalId) {
        this.perviousGoalId = perviousGoalId;
    }

    public boolean isCompleted() {
        return isCompleted;
    }


    public Goal(int id, String name, Location location,int perviousGoalId,boolean isCompleted) {
        this.id = id;
        this.name = name;
        this.location = location;
        this.perviousGoalId = perviousGoalId;
        this.isCompleted = isCompleted;

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

    public void setCompleted (){

       this.isCompleted = true;
    }

    @Override
    public String toString() {
        return "Goal{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", location=" + location +
                ", perviousGoalId=" + perviousGoalId +
                ", isCompleted=" + isCompleted +
                '}';
    }
}
