package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

public class HighLevelSolver {

    private final InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals;
    private HashMap<Integer, Task> allTasks = new HashMap<Integer, Task>();
    private HashMap<Location, Boolean> map;

    public HighLevelSolver(InMemoryDataSource data) {
        this.data = data;
    }

    public Action[][] solve() {
        System.err.println("[HighLevelSolver] Solving...");
        HighLevelState root = new HighLevelState(null);
//        root.updateCost();
        root.updateSolution();
        Conflict firstConflict = getFirstConflict(root);
//        System.err.println("line 27 "+firstConflict.toString());
        hasConflict(root.getSolution());
        return null;
    }

    /**
     * @return void
     * @author Yifei
     * @description
     * @date 2021/4/23
     * //    * @param [allPaths]
     */
    private boolean hasConflict(Location[][] allPaths) {  // TODO: add boxes into the Pathes
        for (int i = 0; i < allPaths[0].length; i++) { //i = timestep
//            ArrayList<Location> locations = new ArrayList<>();
            HashMap<Location, Integer> locations = new HashMap<>();
            for (int j = 0; j < allPaths.length; j++) { //j = agent
                Location location = allPaths[j][i];
//                Set<Integer> indexes = locations.get(location);
                if (locations.get(location) == null) {
                    locations.put(location,j);
                } else {
                    return true;
                }
            }
        }
        return false;
    }

    private Conflict getFirstConflict(HighLevelState state) {
        Location[][] allPaths = state.getSolution();
//        ArrayList<Constraint> constraints = new ArrayList<>();
        for (int i = 0; i < allPaths[0].length; i++) { //i = timestep
//            ArrayList<Location> locations = new ArrayList<>();
            HashMap<Location, Integer> locations = new HashMap<>();
            for (int j = 0; j < allPaths.length; j++) { //j = agent
                Location location = allPaths[j][i];
//                Set<Integer> indexes = locations.get(location);
                if (locations.get(location) == null) {
                    locations.put(location,j);
                } else {
                    int agentId_1 = j;
                    int agentId_2 = locations.get(location);
                    Conflict conflict = new Conflict(agentId_1,agentId_2,location,i);
                    return conflict;
                }

            }
        }
        return null;
    }


//    private boolean hasConflict(){
//
//        return true;
//    }


}
