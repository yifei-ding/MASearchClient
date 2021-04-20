# MASearchClient
Run **build_and_run.sh** to build and run the client

## Compiling the searchclient
> javac searchClient/SearchClient.java
> try also adding:
javac searchClient/SearchClient.java
javac searchClient/TaskHandler.java
javac searchClient/Frontier.java
javac searchClient/State.java


## Run a single level
> java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600  

## Run a single level and write a log
The folder 'logs' needs to be created beforehand.
The log file cannot be overwritten. So file name should not already exist.
> java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g searchClient.SearchClient" -g -s 500 -t 600 -o "../logs/example.log"

## Levels and server.jar should be put parallel to root folder
