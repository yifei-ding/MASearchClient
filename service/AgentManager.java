package service;

import data.InMemoryDataSource;
import domain.Action;

public class AgentManager {

    private InMemoryDataSource data;
    public AgentManager(InMemoryDataSource data){
        this.data = data;
    }

    public Action[][] search(){

        //get map info from SearchClient and store to InMemoryDataSource
        System.err.println("AgentManager search " + data.toString());
        System.err.println("Map " + data.toString2());

        //TODO: for each goal, assign box, assign agent

        //TODO: plan (for each subgoal, do graph search)

        //TODO: merge plan

        //return plan

        return null;
    }
}
