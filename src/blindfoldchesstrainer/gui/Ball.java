/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.gui;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.effect.Glow;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;


/**
 *
 * @author Anton
 */
public class Ball extends Circle {
    
    private final BallType ballType;
    private final double radius;
    
    public Ball(BallType ballType, double radius) {
        this.ballType = ballType;
        this.radius = radius;
        initBall();
    }

    private void initBall() {
        final RadialGradient gradient = new RadialGradient(0, 0, 0.3, 0.3, 0.5, true, CycleMethod.NO_CYCLE, ballType.getStops());
        setRadius(radius);
        setFill(gradient);
    }
    
    public enum BallType {
        
        WHITE {
            @Override
            public List<Stop> getStops() {
                List<Stop> stops = new ArrayList<>();
                stops.add(new Stop(0, Color.web("#ffffff")));
                stops.add(new Stop(0.3, Color.web("#dddddd")));
                stops.add(new Stop(1.0, Color.web("#bbbbbb")));
                return stops;
            }
        },
        BLACK {
            @Override
            public List<Stop> getStops() {
                List<Stop> stops = new ArrayList<>();
                stops.add(new Stop(0, Color.web("#bbbbbb")));
                stops.add(new Stop(0.3, Color.web("#888888")));
                stops.add(new Stop(1.0, Color.web("#404040")));
                return stops;
            }
        };
        
        public abstract List<Stop> getStops();
    }
    
}
