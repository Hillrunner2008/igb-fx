package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.CompletableFuture;
import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.dataprovider.api.ResourceStatus;
import org.lorainelab.igb.preferencemanager.api.PreferencesTabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
@Component(immediate = true)
public class QuickloadRepositoryPreferencesTabProvider extends Tab implements PreferencesTabProvider {

    private static final Logger LOG = LoggerFactory.getLogger(QuickloadRepositoryPreferencesTabProvider.class);
    private static int WEIGHT = 5;

//    @FXML
//    private HBox repoTableContainer;
    @FXML
    private TableView<DataProvider> repoTable;

    @FXML
    private TableColumn refreshColumn;
    @FXML
    private TableColumn nameColumn;
    @FXML
    private TableColumn urlColumn;
    @FXML
    private TableColumn<DataProvider, Boolean> enabledColumn;

    @FXML
    private Button upBtn;
    @FXML
    private Button downBtn;
    @FXML
    private Button addBtn;
    @FXML
    private Button editBtn;
    @FXML
    private Button enterPasswordBtn;
    @FXML
    private Button removeBtn;

//    private TableView<DataProvider> repoTable;
//    private TableColumn<DataProvider, String> refreshColumn;
//    private TableColumn<DataProvider, String> nameColumn;
//    private TableColumn<DataProvider, String> urlColumn;
//    private TableColumn<DataProvider, Boolean> enabledColumn;
    private static final FontAwesomeIconView FONT_AWESOME_REFRESH_ICON = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);
    private QuickloadSiteManager quickloadSiteManager;

    //to ensure class import in manifest header
    private FontAwesomeIconView dummyIcon;

    public QuickloadRepositoryPreferencesTabProvider() {
        setText("Quickload Repositories");
//        initializeRepoTable();
    }

