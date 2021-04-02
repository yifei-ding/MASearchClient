#!/bin/bash


# Compiling the searchclient:
javac service/SearchClient.java

# Starting the server using the searchclient:
# The searchclient uses the BFS strategy by default.
# for observing different maps
# java -jar ../server.jar -l ../complevels/MAaicecubes.lvl -c  "java -Xmx6g service.SearchClient"  -g -s 500 -t 1

# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600 -o "../logs/example.log"

java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600  

# java -jar ../server.jar -l ../levels/SAsoko1_04.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko2_04.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAsoko1_08.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko2_08.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko1_16.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko2_16.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko1_32.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko2_32.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko1_64.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko2_64.lvl -c "java -Xmx6g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko1_128.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko2_128.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAsoko3_08.lvl -c "java -Xmx7g searchclient.SearchClient -greedy" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600

# for exercise 3.2
# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAD2.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAD3.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAfriendofBFS.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600

# for exercise 6.2
# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAD2.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java -Xmx4g searchclient.SearchClient -astar" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAD2.lvl -c "java -Xmx4g searchclient.SearchClient -astar" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAfriendofDFS.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAfriendofBFS.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAfriendofDFS.lvl -c "java -Xmx4g searchclient.SearchClient -astar" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAfriendofBFS.lvl -c "java -Xmx4g searchclient.SearchClient -astar" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAFirefly.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAFirefly.lvl -c "java -Xmx4g searchclient.SearchClient -dfs" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SACrunch.lvl -c "java -Xmx4g searchclient.SearchClient -bfs" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SACrunch.lvl -c "java -Xmx4g searchclient.SearchClient -dfs" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAFirefly.lvl -c "java -Xmx4g searchclient.SearchClient -astar" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SACrunch.lvl -c "java -Xmx4g searchclient.SearchClient -astar" -g -s 500 -t 600

# # exercise 5
# java -jar ../server.jar -l ../levels/SAsoko3_08.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_08.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_16.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_16.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_32.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_32.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAsoko3_32.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/SAsoko3_32.lvl -c "java -Xmx4g searchclient.SearchClient -greedy2" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAsoko3_64.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600

# exercise 7
# java -jar ../server.jar -l ../levels/MAthomasAppartment_blue.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAthomasAppartment_purple.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAthomasAppartment_cyan.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/MAthomasAppartment_red.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/MAthomasAppartment_bluepurple.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAthomasAppartment_bluecyan.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAthomasAppartment_redblue.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAthomasAppartment_bluepurple.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAthomasAppartment_redcyan.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAchallenge.lvl -c "java -Xmx4g searchclient.SearchClient -greedy" -g -s 500 -t 600


# Use arguments -dfs, -astar, -wastar, or -greedy 
# to set alternative search strategies (after you implement them). 
# For instance, to use DFS on the same level as above:
# java -jar ../server.jar -l ../levels/SAD1.lvl -c "java searchclient.SearchClient -dfs" -g -s 150 -t 180