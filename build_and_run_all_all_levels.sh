#!/bin/bash


# Compiling the searchclient:
javac searchClient/SearchClient.java
javac searchClient/TaskHandler.java
javac searchClient/Frontier.java

javac searchClient/State.java
javac searchClient/HighLevelState.java

javac searchClient/HighLevelSolver.java
javac searchClient/LowLevelSolver.java

folder=../levels
files=$(ls $folder)
for file in $files
do
  java -jar ../server.jar -l ../levels/$file -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600

done