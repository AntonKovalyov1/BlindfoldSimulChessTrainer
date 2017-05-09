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
public class RandomEngine extends Engine {

    private final List<Engine> engines;
    private Engine currentEngine;
    
    public RandomEngine(List<Engine> engines) {
        super(engines);
        this.engines = engines;
        this.currentEngine = selectRandom(engines);
    }
    
    @Override
    public boolean start() {
        return currentEngine.start();
    }

    @Override
    public Move executeMove(final int depth, final Board board) {
        currentEngine = selectRandom(engines);
        return currentEngine.executeMove(depth, board);
    }

    @Override
    public void close() {
        for(Engine current : engines)
            current.close();
    }

    @Override
    public boolean isReady() {
        return currentEngine.isReady();
    }
    
    private Engine selectRandom(List<Engine> engines) {
        return engines.get((int)(Math.random() * engines.size()));
    }
    
    @Override
    public String toString() {
        return "Random";
    }

    @Override
    public void forceMoveExecution() {
        currentEngine.forceMoveExecution();
    }

    @Override
    public boolean isRunning() {
        return currentEngine.isRunning();
    }
}