//    private void initializeRepoTable() {
//        repoTable = new TableView<>();
//        refreshColumn = new TableColumn<>();
//        refreshColumn.setStyle("-fx-alignment: CENTER;");
//        refreshColumn.setGraphic(FONT_AWESOME_REFRESH_ICON);
//        refreshColumn.setMinWidth(65.0);
//        refreshColumn.setMaxWidth(65.0);
//        refreshColumn.setResizable(false);
//        nameColumn = new TableColumn<>("Name");
//        nameColumn.setMinWidth(150.0);
//        nameColumn.setPrefWidth(250.0);
//        nameColumn.setMaxWidth(Double.MAX_VALUE);
//        urlColumn = new TableColumn<>("URL");
//        urlColumn.setMinWidth(150.0);
//        urlColumn.setPrefWidth(250.0);
//        urlColumn.setMaxWidth(Double.MAX_VALUE);
//        enabledColumn = new TableColumn<>("Enabled");
//        enabledColumn.setMinWidth(70.0);
//        enabledColumn.setMaxWidth(70.0);
//        enabledColumn.setResizable(false);
//        repoTable.getColumns().add(refreshColumn);
//        repoTable.getColumns().add(nameColumn);
//        repoTable.getColumns().add(urlColumn);
//        repoTable.getColumns().add(enabledColumn);
//        repoTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
//    }
    @Activate
    public void activate() {
        final URL resource = QuickloadRepositoryPreferencesTabProvider.class.getClassLoader().getResource("repositoryManager.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                VBox root = fxmlLoader.load();
//                HBox.setHgrow(repoTable, Priority.ALWAYS);
//                repoTableContainer.getChildren().add(repoTable);
//                nameColumn.prefWidthProperty().bind(repoTableContainer.widthProperty().multiply(.40));
//                urlColumn.prefWidthProperty().bind(repoTableContainer.widthProperty().multiply(.40));
//                refreshColumn.prefWidthProperty().bind(repoTableContainer.widthProperty().multiply(.10));
//                enabledColumn.prefWidthProperty().bind(repoTableContainer.widthProperty().multiply(.10));
                setContent(root);
                initializeComponents();
            } catch (IOException ex) {
                LOG.error(ex.getMessage(), ex);
            }
        });

    }

    @Override
    public Tab getPreferencesTab() {
        return this;
    }

    @Override
    public int getTabWeight() {
        return WEIGHT;
    }

    @Reference
    public void setQuickloadSiteManager(QuickloadSiteManager quickloadSiteManager) {
        this.quickloadSiteManager = quickloadSiteManager;
    }

    private void initializeComponents() {
        initializeTableCellFactories();
        initializeRepoTableContent();
    }

    private void initializeRepoTableContent() {
        final ObservableList<DataProvider> repoItems = repoTable.getItems();
        quickloadSiteManager.getDataProviders().addListener(new SetChangeListener<DataProvider>() {
            @Override
            public void onChanged(SetChangeListener.Change<? extends DataProvider> change) {
                Platform.runLater(() -> {
                    if (change.wasAdded()) {
                        final DataProvider elementAdded = change.getElementAdded();
                        if (!repoItems.contains(elementAdded)) {
                            repoItems.add(elementAdded);
                        }
                    } else {
                        final DataProvider elementRemoved = change.getElementRemoved();
                        if (repoItems.contains(elementRemoved)) {
                            repoItems.remove(elementRemoved);
                        }
                    }
                });
            }
        });
        Platform.runLater(() -> {
            repoItems.addAll(quickloadSiteManager.getDataProviders());
        });
    }

    private void initializeTableCellFactories() {
        nameColumn.setCellFactory(new Callback<TableColumn<DataProvider, String>, TableCell<DataProvider, String>>() {
            @Override
            public TableCell<DataProvider, String> call(TableColumn<DataProvider, String> param) {
                final TableCell<DataProvider, String> tableCell = new TableCell<DataProvider, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (empty) {
                            setGraphic(null);
                        } else {
                            DataProvider dataProvider = getTableView().getItems().get(getIndex());
//                            setDisable(!dataProvider.getStatus().equals(ResourceStatus.Disabled));
                            setEditable(!dataProvider.getStatus().equals(ResourceStatus.Disabled));
                            textProperty().bind(dataProvider.name());
                        }
                    }

                };
                return tableCell;
            }
        });
        urlColumn.setCellFactory(new Callback<TableColumn<DataProvider, String>, TableCell<DataProvider, String>>() {
            @Override
            public TableCell<DataProvider, String> call(TableColumn<DataProvider, String> param) {
                final TableCell<DataProvider, String> tableCell = new TableCell<DataProvider, String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (empty) {
                            setGraphic(null);
                        } else {
                            DataProvider dataProvider = getTableView().getItems().get(getIndex());
//                            setDisable(!dataProvider.getStatus().equals(ResourceStatus.Disabled));
                            textProperty().bind(dataProvider.url());
                        }
                    }

                };
                return tableCell;
            }
        });
        Callback<TableColumn<DataProvider, String>, TableCell<DataProvider, String>> refreshCellFactory = new Callback<TableColumn<DataProvider, String>, TableCell<DataProvider, String>>() {
            @Override
            public TableCell call(final TableColumn<DataProvider, String> param) {
                final TableCell<DataProvider, String> cell = new TableCell<DataProvider, String>() {
                    final FontAwesomeIconView refreshIcon = new FontAwesomeIconView(FontAwesomeIcon.REFRESH);

                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty) {
                            setGraphic(null);
                            setText(null);
                        } else {
                            DataProvider dataProvider = getTableView().getItems().get(getIndex());
                            refreshIcon.setOnMouseClicked(evt -> {
                                CompletableFuture.runAsync(() -> {
                                    disableDataProvider(dataProvider);
                                }).whenComplete((result, ex) -> {
                                    enableDataProvider(dataProvider);
                                });
                            });
                            setGraphic(refreshIcon);
//                            setDisable(dataProvider.getStatus().equals(ResourceStatus.Disabled));
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };
        refreshColumn.setCellFactory(refreshCellFactory);
        enabledColumn.setCellFactory(dp -> {
            CheckBoxTableCell<DataProvider, Boolean> cell = new CheckBoxTableCell<DataProvider, Boolean>(index -> {
                final DataProvider dataProvider = repoTable.getItems().get(index);
                final BooleanProperty isEnabled = new SimpleBooleanProperty(!dataProvider.getStatus().equals(ResourceStatus.Disabled));
                isEnabled.addListener((obs, old, isNowActive) -> {
                    LOG.info("isEnabled fired");
                    if (isNowActive) {
                        dataProvider.setStatus(ResourceStatus.NotInitialized);
                    } else {
                        dataProvider.setStatus(ResourceStatus.Disabled);
                    }
                });
                return isEnabled;
            });

            return cell;
        });
        enabledColumn.setCellFactory(CheckBoxTableCell.forTableColumn(enabledColumn));
        enabledColumn.setEditable(true);
    }

    public void disableDataProvider(DataProvider dataProvider) {
        dataProvider.setStatus(ResourceStatus.Disabled);
        quickloadSiteManager.removeAssociatedGenomeVersions(dataProvider);
    }

    public void enableDataProvider(DataProvider dataProvider) {
        dataProvider.setStatus(ResourceStatus.NotInitialized);
        quickloadSiteManager.initializeDataProvider(dataProvider);
    }

}
