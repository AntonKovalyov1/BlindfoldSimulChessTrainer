/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine;

import blindfoldchesstrainer.engine.board.Move;

/**
 *
 * @author Anton
 */
public abstract class Engine {
    
    public abstract boolean start();
    
    public abstract Move executeMove();
    
    public abstract void close();
    
    public abstract boolean isStarted();
    
    public abstract boolean isReady();
}
