#!/bin/bash


# Compiling the searchclient:
javac searchClient/SearchClient.java
javac searchClient/TaskHandler.java
javac searchClient/Frontier.java

javac searchClient/State.java
javac searchClient/HighLevelState.java

javac searchClient/HighLevelSolver.java
javac searchClient/LowLevelSolver.java

javac data/InMemoryDataSource.java



# Starting the server using the searchclient:
# The searchclient uses the BFS strategy by default.
# for observing different maps

java -jar ../server.jar -l ../19levels/MAMKM.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

# java -jar ../server.jar -l ../testlevels/SAtowersOfHoChiMinh03.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

 # java -jar ../server.jar -l ../levels/MAAIMAS.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600  

# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 -o "../logs/example.log"

# java -jar ../server.jar -l ../levels/MAbispebjerg.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600  

# java -jar ../server.jar -l ../levels/SAtowersOfHoChiMinh03.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 
# java -jar ../server.jar -l ../levels/SAtowersOfSaigon03.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 

# java -jar ../server.jar -l ../levels/MAExample.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAchallenge.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/SAsoko1_16_testObstacle.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/MAsimple2_2_testConflict_withBox4.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 180

# java -jar ../server.jar -l ../MAGroupName.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 180

# java -jar ../server.jar -l ../levels/SAmicromouseContest2011.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 


# java -jar ../server.jar -l ../levels/MAthomasAppartment_bluepurple.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 180

# java -jar ../server.jar -l ../levels/SAsoko1_16_unsovlable.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 180

# java -jar ../server.jar -l ../levels/MAthomasAppartment_redpurple.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 180




# java -jar ../server.jar -l ../levels/MAexample.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 


# java -jar ../server.jar -l ../levels/MAsimple2_3.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/MAsimple2_1_easy.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAsimple2_1_easy_medium.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600
# java -jar ../server.jar -l ../levels/MAsimple2_1_easy_small.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600


