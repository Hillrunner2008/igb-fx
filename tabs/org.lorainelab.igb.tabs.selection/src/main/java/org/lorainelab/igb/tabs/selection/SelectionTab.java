package org.lorainelab.igb.tabs.selection;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;
import static java.util.stream.Collectors.toList;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.Tab;
import javafx.scene.control.TablePosition;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.layout.AnchorPane;
import org.controlsfx.control.spreadsheet.GridBase;
import org.controlsfx.control.spreadsheet.SpreadsheetCell;
import org.controlsfx.control.spreadsheet.SpreadsheetCellType;
import org.controlsfx.control.spreadsheet.SpreadsheetView;
import org.lorainelab.igb.data.model.glyph.CompositionGlyph;
import org.lorainelab.igb.selections.SelectionInfoService;
import org.lorainelab.igb.visualization.tabs.api.TabDockingPosition;
import org.lorainelab.igb.visualization.tabs.api.TabProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Component(immediate = true)
public class SelectionTab implements TabProvider {

    private static final Logger LOG = LoggerFactory.getLogger(SelectionTab.class);
    private static final String TAB_TITLE = "Selection Info";
    private final int TAB_WEIGHT = 0;
    private final Tab selectionTab;
    @FXML
    private AnchorPane tabContent;
    @FXML
    private SpreadsheetView spreadsheetView;
    private SelectionInfoService selectionInfoService;

    public SelectionTab() {
        selectionTab = new Tab(TAB_TITLE);
        final URL resource = SelectionTab.class.getClassLoader().getResource("SelectionTab.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        Platform.runLater(() -> {
            try {
                fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Activate
    public void activate() {
        selectionInfoService.getSelectedGlyphs().addListener((SetChangeListener.Change<? extends CompositionGlyph> change) -> {
            rebuildGridData(); //TODO consider incremental changes instead
        });
    }

    @FXML
    private void initialize() {
        spreadsheetView.setShowRowHeader(true);
        spreadsheetView.setShowColumnHeader(true);
        spreadsheetView.setRowHeaderWidth(140);
        spreadsheetView.editableProperty().set(false);
        spreadsheetView.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        spreadsheetView.setFixingColumnsAllowed(false);
        spreadsheetView.setFixingRowsAllowed(false);
        spreadsheetView.getContextMenu().getItems().clear();
        initializeContextMenu();
        rebuildGridData();
        selectionTab.setContent(tabContent);
        initializeTable();
    }

    private void rebuildGridData() {
        if (spreadsheetView != null) {
            final List<String> rowHeaders = selectionInfoService.getSelectedGlyphs().stream()
                    .flatMap(glyph -> glyph.getTooltipData().keySet().stream())
                    .distinct()
                    .collect(Collectors.toList());
            final List<CompositionGlyph> selectedGlyphs = Lists.newArrayList(selectionInfoService.getSelectedGlyphs().stream().limit(MAX_SELECTION_SIZE).collect(toList()));
            int rowCount = rowHeaders.size();
            int columnCount = selectedGlyphs.size();
            GridBase grid = new GridBase(rowCount, columnCount);
            ObservableList<ObservableList<SpreadsheetCell>> rows = FXCollections.observableArrayList();
            for (int row = 0; row < grid.getRowCount(); ++row) {
                final ObservableList<SpreadsheetCell> list = FXCollections.observableArrayList();
                for (int column = 0; column < grid.getColumnCount(); ++column) {
                    final String cellValue = selectedGlyphs.get(column).getTooltipData().get(rowHeaders.get(row));
                    final SpreadsheetCell gridCell = SpreadsheetCellType.STRING.createCell(row, column, 1, 1, cellValue);
                    gridCell.setWrapText(true);
                    //cellValue=StringUtils.abbreviate(cellValue,20); //TODO consider if this would be useful
                    list.add(gridCell);
                }
                rows.add(list);
            }
            grid.getColumnHeaders().clear();
            for (int column = 0; column < grid.getColumnCount(); ++column) {
                grid.getColumnHeaders().add(selectedGlyphs.get(column).getLabel());
            }
            grid.getRowHeaders().clear();
            grid.getRowHeaders().addAll(rowHeaders);
            grid.setRows(rows);
            spreadsheetView.setGrid(grid);
            spreadsheetView.getColumns().forEach(column -> column.fitColumn());

        }
    }
    private final int MAX_SELECTION_SIZE = 10;

    private void initializeTable() {
//        table.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
//        table.getSelectionModel().setCellSelectionEnabled(false);

//        seqNameColumn.setCellValueFactory(new PropertyValueFactory<>("name"));
//        seqLengthColumn.setCellValueFactory(new PropertyValueFactory<>("length"));
//        table.setItems(tableData);
    }

    @Reference
    public void setSelectionInfoService(SelectionInfoService selectionInfoService) {
        this.selectionInfoService = selectionInfoService;
    }

    @Override
    public Tab getTab() {
        return selectionTab;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.BOTTOM;
    }

    @Override
    public int getTabWeight() {
        return TAB_WEIGHT;
    }

    private void initializeContextMenu() {
        ContextMenu contextMenu = new ContextMenu();
        final MenuItem copyItem = new MenuItem("Copy");
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.SHORTCUT_DOWN));
        copyItem.setOnAction(new EventHandler<ActionEvent>() {
            @Override
            public void handle(ActionEvent e) {
                @SuppressWarnings("rawtypes")
                final ObservableList<TablePosition> posList = spreadsheetView.getSelectionModel().getSelectedCells();
                spreadsheetView.getSelectionModel().getSelectedCells().stream().findFirst().ifPresent(pos -> {
                    SpreadsheetCell cell = spreadsheetView.getGrid().getRows().get(pos.getRow()).get(pos.getColumn());
                    final Clipboard clipboard = Clipboard.getSystemClipboard();
                    final ClipboardContent content = new ClipboardContent();
                    content.putString(cell.getItem().toString());
                    clipboard.setContent(content);
                });

            }
        });
        contextMenu.getItems().add(copyItem);
        spreadsheetView.setContextMenu(contextMenu);
    }

}
