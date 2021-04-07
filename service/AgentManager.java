package service;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class AgentManager {

    private final InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Location, Boolean> map;
    public AgentManager(InMemoryDataSource data){
        this.data = data;
    }

    public Action[][] search(){
        /**
         * 1.1 Assign tasks  ---  Done
         * 1.2 Update priority recursively (a goal surrounded by 3 walls is definitely with highest priority)  --partially done
         * 1.3 Identify blocking goals (mark map with costs from each goal separately)
         */
        TaskHandler taskHandler = new TaskHandler(data);
//        allTasks = taskHandler.assignTask();
//        allTasks = taskHandler.updateTa skPriorityBy3Walls();
        allGoals = data.getAllGoals();
        HashMap<Location,Integer> costMap = taskHandler.getCostMap(allGoals.get(0));

        System.err.println("Cost Map of goal 0: "+ costMap.toString());





        //TODO: plan (for each subgoal, do graph search)
        //TODO: merge plan (by each round)
        //return plan

        return null;
    }

}
