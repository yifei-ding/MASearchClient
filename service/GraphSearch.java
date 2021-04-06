package service;

import domain.Action;
import domain.Location;
import domain.Task;

import java.util.ArrayList;
import java.util.HashSet;

public class GraphSearch {

    public static Action[] search(Location from, Location to)
    {

    return null;
    }

//    public Action[] plan (Task task){
//
//        Location from = task.getAgent().getLocation();
//        Location to = task.getBox().getLocation();
//        //find path from agent to box
//        Action[] action1 = GraphSearch.search(from,to);
//
//        from = task.getBox().getLocation();
//        to = task.getGoal().getLocation();
//        //find path from box to goal
//        Action[] action2 = GraphSearch.search(from,to);
//        //translate to agent action, translate from push/pull to move
//
//        //return action1+action2
//        return null;
//    }

}
