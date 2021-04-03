package service;

import data.InMemoryDataSource;
import domain.Action;
import domain.Color;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.*;

public class SearchClient
{   private static String mapName;

    private AgentManager agentManager;

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
        System.err.println("Read map: "+ mapName);

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

    }


    public Action[][] search()
    {   AgentManager agentManager = AgentManager.getInstance();

        return agentManager.search();
        //return null;
    }
//    public static void writeLog(String mapName, String type, int planLength, String elapsedTime, String remark) throws IOException {
//        File F=new File("log.txt");
//        if(!F.exists()){
//            F.createNewFile();
//        }
//        FileWriter fw=null;
//        SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        Date date = new Date(System.currentTimeMillis());
//        System.out.println(formatter.format(date));
//        String writeLog=formatter.format(date)+" - "+mapName+" - "+ type + " - "+planLength + " - " +  elapsedTime + " - " +remark;
//        try {
//
//            fw=new FileWriter(F, true);
//            System.err.println("Write log successfully");
//            fw.write(writeLog+"\r\n");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally{
//            if(fw!=null){
//                fw.close();
//            }
//        }
//
//    }

    public static void main(String[] args)
            throws IOException
    {

        // Send client name to server. Don't remove. TODO: change to group name
        System.out.println("SearchClient");

        System.err.println("Hello!");
        long startTime = System.nanoTime();
        // Parse the level.
        BufferedReader serverMessages = new BufferedReader(new InputStreamReader(System.in, StandardCharsets.US_ASCII));

        SearchClient searchClient = new SearchClient();

        SearchClient.parseLevel(serverMessages);

        // Search for a plan.
        Action[][] plan;
        try
        {
            plan = searchClient.search();
        }
        catch (OutOfMemoryError ex)
        {
            System.err.println("Maximum memory usage exceeded.");
            plan = null;
        }

        // Print plan to server.
        if (plan == null)
        {
            System.err.println("Unable to solve level.");
            double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
            DecimalFormat df = new DecimalFormat("#.000");
            String elapsedTimeStr = df.format(elapsedTime);
            //SearchClient.writeLog(mapName,"searchType",plan.length, elapsedTimeStr, "fail");
            System.exit(0);

        }
        else
        {
            System.err.format("Found solution of length %,d.\n", plan.length);

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
            double elapsedTime = (System.nanoTime() - startTime) / 1_000_000_000d;
            DecimalFormat df = new DecimalFormat("0.000");
            String elapsedTimeStr = df.format(elapsedTime);
            //SearchClient.writeLog(mapName,"searchType",plan.length, elapsedTimeStr, "baseline");
        }
    }




}
