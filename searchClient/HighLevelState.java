package searchClient;

import domain.Constraint;
import domain.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

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
//        System.err.println("[HighLevelState] Add constraint " + this.constraints.toString());
        if (!this.constraints.contains(constraint))
            this.constraints.add(constraint);
//        System.err.println("[HighLevelState] After adding constraint  " + this.constraints.toString());

    }

    public Location[][] calculateSolution() {
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
        //cost = the sum of the steps of each agent
        for (int i =0;i<this.solution.length;i++){
            this.cost += this.solution[i].length;
        }
//        System.err.println("[HighLevelState] Update Cost: " + this.cost);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HighLevelState that = (HighLevelState) o;
        return Objects.equals(constraints, that.constraints);
    }

    @Override
    public int hashCode() {
        int result = Objects.hash(constraints);
        result = 31 * result + Arrays.hashCode(solution);
        return result;
    }

    @Override
    public String toString() {
        return "HighLevelState{" +
                "constraints=" + constraints +
                '}';
    }
}
