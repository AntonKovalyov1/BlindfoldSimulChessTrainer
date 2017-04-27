package blindfoldchesstrainer.engine.player.ai;

import blindfoldchesstrainer.engine.ai.*;
import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;

/**
 * Created by Anton on 3/13/2017.
 */
public interface MoveStrategy {

    Move execute(Board board);

}
