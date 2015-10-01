import pacsim.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.LinkedList;
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