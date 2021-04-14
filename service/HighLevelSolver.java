package service;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class HighLevelSolver {

    private final InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Location, Boolean> map;
    public HighLevelSolver(InMemoryDataSource data){
        this.data = data;
    }

    public Action[][] solve(){
        System.err.println("[HighLevelSolver] Solving...");

        System.err.println("[HighLevelSolver] Get all tasks: " + data.getAllTasks().size());

        //return plan

        return null;
    }

}
