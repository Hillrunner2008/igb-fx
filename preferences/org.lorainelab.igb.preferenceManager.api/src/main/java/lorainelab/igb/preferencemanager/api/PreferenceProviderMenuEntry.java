/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package lorainelab.igb.preferencemanager.api;

import aQute.bnd.annotation.component.Activate;
import java.util.List;
import java.util.Optional;
import javafx.application.Platform;
import javafx.scene.control.TabPane;
import javafx.stage.Stage;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;

/**
 *
 * @author Devdatta Kulkarni
 */
public class PreferenceProviderMenuEntry implements MenuBarEntryProvider {

    
    private WeightedMenuItem menuItem;
    private TabPane pane;
    private Stage stage;
    
    public PreferenceProviderMenuEntry(){
        menuItem = new WeightedMenuItem(30, "Preferences");
    }

    @Activate
    public void activate(){
        Platform.runLater(()->{
            initComponents();
            layoutComponents();
            Platform.runLater(()-> {stage.centerOnScreen();
                    stage.show();});
        });
    }
    
    
    @Override
    public Optional<List<WeightedMenuItem>> getMenuItems() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    private void initComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private void layoutComponents() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

}
