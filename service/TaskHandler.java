package service;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskHandler {
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals = new HashMap<Integer, Goal>();
    private HashMap<Location, Object> map = new HashMap<Location, Object>();
    private HashMap<Location, Object> dynamicMap = new HashMap<Location, Object>();

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
            //System.err.println("Goal: "+goal.toString());
            //for each goal, find the same name box
            ArrayList<Box> boxList = data.getBoxByName(goal.getName());
            for (Box box: boxList){
                //System.err.println("Box: "+box.toString());
                //then for each box, find same color agent
                ArrayList<Agent> agentList = data.getAgentByColor(box.getColor());
                //System.err.println("AgentList: "+agentList.toString());
                Task task = new Task(goal, box, agentList.get(0));
                allTasks.put(task.getId(),task);
            }
        }
        System.err.println("Initial tasks: "+allTasks.toString());
        return allTasks;
    }

    public HashMap<Integer, Task> updateTaskPriorityBy3Walls(){
        /**
        * @author Yifei
        * @description Identify which goals are surrounded by 3 walls and set their task priority to 5.
        * @date 2021/4/7
        * @param []
        * @return HashMap<Integer, Task> allTasks
         */
        allTasks = assignTask();
        for (Goal goal : allGoals.values()){
            //if the goal has 3 walls, set priority of that goal's task
            if (is3WallGoal(goal)) {
                allTasks.get(goal.getId()).setPriority(5);
            }
        }
        System.err.println("Updating tasks: "+allTasks.toString());

        return allTasks;
    }

    private Boolean is3WallGoal(Goal goal) {
        /**
        * @author Yifei
        * @description To check whether a goal has 3 walls around it.
        * @date 2021/4/7
        * @param [goal]
        * @return Boolean
         */
        map = data.getStaticMap();
        Location up = goal.getLocation().getUpNeighbour();
        Location down = goal.getLocation().getDownNeighbour();
        Location left = goal.getLocation().getLeftNeighbour();
        Location right = goal.getLocation().getRightNeighbour();
        System.err.println("Goal: " + goal.getName());

        int count = 0 ;
        if (((Wall) map.get(up)).isWall())
            count++;
        if (((Wall)map.get(down)).isWall())
            count++;
        if (((Wall)map.get(left)).isWall())
            count++;
        if (((Wall)map.get(right)).isWall())
            count++;
        if (count == 3){
            return true;
        }
        else return false;
    }

    public HashMap<Location, Integer> getCostMap(Goal goal){
        /**
        * @author Yifei
        * @description Given a goal and the current location of boxes and agents, calculate the cost from
         * every location on the map to the goal.
        * @date 2021/4/7
        * @param [goal]
        * @return HashMap<Location, Integer>
         */
        HashMap<Location, Integer> costMap = new HashMap<Location, Integer>();
        //get agent and box location on the map
        return costMap;
    }

}
