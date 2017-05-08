package blindfoldchesstrainer.engine.player;

import blindfoldchesstrainer.engine.Alliance;
import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.board.Move.*;
import blindfoldchesstrainer.engine.board.Tile;
import blindfoldchesstrainer.engine.pieces.Piece;
import blindfoldchesstrainer.engine.pieces.Rook;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Created by Anton on 1/23/2017.
 */
public class BlackPlayer extends Player {
    public BlackPlayer(Board board,
                       Collection<Move> whiteStandardLegalMoves,
                       Collection<Move> blackStandardLegalMoves,
                       PlayerType playerType) {

        super(board, blackStandardLegalMoves, whiteStandardLegalMoves, playerType);
    }

    @Override
    protected Collection<Move> calculateKingCastles(Collection<Move> playerLegals, Collection<Move> opponentsLegals) {

        final List<Move> kingCastles = new ArrayList<>();

        if (this.playerKing.isFirstMove() && this.playerKing.getPiecePosition() == 4) {
            if(!this.board.getTile(5).isTileOccupied() && !this.board.getTile(6).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(7);
                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(4, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(5, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(6, opponentsLegals).isEmpty() &&
                       rookTile.getPiece().getPieceType().isRook()) {
                        kingCastles.add(new KingsideCastleMove(this.board,
                                this.playerKing,
                                6,
                                (Rook)rookTile.getPiece(),
                                rookTile.getTileCoordinate(),
                                5));
                    }
                }
            }
            if(!this.board.getTile(3).isTileOccupied() &&
                    !this.board.getTile(2).isTileOccupied() &&
                    !this.board.getTile(1).isTileOccupied()) {
                final Tile rookTile = this.board.getTile(0);
                if(rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove()) {
                    if(Player.calculateAttacksOnTile(4, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(3, opponentsLegals).isEmpty() &&
                       Player.calculateAttacksOnTile(2, opponentsLegals).isEmpty() &&
                       rookTile.getPiece().getPieceType().isRook()) {
                        kingCastles.add(new QueensideCastleMove(this.board,
                                this.playerKing,
                                2,
                                (Rook)rookTile.getPiece(),
                                rookTile.getTileCoordinate(),
                                3));
                    }
                }
            }
        }
        return Collections.unmodifiableCollection(kingCastles);
    }

    @Override
    public Alliance getAlliance() {
        return Alliance.BLACK;
    }

    @Override
    public Player getOpponent() {
        return this.board.whitePlayer();
    }

    @Override
    public Collection<Piece> getActivePieces() {
        return this.board.getBlackPieces();
    }

    @Override
    public boolean isKingsideCastleCapable() {
        final Tile rookTile = this.board.getTile(7);
        if (getPlayerKing().isFirstMove() && rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove())
            return true;
        return false;
    }

    @Override
    public boolean isQueensideCastleCapable() {
        final Tile rookTile = this.board.getTile(0);
        if (getPlayerKing().isFirstMove() && rookTile.isTileOccupied() && rookTile.getPiece().isFirstMove())
            return true;
        return false;
    }
    
    @Override
    public String toString() {
        return "Black";
    }
}
