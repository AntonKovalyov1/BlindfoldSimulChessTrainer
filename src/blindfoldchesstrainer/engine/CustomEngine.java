package blindfoldchesstrainer.engine;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.player.ai.AlphaBeta;

/**
 *
 * @author Anton
 */
public class CustomEngine extends Engine {

    private AlphaBeta ab;
    
    public CustomEngine() {
        super();
    }
    
    @Override
    public boolean start() {
        return true;
    }

    @Override
    public Move executeMove(final int depth, final Board board) {
        ab = new AlphaBeta(board, depth);
        return ab.execute(board);
    }

    @Override
    public void close() {
        //do nothing
    }

    @Override
    public boolean isReady() {
        // Always ready!
        return true;
    }
    
    @Override
    public String toString() {
        return "Garbage";
    }

    @Override
    public void forceMoveExecution() {
        if (ab != null && ab.isRunning()) {
            ab.forceMoveExecution();
        }
    }

    @Override
    public boolean isRunning() {
        return ab.isRunning();
    }
    
}
