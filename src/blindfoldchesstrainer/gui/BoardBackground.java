/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.gui;

import blindfoldchesstrainer.engine.board.BoardUtils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;

/**
 *
 * @author Anton
 */
public class BoardBackground extends AnchorPane {
    
    private final Color TEXT_COLOR = Color.web("#eeeeee");
    private final Font FONT = Font.font("Arial", FontWeight.BOLD, 12);
    private final double PADDING = 18;
    private final List<Label> numberLabels = initNumberLabels();
    private final List<Label> letterLabels = initLettersLables();
    private final VBox numbersVBox = initNumbersVBox(numberLabels);
    private final HBox lettersHBox = initLettersHBox(letterLabels);
    private final StackPane centerBackground = initCenterBackground();
    
    public BoardBackground() {
        init();
    }

    private void init() {
        getChildren().addAll(numbersVBox, lettersHBox, centerBackground);
        
        AnchorPane.setLeftAnchor(numbersVBox, 0.0);
        AnchorPane.setTopAnchor(numbersVBox, PADDING);
        AnchorPane.setBottomAnchor(numbersVBox, PADDING);
        
        AnchorPane.setLeftAnchor(lettersHBox, PADDING);
        AnchorPane.setRightAnchor(lettersHBox, PADDING);
        AnchorPane.setBottomAnchor(lettersHBox, 0.0);
        
        AnchorPane.setBottomAnchor(centerBackground, PADDING);
        AnchorPane.setTopAnchor(centerBackground, PADDING);
        AnchorPane.setLeftAnchor(centerBackground, PADDING);
        AnchorPane.setRightAnchor(centerBackground, PADDING);
    }

    private List<Label> initNumberLabels() {
        List<Label> n = new ArrayList<>();
        for (int i = BoardUtils.NUM_TILES_PER_ROW; i > 0; i--) {
            Label lb = new Label(String.valueOf(i));
            lb.setAlignment(Pos.CENTER);
            lb.setTextAlignment(TextAlignment.CENTER);
            lb.setFont(FONT);
            lb.setTextFill(TEXT_COLOR);
            lb.setMaxWidth(Double.MAX_VALUE);
            lb.setMaxHeight(Double.MAX_VALUE);
            VBox.setVgrow(lb, Priority.ALWAYS);
            n.add(lb);
        }
        return n;
    }

    private List<Label> initLettersLables() {
        List<Label> l = new ArrayList<>();
        for (int i = (int)'A'; i <= (int)'H'; i++) {
            Label lb = new Label(String.valueOf((char)i));
            lb.setAlignment(Pos.TOP_CENTER);
            lb.setTextAlignment(TextAlignment.CENTER);
            lb.setFont(FONT);
            lb.setTextFill(TEXT_COLOR);
            lb.setMaxWidth(Double.MAX_VALUE);
            lb.setMaxHeight(Double.MAX_VALUE);
            HBox.setHgrow(lb, Priority.ALWAYS);
            l.add(lb);           
        }
        return l;
    }
    
    private VBox initNumbersVBox(List<Label> numberLabels) {
        VBox vb = new VBox();
        vb.setPrefWidth(PADDING);
        vb.getChildren().addAll(numberLabels);
        return vb;
    }    

    private HBox initLettersHBox(List<Label> letterLabels) {
        HBox hb = new HBox();
        hb.setPrefHeight(PADDING);
        hb.getChildren().addAll(letterLabels);
        return hb;
    }

    private StackPane initCenterBackground() {
        StackPane s = new StackPane();
        
        return s;
    }
    
    public void disableCoordinates(boolean disable) {
        numbersVBox.getChildren().clear();
        lettersHBox.getChildren().clear();
        if (!disable) {
            numbersVBox.getChildren().addAll(numberLabels);
            lettersHBox.getChildren().addAll(letterLabels);
        }
    }
    
    public void flipCoordinates() {
        Collections.reverse(numberLabels);
        Collections.reverse(letterLabels);
        if (!numbersVBox.getChildren().isEmpty()) {
            numbersVBox.getChildren().clear();
            numbersVBox.getChildren().addAll(numberLabels);
            lettersHBox.getChildren().clear();
            lettersHBox.getChildren().addAll(letterLabels);
        }
    }
    
    /**
     * @return the centerBackground
     */
    public StackPane getCenterBackground() {
        return centerBackground;
    }
}
