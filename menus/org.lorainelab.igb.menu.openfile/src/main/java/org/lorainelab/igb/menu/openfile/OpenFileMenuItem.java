package org.lorainelab.igb.menu.openfile;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.List;
import java.util.Optional;
import org.lorainelab.igb.datasetloadingservice.api.DataSetLoadingService;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuEntry;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.toolbar.api.ToolbarButtonProvider;
import org.lorainelab.igb.toolbar.api.WeightedButton;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class OpenFileMenuItem implements MenuBarEntryProvider, ToolbarButtonProvider {

    
    private WeightedMenuItem menuItem;
    private WeightedButton openFileButton;

    private DataSetLoadingService fileOpener;
    private SelectionInfoService selectionInfoService;

    @Activate
    public void activate() {
        menuItem = new WeightedMenuItem(1, "Load File");
        openFileButton = new WeightedButton(0, "", new FontAwesomeIconView(FontAwesomeIcon.FOLDER_OPEN));
        openFileButton.setOnAction(action -> fileOpener.openDataSet());
        menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        selectionInfoService.getSelectedGenomeVersion().addListener((observable, oldValue, newValue) -> {
            menuItem.setDisable(!selectionInfoService.getSelectedGenomeVersion().get().isPresent());
        });

        menuItem.setOnAction(action -> fileOpener.openDataSet());
    }

    @Override
    public Optional<List<WeightedMenuEntry>> getMenuItems() {
        final List<WeightedMenuEntry> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    @Override
    public WeightedButton getToolbarButton() {
        return openFileButton;
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Reference
    public void setFileOpener(DataSetLoadingService fileOpener) {
        this.fileOpener = fileOpener;
    }

}
