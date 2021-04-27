#!/bin/bash


# Compiling the searchclient:
javac searchClient/SearchClient.java
javac searchClient/TaskHandler.java
javac searchClient/Frontier.java

javac searchClient/State.java
javac searchClient/HighLevelState.java

javac searchClient/HighLevelSolver.java
javac searchClient/LowLevelSolver.java



# Starting the server using the searchclient:
# The searchclient uses the BFS strategy by default.
# for observing different maps


# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 -o "../logs/example.log"

# java -jar ../server.jar -l ../levels/MAbispebjerg.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600  

# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 

# java -jar ../server.jar -l ../levels/MAexample.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 


# java -jar ../server.jar -l ../levels/MAsimple2_3.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

# java -jar ../server.jar -l ../levels/MAsimple2_1.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

java -jar ../server.jar -l ../levels/MAthomasAppartment_bluepurple.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

