package domain;

public class Task {
    private final int id;
    private Goal goal;
    private Box box;
    private Agent agent;
    private int priority;
    private boolean isCompleted;
    /**
    * @Author Yifei
    * @Description TaskId is equal to GoalId, this is designed for conveniently updating a task, since
    * task is goal-based.
    * @Date 15:37 2021/4/5
     */
    public Task(Goal goal, Box box, Agent agent) {
        this.id = goal.getId();
        this.goal = goal;
        this.box = box;
        this.agent = agent;
        this.priority = 0; //by default
        this.isCompleted = false; //by default
    }

    public int getId() {
        return id;
    }

    public Goal getGoal() {
        return goal;
    }

    public void setGoal(Goal goal) {
        this.goal = goal;
    }

    public Box getBox() {
        return box;
    }

    public void setBox(Box box) {
        this.box = box;
    }

    public Agent getAgent() {
        return agent;
    }

    public void setAgent(Agent agent) {
        this.agent = agent;
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
                //"id=" + id +
                ", goal=" + goal +
                ", box=" + box +
                ", agent=" + agent +
                //", priority=" + priority +
                //", isCompleted=" + isCompleted +
                '}';
    }
}
