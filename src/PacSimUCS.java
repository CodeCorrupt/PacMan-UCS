import pacsim.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.PriorityQueue;
import java.util.Queue;

/**
 *
 * @author glinosd
 */
public class PacSimUCS implements PacAction {

    private Queue<PacFace> moves;

    public PacSimUCS(String fname) {
        PacSim sim = new PacSim( fname );
        sim.init(this);
    }

    public static void main( String[] args ) {

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

        PacCell[][] grid = (PacCell[][]) state;
        PacmanCell pc = PacUtils.findPacman( grid );
        PacFace newFace = null;

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
        if (moves.peek() == null) {
            moves = UniformCostUtils.generateMoves(grid);
            printPath(moves);
        }

        return moves.poll();
    }

    public void printPath(Queue<PacFace> faces) {
        System.out.println("I should probably make this print the moves");
    }
}

class UniformCostUtils {
    public static Queue<PacFace> generateMoves(PacCell[][] grid) {
        //Get a list of all food locations
        ArrayList<Target> foods = findFood(grid);

        // Init the fringe
        PriorityQueue<Fringe> fringe = new PriorityQueue<>();
        ArrayList<Point> start_list = new ArrayList<Point>();
        start_list.add(PacUtils.findPacman(grid).getLoc());
        fringe.add(new Fringe(start_list, foods));

        // Kick off UCS
        Fringe answer = UCS(grid, fringe);

        return null;
    }

    private static ArrayList<Target> findFood(PacCell[][] grid) {
        // Holder for list of points
        ArrayList<Target> foods = new ArrayList<>();
        // Iterate over each cell, if it's food then add the location to the list
        for (PacCell[] row : grid) {
            for (PacCell cell : row) {
                if (cell instanceof FoodCell)
                    foods.add(new Target(cell.getLoc(), false)); //Add to the list
            }
        }
        return foods;
    }

    private static Fringe UCS(PacCell[][] grid, PriorityQueue<Fringe> fringe) {
        while (true) {
            Fringe first = fringe.poll();
            // Check for empty array
            if (first == null) {
                System.out.println("There was an error. Fringe is empty without goal");
                return null;
            }
            if (first.goalTest()) //If the fringe compleats the goal then return the fringe
                return first;
            // For each unvisited food, add that to the fringe
            for (Target trgt : first.getTargets()) {
                if (!trgt.visited) {
                    ArrayList<Point> newPath = first.getOrder();
                    newPath.addAll(pathTo(grid, first.getOrder().get(first.getOrder().size()-1), trgt.loc));
                    ArrayList<Target> newTargets = first.getTargets();
                    newTargets.set(first.getTargets().indexOf(trgt), new Target(trgt.loc, true));
                    fringe.add(new Fringe(newPath, newTargets));
                }
            }
        }
    }

    private static ArrayList<Point> pathTo(PacCell[][] grid, Point a, Point b) {
        //Setup ArrayList to hold the path
        ArrayList<Point> path = new ArrayList<>();
        PacFace lastFace = PacFace.E; // Arbitrary first face

        while (!(path.size() == 0 ? a : path.get(path.size() - 1)).equals(b)) {
            PacFace nextFace = PacUtils.euclideanShortestToTarget(
                    (path.size() == 0 ? a : path.get(path.size() - 1)) , lastFace, b, grid);
            if (nextFace == null) {     //    ^v^v I've hard coded the East face in here because it does not matter.
                nextFace = PacUtils.reverse(lastFace); // The code simply uses the face to determine if pacman has to revese
            }
            lastFace = nextFace;
            path.add(ptAndFaceToPt((path.size() == 0 ? a : path.get(path.size() - 1)), nextFace)); //Add the new point to the list
        }

        return path;
    }

    public static PacFace twoPtToFace(Point a, Point b) {
        return null;
    }

    public static Point ptAndFaceToPt (Point start, PacFace face) {
        Point retPt = new Point(start);
        switch (face) {
            case N: retPt.translate(  0, -1 ); break;
            case E: retPt.translate(  1,  0 ); break;
            case S: retPt.translate(  0,  1 ); break;
            case W: retPt.translate( -1,  0 ); break;
        }
        return retPt;
    }
}

// Stores the target sell (food) and weather it has been visited (eaten) yet
class Target {
    public Point loc;
    public boolean visited;
    public Target(Point loc, boolean visited) {
        this.loc = loc;
        this.visited = visited;
    }
}

// Used as a priority queue, this will store the list of target points and the cost through them
class Fringe implements Comparable<Fringe> {
    private final ArrayList<Point> order;
    private final int cost;
    private final ArrayList<Target> targets;

    public Fringe(ArrayList<Point> order, ArrayList<Target> targets) {
        this.order = order;
        this.cost = order.size();
        this.targets = targets;
    }

    public ArrayList<Point> getOrder() {
        return order;
    }

    public ArrayList<Target> getTargets() {
        return targets;
    }

    public boolean goalTest() {
        for (Target tgt : targets) {
            if (tgt.visited == false)
                return false;
        }
        return true;
    }

    @Override
    public int compareTo(Fringe other) {
        return Integer.valueOf(cost).compareTo(other.cost);
    }
}