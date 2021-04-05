package service;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class AgentManager {

    private InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    public AgentManager(InMemoryDataSource data){
        this.data = data;
    }

    public Action[][] search(){

        //get map info from SearchClient and store to InMemoryDataSource
        //System.err.println("AgentManager search " + data.toString());

        //In progress: for each goal, assign box, assign agent -Ding

        assignTask(data);
        System.err.println("Assign task: "+ allTasks.toString());


        //TODO: plan (for each subgoal, do graph search) -Ren & Liu

        //TODO: merge plan (by each round) Agent1 :100; Agent2: 10  -Liu

        //return plan

        return null;
    }

    /*
    * @Author Yifei
    * @Description Baseline version of assigning tasks: for each goal, find the same name box; then for each box, find
    * same color agent. Assume there's no duplicate name boxes and goals.
    * @Date 15:34 2021/4/5
    * @Param [data]
    * @return void
     **/
    public void assignTask(InMemoryDataSource data){
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
                allTasks.put(goal.getId(),task);
            }
        }
    }



}
