package domain;

import java.util.ArrayList;
import java.util.Objects;

public class Location {
    private int row;
    private int col;

    public Location(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getCol() {
        return col;
    }

    public void setCol(int col) {
        this.col = col;
    }

    public Location getUpNeighbour(){
        return new Location(row-1, col);
    }
    public Location getDownNeighbour(){
        return new Location(row+1, col);
    }
    public Location getLeftNeighbour(){
        return new Location(row, col-1);
    }
    public Location getRightNeighbour(){
        return new Location(row, col+1);
    }


    /**
     * @return 4 neighbouring locations of a location in an ArrayList
     */
    public ArrayList<Location> getNeighbours(){
        ArrayList<Location> neighbours = new ArrayList<>();
        neighbours.add(this.getUpNeighbour());
        neighbours.add(this.getDownNeighbour());
        neighbours.add(this.getLeftNeighbour());
        neighbours.add(this.getRightNeighbour());
        return neighbours;
    }
    @Override
    public String toString() {
        return "(" + row +
                ", " + col +
                ')';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Location location = (Location) o;
        return row == location.row && col == location.col;
    }

    @Override
    public int hashCode() {
        return Objects.hash(row, col);
    }
}
