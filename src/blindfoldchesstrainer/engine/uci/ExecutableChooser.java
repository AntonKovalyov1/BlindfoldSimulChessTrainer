/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package blindfoldchesstrainer.engine.uci;

import java.io.File;
import javafx.stage.FileChooser;

/**
 *
 * @author Anton
 */
public class ExecutableChooser {
            
    public ExecutableChooser() {
        throw new RuntimeException("Not instantiable");
    }
    
    public static String newExecutable() {
        final FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Find UCI engine");
        fileChooser.setInitialDirectory(
                new File(System.getProperty("user.home")));

        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("EXE", "*.exe"));

        //Handle the queryImage
        File file = fileChooser.showOpenDialog(null);
        if (file != null && file.isFile()) {
            try {
                return file.getAbsolutePath();
            } 
            catch (Exception ex) {
                return null;
            }
        }
        return null;
    }
}
