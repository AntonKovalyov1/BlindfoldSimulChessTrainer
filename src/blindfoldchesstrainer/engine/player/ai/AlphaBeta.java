/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine.player.ai;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.player.MoveTransition;
import blindfoldchesstrainer.engine.player.Player;
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
    private final Player playerToMove;
    private final int THRESHOLD = 25;
    private final int FIXED_ORDERING_DEPTH = 2;
    
    public AlphaBeta(final Board board, final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.board = board;
        this.searchDepth = searchDepth;
        this.playerToMove = board.currentPlayer();
    }

    @Override
    public Move compute() {
        return execute(getBoard());
    }

    @Override
    public Move execute(Board board) {
        System.out.println(board.currentPlayer() + "Thinking with depth = " + searchDepth);
        List<Move> legalMoves = new ArrayList<>(board.currentPlayer().getLegalMoves());
        Collections.shuffle(legalMoves);
        List<EvaluatedMove> moves = new ArrayList<>(moveOrdering(board));
        for(final EvaluatedMove candidate : moves) {
            candidate.setValue(getPlayerToMove().getAlliance().isWhite() ?
                    alphabeta(candidate.getMove().execute(), 
                              getSearchDepth() - 1, 
                              Integer.MIN_VALUE, 
                              Integer.MAX_VALUE, 
                              false) :
                    alphabeta(candidate.getMove().execute(), 
                              getSearchDepth() - 1, 
                              Integer.MIN_VALUE, 
                              Integer.MAX_VALUE,
                              true));            
            try {
                Thread.sleep(0);
            }
            catch (InterruptedException ex) {
                return pickBestMove(moves);
            }
        }
        return pickBestMove(moves);
    }
    
    public List<EvaluatedMove> moveOrdering(Board board) {
        List<Move> legalMoves = new ArrayList<>(board.currentPlayer().getLegalMoves());
        Collections.shuffle(legalMoves);
        List<EvaluatedMove> moves = initEvaluatedMoves(legalMoves);
        for(final EvaluatedMove candidate : moves) {
            candidate.setValue(getPlayerToMove().getAlliance().isWhite() ?
                    alphabeta(candidate.getMove().execute(), 
                              FIXED_ORDERING_DEPTH - 1, 
                              Integer.MIN_VALUE, 
                              Integer.MAX_VALUE, 
                              false) :
                    alphabeta(candidate.getMove().execute(), 
                              FIXED_ORDERING_DEPTH - 1, 
                              Integer.MIN_VALUE, 
                              Integer.MAX_VALUE,
                              true));            
        }
        return Collections.unmodifiableList(moves);
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
    
    private List<EvaluatedMove> initEvaluatedMoves(List<Move> legalMoves) {
        List<EvaluatedMove> candidateEvaluatedMoves = new ArrayList<>();
        if (getBoard().currentPlayer().getAlliance().isWhite()) {
            for (Move candidate : legalMoves) {
                final MoveTransition moveTransition = getBoard().currentPlayer().makeMove(candidate);
                if (moveTransition.getMoveStatus().isDone())
                    candidateEvaluatedMoves.add(new EvaluatedMove(candidate, Integer.MIN_VALUE));
            }
        }
        else {
            for (Move candidate : legalMoves) {
                final MoveTransition moveTransition = getBoard().currentPlayer().makeMove(candidate);
                if (moveTransition.getMoveStatus().isDone())
                    candidateEvaluatedMoves.add(new EvaluatedMove(candidate, Integer.MAX_VALUE));
            }
        }
        return candidateEvaluatedMoves;
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

    private Move pickBestMove(List<EvaluatedMove> moves) {
        List<EvaluatedMove> sortedMoves = new ArrayList<>(getPlayerToMove().getAlliance().orderMoves(moves));
        for (int i = 0; i < sortedMoves.size(); i++) {
            System.out.print("{" + sortedMoves.get(i).getValue() + " " + sortedMoves.get(i).getMove() + "} ");            
        }
        System.out.println("");
        List<EvaluatedMove> bestMoves = new ArrayList<>();
        EvaluatedMove topMove = sortedMoves.get(0);
        for (EvaluatedMove candidate : sortedMoves) {
            if (Math.abs(topMove.getValue() - candidate.getValue()) <= THRESHOLD)
                bestMoves.add(candidate);
        }
        int movesNumber = bestMoves.size();
        return bestMoves.get((int)(Math.random() * movesNumber)).getMove();
    }

    /**
     * @return the playerToMove
     */
    public Player getPlayerToMove() {
        return playerToMove;
    }
}
