package blindfoldchesstrainer.gui;

/**
 * Created by Anton on 1/31/2017.
 */

import blindfoldchesstrainer.engine.Alliance;
import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.BoardUtils;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.board.Move.AttackMove;
import blindfoldchesstrainer.engine.board.Move.MajorMove;
import blindfoldchesstrainer.engine.board.Move.PawnPromotion;
import blindfoldchesstrainer.engine.board.Tile;
import blindfoldchesstrainer.engine.pieces.Pawn;
import blindfoldchesstrainer.engine.pieces.Piece;
import blindfoldchesstrainer.engine.player.MoveTransition;
import blindfoldchesstrainer.engine.player.Player;
import blindfoldchesstrainer.engine.player.PlayerType;
import blindfoldchesstrainer.engine.*;
import javafx.animation.*;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.InnerShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.*;
import java.util.concurrent.*;

public class GUI extends Stage {

    private boolean highlightLegalMoves;
    private boolean cheatAll;
    private boolean flipBoard;
    private final BorderPane mainBorderPane = new BorderPane();
    private GamesViewer gamesViewer;
    private ScheduledExecutorService computerMoveExecutor = Executors.newScheduledThreadPool(1);
    private final Scene scene = new Scene(mainBorderPane, 700, 760);
    private final CheckMenuItem fullScreenMenuItem = new CheckMenuItem("Full Screen");
    private final CheckMenuItem smoothTransitionMenuItem = new CheckMenuItem("Smooth Transition");
    private final Text currentGameStatus = new Text("No games in progress");
    private final Text matchStatus = new Text("");
    private final BorderPane footer = makeFooter(getCurrentGameStatus(), getMatchStatus());
    private final int GAME_OVER_PAUSE_TIME = 2000;
    private final int GAME_NOT_OVER_PAUSE_TIME = 200;
    private EditUCIEngines enginesWindow = new EditUCIEngines();
    private final String user = System.getProperty("user.name");

    public GUI() {
        initialize();
    }

    private void initialize() {
        getIcons().add(new Image("images/mainIcon.jpg"));
        setScene(scene);
        scene.getStylesheets().add("main.css");
        getSmoothTransitionMenuItem().setSelected(true);
        setHighlightLegalMoves(true);
        this.gamesViewer = new GamesViewer();
        updateFooter();
        getMainBorderPane().setTop(createMenuBar());
        getMainBorderPane().setCenter(getGamesViewer());
        getMainBorderPane().setBottom(getFooter());
        getMainBorderPane().setId("main-border-pane");
        setTitle("Blindfold Chess Trainer");
        show();
        setOnCloseRequest(e -> {
            getComputerMoveExecutor().shutdownNow();
            closeEngines(enginesWindow.getEngines());
            close();
        });
        fullScreenProperty().addListener(e -> {
            if (isFullScreen())
                getFullScreenMenuItem().setSelected(true);
            else
                getFullScreenMenuItem().setSelected(false);
        });
        scene.setOnKeyReleased(e -> {
            BoardPane current = getGamesViewer().getCurrentGame().getBoardPane();
            if (!current.getMoves().isEmpty()) {
                switch (e.getCode()) {
                    case UP:
                        current.redrawBoard(current.lastBoard());
                        break;
                    case DOWN:
                        current.redrawBoard(current.firstBoard());
                        break;
                    case LEFT:
                        current.redrawBoard(current.previousBoard());
                        break;
                    case RIGHT:
                        current.redrawBoard(current.nextBoard());
                        break;
                    default:
                        break;
                }
            }
        });
    }

    private BorderPane makeFooter(Text currentGameStatus, Text matchStatus) {
       BorderPane bp = new BorderPane();
        bp.setId("footer");
        currentGameStatus.setId("current-game-status-text");
        matchStatus.setId("match-status-text");
        bp.setLeft(currentGameStatus);
        bp.setRight(matchStatus);
        return bp;
    }

    private MenuBar createMenuBar() {
        final MenuBar menuBar = new MenuBar();

        final Menu playMenu = new Menu("Play");
        final Menu settingsMenu = new Menu("Settings");

        final CheckMenuItem cheatCheckMenuItem = new CheckMenuItem("See Pieces");
        cheatCheckMenuItem.setOnAction(e -> {
            cheatAll = cheatCheckMenuItem.isSelected();
            for (int i = 0; i < getGamesViewer().getGames().size(); i++) {
                BoardPane current = getGamesViewer().getGames().get(i).getBoardPane();
                current.redrawBoard(current.getCurrentViewBoard());
            }
        });

        final CheckMenuItem flipBoardMenuItem = new CheckMenuItem("Flip Board/s");
        flipBoardMenuItem.setOnAction(e -> {
            setFlipBoard(!isFlipBoard());
            for (int i = 0; i < getGamesViewer().getGames().size(); i++) {
                getGamesViewer().getGames().get(i).flipBoard();
            }
        });

        final CheckMenuItem highlightCheckMenuItem = new CheckMenuItem("Highlight legal moves");
        highlightCheckMenuItem.setOnAction(e -> {
            this.setHighlightLegalMoves(highlightCheckMenuItem.isSelected());
            for (int i = 0; i < getGamesViewer().getGames().size(); i++) {
                BoardPane current = getGamesViewer().getGames().get(i).getBoardPane();
                current.redrawBoard(current.getCurrentViewBoard());
            }
        });
        highlightCheckMenuItem.setSelected(true);
        this.setHighlightLegalMoves(true);
        // set highliting to false as default
                        
        final MenuItem enginesMenuItem = new MenuItem("Edit Engines");
        enginesMenuItem.setOnAction(e -> {
            enginesWindow = new EditUCIEngines();
            enginesWindow.showAndWait();
        });

        final MenuItem newGameMenuItem = new MenuItem("New Game/s");
        newGameMenuItem.setOnAction(e -> {
            CreateMatch createMatch = new CreateMatch("New Game/s", enginesWindow.getEngines());
            if (createMatch.getNumberOfGames() != -1) {
                closeEngines(enginesWindow.getEngines());
                final int numberOfGames = createMatch.getNumberOfGames();
                final ColorChoice colorChoice = createMatch.getColorChoice();
                final Difficulty difficulty = createMatch.getDifficulty();
                final Engine engine = createMatch.getEngine();
                List<GamePane> games = match(numberOfGames, colorChoice, difficulty, engine, 0);
                setGamesViewer(new GamesViewer(games));
                getMainBorderPane().setCenter(getGamesViewer());
                updateFooter();
            }
        });
        final MenuItem addGameMenuItem = new MenuItem("Add Game/s");
        addGameMenuItem.setOnAction(e -> {
            CreateMatch createMatch = new CreateMatch("Add Game/s", enginesWindow.getEngines());
            if (createMatch.getNumberOfGames() != -1) {
                final int numberOfGames = createMatch.getNumberOfGames();
                final ColorChoice colorChoice = createMatch.getColorChoice();
                final Difficulty difficulty = createMatch.getDifficulty();
                final Engine engine = createMatch.getEngine();
                List<GamePane> games = new ArrayList<>();
                if (getGamesViewer().getActiveGamesCounter() < 1) {
                    getComputerMoveExecutor().shutdownNow();
                    setComputerMoveExecutor(Executors.newScheduledThreadPool(1));
                    games.addAll(match(numberOfGames, colorChoice, difficulty, engine, 0));
                    setGamesViewer(new GamesViewer(games));
                    getMainBorderPane().setCenter(getGamesViewer());
                }
                else {
                    final int offset = getGamesViewer().getGames().size();
                    games.addAll(match(numberOfGames, colorChoice, difficulty, engine, offset));
                    getGamesViewer().addGames(games);
                }
                updateFooter();
            }
        });

        getFullScreenMenuItem().setOnAction(e -> {
            if (isFullScreen())
                setFullScreen(false);
            else
                setFullScreen(true);
        });

        playMenu.getItems().addAll(newGameMenuItem, addGameMenuItem);

        settingsMenu.getItems().addAll(getFullScreenMenuItem(), flipBoardMenuItem, highlightCheckMenuItem, cheatCheckMenuItem, getSmoothTransitionMenuItem(), enginesMenuItem);

        menuBar.getMenus().addAll(playMenu, settingsMenu);
        return menuBar;
    }
    
