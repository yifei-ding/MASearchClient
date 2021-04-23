package searchClient;

import domain.Constraint;
import domain.Location;

import java.util.ArrayList;

public class HighLevelState {

    private ArrayList<Constraint> constraints;
    private Location[][] solution;
    private int cost;

    public HighLevelState(ArrayList<Constraint> constraints) {
        this.constraints = constraints;
    }

    public ArrayList<Constraint> getConstraints() {
        return constraints;
    }

    public void addConstraint(Constraint constraint) {
        this.constraints.add(constraint);
    }

    public Location[][] updateSolution() {
        this.solution = LowLevelSolver.solveForAllAgents(this.constraints);
        System.err.println("[1111111]Solution length" + solution.length);
        return solution;
    }

    public Location[][] getSolution() {
        return solution;
    }

    public int getCost() {
        return cost;
    }

    public void updateCost() {
        //TODO: maybe change to total steps of all agents
        this.cost = this.solution.length; //currently it is makespan
    }
}
