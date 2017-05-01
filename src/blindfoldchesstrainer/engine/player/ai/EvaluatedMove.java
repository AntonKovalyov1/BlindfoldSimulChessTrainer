/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine.player.ai;

import blindfoldchesstrainer.engine.board.Move;

/**
 *
 * @author Anton
 */
public class EvaluatedMove {
    private final Move move;
    private int value;

    public EvaluatedMove(final Move move, int value) {
        this.move = move;
        this.value = value;
    }

    /**
     * @return the move
     */
    public Move getMove() {
        return move;
    }

    /**
     * @return the value
     */
    public int getValue() {
        return value;
    }

    /**
     * @param value the value to set
     */
    public void setValue(int value) {
        this.value = value;
    }
}
