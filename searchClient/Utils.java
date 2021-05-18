package searchClient;

import domain.Location;

import java.util.UUID;

public class Utils {


    public static int getManhattanDistance(Location location1, Location location2){
        return Math.abs(location1.getCol()- location2.getCol())
                + Math.abs(location1.getRow())- location2.getRow();
    };

    public static String getUUID32(){

        return UUID.randomUUID().toString().replace("-", "").toLowerCase();

    }

    public static void main(String[] args) {
        System.out.printf("UUID " + Utils.getUUID32());
    }

}
