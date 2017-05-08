package blindfoldchesstrainer.gui;

import blindfoldchesstrainer.engine.uci.Engine;
import blindfoldchesstrainer.engine.uci.Engine.RandomUCIEngine;
import blindfoldchesstrainer.engine.uci.UCIEngine;
import java.util.ArrayList;
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
    private final double PREF_CB_WIDTH = 180;
    private final double PREF_GAMES_WIDTH = 80;
    private final double PREF_LABELS_WIDTH = 160;

    public CreateMatch(String title, List<Engine> engines) {
        setTitle(title);
        numberOfGames = -1;
        colorChoice = ColorChoice.WHITE;
        difficulty = Difficulty.EASY;
        initializeMatch(engines);
    }

    private void initializeMatch(List<Engine> engines) {
        getIcons().add(new Image("images/mainIcon.jpg"));
        final TextField numberOfGamesField = new TextField();
        numberOfGamesField.setPrefWidth(PREF_GAMES_WIDTH);
        final Label numberOfGamesLabel = new Label("Number of Games: ");
        numberOfGamesLabel.setPrefWidth(PREF_LABELS_WIDTH);
        final HBox numberOfGamesHB = new HBox(10);
        numberOfGamesHB.getChildren().addAll(numberOfGamesLabel, numberOfGamesField);

        final ObservableList<ColorChoice> colorList = FXCollections.observableArrayList(ColorChoice.WHITE, ColorChoice.BLACK,
                ColorChoice.RANDOM);
        final Label colorLabel = new Label("Color: ");
        colorLabel.setPrefWidth(PREF_LABELS_WIDTH);
        final ComboBox<ColorChoice> colorComboBox = new ComboBox<>(colorList);
        colorComboBox.setPrefWidth(PREF_CB_WIDTH);
        colorComboBox.setValue(colorList.get(0));
        final HBox colorHB = new HBox(10);
        colorHB.getChildren().addAll(colorLabel, colorComboBox);

        final ObservableList<Difficulty> difficultyList = FXCollections.observableArrayList(Difficulty.EASY, Difficulty.MEDIUM,
                Difficulty.HARD, Difficulty.INSANE, Difficulty.RANDOM);
        final Label difficultyLabel = new Label("Difficulty: ");
        difficultyLabel.setPrefWidth(PREF_LABELS_WIDTH);
        final HBox difficultyHB = new HBox(10);
        final ComboBox<Difficulty> difficultyComboBox = new ComboBox<>(difficultyList);
        difficultyComboBox.setPrefWidth(PREF_CB_WIDTH);
        difficultyComboBox.setValue(difficultyList.get(0));
        difficultyHB.getChildren().addAll(difficultyLabel, difficultyComboBox);
        
        final ObservableList<Engine> enginesList = FXCollections.observableArrayList(engines);
        enginesList.add(makeRandomEngine(engines));
        final Label enginesLabel = new Label("Engines: ");
        enginesLabel.setPrefWidth(160);
        final HBox enginesHB = new HBox(10);
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
                if (getNumberOfGames() < 1 || getNumberOfGames() > 100) {
                    throw new Exception();
                }
                else {
                    colorChoice = (ColorChoice) colorComboBox.getValue();
                    difficulty = (Difficulty) difficultyComboBox.getValue();
                    engine = (Engine) enginesComboBox.getValue();
                    close();
                }
            }
            catch (Exception ex) {
                errorText.setText("Please enter a number from 1 to 100");
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
    
    public Engine makeRandomEngine(List<Engine> engines) {
        List<UCIEngine> uci_engines = new ArrayList<>();
        for (Engine engine : engines) {
            uci_engines.add(engine.getUCIEngine());
        }
        return new RandomUCIEngine(uci_engines);
    }
}
