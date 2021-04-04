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

        //TODO: for each goal, assign box, assign agent -Ding

        //TODO: plan (for each subgoal, do graph search) -Ren & Liu

        //TODO: merge plan (by each round) Agent1 :100; Agent2: 10  -Liu

        //return plan

        return null;
    }
}
