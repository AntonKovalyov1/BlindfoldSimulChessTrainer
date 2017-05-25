package blindfoldchesstrainer.engine.uci;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.BoardUtils;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.board.Move.MoveFactory;
import blindfoldchesstrainer.engine.pieces.Pawn;
import blindfoldchesstrainer.engine.pieces.Piece.PieceType;
import java.util.List;

/**
 *
 * @author Anton
 */
public class UCIUtilities {

    private UCIUtilities() {
        throw new RuntimeException("Not instantiable");
    }
    
    public static Board createGameFromFEN(final String fenString) {
        return null;
    }
    
    public static String createFENFromGame(final Board board) {
        return calculateBoardText(board) + " " + 
               calculateCurrentPlayerText(board) + " " +
               calculateCastleText(board) + " " +
               calculateEnPassantSquare(board) + " " +
               " 0 1";
    }
    
    private static String calculateBoardText(final Board board) {
        final StringBuilder s = new StringBuilder();
        for(int i = 0; i < BoardUtils.NUM_TILES; i++) {
            final String tileText = board.getTile(i).toString();
            s.append(tileText);
        }
        s.insert(8, "/");
        s.insert(17, "/");
        s.insert(26, "/");
        s.insert(35, "/");
        s.insert(44, "/");
        s.insert(53, "/");
        s.insert(62, "/");

        return s.toString().replaceAll("--------", "8")
                           .replaceAll("-------", "7")
                           .replaceAll("------", "6")
                           .replaceAll("-----", "5")
                           .replaceAll("----", "4")
                           .replaceAll("---", "3")
                           .replaceAll("--", "2")
                           .replaceAll("-", "1");
    }

    private static String calculateCurrentPlayerText(final Board board) {
        return board.currentPlayer().getAlliance().toString().substring(0, 1).toLowerCase();
    }

    private static String calculateCastleText(final Board board) {
        final StringBuilder s = new StringBuilder(); 
        //white
        if(board.whitePlayer().isKingsideCastleCapable()) {
            s.append("K");
        }
        if(board.whitePlayer().isQueensideCastleCapable()) {
            s.append("Q");
        }
        //black
        if(board.blackPlayer().isKingsideCastleCapable()) {
            s.append("k");
        }
        if(board.blackPlayer().isQueensideCastleCapable()) {
            s.append("q");
        }
        final String result = s.toString();
        return result.isEmpty() ? "-" : result;
    }

    private static String calculateEnPassantSquare(final Board board) {
        final Pawn enPassantPawn = board.getEnPassantPawn();
        
        if(enPassantPawn != null) {
            return BoardUtils.getPositionAtCoordinate(enPassantPawn.getPiecePosition() + 
                    8 * enPassantPawn.getPieceAlliance().getOppositeDirection());
        }
        return "-";
    }    
    
    public static String movesToUCIformat(final List<Move> movesPlayed) {
        StringBuilder sb = new StringBuilder();
        for (final Move move : movesPlayed) {
            sb.append(move.getUCIformat());
            sb.append(" ");
        }
        return sb.toString();
    }
    
        
    public static Move getMoveFromUCIformat(final String uci_move, final Board board) {
        System.out.println(uci_move);
        int start = BoardUtils.getCoordinateAtPosition(uci_move.substring(0, 2));
        int end = BoardUtils.getCoordinateAtPosition(uci_move.substring(2, 4));
        if (uci_move.length() > 4) {
            return MoveFactory.createMove(board, start, end, getPieceType(uci_move.substring(4)));
        }
        return MoveFactory.createMove(board, start, end);
    }
    
    private static PieceType getPieceType(String s) {
        if (s.equals("q"))
            return PieceType.QUEEN;
        if (s.equals("r"))
            return PieceType.ROOK;
        if (s.equals("n"))
            return PieceType.KNIGHT;
        return PieceType.BISHOP;
    }
    
}
