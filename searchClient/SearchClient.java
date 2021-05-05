package searchClient;

import data.InMemoryDataSource;
import domain.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SearchClient {
    private static String mapName;
    private static InMemoryDataSource data;
    private HashMap<Integer, Goal> allGoals = new HashMap<Integer, Goal>();

    public static void readMap(BufferedReader serverMessages)
            throws IOException {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        mapName = serverMessages.readLine(); // <name>
        System.err.println("[SearchClient] Read map: " + mapName);

        // Read colors
        serverMessages.readLine(); // #colors
        Color[] agentColors = new Color[10];
        Color[] boxColors = new Color[26];
        String line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities) {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9') {
                    agentColors[c - '0'] = color;

                } else if ('A' <= c && c <= 'Z') {
                    boxColors[c - 'A'] = color;
                }
            }
            line = serverMessages.readLine();
        }

        // Read initial state
        // line is currently "#initial"
        int numRows = 0;
        int numCols = 0;
        ArrayList<String> levelLines = new ArrayList<>(64);
        line = serverMessages.readLine();
        while (!line.startsWith("#")) {
            levelLines.add(line);
            numCols = Math.max(numCols, line.length());
            ++numRows;
            line = serverMessages.readLine();
        }
        int numAgents = 0;
        int[] agentRows = new int[10];
        int[] agentCols = new int[10];
        boolean[][] walls = new boolean[numRows][numCols];
        char[][] boxes = new char[numRows][numCols];
        for (int row = 0; row < numRows; ++row) {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9') {
                    agentRows[c - '0'] = row;
                    agentCols[c - '0'] = col;
                    ++numAgents;
                } else if ('A' <= c && c <= 'Z') {
                    boxes[row][col] = c;
                } else if (c == '+') {
                    walls[row][col] = true;
                }
            }
        }
        agentRows = Arrays.copyOf(agentRows, numAgents);
        agentCols = Arrays.copyOf(agentCols, numAgents);

        // Read goal state
        // line is currently "#goal"
        char[][] goals = new char[numRows][numCols];
        line = serverMessages.readLine();
        int row = 0;
        while (!line.startsWith("#")) {
            for (int col = 0; col < line.length(); ++col) {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z')) {
                    goals[row][col] = c;
                }
            }

            ++row;
            line = serverMessages.readLine();
        }

        // End
        // line is currently "#end"

        //store to all info on the map to ImMemoryDataSource