    public List<GamePane> match(int numberOfGames, ColorChoice colorChoice, Difficulty difficulty, Engine engine, int offset) {
        List<GamePane> games = new ArrayList<>();
        for (int i = offset; i < numberOfGames + offset; i++) {
            if (colorChoice.getAlliance().isWhite())
                games.add(new GamePane(i, 
                        Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.COMPUTER), 
                        engine, 
                        difficulty.getDepth()));
            else
                games.add(new GamePane(i, Board.createStandardGameBoard(PlayerType.COMPUTER, 
                        PlayerType.HUMAN), 
                        engine, 
                        difficulty.getDepth()));
        }
        return games;
    }

    public GamesViewer getGamesViewer() {
        return gamesViewer;
    }

    public void setGamesViewer(GamesViewer gamesViewer) {
        this.gamesViewer = gamesViewer;
    }

    public ScheduledExecutorService getComputerMoveExecutor() {
        return computerMoveExecutor;
    }

    public void setComputerMoveExecutor(ScheduledExecutorService computerMoveExecutor) {
        this.computerMoveExecutor = computerMoveExecutor;
    }

    public boolean isHighlightLegalMoves() {
        return highlightLegalMoves;
    }

    public void setHighlightLegalMoves(boolean highlightLegalMoves) {
        this.highlightLegalMoves = highlightLegalMoves;
    }

    public BorderPane getMainBorderPane() {
        return mainBorderPane;
    }

    public boolean isFlipBoard() {
        return flipBoard;
    }

    public void setFlipBoard(boolean flipBoard) {
        this.flipBoard = flipBoard;
    }

    public CheckMenuItem getFullScreenMenuItem() {
        return fullScreenMenuItem;
    }

    public CheckMenuItem getSmoothTransitionMenuItem() {
        return smoothTransitionMenuItem;
    }

    public Text getCurrentGameStatus() {
        return currentGameStatus;
    }

    public BorderPane getFooter() {
        return footer;
    }

    private void closeEngines(List<Engine> engines) {
        for (Engine engine : engines) {
            engine.close();
        }
    }    
            
    public void updateFooter() {
        if (getGamesViewer().isNoGameMode()) {
            getCurrentGameStatus().setText("No games in progress");
        }
        else {
            getCurrentGameStatus().setText(gameInfo(getGamesViewer().getCurrentGame().getBoardPane()));
            String scoreHumanString = Double.toString(getGamesViewer().getScoreHuman()).replace(".0", "");
            String scoreComputerString = Double.toString(getGamesViewer().getScoreComputer()).replace(".0", "");
            getMatchStatus().setText("  Score: " + scoreHumanString + "-" + scoreComputerString);
        }
    }

    public String gameInfo(BoardPane boardPane) {
        String s = "";
        Engine computerPlayer = boardPane.getEngine();
        String engineName = computerPlayer instanceof RandomEngine ? "Random Engines" : computerPlayer.toString();
        if (boardPane.getBoard().whitePlayer().getPlayerType().isHuman()) {
            s += user + " vs. " + engineName + " ";
        }
        else
            s += engineName + " vs. " + user + "/t";
        s += boardPane.getGameOverText();
        return s;
    }

    /**
     * @return the matchStatus
     */
    private Text getMatchStatus() {
        return matchStatus;
    }

    public final class GamesViewer extends GridPane {

        private final List<GamePane> games;
        private Pagination pagination;
        private final Button btResign = resignButton();
        private final Button btDraw = drawButton();
        private final Button btWin = winButton();
        private final Button btCheat = cheatButton();
        private final Button btFlip = flipButton();
        private final Button btTakeBack = takeBackButton();
        private final Button btResetGame = resetButton();
        private final Button btMakeMove = makeMoveButton();
        private final VBox options = createOptions();
        private final SequentialTransition st = new SequentialTransition();
        private final PauseTransition pauseBeforeNextGame = new PauseTransition(Duration.ZERO);
        private GamePane currentGame;
        private final StackPane gameContainer = new StackPane();
        private int activeGamesCounter = 0;
        private boolean noGameMode;
        private double scoreHuman = 0;
        private double scoreComputer = 0;

        public GamesViewer() {
            this.currentGame = new GamePane(0, Board.createStandardGameBoard(PlayerType.HUMAN, PlayerType.HUMAN), null, Difficulty.EASY.getDepth());
            this.games = new ArrayList<>();
            this.games.add(this.currentGame);
            this.pagination = createPaginationControl(0);
            this.currentGame = games.get(0);
            updateActiveGamesCounter(0);
            setNoGameMode(true);
            noGameControls();
            initializeGamesViewer();
        }
        
        public GamesViewer(List<GamePane> games) {
            this.games = games;
            this.pagination = createPaginationControl(0);
            this.currentGame = games.get(0);
            updateActiveGamesCounter(games.size());
            initializeGamesViewer();
        }

        public void initializeGamesViewer() {
            getStyleClass().add("games-viewer");
            getGameContainer().getChildren().add(getCurrentGame());
            add(getPagination(), 0, 0);
            add(getGameContainer(), 0, 1);
            add(getOptions(), 1, 1);
            setAlignment(Pos.CENTER);
        }

        public VBox createOptions() {
            VBox gameOptions = new VBox();
            gameOptions.setPadding(new Insets(20, 20, 0, 2));
            gameOptions.getChildren().addAll(getBtMakeMove(), getBtResign(), getBtDraw(), getBtWin(), getBtCheat(), getBtFlip(), getBtTakeBack(), getBtResetGame());
            return gameOptions;
        }

        public Pagination createPaginationControl(int currentIndex) {
            Pagination p = new Pagination(getGames().size());
            p.currentPageIndexProperty().addListener(InvalidationListener -> {
                disableControls();
                final GamePane nextGame = getGames().get(getPagination().getCurrentPageIndex());
                nextGame.getBoardPane().resetMoveSelection();
                if (nextGame.getBoardPane().isCheat()) {
                    nextGame.getBoardPane().setCheat(false);
                    nextGame.getBoardPane().redrawBoard(nextGame.getBoardPane().getCurrentViewBoard());
                }
                if (getSmoothTransitionMenuItem().isSelected()) {
                    smoothTransition(nextGame);
                }
                else
                    fastTransition(nextGame);
            });
            return p;
        }

        private void fastTransition(GamePane nextGame) {
            getSt().getChildren().clear();
            getPauseBeforeNextGame().setOnFinished(e -> {
                resetOptions();
                getGameContainer().getChildren().set(0, nextGame);
                setCurrentGame(nextGame);
                updateFooter();
                if (getCurrentGame().isGameOver())
                    gameOver();
                else
                    enableControls();
            });
            getPauseBeforeNextGame().play();
        }

        public void smoothTransition(GamePane nextGame) {
            getPauseBeforeNextGame().setOnFinished(null);
            FadeTransition ft = new FadeTransition(Duration.millis(600), getCurrentGame());
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> {
                getGameContainer().getChildren().set(0, nextGame);
                resetOptions();
            });
            PauseTransition pt = new PauseTransition(Duration.millis(100));
            FadeTransition ft2 = new FadeTransition(Duration.millis(600), nextGame);
            ft2.setFromValue(0.0);
            ft2.setToValue(1.0);
            getSt().getChildren().clear();
            getSt().getChildren().addAll(getPauseBeforeNextGame(), ft, pt, ft2);
            getSt().play();
            getSt().setOnFinished(e -> {
                getPauseBeforeNextGame().setDuration(Duration.ZERO);
                getCurrentGame().setOpacity(1.0);
                setCurrentGame(nextGame);
                updateFooter();
                if (getCurrentGame().isGameOver()) {
                    gameOver();
                }
                else
                    enableControls();
            });
        }
        
        public void gameOver() {
            gameOverControls();
            updateActiveGamesCounter(-1);
            if (getCurrentGame().isShowable()) {
                getCurrentGame().getBoardPane().resetMoveSelection();
                getCurrentGame().setShowable(false);
                getCurrentGame().setGameOver(true);
                updatePage(GAME_OVER_PAUSE_TIME);
            }          
        }

        public int updatePage(int delay) {
            getCurrentGame().getBoardPane().disablePawnPromotionMode();
            getPauseBeforeNextGame().setDuration(Duration.millis(delay));
            int current = getCurrentGame().getGameID();
            int next = getNextActiveGameIndex(current, getGames());
            if (next != current)
                getPagination().setCurrentPageIndex(next);
            return next;
        }
        
        public int getNextActiveGameIndex(int current, List<GamePane> games) {
            int next = getNextGameIndex(current, games);
            while (!games.get(next).isShowable() && next != current) {
                next = getNextGameIndex(next, games);
            }            
            return next;
        }
        
        public int getNextGameIndex(int current, List<GamePane> games) {
            if (current < games.size() - 1) {
                return current + 1;
            }
            return 0;
        }

        public void addGames(List<GamePane> games) {
            int currentGameIndex = getPagination().getCurrentPageIndex();
            getGames().addAll(games);
            updateActiveGamesCounter(games.size());
            if (getSmoothTransitionMenuItem().isSelected()) {
                getSmoothTransitionMenuItem().setSelected(false);
                getPagination().setPageCount(getGames().size());
                getPagination().setCurrentPageIndex(currentGameIndex);
                getSmoothTransitionMenuItem().setSelected(true);
            }
            else {
                getPagination().setPageCount(getGames().size());
                getPagination().setCurrentPageIndex(currentGameIndex);
            }
        }

        public void updateActiveGamesCounter(int offset) {
            setActiveGamesCounter(getActiveGamesCounter() + offset);
        }

        private Button makeMoveButton() {
            Button b = new Button();
            b.setId("make-move-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("play!"));
            b.setOnAction(e -> {
                getCurrentGame().makeAMove();
            });
            return b;
        }

        private Button resignButton() {
            Button b = new Button();
            b.setId("resign-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("resign"));
            b.setOnAction(e -> {
                getCurrentGame().resign();
            });
            return b;
        }

        private Button drawButton() {
            Button b = new Button();
            b.setId("draw-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("draw"));
            b.setOnAction(e -> {
                getCurrentGame().makeDraw();
            });
            return b;
        }

        private Button winButton() {
            Button b = new Button();
            b.setId("win-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("win"));
            b.setOnAction(e -> {
                getCurrentGame().win();
            });
            return b;
        }

        private Button cheatButton() {
            Button b = new Button();
            b.setId("cheat-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("cheat"));
            b.setOnAction(e -> {
                BoardPane boardPane = getCurrentGame().getBoardPane();
                if (boardPane.isCheat()) {
                    boardPane.setCheat(false);
                    boardPane.redrawBoard(boardPane.getCurrentViewBoard());
                    b.setId("cheat-button");
                    b.setTooltip(new Tooltip("cheat"));
                }
                else {
                    boardPane.setCheat(true);
                    boardPane.redrawBoard(boardPane.getCurrentViewBoard());
                    b.setId("play-fair-button");
                    b.setTooltip(new Tooltip("play fair"));
                }
            });
            return b;
        }

        private Button flipButton() {
            Button b = new Button();
            b.setId("flip-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("flip board"));
            b.setOnAction(e -> {
                getCurrentGame().flipBoard();
            });
            return b;
        }

        private Button takeBackButton() {
            Button b = new Button();
            b.setId("take-back-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("take back"));
            b.setOnAction(e -> {
                getCurrentGame().takeBack();
            });
            return b;
        }

        private Button resetButton() {
            Button b = new Button();
            b.setId("reset-button");
            b.getStyleClass().add("options-buttons");
            b.setTooltip(new Tooltip("reset game"));
            b.setOnAction(e -> {
                getCurrentGame().resetGame();
            });
            return b;
        }

        public void resetOptions() {
            getBtCheat().setId("cheat-button");
        }

        public void disableControls() {
            for (Node current : getOptions().getChildren()) {
                current.setDisable(true);
            }
            getPagination().setDisable(true);
        }

        private void enableControls() {
            for (Node current : getOptions().getChildren()) {
                current.setDisable(false);
            }
            getPagination().setDisable(false);
        }

        public void gameOverControls() {
            disableControls();
            getBtCheat().setDisable(false);
            getBtFlip().setDisable(false);
            getPagination().setDisable(false);
        }

        public void noGameControls() {
            disableControls();
            getBtCheat().setDisable(false);
            getBtFlip().setDisable(false);
            getBtTakeBack().setDisable(false);
            getBtResetGame().setDisable(false);
        }

        public List<GamePane> getGames() {
            return games;
        }

        public Pagination getPagination() {
            return pagination;
        }

        public SequentialTransition getSt() {
            return st;
        }

        public GamePane getCurrentGame() {
            return currentGame;
        }

        public void setCurrentGame(GamePane currentGame) {
            this.currentGame = currentGame;
        }

        public VBox getOptions() {
            return options;
        }

        public Button getBtCheat() {
            return btCheat;
        }

        public Button getBtFlip() {
            return btFlip;
        }

        public StackPane getGameContainer() {
            return gameContainer;
        }

        public Button getBtMakeMove() {
            return btMakeMove;
        }

        public Button getBtResign() {
            return btResign;
        }

        public Button getBtDraw() {
            return btDraw;
        }

        public Button getBtWin() {
            return btWin;
        }

        public Button getBtTakeBack() {
            return btTakeBack;
        }

        public Button getBtResetGame() {
            return btResetGame;
        }

        public void setPagination(Pagination pagination) {
            this.pagination = pagination;
        }

        /**
         * @return the pauseBeforeNextGame
         */
        public PauseTransition getPauseBeforeNextGame() {
            return pauseBeforeNextGame;
        }

        /**
         * @return the activeGamesCounter
         */
        public int getActiveGamesCounter() {
            return activeGamesCounter;
        }

        /**
         * @param activeGamesCounter the activeGamesCounter to set
         */
        public void setActiveGamesCounter(int activeGamesCounter) {
            this.activeGamesCounter = activeGamesCounter;
        }

        /**
         * @return the noGameMode
         */
        private boolean isNoGameMode() {
            return noGameMode;
        }

        /**
         * @param noGameMode the noGameMode to set
         */
        private void setNoGameMode(boolean noGameMode) {
            this.noGameMode = noGameMode;
        }

        /**
         * @return the scoreHuman
         */
        private double getScoreHuman() {
            return scoreHuman;
        }

        /**
         * @param scoreHuman the scoreHuman to set
         */
        private void setScoreHuman(double scoreHuman) {
            this.scoreHuman = scoreHuman;
        }

        /**
         * @return the scoreComputer
         */
        private double getScoreComputer() {
            return scoreComputer;
        }

        /**
         * @param scoreComputer the scoreComputer to set
         */
        private void setScoreComputer(double scoreComputer) {
            this.scoreComputer = scoreComputer;
        }
    }

    public final class GamePane extends BorderPane {

        private final int gameID;
        private final StackPane boardBackground = new StackPane();
        private final BoardPane boardPane;
        private final Text whiteMoveText = new Text("1.?");
        private final Text blackMoveText = new Text("");
        private final HBox whiteSide = new HBox(7);
        private final HBox blackSide = new HBox(7);
        private final Button btPrevPrev = prevPrevButton();
        private final Button btPrev = prevButton();
        private final Button btNext = nextButton();
        private final Button btNextNext = nextNextButton();
        private boolean showable = true;
        private boolean gameOver;

        public GamePane(final int gameID, Board board, Engine engine, final int depth) {
            this.gameID = gameID;
            this.boardPane = new BoardPane(this, board, engine, depth);
            initialize(board);
        }

        private void initialize(Board board) {
            initWhiteSide();
            initBlackSide();
            BorderPane boardAndMovesPane = new BorderPane();
            if (board.whitePlayer().getPlayerType().isComputer())
                flipBoard();
            if (isFlipBoard()) {
                flipBoard();
            }
            boardAndMovesPane.setTop(this.getBlackSide());
            BorderPane.setAlignment(this.getBlackSide(), Pos.BOTTOM_RIGHT);
            boardAndMovesPane.setBottom(bottomControls());
            getBoardPane().setAlignment(Pos.CENTER);
            getBoardBackground().getChildren().add(createPawnPromotionBackground());
            boardAndMovesPane.setCenter(getBoardBackground());
            drawBackground();
            setCenter(boardAndMovesPane);
            setPadding(new Insets(0, 0, 20, 20));
            setAlignment(boardAndMovesPane, Pos.TOP_CENTER);
            
            getWhiteMoveText().getStyleClass().add("move-text");
            getBlackMoveText().getStyleClass().add("move-text");
        }

        private void drawBackground() {
            getBoardBackground().getStyleClass().add("board-background");
            setBoardInsideBackgroundEffect();
        }

        private void setBoardInsideBackgroundEffect() {
            InnerShadow innerShadow = new InnerShadow();
            innerShadow.setOffsetX(-1);
            innerShadow.setOffsetY(-1);
            innerShadow.setColor(Color.web("#EBCCAD", 0.6));

            getBoardPane().setEffect(innerShadow);
        }
        
        private StackPane createPawnPromotionBackground() {
            StackPane pawnPromotionBackground = new StackPane();
            pawnPromotionBackground.setId("promotion-background");
            pawnPromotionBackground.getChildren().add(getBoardPane());
            return pawnPromotionBackground;
        }

        private void flipBoard() {
            Collections.reverse(getBoardPane().getBoardTiles());
            swapSides();
            getBoardPane().redrawBoard(getBoardPane().getCurrentViewBoard());
        }

        private void initWhiteSide() {
            Circle circle = new Circle(5, Color.WHITE);
            circle.setStroke(Color.BLACK);
            getWhiteMoveText().setLineSpacing(1);
            getWhiteSide().getChildren().addAll(circle, getWhiteMoveText());
            getWhiteSide().setAlignment(Pos.BASELINE_LEFT);
            //replay controls
            getWhiteSide().getChildren().addAll(getBtPrevPrev(), getBtPrev(), getBtNext(), getBtNextNext());
        }

        private void initBlackSide() {
            Circle circle = new Circle(5, Color.BLACK);
            circle.setStroke(Color.BLACK);
            getBlackMoveText().setLineSpacing(1);
            getBlackSide().getChildren().addAll(getBlackMoveText(), circle);
            getBlackSide().setAlignment(Pos.BASELINE_RIGHT);
        }

        private Button nextNextButton() {
            Button b = new Button();
            b.setId("next-next-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().lastBoard());
                }
            });
            return b;
        }

        private Button prevPrevButton() {
            Button b = new Button();
            b.setId("prev-prev-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().firstBoard());
                }
            });
            return b;
        }

        private Button nextButton() {
            Button b = new Button();
            b.setId("next-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().nextBoard());
                }
            });
            return b;
        }

        private Button prevButton() {
            Button b = new Button();
            b.setId("prev-button");
            b.getStyleClass().add("replay-button");
            b.setOnAction(e -> {
                if (!getBoardPane().getMoves().isEmpty()) {
                    getBoardPane().redrawBoard(getBoardPane().previousBoard());
                }
            });
            return b;
        }

        public HBox replayControls() {
            HBox hb = new HBox();
            hb.getChildren().addAll(getBtPrevPrev(), getBtPrev(), getBtNext(), getBtNextNext());
            hb.setAlignment(Pos.BASELINE_RIGHT);
            return hb;
        }

        public BorderPane bottomControls() {
            BorderPane bp = new BorderPane();
            bp.setLeft(getWhiteSide());
            bp.setRight(replayControls());
            return bp;
        }

        public void swapSides() {
            Stack<Node> tempStack = new Stack<>();
            tempStack.push(getBlackSide().getChildren().get(0));
            tempStack.push(getBlackSide().getChildren().get(1));
            getBlackSide().getChildren().clear();
            getBlackSide().getChildren().addAll(getWhiteSide().getChildren().remove(1), getWhiteSide().getChildren().remove(0));
            getWhiteSide().getChildren().clear();
            getWhiteSide().getChildren().addAll(tempStack.pop(), tempStack.pop());
        }

        public void makeAMove() {
            if (getBoardPane().isRunning()) {
                getBoardPane().getEngine().forceMoveExecution();
            }
        }

        public void resign() {
            getBoardPane().disablePawnPromotionMode();
            if (!isGameOver()) {
                interruptComputerMove();
                getGamesViewer().gameOver();
                if (getBoardPane().getBoard().whitePlayer().getPlayerType().isHuman()) {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().whitePlayer(), getBoardPane().getBoard().currentPlayer());
                }
                else {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().blackPlayer(), getBoardPane().getBoard().currentPlayer());
                }
            }
        }


        public void makeDraw() {
            getBoardPane().disablePawnPromotionMode();
            if (!isGameOver()) {
                interruptComputerMove();
                //TODO
                getGamesViewer().gameOver();
                getBoardPane().updateResultAsDraw(getBoardPane().getBoard().currentPlayer());
            }
        }

        public void win() {
            getBoardPane().disablePawnPromotionMode();
            if (!isGameOver()) {
                interruptComputerMove();
                //TODO
                getGamesViewer().gameOver();
                if (getBoardPane().getBoard().whitePlayer().getPlayerType().isComputer()) {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().whitePlayer(), getBoardPane().getBoard().currentPlayer());
                }
                else {
                    getBoardPane().updateResultAsLoss(getBoardPane().getBoard().blackPlayer(), getBoardPane().getBoard().currentPlayer());
                }
            }
        }

        public void resetGame() {
            getBoardPane().disablePawnPromotionMode();
            Stack<Move> moves = getBoardPane().getMoves();
            if (!moves.isEmpty() && !isGameOver()) {
                interruptComputerMove();
                Board startOverBoard = Board.createStandardGameBoard(getBoardPane().getBoard().whitePlayer().getPlayerType(),
                        getBoardPane().getBoard().blackPlayer().getPlayerType());
                getBoardPane().resetMoveSelection();
                getBoardPane().getMoves().clear();
                getBoardPane().updateBoard(startOverBoard);
                getWhiteMoveText().setText("1.?");
                getBlackMoveText().setText("");
            }
        }

        public void takeBack() {
            getBoardPane().disablePawnPromotionMode();
            Stack<Move> moves = getBoardPane().getMoves();
            if (getBoardPane().getBoard().blackPlayer().getPlayerType().isHuman() && 
                getBoardPane().getBoard().whitePlayer().getPlayerType().isHuman()) {
                if (!moves.isEmpty()) {
                    Board board = moves.pop().getBoard();
                    getBoardPane().updateBoard(board);
                }
            }
            else {
                if (!moves.isEmpty() && !isGameOver()) {
                    interruptComputerMove();
                    Player currentPlayer = getBoardPane().getBoard().currentPlayer();
                    if (currentPlayer.getPlayerType().isComputer()) {
                        Board board = moves.pop().getBoard();
                        getBoardPane().updateBoard(board);
                    }
                    else if (currentPlayer.getPlayerType().isHuman() && moves.size() > 1) {
                        moves.pop();
                        Board board = moves.pop().getBoard();
                        getBoardPane().updateBoard(board);
                    }
                }
            }
        }

        private void updateMovesText(final Board board, final Move move, final int moveNumber) {
            if (move == null) {
                getWhiteMoveText().setText("1.?");
                getBlackMoveText().setText("");
            }
            else {
                String moveToString = getBoardPane().checkOrCheckMate(board, move);
                if (moveNumber % 2 == 0) {
                    getBlackMoveText().setText(moveNumber / 2 + "..." + moveToString);
                    getWhiteMoveText().setText(moveNumber / 2 + 1 + ".?");
                }
                else {
                    getBlackMoveText().setText((moveNumber + 1) / 2 + "...?");
                    getWhiteMoveText().setText((moveNumber + 1) / 2 + "." + moveToString);
                }
            }
        }

        public void interruptComputerMove() {
            if (getBoardPane().getFutureComputerMove() != null &&
                getBoardPane().getBoard().currentPlayer().getPlayerType().isComputer()) {
                getBoardPane().setInterrupt(true);
                if (getBoardPane().isRunning())
                    getBoardPane().getEngine().forceMoveExecution();
                else 
                    getBoardPane().getFutureComputerMove().cancel(true);
            }
        }

        public StackPane getBoardBackground() {
            return boardBackground;
        }

        public BoardPane getBoardPane() {
            return boardPane;
        }

        public Text getWhiteMoveText() {
            return whiteMoveText;
        }

        public Text getBlackMoveText() {
            return blackMoveText;
        }

        public HBox getWhiteSide() {
            return whiteSide;
        }

        public HBox getBlackSide() {
            return blackSide;
        }

        public Button getBtPrevPrev() {
            return btPrevPrev;
        }

        public Button getBtPrev() {
            return btPrev;
        }

        public Button getBtNext() {
            return btNext;
        }

        public Button getBtNextNext() {
            return btNextNext;
        }

        public int getGameID() {
            return gameID;
        }

        /**
         * @return the showable
         */
        public boolean isShowable() {
            return showable;
        }

        /**
         * @param showable the showable to set
         */
        public void setShowable(boolean showable) {
            this.showable = showable;
        }

        /**
         * @return the gameOver
         */
        public boolean isGameOver() {
            return gameOver;
        }

        /**
         * @param gameOver the gameOver to set
         */
        public void setGameOver(boolean gameOver) {
            this.gameOver = gameOver;
        }
    }

    public final class BoardPane extends GridPane {

        private final GamePane game;
        private final List<TilePane> boardTiles = new ArrayList<>();
        private final Stack<Move> moves = new Stack<>();
        private Board board;
        private final int depth;
        private Piece pieceToMove;
        private Tile sourceTile;
        private Tile destinationTile;
        private Move lastMove;
        private boolean cheat;
        private volatile boolean interrupt;
        private Future<?> futureComputerMove;
        private volatile boolean running;
        private int currentBoardIndex = 0;
        private boolean promotionMode;
        private final Engine engine;
        private String gameOverText = "";

        public BoardPane(GamePane game, Board board, Engine engine, int depth) {
            this.game = game;
            this.board = board;
            this.engine = engine;
            this.depth = depth;
            initialize();
        }

        public void initialize() {
            int tileIndex = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    final TilePane tilePane = new TilePane(this, tileIndex);
                    getBoardTiles().add(tilePane);
                    add(tilePane, j, i);
                    tileIndex++;
                }
            }
            updateBoard(getBoard());
        }

        public synchronized void updateBoard(Board board) {
            
            setBoard(board);
            setCurrentBoardIndex(getMoves().size());
            if (!getMoves().isEmpty()) {
                getGame().updateMovesText(board, getMoves().peek(), getMoves().size());
                setLastMove(getMoves().peek());
            }
            else {
                setLastMove(null);
            }
            checkGameOver(board);
            redrawBoard(board);
            if (board.currentPlayer().getPlayerType().isComputer() && !getGame().isGameOver()) {
                makeMove();
            }
        }

        public synchronized void redrawBoard(Board board) {
            getChildren().clear();
            int index = 0;
            for (int i = 0; i < BoardUtils.NUM_TILES_PER_ROW; i++) {
                for (int j = 0; j < BoardUtils.NUM_TILES_PER_ROW; j++) {
                    this.getBoardTiles().get(index).drawTile(board);
                    add(this.getBoardTiles().get(index), j, i);
                    index++;
                }
            }
        }

        public void makeMove() {
            AIThinker makeComputerMove = new AIThinker();
            if (getGame().getGameID() == getGamesViewer().updatePage(GAME_NOT_OVER_PAUSE_TIME)) {
                if (getMoves().isEmpty())
                    setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 1500, TimeUnit.MILLISECONDS));
                else 
                    setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 200, TimeUnit.MILLISECONDS));
            }
            else 
                setFutureComputerMove(getComputerMoveExecutor().schedule(makeComputerMove, 1500, TimeUnit.MILLISECONDS));
        }

        private String checkOrCheckMate(final Board board, final Move move) {
            if (board.currentPlayer().isInCheckMate())
                return move + "#";
            if (board.currentPlayer().isInCheck())
                return move + "+";
            return move.toString();
        }

        private void checkGameOver(final Board board) {
            if (board.currentPlayer().isInCheckMate()) {
                getGame().setGameOver(true);
                updateResultAsLoss(board.currentPlayer(), board.currentPlayer());
                getGamesViewer().gameOverControls();
            }
            else if (board.currentPlayer().isInStalemate()) {
                getGame().setGameOver(true);
                updateResultAsDraw(board.currentPlayer());
                getGamesViewer().gameOverControls();
            }
            if (getGame().isGameOver() && 
                getGame().isShowable() && 
                getGamesViewer().getCurrentGame() == getGame()) {
                getGamesViewer().gameOver();
            }
        }

        private void updateResultAsLoss(final Player playerThatLost, final Player currentPlayer) {
            if (playerThatLost.getAlliance().isWhite()) {
                setGameOverText("0-1");
                if (currentPlayer.getAlliance().isWhite())
                    getGame().getWhiteMoveText().setText("0-1");
                else
                    getGame().getBlackMoveText().setText("0-1");
                if (getBoard().blackPlayer().getPlayerType().isComputer())
                    getGamesViewer().setScoreComputer(getGamesViewer().getScoreComputer() + 1);
                else
                    getGamesViewer().setScoreHuman(getGamesViewer().getScoreHuman() + 1);
            }
            else {
                setGameOverText("1-0");
                if (currentPlayer.getAlliance().isWhite())
                    getGame().getWhiteMoveText().setText("1-0");
                else
                    getGame().getBlackMoveText().setText("1-0");
                if (getBoard().whitePlayer().getPlayerType().isComputer())
                    getGamesViewer().setScoreComputer(getGamesViewer().getScoreComputer() + 1);
                else
                    getGamesViewer().setScoreHuman(getGamesViewer().getScoreHuman() + 1);
            }
            updateFooter();
        }

        private void updateResultAsDraw(final Player playerToMove) {
            setGameOverText("1/2-1/2");
            if (playerToMove.getAlliance().isWhite())
                getGame().getWhiteMoveText().setText("1/2-1/2");
            else
                getGame().getBlackMoveText().setText("1/2-1/2");
            getGamesViewer().setScoreHuman(getGamesViewer().getScoreHuman() + 0.5);
            getGamesViewer().setScoreComputer(getGamesViewer().getScoreComputer() + 0.5);
            updateFooter();
        }

        private void resetMoveSelection() {
            setSourceTile(null);
            setDestinationTile(null);
            setPieceToMove(null);
        }

        public Board previousBoard() {
            disablePawnPromotionMode();
            if (getCurrentBoardIndex() < 2) {
                return firstBoard();
            }
            setCurrentBoardIndex(getCurrentBoardIndex() - 1);
            Board previousBoard = getMoves().get(getCurrentBoardIndex()).getBoard();
            Move move = getMoves().get(getCurrentBoardIndex() - 1);
            setLastMove(move);
            resetMoveSelection();
            getGame().updateMovesText(previousBoard, move, getCurrentBoardIndex());
            return previousBoard;
        }

        public Board nextBoard() {
            disablePawnPromotionMode();
            if (getCurrentBoardIndex() >= getMoves().size() - 1) {
                return lastBoard();
            }
            setCurrentBoardIndex(getCurrentBoardIndex() + 1);
            Board nextBoard = getMoves().get(getCurrentBoardIndex()).getBoard();
            Move move = getMoves().get(getCurrentBoardIndex() - 1);
            setLastMove(move);
            resetMoveSelection();
            getGame().updateMovesText(nextBoard, move, getCurrentBoardIndex());
            return nextBoard;
        }

        public Board firstBoard() {
            disablePawnPromotionMode();
            setCurrentBoardIndex(0);
            Board firstBoard = getMoves().get(getCurrentBoardIndex()).getBoard();
            setLastMove(null);
            resetMoveSelection();
            getGame().updateMovesText(firstBoard, null, 0);
            return firstBoard;
        }

        public Board lastBoard() {
            disablePawnPromotionMode();
            setCurrentBoardIndex(getMoves().size());
            Move move = getMoves().get(getCurrentBoardIndex() - 1);
            setLastMove(move);
            resetMoveSelection();
            getGame().updateMovesText(getBoard(), move, getCurrentBoardIndex());
            return getBoard();
        }

        public Board getCurrentViewBoard() {
            if (getMoves().isEmpty() || getCurrentBoardIndex() == getMoves().size())
                return getBoard();
            if (getCurrentBoardIndex() == 0)
                return firstBoard();
            return getMoves().get(getCurrentBoardIndex()).getBoard();
        }
             
        private void enablePawnPromotionMode(Tile promotionTile) {
            setPromotionMode(true);
            disableTiles(true);
            setDestinationTile(promotionTile);
        }
        
        private void disablePawnPromotionMode() {
            setPromotionMode(false);
            disableTiles(false);
        }
        
        private void disableTiles(boolean disable) {
            double opacity = 1.0;
            if (disable) 
                opacity = 0.3;
            for (TilePane tile : getBoardTiles())
                tile.setOpacity(opacity);
        }

        public int getCurrentBoardIndex() {
            return currentBoardIndex;
        }

        public void setCurrentBoardIndex(int currentBoardIndex) {
            this.currentBoardIndex = currentBoardIndex;
        }

        public boolean isCheat() {
            return cheat;
        }

        public void setCheat(boolean cheat) {
            this.cheat = cheat;
        }

        public List<TilePane> getBoardTiles() {
            return boardTiles;
        }

        public Stack<Move> getMoves() {
            return moves;
        }

        public Board getBoard() {
            return board;
        }

        public Piece getPieceToMove() {
            return pieceToMove;
        }

        public Tile getSourceTile() {
            return sourceTile;
        }

        public Tile getDestinationTile() {
            return destinationTile;
        }

        public void setBoard(Board board) {
            this.board = board;
        }

        public GamePane getGame() {
            return game;
        }

        private void setPieceToMove(Piece pieceToMove) {
            this.pieceToMove = pieceToMove;
        }

        private void setSourceTile(Tile sourceTile) {
            this.sourceTile = sourceTile;
        }

        private void setDestinationTile(Tile destinationTile) {
            this.destinationTile = destinationTile;
        }

        public Future<?> getFutureComputerMove() {
            return futureComputerMove;
        }

        public void setFutureComputerMove(Future<?> futureComputerMove) {
            this.futureComputerMove = futureComputerMove;
        }

        public boolean isInterrupt() {
            return interrupt;
        }

        public void setInterrupt(boolean interrupt) {
            this.interrupt = interrupt;
        }

        public int getDepth() {
            return depth;
        }

        public boolean isRunning() {
            return running;
        }

        public void setRunning(boolean running) {
            this.running = running;
        }

        public Move getLastMove() {
            return lastMove;
        }

        public void setLastMove(Move lastMove) {
            this.lastMove = lastMove;
        }

        /**
         * @return the promotionMode
         */
        public boolean isPromotionMode() {
            return promotionMode;
        }

        /**
         * @param promotionMode the promotionMode to set
         */
        public void setPromotionMode(boolean promotionMode) {
            this.promotionMode = promotionMode;
        }

        /**
         * @return the engine
         */
        public Engine getEngine() {
            return engine;
        }        

        /**
         * @return the gameOverText
         */
        private String getGameOverText() {
            return gameOverText;
        }

        /**
         * @param gameOverText the gameOverText to set
         */
        private void setGameOverText(String gameOverText) {
            this.gameOverText = gameOverText;
        }
        
        private class AIThinker implements Runnable {

            @Override
            public void run() {
                setRunning(true);
                if (!getGame().isGameOver()) {
                    Move bestMove = getEngine().executeMove(getDepth(), getBoard());
                    if (!isInterrupt()) {
                        getMoves().push(bestMove);
                        Platform.runLater(() -> {
                            updateBoard(getBoard().currentPlayer().makeMove(bestMove).getTransitionBoard());
                        });
                    }
                }
                setInterrupt(false);
                setRunning(false);
            }
        }
    }


    public final class TilePane extends StackPane {

        private final int tileID;
        private final BoardPane boardPane;
        private final String WHITE_TILE = "-fx-background-image: url('images/lightwoodtile.jpg');";
        private final String BLACK_TILE = "-fx-background-image: url('images/darkwoodtile.jpg');";
        private final ImageView pieceImageView = new ImageView();
        private final Region tileSelection = new Region();

        public TilePane(final BoardPane boardPane, final int tileID) {
            this.boardPane = boardPane;
            this.tileID = tileID;
            initializeTile();
        }
        
        private void initializeTile() {
            drawTile(boardPane.getBoard());
            setOnMouseClicked(e -> {
                Board chessBoard = getBoardPane().getBoard();
                if (!getBoardPane().getGame().isGameOver() &&
                    chessBoard.currentPlayer().getPlayerType().isHuman() &&
                    getGamesViewer().getSt().getStatus() != Animation.Status.RUNNING) {
                    if (e.getButton() == MouseButton.PRIMARY) {
                        if (getBoardPane().getSourceTile() == null) {
                            // first click
                            getBoardPane().setSourceTile(chessBoard.getTile(getTileID()));
                            getBoardPane().setPieceToMove(getBoardPane().getSourceTile().getPiece());
                            if (getBoardPane().getPieceToMove() == null ||
                                    getBoardPane().getPieceToMove().getPieceAlliance() != chessBoard.currentPlayer().getAlliance()) {
                                getBoardPane().setSourceTile(null);
                            }
                        }
                        // second click or third click if promotionMode is on
                        else {
                            final Move move;
                            final int sourceCoordinate = getBoardPane().getSourceTile().getTileCoordinate();
                            //Pawn promotion
                            if (getBoardPane().getPieceToMove().getPieceType().isPawn() && 
                                chessBoard.currentPlayer().getAlliance().isPawnPromotionSquare(getTileID()) &&
                                !getBoardPane().isPromotionMode()) {
                                //check if the move is a legal move
                                move = Move.MoveFactory.createMove(chessBoard, 
                                        sourceCoordinate, 
                                        getTileID());
                                final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                                if (transition.getMoveStatus().isDone()) 
                                    getBoardPane().enablePawnPromotionMode(chessBoard.getTile(getTileID()));
                            }
                            else {
                                if (getBoardPane().isPromotionMode())
                                    move = getPromotionMove();
                                else {
                                    getBoardPane().setDestinationTile(chessBoard.getTile(getTileID()));
                                    move = Move.MoveFactory.createMove(chessBoard,
                                        sourceCoordinate,
                                        getBoardPane().getDestinationTile().getTileCoordinate());
                                }
                                final MoveTransition transition = chessBoard.currentPlayer().makeMove(move);
                                if (transition.getMoveStatus().isDone()) {
                                    getBoardPane().getMoves().push(move);
                                    chessBoard = transition.getTransitionBoard();
                                }
                                getBoardPane().resetMoveSelection();
                                getBoardPane().disablePawnPromotionMode();
                            }
                        }

                    }
                    else if (e.getButton() == MouseButton.SECONDARY) {
                        getBoardPane().resetMoveSelection();
                        getBoardPane().disablePawnPromotionMode();
                    }
                    this.getBoardPane().updateBoard(chessBoard);
                }
            });
            // Resizing
            resizeTile();
            scene.widthProperty().addListener(InvalidationListener -> {
                resizeTile();
            });

            scene.heightProperty().addListener(InvalidationListener -> {
                resizeTile();
            });
        }

        private void resizeTile() {
            double width = (scene.getWidth() - 110) / 8;
            double height = (scene.getHeight() - 230) / 8;
            if (height > 30 && width > 30) {
                if (height < width) {
                    getPieceImageView().setFitHeight(height);
                    getPieceImageView().setFitWidth(height);
                }
                else {
                    getPieceImageView().setFitHeight(width);
                    getPieceImageView().setFitWidth(width);
                }
            }
        }

        private void drawTile(final Board board) {
            getStyleClass().clear();
            getChildren().clear();
            setTileColor();
            assignSelectionRectangle(board);
            assignPieceOnTile(board);
            if (getBoardPane().isPromotionMode())
                drawPromotionMode(board);
        }

        private void assignSelectionRectangle(final Board board) {
            getTileSelection().getStyleClass().clear();
            if (getBoardPane().getSourceTile() != null) {
                if (getBoardPane().getSourceTile().getTileCoordinate() == this.getTileID()) {
                    highlightSourceTile(getTileSelection(), getBoardPane().getPieceToMove().getPieceAlliance());
                }
                else {
                    highlightLegals(getTileSelection(), board);
                }
            }
            else {
                Move lastMove = getBoardPane().getLastMove();
                if (lastMove != null) {
                    if (lastMove.getCurrentCoordinate() == getTileID()) {
                        highlightSourceTile(getTileSelection(), lastMove.getMovedPiece().getPieceAlliance());
                    }
                    else if (lastMove.getDestinationCoordinate() == getTileID()) {
                        highlightMoveDestinationTile(getTileSelection(), lastMove.getMovedPiece().getPieceAlliance());
                    }
                }
            }
            getChildren().add(getTileSelection());
        }

        private void assignPieceOnTile(final Board board) {
            getPieceImageView().setImage(null);
            if (getBoardPane().isCheat() || cheatAll) {
                if (board.getTile(this.getTileID()).isTileOccupied()) {
                    final Image pieceImage = new Image("images/pieces/" + board.getTile(this.getTileID()).getPiece().getPieceAlliance().toString().substring(0, 1)
                            + board.getTile(this.getTileID()).getPiece().toString() + ".png");
                    getPieceImageView().setImage(pieceImage);
                }
            }
            this.getChildren().add(getPieceImageView());
        }

        private void setTileColor() {
            if (BoardUtils.FIRST_ROW[this.getTileID()] ||
                    BoardUtils.THIRD_ROW[this.getTileID()] ||
                    BoardUtils.FIFTH_ROW[this.getTileID()] ||
                    BoardUtils.SEVENTH_ROW[this.getTileID()]) {
                setStyle(this.getTileID() % 2 == 0 ? WHITE_TILE : BLACK_TILE);
            } else if (BoardUtils.SECOND_ROW[this.getTileID()] ||
                    BoardUtils.FOURTH_ROW[this.getTileID()] ||
                    BoardUtils.SIXTH_ROW[this.getTileID()] ||
                    BoardUtils.EIGHT_ROW[this.getTileID()]) {
                setStyle(this.getTileID() % 2 != 0 ? WHITE_TILE : BLACK_TILE);
            }
        }

        private void highlightSourceTile(final Region tileSelection, final Alliance alliance) {
            if (!getBoardPane().isPromotionMode()) {
                if (alliance.isWhite())
                    tileSelection.getStyleClass().add("source-tile-white");
                else
                    tileSelection.getStyleClass().add("source-tile-black");
            }
        }

        private void highlightMoveDestinationTile(final Region tileSelection, final Alliance alliance) {
            if (alliance.isWhite())
                tileSelection.getStyleClass().add("move-destination-tile-white");
            else
                tileSelection.getStyleClass().add("move-destination-tile-black");
        }

        private void highlightDestinationTile(final Region tileSelection, final Alliance alliance) {
            if (!getBoardPane().isPromotionMode()) {
                if (alliance.isWhite())
                    tileSelection.getStyleClass().add("destination-tile-white");
                else
                    tileSelection.getStyleClass().add("destination-tile-black");
            }
        }

        private void highlightLegals(final Region tileSelection, final Board board) {
            if (isHighlightLegalMoves() && !getBoardPane().isPromotionMode()) {
                if (getBoardPane().getPieceToMove() != null &&
                    getBoardPane().getPieceToMove().getPieceAlliance() == board.currentPlayer().getAlliance()) {
                    final Move move = Move.MoveFactory.createMove(board, getBoardPane().getSourceTile().getTileCoordinate(), this.getTileID());
                    final MoveTransition transition = board.currentPlayer().makeMove(move);
                    if (transition.getMoveStatus().isDone()) {
                        highlightDestinationTile(tileSelection, getBoardPane().getPieceToMove().getPieceAlliance());
                    }
                }
            }
        }
            
        private Move getPromotionMove() {
            int promotionCoordinate = getBoardPane().getDestinationTile().getTileCoordinate();
            int promotionOffset = BoardUtils.NUM_TILES_PER_ROW * 
                    getBoardPane().getBoard().currentPlayer().getAlliance().getOppositeDirection();
            Pawn promotedPawn = (Pawn)getBoardPane().getSourceTile().getPiece();
            if (getTileID() == promotionCoordinate)
                return new PawnPromotion(decoratedMove(promotedPawn.getPromotionQueen()));
            if (getTileID() == promotionCoordinate + promotionOffset)
                return new PawnPromotion(decoratedMove(promotedPawn.getPromotionRook()));
            if (getTileID() == promotionCoordinate + 2 * promotionOffset)
                return new PawnPromotion(decoratedMove(promotedPawn.getPromotionKnight()));
            if (getTileID() == promotionCoordinate + 3 * promotionOffset)
                return new PawnPromotion(decoratedMove(promotedPawn.getPromotionBishop()));
            return Move.NULL_MOVE;
        }
        
        private Move decoratedMove(Piece piece) {
            Tile destination = getBoardPane().getDestinationTile();
            if (destination.isTileOccupied())
                return new PawnPromotion(new AttackMove(getBoardPane().getBoard(), piece, destination.getTileCoordinate(), destination.getPiece()));
            return new PawnPromotion(new MajorMove(getBoardPane().getBoard(), piece, destination.getTileCoordinate()));
        }
                
        private void drawPromotionMode(final Board board) {
            int promotionCoordinate = getBoardPane().getDestinationTile().getTileCoordinate();
            int promotionOffset = BoardUtils.NUM_TILES_PER_ROW * 
                    getBoardPane().getBoard().currentPlayer().getAlliance().getOppositeDirection();
            if (getTileID() == getBoardPane().getSourceTile().getTileCoordinate()) {
                getPieceImageView().setImage(null);
            }
            if (getTileID() == promotionCoordinate) {
                getStyleClass().add("promotion-tile");
                getPieceImageView().setImage(new Image("images/pieces/" + board.currentPlayer().getAlliance().toString()
                            + "Q.png"));
            }
            else if (getTileID() == promotionCoordinate + promotionOffset) {
                getStyleClass().add("promotion-tile");
                getPieceImageView().setImage(new Image("images/pieces/" + board.currentPlayer().getAlliance().toString()
                            + "R.png"));
            }
            else if (getTileID() == promotionCoordinate + 2 * promotionOffset) {
                getStyleClass().add("promotion-tile");
                getPieceImageView().setImage(new Image("images/pieces/" + board.currentPlayer().getAlliance().toString()
                            + "N.png"));
            }
            else if (getTileID() == promotionCoordinate + 3 * promotionOffset) {
                getStyleClass().add("promotion-tile");
                getPieceImageView().setImage(new Image("images/pieces/" + board.currentPlayer().getAlliance().toString()
                            + "B.png"));
            }
        }

        public int getTileID() {
            return this.tileID;
        }

        public BoardPane getBoardPane() {
            return this.boardPane;
        }

        public ImageView getPieceImageView() {
            return pieceImageView;
        }

        public Region getTileSelection() {
            return tileSelection;
        }
    }
}
