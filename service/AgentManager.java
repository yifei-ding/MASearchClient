package service;

import data.InMemoryDataSource;
import domain.Action;

public class AgentManager {
    static AgentManager instance;
    private InMemoryDataSource data;
    private AgentManager(){
        this.data = InMemoryDataSource.getInstance();
    }
    public static AgentManager getInstance() {
        if (instance == null)
            instance = new AgentManager();
        System.err.println("AgentManager instance");
        return instance;
    }
    public Action[][] search(){
        //TODO: get map info from SearchClient and store to InMemoryDataSource
        System.err.println("AgentManager search");

        return null;
    }
}
