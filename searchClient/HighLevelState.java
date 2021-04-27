package searchClient;

import domain.Constraint;
import domain.Location;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class HighLevelState {

    private ArrayList<Constraint> constraints = new ArrayList<>();
    private Location[][] solution;
    private int cost;

    public HighLevelState(ArrayList<Constraint> constraints) {
//        this.constraints = constraints;
        this.constraints.addAll(constraints);
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
        solution = LowLevelSolver.solveForAllAgents(this.constraints);
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
        if(solution!=null){
            for (int i =0;i<this.solution.length;i++){
                if(solution[i]!=null) { // TODO: why solution[i] maybe null
                    cost += this.solution[i].length;
                }
            }
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
        return Objects.hash(constraints);
    }

    @Override
    public String toString() {
        return "HighLevelState{" +
                "constraints=" + constraints +
                '}';
    }

    public static void main(String[] args) {
        ArrayList<Constraint> constraints1 = new ArrayList<>();
        constraints1.add(new Constraint(1,1,new Location(1,1)));
        constraints1.add(new Constraint(2,2,new Location(2,2)));
        ArrayList<Constraint> constraints2 = new ArrayList<>();
        constraints2.add(new Constraint(2,2,new Location(2,2)));
        constraints2.add(new Constraint(1,1,new Location(1,1)));
        System.out.printf(""+constraints2.equals(constraints1));

    }

}
