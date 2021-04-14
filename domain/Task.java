package domain;

public class Task {
    private int id;
    private int boxId;
    private int agentId;
    private Location targetLocation;
    private int priority;
    private boolean isCompleted;

    /**
    * @author Yifei
    * @description Construct a task with boxId means the agent need to push/pull the box to goal.
    * @date 2021/4/14
     */
    public Task(int id, int boxId, int agentId, Location targetLocation, int priority) {
        this.id = id;
        this.boxId = boxId;
        this.agentId = agentId;
        this.targetLocation = targetLocation;
        this.priority = priority;
        this.isCompleted = false;
    }
    /**
     * @author Yifei
     * @description Construct a task without boxId means the agent is moving by itself. boxId = -1 denotes without box.
     * @date 2021/4/14
     */
    public Task(int id, int agentId, Location targetLocation, int priority) {
        this.id = id;
        this.boxId = -1;
        this.agentId = agentId;
        this.targetLocation = targetLocation;
        this.priority = priority;
        this.isCompleted = false;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getBoxId() {
        return boxId;
    }

    public void setBoxId(int boxId) {
        this.boxId = boxId;
    }

    public int getAgentId() {
        return agentId;
    }

    public void setAgentId(int agentId) {
        this.agentId = agentId;
    }

    public Location getTargetLocation() {
        return targetLocation;
    }

    public void setTargetLocation(Location targetLocation) {
        this.targetLocation = targetLocation;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return isCompleted;
    }

    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", boxId=" + boxId +
                ", agentId=" + agentId +
                ", targetLocation=" + targetLocation +
                ", priority=" + priority +
                ", isCompleted=" + isCompleted +
                '}';
    }
}



