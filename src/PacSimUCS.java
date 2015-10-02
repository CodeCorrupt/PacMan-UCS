import pacsim.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.*;

public class PacSimUCS implements PacAction {

    private Queue<PacFace> moves;

    public PacSimUCS(String fname) {
        PacSim sim = new PacSim( fname );
        sim.init(this);
    }

    public static void main( String[] args ) {

        //new PacSimReplan( args[ 0 ] );

        String fname ="";
        if( args.length > 0 ) {
            fname = args[ 0 ];
        }
        else {
            JFileChooser chooser = new JFileChooser(
                    new File("."));
            int result = chooser.showOpenDialog(null);

            if( result != JFileChooser.CANCEL_OPTION ) {
                File file = chooser.getSelectedFile();
                fname = file.getAbsolutePath();
            }
        }
        new PacSimUCS( fname );
    }

    @Override
    public void init() {
        moves = new LinkedList<>();
    }

    @Override
    public PacFace action( Object state ) {
        //Store the grid passed to in
        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman(grid);

        // make sure Pacman is in this game
        if (pc == null) {
            System.out.println("Error: No Pacman on provided grid");
            return null;
        }

        // Check if you won (no food remaining)
        if (!PacUtils.foodRemains(grid)) {
            System.out.println("YOU WIN!");
            return null;
        }

        // If there are no moves in the moves queue then create some
        if (moves == null || moves.peek() == null) {
            moves = UniformCostUtils.generateMoves(grid);
            // If moves is still null then there must have been an error
            if (moves == null || moves.peek() == null) {
                System.out.println("ERROR: Unable to generate moves");
                return null;
            }
        }
        // Return the next move and dequeue it.
        return moves.poll();
    }
}

class UniformCostUtils {
    public static Queue<PacFace> generateMoves(PacCell[][] grid) {
        //Establish fringe
        Fringe fringe = new Fringe(grid);
        ArrayList<Food> foods = findFood(grid);
        Queue<Direction> directions = UCS(grid, fringe);
        return null;
    }

    public static ArrayList<PacFace> pathTo(PacCell[][] grid, Point a, Point b, PacFace face) {
        return null;
    }

    public static Queue<Direction> UCS(PacCell[][] grid, Fringe fringe) {
        return null;
    }

    public static ArrayList<Food> findFood(PacCell[][] grid) {
        // Holder for list of points
        ArrayList<Food> foods = new ArrayList<>();
        // Iterate over each cell, if it's food then add the location to the list
        for (PacCell[] row : grid) {
            for (PacCell cell : row) {
                if (cell instanceof FoodCell)
                    foods.add(new Food(cell.getLoc(), false)); //Add to the list
            }
        }
        return foods;
    }
}

// Stored the fringe and all methods to interact with it
class Fringe {
    private PriorityQueue<FringeElement> fringe;

    public Fringe() {
        fringe = new PriorityQueue<>();
    }

    public Fringe(PacCell[][] grid) {
        fringe = new PriorityQueue<>();
        //Create fringe element for pacman's start position
        ArrayList<Direction> startDir = new ArrayList<>();
        startDir.add(new Direction(PacUtils.findPacman(grid).getLoc(), PacUtils.findPacman(grid).getFace()));
        ArrayList<Food> foods = UniformCostUtils.findFood(grid);
        FringeElement start = new FringeElement(startDir, foods);
        //Finally add new element to fringe
        fringe.add(start);
    }

    public FringeElement dequeueNext() {
        return  fringe.poll();
    }

    public FringeElement lookNext() {
        return fringe.peek();
    }

    public void add(FringeElement element) {
        fringe.add(element);
    }
}

// Class to store the information of fringe elements.
//      Stores the path to that fringe element
//      Stores the cost to that element by calculating the length
class FringeElement implements Comparable<FringeElement> {
    public ArrayList<Direction> directions;
    public int cost;
    public ArrayList<Food> foods;

    public FringeElement (ArrayList<Direction> directions, ArrayList<Food> foods) {
        this.directions = directions;
        this.cost = directions.size();
        this.foods = foods;
    }

    @Override
    public int compareTo(FringeElement other) {
        return Integer.valueOf(cost).compareTo(other.cost);
    }
}

class Direction {
    public Point pt;
    public PacFace face;

    public Direction (Point pt, PacFace face) {
        this.pt = pt;
        this.face = face;
    }
}

// Stores the target sell (food) and weather it has been visited (eaten) yet
class Food {
    public Point loc;
    public boolean visited;
    public Food(Point loc, boolean visited) {
        this.loc = loc;
        this.visited = visited;
    }
}