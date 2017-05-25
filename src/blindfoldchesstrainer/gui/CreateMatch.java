package blindfoldchesstrainer.gui;

import blindfoldchesstrainer.engine.RandomEngine;
import blindfoldchesstrainer.engine.*;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 * Created by Anton on 3/15/2017.
 */
public class CreateMatch extends Stage {

    private int numberOfGames;
    private ColorChoice colorChoice;
    private Difficulty difficulty;
    private Engine engine;
    private final double PREF_CB_WIDTH = 165;
    private final double PREF_GAMES_WIDTH = 80;
    private final double PREF_LABELS_WIDTH = 160;
    private final double SPACING = 10;
    private final int maxGamesNumber;

    public CreateMatch(String title, List<Engine> engines, int maxGamesNumber) {
        setTitle(title);
        numberOfGames = -1;
        colorChoice = ColorChoice.WHITE;
        difficulty = Difficulty.EASY;
        this.maxGamesNumber = maxGamesNumber;
        initializeMatch(engines);
    }

    private void initializeMatch(List<Engine> engines) {
        getIcons().add(new Image("images/mainIcon.jpg"));
        final TextField numberOfGamesField = new TextField();
        numberOfGamesField.setPrefWidth(PREF_GAMES_WIDTH);
        final Label numberOfGamesLabel = new Label("Number of Games: ");
        numberOfGamesLabel.setPrefWidth(PREF_LABELS_WIDTH);
        numberOfGamesLabel.setAlignment(Pos.CENTER_RIGHT);
        final HBox numberOfGamesHB = new HBox(SPACING);
        numberOfGamesHB.getChildren().addAll(numberOfGamesLabel, numberOfGamesField);

        final ObservableList<ColorChoice> colorList = FXCollections.observableArrayList(ColorChoice.WHITE, ColorChoice.BLACK,
                ColorChoice.RANDOM);
        final Label colorLabel = new Label("Color: ");
        colorLabel.setPrefWidth(PREF_LABELS_WIDTH);
        colorLabel.setAlignment(Pos.CENTER_RIGHT);
        final ComboBox<ColorChoice> colorComboBox = new ComboBox<>(colorList);
        colorComboBox.setPrefWidth(PREF_CB_WIDTH);
        colorComboBox.setValue(colorList.get(0));
        final HBox colorHB = new HBox(SPACING);
        colorHB.getChildren().addAll(colorLabel, colorComboBox);

        final ObservableList<Difficulty> difficultyList = FXCollections.observableArrayList(Difficulty.EASY, Difficulty.MEDIUM,
                Difficulty.HARD, Difficulty.INSANE, Difficulty.RANDOM);
        final Label difficultyLabel = new Label("Difficulty: ");
        difficultyLabel.setPrefWidth(PREF_LABELS_WIDTH);
        difficultyLabel.setAlignment(Pos.CENTER_RIGHT);
        final HBox difficultyHB = new HBox(SPACING);
        final ComboBox<Difficulty> difficultyComboBox = new ComboBox<>(difficultyList);
        difficultyComboBox.setPrefWidth(PREF_CB_WIDTH);
        difficultyComboBox.setValue(difficultyList.get(0));
        difficultyHB.getChildren().addAll(difficultyLabel, difficultyComboBox);
        
        final ObservableList<Engine> enginesList = FXCollections.observableArrayList(engines);
        enginesList.add(new RandomEngine(engines));
        final Label enginesLabel = new Label("Engines: ");
        enginesLabel.setPrefWidth(PREF_LABELS_WIDTH);
        enginesLabel.setAlignment(Pos.CENTER_RIGHT);
        final HBox enginesHB = new HBox(SPACING);
        final ComboBox<Engine> enginesComboBox = new ComboBox<>(enginesList);
        enginesComboBox.setPrefWidth(PREF_CB_WIDTH);
        enginesComboBox.setValue(engines.get(0));
        enginesHB.getChildren().addAll(enginesLabel, enginesComboBox);

        Text errorText = new Text("");
        errorText.setFill(Color.RED);

        final Button playButton = new Button("Play!");
        playButton.setDefaultButton(true);
        playButton.setOnAction(e -> {
            try {
                numberOfGames = Integer.parseInt(numberOfGamesField.getText());
                if (getNumberOfGames() < 1 || getNumberOfGames() > maxGamesNumber) {
                    throw new Exception();
                }
                else {
                    colorChoice = (ColorChoice) colorComboBox.getValue();
                    difficulty = (Difficulty) difficultyComboBox.getValue();
                    engine = (Engine)enginesComboBox.getValue();
                    close();
                }
            }
            catch (Exception ex) {
                errorText.setText("Please enter a number from 1 to " + maxGamesNumber);
                numberOfGamesLabel.setTextFill(Color.RED);
                numberOfGames = -1;
            }
        });

        final Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> {
            numberOfGames = -1;
            close();
        });

        setOnCloseRequest(e -> {
            numberOfGames = -1;
            close();
        });

        final HBox buttonsHB = new HBox(5);
        buttonsHB.getChildren().addAll(playButton, cancelButton);
        buttonsHB.setAlignment(Pos.BOTTOM_RIGHT);
        buttonsHB.setPadding(new Insets(30, 10 ,0 ,0));

        VBox mainVB = new VBox(20);
        mainVB.setPadding(new Insets(30,10,10,20));
        mainVB.getChildren().addAll(numberOfGamesHB, colorHB, difficultyHB, enginesHB, errorText, buttonsHB);

        Scene scene = new Scene(mainVB);
        scene.getStylesheets().add("main.css");
        setScene(scene);
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        showAndWait();
    }

    public int getNumberOfGames() {
        return numberOfGames;
    }

    public ColorChoice getColorChoice() {
        return colorChoice;
    }

    public Difficulty getDifficulty() {
        return difficulty;
    }
    
    /**
     * @return the engine
     */
    public Engine getEngine() {
        return engine;
    }

    /**
     * @param engine the engine to set
     */
    public void setEngine(Engine engine) {
        this.engine = engine;
    }
}
