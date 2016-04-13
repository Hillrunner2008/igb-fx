package org.lorainelab.igb.menu.openfile;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import javafx.stage.FileChooser;
import org.lorainelab.igb.datasource.api.DataSource;
import org.lorainelab.igb.filehandler.api.FileTypeHandlerRegistry;
import org.lorainelab.igb.menu.api.MenuBarEntryProvider;
import org.lorainelab.igb.menu.api.model.ParentMenu;
import org.lorainelab.igb.menu.api.model.WeightedMenuItem;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class OpenFileMenuItem implements MenuBarEntryProvider {

    private DataSource dataSource;
    private WeightedMenuItem menuItem;
    private FileTypeHandlerRegistry fileTypeHandlerRegistry;

    @Activate
    public void activate() {
        menuItem = new WeightedMenuItem(1, "Load File");
        menuItem.setOnAction(action -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Load File");
            File homeDirectory = new File(System.getProperty("user.home"));
            fileChooser.setInitialDirectory(homeDirectory);
            addFileExtensionFilters(fileChooser);
            Optional.ofNullable(fileChooser.showOpenMultipleDialog(null)).ifPresent(selectedFiles -> {

            });
        });
    }

    @Override
    public Optional<List<WeightedMenuItem>> getMenuItems() {
        final List<WeightedMenuItem> menuItems = Lists.newArrayList(menuItem);
        return Optional.of(menuItems);
    }

    @Override
    public ParentMenu getParentMenu() {
        return ParentMenu.FILE;
    }

    @Reference(target = "(&(component.name=" + "LocalDataSource" + "))")
    public void setLocalDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Reference
    public void setFileTypeHandlerRegistry(FileTypeHandlerRegistry fileTypeHandlerRegistry) {
        this.fileTypeHandlerRegistry = fileTypeHandlerRegistry;
    }

    private void addFileExtensionFilters(FileChooser fileChooser) {
        fileTypeHandlerRegistry.getFileTypeHandlers().stream()
                .map(fileTypeHandler -> {
            return new FileChooser.ExtensionFilter(fileTypeHandler.getName(), fileTypeHandler.getSupportedExtensions().stream().map(ext -> "*." + ext).collect(Collectors.toList()));
                })
                .forEach(extensionFilter -> fileChooser.getExtensionFilters().add(extensionFilter));
    }

}
