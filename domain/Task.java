package domain;


import data.InMemoryDataSource;

import java.util.Objects;

public class Task {
    private int id;
    private int boxId;
    private int agentId;
    private Location targetLocation;
    private int priority;
    private boolean isCompleted;
    private int previousTaskId;
    private static int counter = 0;
    private int goalId = -1;
    private int reverseTaskId = -1; //if the task is completed, the reverse task is no longer completed

    private static InMemoryDataSource data = InMemoryDataSource.getInstance();

    public int getPreviousTaskId() {
        return previousTaskId;
    }

    public void setPreviousTaskId(int previousTaskId) {
        this.previousTaskId = previousTaskId;
    }

    private Location startLocation; // used for checking the box/agent is still at the orginal location or not.
    /**
    * @author Yifei
    * @description Construct a task with boxId means the agent need to push/pull the box to goal.
    * @date 2021/4/14
     */
    public Task(int id, int agentId, int boxId, Location targetLocation, int priority) {
        this.id = ++counter;
        this.boxId = boxId;
        this.agentId = agentId;
        this.targetLocation = targetLocation;
        this.priority = priority;
        this.isCompleted = false;
        this.previousTaskId = -1;
    }
    /**
     * @author Yifei
     * @description Construct a task without boxId means the agent is moving by itself. boxId = -1 denotes without box.
     * @date 2021/4/14
     */
    public Task(int id, int agentId, Location targetLocation, int priority) {
        this.id = ++counter;
        this.boxId = -1;
        this.agentId = agentId;
        this.targetLocation = targetLocation;
        this.priority = priority;
        this.isCompleted = false;
        this.previousTaskId = -1; //5/15 debug: add default previousTaskId -1 to tasks without box

    }

    public int getGoalId() {
        return goalId;
    }

    public void setGoalId(int goalId) {
        this.goalId = goalId;
    }

    public int getReverseTaskId() {
        return reverseTaskId;
    }

    public void setReverseTaskId(int reverseTaskId) {
        this.reverseTaskId = reverseTaskId;
    }

    public Location getStartLocation() {
        return startLocation;
    }

    public void setStartLocation(Location startLocation) {
        this.startLocation = startLocation;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Task task = (Task) o;
        return boxId == task.boxId && agentId == task.agentId && priority == task.priority && Objects.equals(targetLocation, task.targetLocation);
    }

    @Override
    public int hashCode() {
        return Objects.hash(boxId, agentId, targetLocation, priority);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", boxId=" + boxId +
                ", agentId=" + agentId +
                ", targetLocation=" + targetLocation +
                ", priority=" + priority +
//                ", isCompleted=" + isCompleted +
//                ", previousTaskId=" + previousTaskId +
                ", goalId=" + goalId +
                ", reverseTaskId=" + reverseTaskId +
//                ", startLocation=" + startLocation +
                '}';
    }
}



