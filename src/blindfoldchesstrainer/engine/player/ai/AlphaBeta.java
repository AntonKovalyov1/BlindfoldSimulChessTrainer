/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine.player.ai;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.player.MoveTransition;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.RecursiveTask;

/**
 *
 * @author Anton
 */
public class AlphaBeta extends RecursiveTask implements MoveStrategy {
    
    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    private final Board board;
    
    public AlphaBeta(final Board board, final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.board = board;
        this.searchDepth = searchDepth;
    }

    @Override
    public Move compute() {
        return execute(getBoard());
    }

    @Override
    public Move execute(Board board) {

        Move bestMove = null;

        int highestSeenValue = Integer.MIN_VALUE;
        int lowestSeenValue = Integer.MAX_VALUE;
        int currentValue;

        System.out.println(board.currentPlayer() + "Thinking with depth = " + searchDepth);
        List<Move> legalMoves = new ArrayList<>(board.currentPlayer().getLegalMoves());
        Collections.shuffle(legalMoves);
        for(final Move move : legalMoves) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if(moveTransition.getMoveStatus().isDone()) {
                currentValue = board.currentPlayer().getAlliance().isWhite() ?
                        alphabeta(moveTransition.getTransitionBoard(), 
                                  getSearchDepth() - 1, 
                                  Integer.MIN_VALUE, 
                                  Integer.MAX_VALUE, 
                                  false) :
                        alphabeta(moveTransition.getTransitionBoard(), 
                                  getSearchDepth() - 1, 
                                  Integer.MIN_VALUE, 
                                  Integer.MAX_VALUE,
                                  true);
                if(board.currentPlayer().getAlliance().isWhite() && currentValue >= highestSeenValue) {
                    highestSeenValue = currentValue;
                    bestMove = move;
                }
                else if(board.currentPlayer().getAlliance().isBlack() && currentValue <= lowestSeenValue){
                    lowestSeenValue = currentValue;
                    bestMove = move;
                }
                try {
                    Thread.sleep(0);
                }
                catch (InterruptedException ex) {
                    return bestMove;
                }
            }
        }
        return bestMove;
    }
    
    
    private int alphabeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || isEndGameScenario(board))
            return getBoardEvaluator().evaluate(board, depth);
        List<Move> legalMoves = new ArrayList<>(board.currentPlayer().getLegalMoves());
        Collections.shuffle(legalMoves);
        if (maximizingPlayer) {
            int v = Integer.MIN_VALUE;
            for (Move move : board.currentPlayer().getLegalMoves()) {
                final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
                if (moveTransition.getMoveStatus().isDone()) {
                    v = Integer.max(v, alphabeta(move.execute(), depth - 1, alpha, beta, false));
                    alpha = Integer.max(alpha, v);
                    if (beta <= alpha)
                        break; // Beta cut-off
                }
            }
            return v;
        }
        int v = Integer.MAX_VALUE;
        for (Move move : board.currentPlayer().getLegalMoves()) {
            final MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone()) {
                v = Integer.min(v, alphabeta(move.execute(), depth - 1, alpha, beta, true));
                beta = Integer.min(beta, v);
                if (beta <= alpha)
                    break; // alpha cut-off
            }
        }
        return v;
    }   
    
    private boolean isEndGameScenario(Board board) {
        return board.currentPlayer().isInCheckMate() ||
               board.currentPlayer().isInStalemate();
    }
    
    @Override
    public String toString() {
        return "MiniMax + AlpaBeta";
    }

    /**
     * @return the boardEvaluator
     */
    public BoardEvaluator getBoardEvaluator() {
        return boardEvaluator;
    }

    /**
     * @return the searchDepth
     */
    public int getSearchDepth() {
        return searchDepth;
    }

    /**
     * @return the board
     */
    public Board getBoard() {
        return board;
    }
}
