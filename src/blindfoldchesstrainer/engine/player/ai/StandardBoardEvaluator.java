package blindfoldchesstrainer.engine.player.ai;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.pieces.Piece;
import blindfoldchesstrainer.engine.player.Player;

/**
 * Created by Anton on 3/13/2017.
 */
public final class StandardBoardEvaluator implements BoardEvaluator {

    private static final int CHECKMATE_BONUS = 10000;
    private static final int DEPTH_BONUS = 100;
    private static final int CASTLE_BONUS = 30;
    private static final int ONE_SIDE_CASTLE_CAPABLE_BONUS = 5;

    @Override
    public int evaluate(final Board board,
                        final int depth) {
        return scorePlayer(board.whitePlayer(), depth, board) -
               scorePlayer(board.blackPlayer(), depth, board);
    }
    
    public int scorePlayer(final Player player, final int depth, final Board board) {
        return pieceValueAndPosition(player) +
               checkmate(player, depth) +
               mobility(player) +
               castle(player);
        // + other heuristics
    }
    
    public static int pieceValueAndPosition(final Player player) {
        int value = 0;
        for (final Piece piece : player.getActivePieces()) {
            value += piece.getPieceValue() + piece.positionBonus();
        }
        return value;
    }

    public static int castle(Player player) {
        return player.isCastled() ? CASTLE_BONUS : queensideCastleCapable(player) + kingsideCastleCapable(player);
    }

    public static int checkmate(Player player, int depth) {
        return player.getOpponent().isInCheckMate() ? CHECKMATE_BONUS * depthBonus(depth) : 0;
    }

    public static int depthBonus(int depth) {
        return depth == 0 ? 1 : DEPTH_BONUS * depth;
    }

    public static int mobility(final Player player) {
        return player.getLegalMoves().size();
    }

    public static int pieceValue(Player player) {
        int pieceValueScore = 0;
        for(final Piece piece : player.getActivePieces()) {
            pieceValueScore += piece.getPieceValue();
        }
        return pieceValueScore;
    }

    public static int kingsideCastleCapable(Player player) {
        if (player.isKingsideCastleCapable())
            return ONE_SIDE_CASTLE_CAPABLE_BONUS;
        return 0;
    }
    
    public static int queensideCastleCapable(Player player) {
        if (player.isQueensideCastleCapable())
            return ONE_SIDE_CASTLE_CAPABLE_BONUS;
        return 0;
    }
}
