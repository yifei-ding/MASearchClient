package searchClient;

import domain.Constraint;
import domain.Location;
import domain.LocationPair;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class HighLevelState {

    private HashSet<Constraint> constraints = new HashSet<>();
    private LocationPair[][] solution;
    private int cost;

    public HighLevelState(HashSet<Constraint> constraints) {
//        this.constraints = constraints;
        this.constraints.addAll(constraints);
    }

    public HashSet<Constraint> getConstraints() {
        return constraints;
    }

    public void addConstraint(Constraint constraint) {
        this.constraints.add(constraint);
    }
    public void addRangeConstraints(Constraint constraint, int length){
        int startTimeStep = constraint.getTimeStep();
        int endTimeStep = startTimeStep + length;
        int agentId = constraint.getAgentId();
        boolean isBoxConstraint = constraint.isBoxConstraint();
        Location location = constraint.getLocation();
        for (int i = startTimeStep; i < endTimeStep; i++) {
            this.constraints.add(new Constraint(agentId,isBoxConstraint,i,location));
        }
    }


    public LocationPair[][] calculateSolution() {
        solution = LowLevelSolver.solveForAllAgents(this.constraints);
        return solution;
    }

    public LocationPair[][] getSolution() {
        //print solution
//        System.err.println("[HighLevelState] Get solution:");
//        for (int i=0; i<solution.length;i++)
//            System.err.println("Agent "+i+" : " + Arrays.toString(solution[i]));

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
        return constraints.equals(that.constraints);
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



//    public static void main(String[] args) {
//        HashSet<Constraint> constraints1 = new HashSet<>();
//        constraints1.add(new Constraint(3,1,new Location(1,1)));
//        constraints1.add(new Constraint(1,1,new Location(1,1)));
//        constraints1.add(new Constraint(2,2,new Location(2,2)));
//        HashSet<Constraint> constraints2 = new HashSet<>();
//        constraints2.add(new Constraint(2,2,new Location(2,2)));
//        constraints2.add(new Constraint(1,1,new Location(1,1)));
//        constraints2.add(new Constraint(3,1,new Location(1,1)));
//        System.out.printf(""+constraints2.equals(constraints1));
//
//    }

}
