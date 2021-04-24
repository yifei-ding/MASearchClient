package searchClient;

import java.util.ArrayDeque;
import java.util.HashSet;
import java.util.PriorityQueue;

public interface Frontier
{
    void add(State state);
    State pop();
    boolean isEmpty();
    int size();
    boolean contains(State state);
    String getName();
}
class FrontierBestFirst
        implements Frontier {
    private Heuristic heuristic;
    private PriorityQueue<State> queue;
    private HashSet<State> set;

    public FrontierBestFirst(Heuristic h) {
        this.heuristic = h;
        queue = new PriorityQueue<>(heuristic);
        set = new HashSet<>(65536);
    }

    @Override
    public void add(State state) {
//        System.err.println("[FrontierBestFirst] add state " + state.toString());
        this.queue.offer(state);
        this.set.add(state);
    }

    @Override
    public State pop() {
        State state = this.queue.poll();
        this.set.remove(state);
        return state;
    }

    @Override
    public boolean isEmpty() {
        return this.queue.isEmpty();
    }

    @Override
    public int size() {
        return this.queue.size();
    }

    @Override
    public boolean contains(State state) {
        return this.set.contains(state);

    }

    @Override
    public String getName() {
        return String.format("best-first search using %s", this.heuristic.toString());
    }
}

    class FrontierBFS
            implements Frontier
    {
        private final ArrayDeque<State> queue = new ArrayDeque<>(65536);
        private final HashSet<State> set = new HashSet<>(65536);

        @Override
        public void add(State state)
        {
//        System.err.println("[FrontierBFS] add state " + state.toString());
            this.queue.addLast(state);
            this.set.add(state);
        }

        @Override
        public State pop()
        {
            State state = this.queue.pollFirst();
            this.set.remove(state);
            return state;
        }

        @Override
        public boolean isEmpty()
        {
            return this.queue.isEmpty();
        }

        @Override
        public int size()
        {
            return this.queue.size();
        }

        @Override
        public boolean contains(State state)
        {
            return this.set.contains(state);
        }

        @Override
        public String getName()
        {
            return "breadth-first search";
        }
    }

    class FrontierDFS
            implements Frontier
    {
        private final ArrayDeque<State> stack = new ArrayDeque<>(65536);;
        private final HashSet<State> set = new HashSet<>(65536);

        @Override
        public void add(State state)
        {
//        System.err.println("[FrontierDFS] add state " + state.toString());

            this.stack.addLast(state);
            this.set.add(state);

        }

        @Override
        public State pop()
        {
            State state = this.stack.pollLast();
            this.set.remove(state);
            return state;

            //throw new NotImplementedException();
        }

        @Override
        public boolean isEmpty()
        {
            //return this.stack.isEmpty();
            return this.stack.isEmpty();
            //throw new NotImplementedException();
        }

        @Override
        public int size()
        {
            return this.stack.size();

            //throw new NotImplementedException();
        }

        @Override
        public boolean contains(State state)
        {
            return this.set.contains(state);

            //throw new NotImplementedException();
        }

        @Override
        public String getName()
        {
            return "depth-first search";
        }
    }



