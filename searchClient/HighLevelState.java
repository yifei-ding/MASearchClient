package searchClient;

import domain.Conflict;
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
    private int numberOfConflicts;
    private int cost;

    public HighLevelState(HashSet<Constraint> constraints) {
//        this.constraints = constraints;
        this.constraints.addAll(constraints);
    }

    public int getNumberOfConflicts() {
        return numberOfConflicts;
    }

    public void setNumberOfConflicts(int numberOfConflicts) {
        this.numberOfConflicts = numberOfConflicts;
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
    public void addRangeConstraintsBackwards(Constraint constraint, int length){
        System.err.println("Add RangeConstraintsBackwards: " + constraint.toString() + " length= " + length );

        int startTimeStep = constraint.getTimeStep();
        int endTimeStep = startTimeStep - length;
        int agentId = constraint.getAgentId();
        boolean isBoxConstraint = constraint.isBoxConstraint();
        Location location = constraint.getLocation();
        for (int i = startTimeStep; i > endTimeStep; i--) {
            Constraint constraint1 = new Constraint(agentId,isBoxConstraint,i,location);
//            System.err.println("Add constarint: " + constraint1.toString() );
            this.constraints.add(constraint1);
        }
    }

    private void updateNumberOfConflicts(){
        LocationPair[][] solution = this.getSolution();
        for(int i =0;i<solution.length;i++) { //i=agent 1
            for (int j = i + 1; j < solution.length; j++) { //j=agent 2
                LocationPair[] route1 = solution[i];
                LocationPair[] route2 = solution[j];
                //now we have one path each for agent1 and agent2
                if ((route1 != null) && (route2 != null) && (route1.length>1) && (route2.length>1)){
                    this.numberOfConflicts += countConflict(route1,route2);
                }
            }
        }
    }

    private int countConflict(LocationPair[] route1, LocationPair[] route2) {
        int count=0;
        int minIndex = Math.min(route1.length, route2.length)-1;
        for (int k=0; k< minIndex; k++){ //timestep
            if (route1[k].overlaps(route2[k])) {
              count++;
            }
            else if (route1[k+1].overlaps(route2[k])) {
                count++;
            }
            else if (route1[k].overlaps(route2[k+1])) {
                count++;
            }
        }
        return count;
    }

    public LocationPair[][] calculateSolution() {
        this.solution = LowLevelSolver.solveForAllAgents(this.constraints);
        if (validSolution())
                this.updateNumberOfConflicts();

        return this.solution;
    }

    public LocationPair[][] getSolution() {
        return  this.solution;
    }

    public boolean validSolution(){
        int i = -1;
        for (LocationPair[] singeAgentSolution: solution){
            i++;
            if (singeAgentSolution == null)
                System.err.println("[HighLevelState] Agent " + i + " task error in low level");

            if (singeAgentSolution.length==0) {
                System.err.println("[HighLevelState] Agent " + i + " doesn't have solution in low level");
                return false;
            }
        }
        return true;
    }

    public void printSolution(){
        //print solution
        System.err.println("[HighLevelState] Get solution:");
        for (int i=0; i<solution.length;i++){
            System.err.println("Agent "+i+" : " + Arrays.toString(solution[i]));
                    System.err.println("Agent "+i+" : " + solution[i].length);}

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

    public void addConstraints(ArrayList<Constraint> constraints) {
        for (Constraint constraint:constraints){
            this.addConstraint(constraint);
        }
//        System.err.println(this.constraints.toString());
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
