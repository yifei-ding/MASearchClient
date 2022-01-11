## Description
* Conflict-based search and Rolling-Horizon Collision Resolution (RHCR) for solving the multi-agent Sokoban tasks
* Reference: https://arxiv.org/pdf/2005.07371.pdf

# MASearchClient
Run **build_and_run.sh** to build and run the client

## Compiling the searchclient
> javac searchClient/SearchClient.java
### Try also adding these if change of some code is not applied:
* javac searchClient/SearchClient.java
* javac searchClient/TaskHandler.java
* javac searchClient/Frontier.java
* javac searchClient/State.java
* ... 


## Run a single level
> java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600  

## Run a single level and write a log
The folder 'logs' needs to be created beforehand.
The log file cannot be overwritten. So file name should not already exist.
> java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 -o "../logs/example.log"

## Levels and server.jar should be put parallel to root folder

## To zip the log files: "../solvedlevels" should be the folder of maps

java -jar ../server.jar -c "java -Xmx6g searchClient.SearchClient" -l ../solvedlevels -t 180 -o "MAG18-"$(date "+%H%M%S")".zip"

