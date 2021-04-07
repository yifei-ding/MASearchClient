#!/bin/bash


# Compiling the searchclient:
javac service/SearchClient.java

# Starting the server using the searchclient:
# The searchclient uses the BFS strategy by default.
# for observing different maps


# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600 -o "../logs/example.log"

# java -jar ../server.jar -l ../levels/MAbispebjerg.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600  

# java -jar ../server.jar -l ../levels/SAsoko3_04.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600 

# java -jar ../server.jar -l ../levels/MAexample.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600 

# java -jar ../server.jar -l ../levels/MAthomasAppartment_redbluepurple.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600 

java -jar ../server.jar -l ../levels/MAsimple2.lvl -c "java -Xmx6g service.SearchClient" -g -s 500 -t 600 

# folder=../levels
# files=$(ls $folder)
# for file in $files
# do
#   java -jar ../server.jar -l ../levels/$file -c "java -Xmx6g service.SearchClient" -g -s 500 -t 1

# done
