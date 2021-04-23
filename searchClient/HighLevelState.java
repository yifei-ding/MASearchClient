package searchClient;

import domain.Constraint;
import domain.Location;

import java.util.ArrayList;
import java.util.Arrays;

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
        return solution;
    }

    public Location[][] getSolution() {
        return solution;
    }

    public int getCost() {
        return cost;
    }

    public void updateCost() {
        System.err.println("[HighLevelState] Update Cost: " + this.solution.length);
        //TODO: maybe change to total steps of all agents
        this.cost = this.solution.length; //currently it is makespan
    }
}
