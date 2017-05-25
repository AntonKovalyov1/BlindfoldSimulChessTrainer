package blindfoldchesstrainer.engine;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;
import blindfoldchesstrainer.engine.uci.UCIChess;
import blindfoldchesstrainer.engine.uci.UCIUtilities;

/**
 *
 * @author Anton
 */
public class UCIEngine extends Engine {
    
    private final String fileString;
    private UCIChess engine;
    private volatile boolean forceMoveExecution;
    private volatile boolean running;
    private boolean started;
    public final long MAX_THINK_TIME = 3000;
    public final String BOOK_FILE_CMD = "setoption name Book File value src\\Engines\\book.bin";
    public final String THREADS_NUMBER_CMD = "setoption name Threads value 2";
    public final String OWN_BOOK_CMD = "setoption name OwnBook value true";
    public final String BEST_BOOK_LINE_CMD = "setoption name Best Book Line value false";
    
    public UCIEngine(String fileString) {
        super(fileString);
        this.fileString = fileString;
    }
     
    @Override
    public boolean start() {
        try {
            engine = new UCIChess(fileString);
            if (engine.get_UciOk(false)) {
                engine.send_uciNewGame();
                if (engine.get_ReadyOk(false)) {
                    started = true;
                    engine.send_uci_cmd(THREADS_NUMBER_CMD);
                    engine.send_uci_cmd(OWN_BOOK_CMD);
                    engine.send_uci_cmd(BOOK_FILE_CMD);
                    engine.send_uci_cmd(BEST_BOOK_LINE_CMD);
                    return started;
                }
            }
        }
        catch (Exception ex) {
            return false;
        }
        return false;
    }
    
    private void setupGame(final Board board) {
        engine.move_FromFEN(UCIUtilities.createFENFromGame(board), "", true);
    }
    
    @Override
    public Move executeMove(final int depth, final Board board) {
        running = true;
        if (!started && !start())
            return Move.NULL_MOVE;
        setupGame(board);
        engine.go_Think_Depth(depth);
        new Thread(() -> {
            try {
                long thinkingTime = 0;
                while(!forceMoveExecution && running && thinkingTime < MAX_THINK_TIME) {
                    Thread.sleep(10);    
                    thinkingTime += 10;
                }
                engine.send_uci_cmd("stop");
            }
            catch (InterruptedException ex) {
                engine.send_uci_cmd("stop");
            }
        }).start();
        Move engineMove = UCIUtilities.getMoveFromUCIformat(engine.get_BestMove(false), board);
        forceMoveExecution = false;
        running = false;
        return engineMove;
    }
    
    @Override    
    public String getEngineName() {
        return fileString.substring(fileString.lastIndexOf("\\") + 1, 
                                    fileString.lastIndexOf("."));
    }

    @Override
    public void close() {
        if (started) {
            engine.stop_Engine();
            started = false;
        }
    }
    
    @Override
    public String toString() {
        return getEngineName();
    }

    /**
     * @return the fileString
     */
    public String getFileString() {
        return fileString;
    }

    @Override
    public boolean isReady() {
        return engine.get_ReadyOk(false);
    }

    @Override
    public void forceMoveExecution() {
        if (isRunning())
            forceMoveExecution = true;
    }

    @Override
    public boolean isRunning() {
        return running;
    }
}
