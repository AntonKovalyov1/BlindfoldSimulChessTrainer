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

/**
 *
 * @author Anton
 */
public class AlphaBeta implements MoveStrategy {
    
    private final BoardEvaluator boardEvaluator;
    private final int searchDepth;
    private final Board board;
    private final int FIXED_ORDERING_DEPTH = 3;
    private boolean running;
    private boolean forceMoveExecution;
    private long startRunningTime;
    private final long FIXED_TIME_LIMIT = 3000;
    
    public AlphaBeta(final Board board, final int searchDepth) {
        this.boardEvaluator = new StandardBoardEvaluator();
        this.board = board;
        this.searchDepth = searchDepth;
    }

    @Override
    public Move execute(Board board) {
        running = true;
        startRunningTime = System.currentTimeMillis();
        System.out.println(forceMoveExecution);
        int maxSeenValue = Integer.MIN_VALUE;
        int minSeenValue = Integer.MAX_VALUE;
        List<Move> possibleMoves = getPossibleMoves(board);
        Move quickBestMove = getQuickBestMove(board);
        Move bestMove = getQuickBestMove(board);
        for(final Move candidate : possibleMoves) {
            int currentValue;
            if (board.currentPlayer().getAlliance().isWhite()) {
                currentValue = alphabeta(candidate.execute(), 
                              getSearchDepth() - 1, 
                              maxSeenValue, 
                              minSeenValue, 
                              false);
                if (currentValue > maxSeenValue) {
                    bestMove = candidate;
                    maxSeenValue = currentValue;
                }
            }
            else {
                currentValue = alphabeta(candidate.execute(), 
                              getSearchDepth() - 1, 
                              maxSeenValue, 
                              minSeenValue,
                              true);
                if (currentValue < minSeenValue) {
                    bestMove = candidate;
                    minSeenValue = currentValue;
                }
            }
            if (forceMoveExecution || System.currentTimeMillis() - startRunningTime > FIXED_TIME_LIMIT) {
                forceMoveExecution = false;
                running = false;
                return quickBestMove;
            }
        }
        forceMoveExecution = false;
        running = false;
        return bestMove;
    }
    
    public Move getQuickBestMove(Board board) {
        int maxSeenValue = Integer.MIN_VALUE;
        int minSeenValue = Integer.MAX_VALUE;
        List<Move> possibleMoves = getPossibleMoves(board);
        Move quickBestMove = possibleMoves.get(0);
        for(final Move candidate : possibleMoves) {
            int currentValue;
            if (board.currentPlayer().getAlliance().isWhite()) {
                currentValue = alphabeta(candidate.execute(), 
                              FIXED_ORDERING_DEPTH - 1, 
                              maxSeenValue, 
                              minSeenValue, 
                              false);
                if (currentValue > maxSeenValue) {
                    quickBestMove = candidate;
                    maxSeenValue = currentValue;
                }
            }
            else {
                currentValue = alphabeta(candidate.execute(), 
                              FIXED_ORDERING_DEPTH - 1, 
                              maxSeenValue, 
                              minSeenValue,
                              true);
                if (currentValue < minSeenValue) {
                    quickBestMove = candidate;
                    minSeenValue = currentValue;
                }
            }
        }
        return quickBestMove;           
    }
    
    private int alphabeta(Board board, int depth, int alpha, int beta, boolean maximizingPlayer) {
        if (depth == 0 || isEndGameScenario(board))
            return getBoardEvaluator().evaluate(board, depth);
        List<Move> possibleMoves = getPossibleMoves(board);
        if (maximizingPlayer) {
            int v = Integer.MIN_VALUE;
            for (Move move : possibleMoves) {
                if (forceMoveExecution || System.currentTimeMillis() - startRunningTime > FIXED_TIME_LIMIT) {
                    break;
                }
                v = Integer.max(v, alphabeta(move.execute(), depth - 1, alpha, beta, false));
                alpha = Integer.max(alpha, v);
                if (beta <= alpha)
                    break; // Beta cut-off
            }
            return v;
        }
        int v = Integer.MAX_VALUE;
        for (Move move : possibleMoves) {
            if (forceMoveExecution || System.currentTimeMillis() - startRunningTime > FIXED_TIME_LIMIT) {
                break;
            }
            v = Integer.min(v, alphabeta(move.execute(), depth - 1, alpha, beta, true));
            beta = Integer.min(beta, v);
            if (beta <= alpha)
                break; // alpha cut-off
        }
        return v;
    }
    
    private boolean isEndGameScenario(Board board) {
        return board.currentPlayer().isInCheckMate() ||
               board.currentPlayer().isInStalemate();
    }   
    
    private List<Move> getPossibleMoves(Board board) {
        List<Move> possibleMoves = new ArrayList<>();
        List<Move> allLegalMoves = new ArrayList<>(board.currentPlayer().getLegalMoves());
        Collections.shuffle(allLegalMoves);
        for (final Move move : allLegalMoves) {
            MoveTransition moveTransition = board.currentPlayer().makeMove(move);
            if (moveTransition.getMoveStatus().isDone())
                possibleMoves.add(move);
        }
        return possibleMoves;
    }
    
    public boolean isRunning() {
        return running;
    }
    
    public void forceMoveExecution() {
        if (isRunning())
            forceMoveExecution = true;
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
