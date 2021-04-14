package service;

import data.InMemoryDataSource;
import domain.*;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class SearchClient
{   private static String mapName;
    private static InMemoryDataSource data;

    public static void parseLevel(BufferedReader serverMessages)
            throws IOException
    {
        // We can assume that the level file is conforming to specification, since the server verifies this.
        // Read domain
        serverMessages.readLine(); // #domain
        serverMessages.readLine(); // hospital

        // Read Level name
        serverMessages.readLine(); // #levelname
        mapName = serverMessages.readLine(); // <name>
        System.err.println("[SearchClient] Read map: "+ mapName);

        // Read colors
        serverMessages.readLine(); // #colors
        Color[] agentColors = new Color[10];
        Color[] boxColors = new Color[26];
        String line = serverMessages.readLine();
        while (!line.startsWith("#"))
        {
            String[] split = line.split(":");
            Color color = Color.fromString(split[0].strip());
            String[] entities = split[1].split(",");
            for (String entity : entities)
            {
                char c = entity.strip().charAt(0);
                if ('0' <= c && c <= '9')
                {
                    agentColors[c - '0'] = color;

                }
                else if ('A' <= c && c <= 'Z')
                {
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
        while (!line.startsWith("#"))
        {
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
        for (int row = 0; row < numRows; ++row)
        {
            line = levelLines.get(row);
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if ('0' <= c && c <= '9')
                {
                    agentRows[c - '0'] = row;
                    agentCols[c - '0'] = col;
                    ++numAgents;
                }
                else if ('A' <= c && c <= 'Z')
                {
                    boxes[row][col] = c;
                }
                else if (c == '+')
                {
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
        while (!line.startsWith("#"))
        {
            for (int col = 0; col < line.length(); ++col)
            {
                char c = line.charAt(col);

                if (('0' <= c && c <= '9') || ('A' <= c && c <= 'Z'))
                {
                    goals[row][col] = c;
                }
            }

            ++row;
            line = serverMessages.readLine();
        }

        // End
        // line is currently "#end"

        //store to data
        int agentSize = agentRows.length;
        for (int i = 0; i < agentSize; i ++ )
        {
            int id = i;
            Color color = agentColors[i];
            int agentRow = agentRows[i];
            int agentCol = agentCols[i];
            Location location = new Location(agentRow,agentCol);
            Agent agent = new Agent(id, color, location);
            data.addAgent(agent);
            data.setDynamicMap(location,agent);
        }

        int boxId=0;
        for (int boxRow = 0; boxRow < boxes.length; boxRow ++){
            for (int boxCol=0; boxCol < boxes[boxRow].length; boxCol++){
                if (boxes[boxRow][boxCol] != 0){
                    char boxChar = boxes[boxRow][boxCol];
                    String boxName = String.valueOf(boxChar);
                    Color color = boxColors[boxChar - 65];
                    //add box color together with location
                    Location location = new Location(boxRow,boxCol);
                    Box box = new Box(boxId, boxName, color, location);
                    data.addBox(box);
                    data.setDynamicMap(location,box);
                    boxId++;
                }

            }
        }

        int goalId=0;
        for (int goalRow = 0; goalRow < goals.length; goalRow++){
            for (int goalCol=0; goalCol < goals[goalRow].length;goalCol++){
                if (goals[goalRow][goalCol] > 0){
                    //System.err.println("In getGoalsMap: " + this.goals[row][col]);
                    char goalChar = goals[goalRow][goalCol];
                    String goalName = String.valueOf(goalChar);

                    Location location = new Location(goalRow,goalCol);
                    Goal goal = new Goal(goalId, goalName, location);
                    data.addGoal(goal);
                    data.setStaticMap(location,goal);
                    goalId++;
                }

            }
        }

        for (int i = 0; i < walls.length; i++){
            for (int j=0; j < walls[i].length;j++){
                    Location location = new Location(i,j);
                    Wall wall = new Wall(location, walls[i][j]);
                    data.setStaticMap(location, wall);

            }
        }


    }


    public Action[][] search()
    {
        HighLevelSolver highLevelSolver = new HighLevelSolver(data);

        return highLevelSolver.solve();
        //return null;
    }


    public static void main(String[] args)
            throws IOException
    {

        // Send client name to server. Don't remove. TODO: change to group name
        System.out.println("SearchClient");

        System.err.println("[SearchClient] Start");

        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));

        SearchClient searchClient = new SearchClient();
        data = InMemoryDataSource.getInstance();
        SearchClient.parseLevel(serverMessages);

        TaskHandler taskHandler = new TaskHandler(data);
        taskHandler.assignTask();

        // Search for a plan.
        Action[][] plan;
        try
        {
            plan = searchClient.search();
        }
        catch (OutOfMemoryError ex)
        {
            System.err.println("[SearchClient] Maximum memory usage exceeded.");
            plan = null;
        }

        // Print plan to server.
        if (plan == null)
        {
            System.err.println("[SearchClient] Unable to solve level.");
            System.exit(0);

        }
        else
        {
            System.err.format("[SearchClient] Found solution of length %,d.\n", plan.length);

            for (Action[] jointAction : plan)
            {
                System.out.print(jointAction[0].name);
                for (int action = 1; action < jointAction.length; ++action)
                {
                    System.out.print("|");
                    System.out.print(jointAction[action].name);
                }
                System.out.println();

                // We must read the server's response to not fill up the stdin buffer and block the server.
                serverMessages.readLine();
            }
        }
    }




}
