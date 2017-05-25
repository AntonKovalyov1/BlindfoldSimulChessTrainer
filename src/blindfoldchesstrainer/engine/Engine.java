/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine;

import blindfoldchesstrainer.engine.board.Board;
import blindfoldchesstrainer.engine.board.Move;
import java.util.List;

/**
 *
 * @author Anton
 */
public abstract class Engine {
    
    public Engine() {

    }
    
    public Engine(String pathString) {

    }
    
    public Engine(List<Engine> engines) {

    }
    
    public abstract boolean start();
    
    public abstract Move executeMove(final int depth, final Board board);
    
    public abstract void close();
                
    public abstract boolean isReady();

    public abstract void forceMoveExecution();
    
    public abstract boolean isRunning();
    
    public abstract String getEngineName();
    
}
