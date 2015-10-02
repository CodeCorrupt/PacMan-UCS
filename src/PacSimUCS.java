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
        ArrayList<Direction> directions = UCS(grid, fringe);
        directions.remove(0); //Remove first element because it's pacmans current location
        // Queue for moves.
        Queue<PacFace> moves = new LinkedList<>();
        //Read directions intto moves queue
        for (Direction direction : directions) {
            moves.add(direction.face);
        }
        return moves;
    }

    public static ArrayList<Direction> pathTo(PacCell[][] grid, Point a, Point b, PacFace face) {
        //Establish fringe
        PriorityQueue<FringeElement> fringe = new PriorityQueue<>();
        //Establish fringe
        ArrayList<Direction> directions = new ArrayList<>();
        directions.add(new Direction(a, face));
        fringe.add(new FringeElement(directions, null));
        // boolean array to hold visited pts
        boolean[][] visited = new boolean[grid.length][grid[0].length];
        for (boolean[] col : visited) {
            for (boolean cell : col) {
                cell = false;
            }
        }
        while (true) {
            FringeElement toExpand = fringe.poll();
            //Check if the cell we're expanding has reached the goal
            if (toExpand.directions.get(toExpand.directions.size() - 1).pt.equals(b)) {
                toExpand.directions.remove(0);
                return toExpand.directions;
            }

            Direction nDir = null;
            Direction lDir = toExpand.directions.get(toExpand.directions.size() - 1);

            //Check North
            nDir = new Direction(new Point(lDir.pt.x, lDir.pt.y - 1), PacFace.N);
            if (!(grid[nDir.pt.x][nDir.pt.y] instanceof WallCell)
                    && !visited[nDir.pt.x][nDir.pt.y]) {
                ArrayList<Direction> newDirections = new ArrayList<>(toExpand.directions);
                newDirections.add(nDir); //Add new direction to new list
                fringe.add(new FringeElement(newDirections, null));
                visited[nDir.pt.x][nDir.pt.y] = true;
            }

            //Check South
            nDir = new Direction(new Point(lDir.pt.x, lDir.pt.y + 1), PacFace.S);
            if (!(grid[nDir.pt.x][nDir.pt.y] instanceof WallCell)
                    && !visited[nDir.pt.x][nDir.pt.y]) {
                ArrayList<Direction> newDirections = new ArrayList<>(toExpand.directions);
                newDirections.add(nDir); //Add new direction to new list
                fringe.add(new FringeElement(newDirections, null));
                visited[nDir.pt.x][nDir.pt.y] = true;
            }

            //Check East
            nDir = new Direction(new Point(lDir.pt.x + 1, lDir.pt.y), PacFace.E);
            if (!(grid[nDir.pt.x][nDir.pt.y] instanceof WallCell)
                    && !visited[nDir.pt.x][nDir.pt.y]) {
                ArrayList<Direction> newDirections = new ArrayList<>(toExpand.directions);
                newDirections.add(nDir); //Add new direction to new list
                fringe.add(new FringeElement(newDirections, null));
                visited[nDir.pt.x][nDir.pt.y] = true;
            }

            //Check West
            nDir = new Direction(new Point(lDir.pt.x - 1, lDir.pt.y), PacFace.W);
            if (!(grid[nDir.pt.x][nDir.pt.y] instanceof WallCell)
                    && !visited[nDir.pt.x][nDir.pt.y]) {
                ArrayList<Direction> newDirections = new ArrayList<>(toExpand.directions);
                newDirections.add(nDir); //Add new direction to new list
                fringe.add(new FringeElement(newDirections, null));
                visited[nDir.pt.x][nDir.pt.y] = true;
            }
        }
    }

    public static ArrayList<Direction> UCS(PacCell[][] grid, Fringe fringe) {
        boolean keepGoing; //Has to be true to start loop
        while (true) {
            keepGoing = false; //Assume we're done until proven otherwise
            //Take first element off fringe
            FringeElement toExpand = fringe.dequeueNext();
            ArrayList<Food> lFoods = toExpand.foods;
            // Add the path to EACH of the remaining foods to the fringe
            for (Food fud : lFoods) {
                if (fud.visited == false) {
                    keepGoing = true; // If we had to expand a food then we haven't reached goal state
                    // ArrayList to store new directions
                    ArrayList<Direction> nDirs = new ArrayList<>(toExpand.directions);
                    // Get the last direction to pass to pathTo
                    Direction lDir = toExpand.directions.get(toExpand.directions.size() - 1);
                    nDirs.addAll(pathTo(grid, lDir.pt, fud.loc,lDir.face));
                    // create new food list to store visited
                    ArrayList<Food> nFoods = new ArrayList<>(lFoods);
                    nFoods.set(lFoods.indexOf(fud), new Food(fud.loc, true)); //set food to visited
                    //Add new fringe element to fringe
                    FringeElement nFringe = new FringeElement(nDirs, nFoods);
                    fringe.add(nFringe);
                }
            }
            if (keepGoing == false) {
                return toExpand.directions;
            }
        }
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