//        boolean[][] staticMap = walls;
//        int[][] dynamicMap = new int[numRows][numCols];
        data.setCol(numCols);
        data.setRow(numRows);

        /**
         * Store agents
         */
        int agentSize = agentRows.length;
        for (int i = 0; i < agentSize; i++) {
            int id = i;
            Color color = agentColors[i];
            int agentRow = agentRows[i];
            int agentCol = agentCols[i];
            Location location = new Location(agentRow, agentCol);
            Agent agent = new Agent(id, color, location);
            data.addAgent(agent);
            data.setDynamicMap(location, agent);
        }

        int boxId = 0;
        for (int boxRow = 0; boxRow < boxes.length; boxRow++) {
            for (int boxCol = 0; boxCol < boxes[boxRow].length; boxCol++) {
                if (boxes[boxRow][boxCol] != 0) {
                    char boxChar = boxes[boxRow][boxCol];
                    String boxName = String.valueOf(boxChar);
                    Color color = boxColors[boxChar - 65];
                    //add box color together with location
                    Location location = new Location(boxRow, boxCol);
                    Box box = new Box(boxId, boxName, color, location);
                    data.addBox(box);
                    data.setDynamicMap(location, box);
                    boxId++;
                }

            }
        }

        int goalId = 0;
        for (int goalRow = 0; goalRow < goals.length; goalRow++) {
            for (int goalCol = 0; goalCol < goals[goalRow].length; goalCol++) {
                if (goals[goalRow][goalCol] > 0) {
                    //System.err.println("In getGoalsMap: " + this.goals[row][col]);
                    char goalChar = goals[goalRow][goalCol];
                    String goalName = String.valueOf(goalChar);

                    Location location = new Location(goalRow, goalCol);
                    Goal goal = new Goal(goalId, goalName, location, -1, false);
                    data.addGoal(goal);
                    data.setStaticMap(location, goal);
                    goalId++;
                }

            }
        }

        for (int i = 0; i < walls.length; i++) {
            for (int j = 0; j < walls[i].length; j++) {
                Location location = new Location(i, j);
                Wall wall = new Wall(location, walls[i][j]);
                data.setWallMap(location, wall);
                if (data.getStaticMap().get(location) == null) //If the location is already stored as a goal, don't overwrite
                    data.setStaticMap(location, wall);

            }
        }


    }


    public static void setGoalOrder() {
        ArrayList<Goal> degree1Goals = new ArrayList<Goal>();
        ArrayList<Goal> degree2Goals = new ArrayList<Goal>();

        HashMap<Location, Integer> StaticdegreeMap = data.getDegreeMap();
        HashMap<Integer, Goal> allGoals = data.getAllGoals();
        Iterator<Integer> iterator = allGoals.keySet().iterator();
        while (iterator.hasNext()) {
            int goalId = iterator.next();
            Goal goal = allGoals.get(goalId);
            Location location = goal.getLocation();
            int degreenum = StaticdegreeMap.get(location);
            if (degreenum == 1) {
                degree1Goals.add(goal);
            } else if (degreenum == 2) {
                degree2Goals.add(goal);

            }

        }
        for (Goal goal : degree1Goals) {// for solving the goal in the bottom
            HashSet<Location> exploredPath = new HashSet<Location>();
            exploredPath.add(goal.getLocation());
            Location currentLocation = goal.getLocation();
            int previousGoalId = goal.getId(); // initially goalId
            do {
                ArrayList<Location> fourDirections = new ArrayList<Location>(); // to explore the 4 neighbors
                int i = currentLocation.getRow();
                int j = currentLocation.getCol();
                Location locationUp = new Location(i, j + 1);
                Location locationDown = new Location(i, j - 1);
                Location locationLeft = new Location(i - 1, j);
                Location locationRight = new Location(i + 1, j);
                fourDirections.add(locationUp);
                fourDirections.add(locationDown);
                fourDirections.add(locationLeft);
                fourDirections.add(locationRight);
                for (Location location : fourDirections) {
                    if (StaticdegreeMap.get(location) != null) {
                        if (StaticdegreeMap.get(location) == 2 && !exploredPath.contains(location)) {// that means this is the next cell
                            currentLocation = location; //update location
                            exploredPath.add(currentLocation);
                            //set pervious goal ID
                            if (allGoals.get(location) != null) {//use the cell location to check if there has goal.
                                Goal newGoal = allGoals.get(location);
                                newGoal.setPerviousGoalId(previousGoalId);//set previous goal
                                previousGoalId = newGoal.getId(); //update the goal Id
                            }
                        }
                    }
                }
            } while (StaticdegreeMap.get(currentLocation) != 3);
        }
        // TODO: for degree2 goals
    }


    public Action[][] search() {
        HighLevelSolver highLevelSolver = new HighLevelSolver(data);

        return highLevelSolver.solve();
//        System.err.println("[SearchClient] Skip highlevel to test low level");
//        return null;

    }


    public static void testLowLevel(InMemoryDataSource data) {
        LowLevelSolver.solveForAllAgents(new HashSet<>());

    }

    public static void main(String[] args)
            throws IOException {

        // Send client name to server. Don't remove. TODO: change to group name
        System.out.println("SearchClient");

        System.err.println("[SearchClient] Start");

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));

        SearchClient searchClient = new SearchClient();
        data = InMemoryDataSource.getInstance();
        SearchClient.readMap(serverMessages);
        SearchClient.setGoalOrder();
        TaskHandler taskHandler = TaskHandler.getInstance();
        taskHandler.assignTask2();
        SearchClient.testLowLevel(data);
//        System.err.println("[SearchClient]: all boxes " + data.getAllBoxes().toString());

        // Search for a plan.
        Action[][] plan;
        try {
            plan = searchClient.search(); // todo: Is this mean we should return all actions at once?
        } catch (OutOfMemoryError ex) {
            System.err.println("[SearchClient] Maximum memory usage exceeded.");
            plan = null;
        }

        // Print plan to server.
        if (plan == null) {
            System.err.println("[SearchClient] Unable to solve level.");
            System.exit(0);

        } else {
            System.err.format("[SearchClient] Found solution of length %,d.\n", plan.length);

            int max = 0;
            for (int i = 0; i < plan.length; i++) { //i=timestep
                if (plan[i].length > max)
                    max = plan[i].length;
            }
            System.err.println("[SearchClient] Max plan length = " + max);

            for (int i = 0; i < max; i++) { //i=timestep
                for (int j = 0; j < plan.length; j++) { //j=agent
                    if (i < plan[j].length) {
                        if (j == 0)
                            System.out.print(plan[j][i].name);
                        else {
                            System.out.print("|"); //when print to server, actions after the first action need a "|" in front
                            System.out.print(plan[j][i].name);
                        }
                    } else {
                        if (j == 0)
                            System.out.print("NoOp");
                        else {
                            System.out.print("|"); //when print to server, actions after the first action need a "|" in front
                            System.out.print("NoOp");
                        }
                    }

                }
                System.out.println();

            }


            for (int i = 0; i < max; i++) { //i=timestep
                for (int j = 0; j < plan.length; j++) { //j=agent
                    if (i < plan[j].length) {
                        if (j == 0)
                            System.err.print(plan[j][i].name);
                        else {
                            System.err.print("|"); //when print to server, actions after the first action need a "|" in front
                            System.err.print(plan[j][i].name);
                        }
                    } else {
                        if (j == 0)
                            System.err.print("NoOp");
                        else {
                            System.err.print("|"); //when print to server, actions after the first action need a "|" in front
                            System.err.print("NoOp");
                        }
                    }

                }
                System.err.println();

            }

            // We must read the server's response to not fill up the stdin buffer and block the server.
            serverMessages.readLine();
        }
    }
}


