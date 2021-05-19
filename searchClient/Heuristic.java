package searchClient;

import domain.Location;

import java.util.Comparator;
import java.lang.Math;

public abstract class Heuristic
        implements Comparator<State>
{
    public Heuristic(State initialState)
    {
        // Here's a chance to pre-process the static parts of the level.
    }

    public int h(State s)
    {
        if (s.getBoxId() == -1)
            return this.closedFormSolution(s.getLocation(),s.getGoalLocation());
        else {
            if (s.getAgentLocation().getNeighbours().contains(s.getLocation())) // if agent is next to box
            {
//                System.err.println("Agent is next to box");
                return this.closedFormSolution(s.getLocation(), s.getGoalLocation()); //return box-goal distance
            }
                else {
//                    System.err.println("Agent is not next to box");
                    return this.closedFormSolution(s.getAgentLocation(), s.getLocation()); //else return agent-box distance
                }
                }

    }

    private int closedFormSolution(Location location1, Location location2){
        int x1 = location1.getRow();
        int x2 = location2.getRow();
        int y1 = location1.getCol();
        int y2 = location2.getCol();
        int dx = Math.abs(x1-x2);
        int dy = Math.abs(y1-y2);
        double h = (dx+dy) + (Math.sqrt(2)-2)*Math.min(dx,dy);
        return (int) Math.floor(h);
    }

    private int getManhattanDistance(Location location1, Location location2){
        return Math.abs(location1.getCol()- location2.getCol())
                + Math.abs(location1.getRow())- location2.getRow();
    };

    public abstract int f(State s);

    @Override
    public int compare(State s1, State s2)
    {
        return this.f(s1) - this.f(s2);
    }
}

class HeuristicAStar
        extends Heuristic
{
    public HeuristicAStar(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
//        System.err.println("A* h value = " + this.h(s));
//        System.err.println("A* f value = " + s.g()+this.h(s));

        return s.g() + this.h(s);
    }

    @Override
    public String toString()
    {
        return "A* evaluation";
    }
}

class HeuristicWeightedAStar
        extends Heuristic
{
    private int w;

    public HeuristicWeightedAStar(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s)
    {
        return s.g() + this.w * this.h(s);
    }

    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}

class HeuristicGreedy
        extends Heuristic
{
    public HeuristicGreedy(State initialState)
    {
        super(initialState);
    }

    @Override
    public int f(State s)
    {
//        System.err.println("Greedy h value = " + this.h(s));
        return this.h(s);
    }

    @Override
    public String toString()
    {
        return "greedy evaluation";
    }
}


//pwXD is a piecewise downward curve which uses A* until h<g, after which is uses WA* with a weight of 2w-1.
//Returns w-optimal solutions.


class HeuristicPWXD
        extends Heuristic
{
    private int w;

    public HeuristicPWXD(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s){
        if(h(s)> s.g()){
            return s.g() + this.w * this.h(s);
        }
        else{
            return  (s.g()+(2*w-1)*h(s))/w;
        }
    }
    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}


/*
XDP stands for convex downward parabola. This is a small modification to weighted A*. In Weighted A*,
the set of states with the same priority are on a straight line. XDP changes this straight line to a parabola.
XDP causes the best-first search to find near-optimal paths near the start and paths that are up to (2w-1) supoptimal
near the goal. Overall the paths found are still w-optimal, and re-openings are not required.

*/




class HeuristicXDP
        extends Heuristic
{
    private int w;

    public HeuristicXDP(State initialState, int w)
    {
        super(initialState);
        this.w = w;
    }

    @Override
    public int f(State s){
       return (int) ((0.5*w)*(s.g()+(2*w-1)*h(s)+Math.sqrt(Math.pow((s.g()-h(s)),2)+4*w*s.g()*h(s))));
    }
    @Override
    public String toString()
    {
        return String.format("WA*(%d) evaluation", this.w);
    }
}
