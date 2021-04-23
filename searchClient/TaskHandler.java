package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskHandler {
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals = new HashMap<Integer, Goal>();
    private HashMap<Integer, Agent> allAgents = new HashMap<Integer, Agent>();
    private HashMap<Integer, Box> allBoxes = new HashMap<Integer, Box>();

    private HashMap<Location, Object> staticMap = new HashMap<Location, Object>();
    private HashMap<Location, Object> dynamicMap = new HashMap<Location, Object>();
    private HashMap<Location, Integer> costMap = new HashMap<Location,Integer>();
    private static final int INFINITY = 2147483647;
    private static final int PRIORITY_SCALING_PARAM = 10;
    public TaskHandler(InMemoryDataSource data) {
        this.data = data;
    }

    public void assignTask(){
        /**
         * @author Yifei
         * @description Baseline version of assigning tasks: for each goal, find the same name box; then for each box, find
         * same color agent. Assume there's no duplicate name boxes and goals. After the match, create 2 tasks for each
         * goal. And add task to InMemoryDataSource.
         * @date 15:34 2021/4/5
         * @param
         * @return void
         */

        System.err.println("[TaskHandler]: Assigning tasks");

        allGoals = data.getAllGoals();
        allAgents = data.getAllAgents();
        allBoxes = data.getAllBoxes();
        int taskId = 0;
        int goalCount = allGoals.size(); //for setting priority
        int priority = 0;
        for (Goal goal : allGoals.values()){
            //for each goal, find a same name box. Currently use the first matching box.
            ArrayList<Integer> boxList = data.getBoxByName(goal.getName());

            Box box = allBoxes.get(boxList.get(0)); //TODO: improve goal-box matching. Remove box after pairing

            //then for the box, find a same color agent. Currently use the first matching agent.
            ArrayList<Integer> agentList = data.getAgentByColor(box.getColor());
            Agent agent = allAgents.get(agentList.get(0));//TODO: improve box-agent matching

            //for each goal, create 2 tasks. Tasks of the first goal has highest priority.
            //task1 is to find box. Task1 has higher priority than task2.
            priority = goalCount*PRIORITY_SCALING_PARAM;
            Task task1 = new Task(taskId,agent.getId(),box.getLocation(),priority);
            taskId++;
            //task2 is to push/pull box to goal.
            //TODO: When planning task2, need to check if agent is beside the box. Otherwise create a new task of task1 to let agent find box.
            Task task2 = new Task(taskId,agent.getId(),box.getId(), goal.getLocation(),priority-1);
            taskId++;
            data.addTask(task1);
            data.addTask(task2);
            goalCount--;

        }
        System.err.println("[TaskHandler] Initial tasks: "+data.getAllTasks().toString());

    }


    public void assignTask3(){
        /**
         * @author Yifei
         * @description Baseline version of assigning tasks: for each goal, find the same name box; then for each box, find
         * same color agent. Assume there's no duplicate name boxes and goals. After the match, create 2 tasks for each
         * goal. And add task to InMemoryDataSource.
         * @date 15:34 2021/4/5
         * @param
         * @return void
         */

        System.err.println("[TaskHandler]: Assigning tasks");

        allGoals = data.getAllGoals();
        allAgents = data.getAllAgents();
        allBoxes = data.getAllBoxes();
        int taskId = 0;
        int goalCount = allGoals.size(); //for setting priority
        int priority = 0;
        Box box;
        for (Goal goal : allGoals.values()){
            box = null;


            Agent agent = data.getAgent(goal.getId());

            //for each goal, create 2 tasks. Tasks of the first goal has highest priority.
            //task1 is to find box. Task1 has higher priority than task2.
            priority = goalCount*PRIORITY_SCALING_PARAM;
            Task task1 = new Task(taskId,agent.getId(),null,priority);
            taskId++;
            //task2 is to push/pull box to goal.
            //TODO: When planning task2, need to check if agent is beside the box. Otherwise create a new task of task1 to let agent find box.
            Task task2 = new Task(taskId,agent.getId(),-1, goal.getLocation(),priority-1);
            taskId++;
            data.addTask(task1);
            data.addTask(task2);
            goalCount--;

        }
        System.err.println("[TaskHandler] Initial tasks: "+data.getAllTasks().toString());

    }

}
