import com.sun.org.apache.xpath.internal.operations.Bool;
import pacsim.FoodCell;
import pacsim.PacCell;
import pacsim.PacFace;
import pacsim.PacUtils;

import java.util.PriorityQueue;
import java.util.Queue;

import java.awt.*;
import java.util.ArrayList;

public class UniformCostUtils {
    public static Queue<PacFace> generateMoves(PacCell[][] grid) {
        //Get a list of all food locations
        ArrayList<Target> foods = findFood(grid);

        // Init the fringe
        PriorityQueue<Fringe> fringe = new PriorityQueue<>();
        ArrayList<Point> start_list = new ArrayList<Point>();
        start_list.add(PacUtils.findPacman(grid).getLoc());
        fringe.add(new Fringe(start_list, 0, foods));

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
        return null;
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
