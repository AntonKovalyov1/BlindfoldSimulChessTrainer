/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine.uci;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;

/**
 *
 * @author Anton
 */
public class UCIEngine {
    
    private final String fileString;
    private UCIChess engine;
    private volatile boolean forceMoveExecution;
    private volatile boolean done;
    private boolean started;
    public final long MAX_THINK_TIME = 3000;
    public final String BOOK_FILE_CMD = "setoption name Book File value src\\Engines\\book.bin";
    public final String THREADS_NUMBER_CMD = "setoption name Threads value 1";
    public final String OWN_BOOK_CMD = "setoption name OwnBook value true";
    public final String BEST_BOOK_LINE_CMD = "setoption name Best Book Line value false";
    
    public UCIEngine(String fileString) {
        this.fileString = fileString;
    }
    
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
                    return true;
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
    
    public Move executeMove(final int depth, final Board board) {
        setDone(false);
        setupGame(board);
        engine.go_Think_Depth(depth);
        new Thread(() -> {
            try {
                long thinkingTime = 0;
                while(!forceMoveExecution && !done && thinkingTime < MAX_THINK_TIME) {
                    Thread.sleep(10);    
                    thinkingTime += 10;
                }
                engine.send_uci_cmd("stop");
            }
            catch (InterruptedException ex) {
                engine.stop_Engine();
            }
        }).start();
        Move engineMove = UCIUtilities.getMoveFromUCIformat(engine.get_BestMove(true), board);
        setDone(true);
        return engineMove;
    }
    
        
    private String getEngineName(String path) {
        return path.substring(path.lastIndexOf("\\") + 1, path.lastIndexOf("."));
    }

    public void close() {
        engine.stop_Engine();
    }

    /**
     * @return the forceMoveExecution
     */
    public boolean isForceMoveExecution() {
        return forceMoveExecution;
    }

    /**
     * @param forceMoveExecution the forceMoveExecution to set
     */
    public void setForceMoveExecution(boolean forceMoveExecution) {
        this.forceMoveExecution = forceMoveExecution;
    }

    /**
     * @return the done
     */
    public boolean isDone() {
        return done;
    }

    /**
     * @param done the done to set
     */
    public void setDone(boolean done) {
        this.done = done;
    }
    
    @Override
    public String toString() {
        return getEngineName(fileString);
    }

    /**
     * @return the fileString
     */
    public String getFileString() {
        return fileString;
    }

    /**
     * @return the started
     */
    public boolean isStarted() {
        return started;
    }

    /**
     * @param started the started to set
     */
    public void setStarted(boolean started) {
        this.started = started;
    }

    public boolean isReady() {
        return engine.get_ReadyOk(false);
    }
}
