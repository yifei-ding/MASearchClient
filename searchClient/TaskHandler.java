package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class TaskHandler {
    private static final TaskHandler taskHandler = new TaskHandler();

    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Integer, Goal> allGoals = new HashMap<Integer, Goal>();
    private HashMap<Integer, Agent> allAgents = new HashMap<Integer, Agent>();
    private HashMap<Integer, Box> allBoxes = new HashMap<Integer, Box>();

    private HashMap<Location, Object> staticMap = new HashMap<Location, Object>();
    private HashMap<Location, Object> dynamicMap = new HashMap<Location, Object>();
    private HashMap<Location, Integer> StaticdegreeMap = new HashMap<Location, Integer>();
    private HashMap<Integer,ArrayList<Location>> corridor = new HashMap<Integer,ArrayList<Location>>();
    private static InMemoryDataSource data = InMemoryDataSource.getInstance();



    private HashMap<Location, Integer> costMap = new HashMap<Location,Integer>();
    private static final int INFINITY = 2147483647;
    private static final int PRIORITY_SCALING_PARAM = 10;
    private TaskHandler() {

    }
    public static TaskHandler getInstance() {

        return taskHandler;
    }









    public String getMapType() { //return which value??
        HashMap<Location, Integer> StaticdegreeMap = data.getDegreeMap();
        Iterator<Location> iterator = StaticdegreeMap.keySet().iterator();
        while (iterator.hasNext()){
            Location key = iterator.next();
            int degreenum = StaticdegreeMap.get(key);
            if( degreenum == 2){
                int i = key.getRow();
                int j = key.getCol();
                Location locationup = new Location(i,j+1); //嵌套循环然后返回走廊的location list？？
                Location locationdown = new Location(i,j+1);
                Location locationleft = new Location(i-1,j);
                Location locationright = new Location(i + 1,j);
                if(StaticdegreeMap.get(locationup) == 2 || StaticdegreeMap.get(locationdown) == 2 || StaticdegreeMap.get(locationleft) == 2 ||StaticdegreeMap.get(locationright) == 2){
                    return "corridoroccur"; }

                //check adjacent
                }
            else if (degreenum == 3){
                return "deadendoccur";
            }
            else{
            }
        }
        return  "normalmap";
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

    /**
    * @author Yifei
    * @description This method is for testing maps without box
    * @date 2021/4/27
    * @param
    * @return void
     */
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
            Task task1 = new Task(taskId,agent.getId(),goal.getLocation(),priority);
            taskId++;
            //task2 is to push/pull box to goal.
            //TODO: When planning task2, need to check if agent is beside the box. Otherwise create a new task of task1 to let agent find box.
            taskId++;
            data.addTask(task1);

            goalCount--;

        }
        System.err.println("[TaskHandler] Initial tasks: "+data.getAllTasks().toString());

    }

    private int getManhattanDistance(Location location1, Location location2){
        return Math.abs(location1.getCol()- location2.getCol())
                + Math.abs(location1.getRow())- location2.getRow();
    };

    public void assignTask2(){
        allGoals = data.getAllGoals();
        allAgents = data.getAllAgents();
        allBoxes = data.getAllBoxes();
        int taskId = 0;
//        int goalCount = allGoals.size(); //for setting priority
        int priority = 0;
        for (Goal goal : allGoals.values()){
            //for each goal, find a same name box. Currently use the first matching box.
            ArrayList<Integer> boxList = data.getBoxByName(goal.getName());
            if(boxList == null){// No box match, so goal match agent by name ? or + color?
                //agent to goal without box
                int goalName = Integer.parseInt(goal.getName());
                Agent agent = allAgents.get(goalName); // according to goalName match agent
                priority = getManhattanDistance(agent.getLocation(),goal.getLocation())*10;
                priority = priority+10000;//considering normally the agent need done box tasks
                Task task = new Task(taskId,agent.getId(),goal.getLocation(),priority);
                taskId++;
                data.addTask(task);
            }
            // TODO: box-goal matching
            else { // if there is any box
//                Box box = allBoxes.get(boxList.get(0)); //TODO: only one box? improve goal-box matching. Remove box after pairing
                int min_distance = -1; // match the nearest box
                Box matchedBox = null; //
                ArrayList<Integer>  matchAgentIds= new ArrayList<>();
                for (int boxId : boxList){
                    Box box = allBoxes.get(boxId);
                    ArrayList<Integer> agentList = data.getAgentByColor(box.getColor());
                    if(agentList.size() == 0 || agentList == null){
                        continue;
                    }
                    int distance = getManhattanDistance(goal.getLocation(),box.getLocation());
                    if (min_distance == -1 || distance < min_distance){
                        min_distance = distance;
                        matchedBox = box;
                        matchAgentIds = agentList;
                    }
                }
                if (matchedBox == null){
                    System.err.println("This map is not solvable!");
                    continue;
                }
                //then for the box, find a same color agent. Currently use the first matching agent.
                //TODO: improve box-agent matching
                int min_score = -1;
                int min_distance2 = -1;
                Agent matchedAgent = null;
                for(int agentId : matchAgentIds){
                    Agent agent = allAgents.get(agentId);
                    ArrayList<Integer> tasks = data.getAllTasksByAgent(agentId);
                    int task_score = 0;
                    if(tasks != null){
                        task_score = tasks.size()*10;
                    }
                    int distance = getManhattanDistance(agent.getLocation(), matchedBox.getLocation());
                    int score = distance + task_score;
                    if (min_score == -1 || score<min_score){
                        min_score = score;
                        min_distance2 = distance;
                        matchedAgent = agent;
                    }
                }

                //for each goal, create 2 tasks. Tasks of the first goal has highest priority.
                //task1 is to find box. Task1 has higher priority than task2.
//                priority = goalCount*PRIORITY_SCALING_PARAM;
                priority = (min_distance + min_distance2)*10; // agent-box-goal sum distance x10
               //Task task1 = new Task(taskId,matchedAgent.getId(),matchedBox.getLocation(),priority); // 5/5 skip task1
                taskId++;
                //task2 is to push/pull box to goal.
                //TODO: When planning task2, need to check if agent is beside the box. Otherwise create a new task of task1 to let agent find box.
                Task task2 = new Task(taskId,matchedAgent.getId(),matchedBox.getId(), goal.getLocation(),priority-1);
                taskId++;
                //data.addTask(task1);
                data.addTask(task2);
//                goalCount--;
            }


        }
        System.err.println("[TaskHandler] Initial tasks: "+data.getAllTasks().toString());
    }

    public Task pop(int agentId){
        /**
        * @author Yifei
        * @description Pop a task of the given agentId that is not completed and with highest priority
        * @date 2021/4/27
        * @param [agentId]
        * @return Task
         */
        Task task = null;
        ArrayList<Integer> taskList = data.getAllTasksByAgent(agentId); //already in descending order
        if (taskList != null){
            for (Integer taskId:taskList){
                if (!data.getTaskById(taskId).isCompleted()) //check whether the task is completed TODO:check whether precondition tasks are completed
                    task = data.getTaskById(taskId);
            }
        }
        return task;
    }

    public void completeTask(int agentId) {
      Task task = pop(agentId);
      if (task != null){
          //update task status to complete
          data.setTaskAsComplete(task.getId());
      }

    }
}
