package service;

import data.InMemoryDataSource;
import domain.Agent;
import domain.Box;
import domain.Goal;
import domain.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskHandler {
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;

    public TaskHandler(InMemoryDataSource data) {
        this.data = data;
    }

    public HashMap<Integer, Task> assignTask(){
        /**
         * @author Yifei
         * @description Baseline version of assigning tasks: for each goal, find the same name box; then for each box, find
         * same color agent. Assume there's no duplicate name boxes and goals.
         * @date 15:34 2021/4/5
         * @param [data]
         * @return void
         */
        allGoals = data.getAllGoals();
        for (Goal goal : allGoals.values()){
            System.err.println("Goal: "+goal.toString());
            //for each goal, find the same name box
            ArrayList<Box> boxList = data.getBoxByName(goal.getName());
            for (Box box: boxList){
                System.err.println("Box: "+box.toString());
                //then for each box, find same color agent
                ArrayList<Agent> agentList = data.getAgentByColor(box.getColor());
                System.err.println("AgentList: "+agentList.toString());
                Task task = new Task(goal, box, agentList.get(0));
                allTasks.put(task.getId(),task);
            }
        }
        return allTasks;
    }


}
