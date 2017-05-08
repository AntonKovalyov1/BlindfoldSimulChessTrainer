/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine.uci;

import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Anton
 */
public abstract class Engine {

    private final List<UCIEngine> engines;
    
    public Engine(final UCIEngine engine) {
        this.engines = new ArrayList<>();
        this.engines.add(engine);
    }
    
    public Engine(final List<UCIEngine> engines) {
        this.engines = engines;
    }
    
    public abstract UCIEngine getUCIEngine();

    /**
     * @return the engines
     */
    public List<UCIEngine> getEngines() {
        return engines;
    }
    
    public static final class RandomUCIEngine extends Engine {
        
        public RandomUCIEngine(final List<UCIEngine> engines) {
            super(engines);
        }
        
        @Override
        public UCIEngine getUCIEngine() {
            return getEngines().get((int)(Math.random() * getEngines().size()));
        }

        @Override
        public String toString() {
            return "Random";
        }
    }
    
    public static final class SpecificUCIEngine extends Engine {

        public SpecificUCIEngine(final UCIEngine engine) {
            super(engine);
        }
        
        @Override
        public UCIEngine getUCIEngine() {
            return getEngines().get(0);
        }
        
        @Override
        public String toString() {
            return getEngines().get(0).toString();
        }
    }
}
