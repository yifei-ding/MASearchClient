package searchClient;

import data.InMemoryDataSource;
import domain.*;

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
        HighLevelState root = new HighLevelState(null);
        root.updateSolution();
//        root.updateCost();

//        checkConflict(allPaths);
        return null;
    }

    /**
    * @author Yifei
    * @description
    * @date 2021/4/23
    * @param [allPaths]
    * @return void
     */
    private void checkConflict(Location[][] allPaths) {
        for (int i=0; i < allPaths[0].length; i++){ //i = timestep
            for (int j=0; j< allPaths.length;j ++){ //j = agent

            }

        }


    }


}
