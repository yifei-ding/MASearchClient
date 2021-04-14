package searchClient;

import data.InMemoryDataSource;
import domain.Action;
import domain.Constraint;
import domain.Location;
import domain.Task;

import java.util.ArrayList;

public class LowLevelSolver {

    public static Action[][] solveForAllAgents(InMemoryDataSource data, ArrayList<Constraint> constraints)
    {
        //for each agent, do:
          //task = get an uncompleted task of the agent with highest priority
          //Preprocess data: check task type; get everything needed for LowLevelSolver.solve
          //Then call LowLevelSolver.solve


        return null;
    }


    public static Action[] solve(Integer[][] map, ArrayList<Constraint> constraints, Location from, Location to)
    {
        //Use graph search to find a solution

        return null;
    }




}
