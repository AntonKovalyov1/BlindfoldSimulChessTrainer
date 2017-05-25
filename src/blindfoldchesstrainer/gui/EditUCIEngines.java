package blindfoldchesstrainer.gui;

import blindfoldchesstrainer.engine.*;
import blindfoldchesstrainer.engine.uci.ExecutableChooser;
import blindfoldchesstrainer.engine.UCIEngine;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ListView;
import javafx.scene.image.Image;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Modality;
import javafx.stage.Stage;

/**
 *
 * @author Anton
 */
public class EditUCIEngines extends Stage {
    
    private final ListView<Engine> enginesLV = new ListView<>();
    private final ObservableList<Engine> enginesList = FXCollections.observableArrayList();
    private final Text errorText = new Text("");
    private final List<Engine> updatedEnginesList = new ArrayList<>();
    private final String ENGINES_PATH = "src\\Engines\\Engines_List\\engines.dat";
    private final UCIEngine KOMODO_64bit = new UCIEngine("src\\Engines\\komodo-8-64bit.exe");
    private final UCIEngine STOCKFISH_64bit = new UCIEngine("src\\Engines\\stockfish_8_x64.exe");
    private final UCIEngine FIRE_64bit = new UCIEngine("src\\Engines\\Fire 5 x64.exe");
    private final int DEFAULT_ENGINES_NUM = 3;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean removable;
    
    public EditUCIEngines() {
        initialize();
    }
    
    private void initialize() {
        loadEnginesFile();
        enginesLV.setItems(enginesList);
        enginesLV.setFixedCellSize(30);
        enginesLV.setPrefWidth(280);
        enginesLV.setPrefHeight(320);
        
        final Button btAdd = createAddButton();
        
        final Button btRemove = createRemoveButton();
        
        final Button btOK = createOKButton();
        
        final Button btCancel = createCanceButton();
        
        final HBox hb1 = new HBox(5);
        hb1.getChildren().addAll(btAdd, btRemove);
        hb1.setAlignment(Pos.BASELINE_LEFT);
        hb1.setPadding(new Insets(0, 0, 10, 0));
        
        errorText.setFill(Color.RED);
        
        final HBox hb2 = new HBox(5);
        hb2.getChildren().addAll(btOK, btCancel);
        hb2.setAlignment(Pos.BASELINE_RIGHT);
        hb2.setPadding(new Insets(10, 10, 10, 10));
                
        final VBox vb = new VBox(5);
        vb.getChildren().addAll(enginesLV, hb1, errorText);
        vb.setPadding(new Insets(20, 40, 0, 20));
        
        final BorderPane bp = new BorderPane();
        bp.setCenter(vb);
        bp.setBottom(hb2);
        
        Scene scene = new Scene(bp);
        setScene(scene);
        scene.getStylesheets().add("main.css");
        setTitle("Edit Engines");
        getIcons().add(new Image("images/mainIcon.jpg"));
        setResizable(false);
        initModality(Modality.APPLICATION_MODAL);
        
        setOnCloseRequest(e -> {
            //Don't save changes
            errorText.setText("");
            close();
        });
    }
    
    @SuppressWarnings("unchecked")
    public void loadEnginesFile() {
        // Start and Add default engines
        initDefaultEngines();
        try {
            in = new ObjectInputStream(new BufferedInputStream(new FileInputStream(ENGINES_PATH)));
            List<String> list = (ArrayList<String>)in.readObject();
            for (String s : list) {
                initEngine(new UCIEngine(s));
            }
            in.close();
        }
        catch (IOException | ClassNotFoundException ex) {
            // The engines file doesn't exist or it's corrupted, do nothing.
        }
    }
    
    public void saveEnginesFile() {
        List<String> list = new ArrayList<>();
        for (int i = DEFAULT_ENGINES_NUM; i < updatedEnginesList.size(); i++) {
            if (updatedEnginesList.get(i) instanceof UCIEngine) {
                UCIEngine uci_engine = (UCIEngine)updatedEnginesList.get(i);
                list.add(uci_engine.getFileString());
            }
        }
        try {
            out = new ObjectOutputStream(new BufferedOutputStream(new FileOutputStream(ENGINES_PATH)));
            out.writeObject(list);
            out.close();
        }
        catch (FileNotFoundException ex) {
            // do nothing
        }
        catch (IOException ex) {
            // do nothing
        }
    }

    private Button createAddButton() {
        Button b = new Button("Add");
        b.setOnAction(e -> {
            if (enginesList.size() > 9)
               errorText.setText("The limit of 10 engines has been reached");
            else {
                String newEnginePath = ExecutableChooser.newExecutable();
                if (newEnginePath != null) {
                    UCIEngine newEngine = new UCIEngine(newEnginePath);
                    if (newEngine.start()) {
                        errorText.setText("");
                        enginesList.add(newEngine);
                    }
                    else {
                        errorText.setText("Engine not added: Bad executable");
                    }
                }
                else {
                    errorText.setText("");
                }
            }
        });
        return b;
    }

    private Button createRemoveButton() {
        Button b = new Button("Remove");
        b.setOnAction(e -> {
            int selectedIndex = enginesLV.getSelectionModel().getSelectedIndex();
            if (selectedIndex < 3) {
                errorText.setText("Default engines cannot be removed");
            }
            else if (isRemovable()) {
                errorText.setText("");
                enginesList.remove(selectedIndex);
            }
            else {
                errorText.setText("Please finish your match first.");
            }
        });
        return b;
    }

    private Button createOKButton() {
        Button b = new Button("OK");
        b.setDefaultButton(true);
        b.setOnAction(e -> {
            //save changes TODO
            updatedEnginesList.clear();
            updatedEnginesList.addAll(enginesList);
            errorText.setText("");
            saveEnginesFile();
            close();
        });
        return b;        
    }

    private Button createCanceButton() {
        Button b = new Button("Cancel");
        b.setOnAction(e -> {
            //don't save changes
            errorText.setText("");
            close();
        });
        return b;          
    }
    
    public void initDefaultEngines() {
        initEngine(KOMODO_64bit);
        initEngine(STOCKFISH_64bit);
        initEngine(FIRE_64bit);
    }
    
    public void initEngine(Engine engine) {
        if (engine.start())
            addEngine(engine);
    }
    
    public List<Engine> getEngines() {
        return Collections.unmodifiableList(updatedEnginesList);
    }
    
    public void addEngine(Engine engine) {
        enginesList.add(engine);
        updatedEnginesList.add(engine);
    }
    
    public void removeEngine(Engine engine) {
        enginesList.remove(engine);
        updatedEnginesList.remove(engine);
        saveEnginesFile();
    }
    
    public void removeEngineList(List<Engine> engines) {
        enginesList.removeAll(engines);
        updatedEnginesList.removeAll(engines);
        saveEnginesFile();
    }

    /**
     * @return the removable
     */
    public boolean isRemovable() {
        return removable;
    }

    /**
     * @param removable the removable to set
     */
    public void setRemovable(boolean removable) {
        this.removable = removable;
    }
}
