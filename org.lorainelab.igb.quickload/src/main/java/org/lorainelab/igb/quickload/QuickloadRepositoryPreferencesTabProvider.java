package org.lorainelab.igb.quickload;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.prefs.BackingStoreException;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.SetChangeListener;
import javafx.collections.transformation.SortedList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Tooltip;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.util.Callback;
import org.lorainelab.igb.dataprovider.api.BaseDataProvider;
import org.lorainelab.igb.dataprovider.api.DataProvider;
import org.lorainelab.igb.dataprovider.api.DataProviderComparator;
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

    @FXML
    private TableView<DataProvider> repoTable;

    private ObservableList<DataProvider> repoItems;

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

    private QuickloadSiteManager quickloadSiteManager;
    private SortedList<DataProvider> sortedList;
    private QuickloadDataProviderFactory dataProviderFactory;

    public QuickloadRepositoryPreferencesTabProvider() {
        setText("Quickload Repositories");
        repoItems = FXCollections.observableArrayList();
        sortedList = new SortedList<DataProvider>(repoItems, new DataProviderComparator());
    }

    @Activate
    public void activate() {
        final URL resource = QuickloadRepositoryPreferencesTabProvider.class.getClassLoader().getResource("repositoryManager.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                VBox root = fxmlLoader.load();
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
        initializeActionButtons();
    }

    private void initializeRepoTableContent() {
        repoTable.setItems(sortedList);
        sortedList.comparatorProperty().bind(repoTable.comparatorProperty());
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
                    Collections.sort(repoItems, new DataProviderComparator());
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
                            setText("");
                        } else {
                            DataProvider dataProvider = getTableView().getItems().get(getIndex());
                            setText(dataProvider.name().get());
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
                            setText("");
                        } else {
                            DataProvider dataProvider = getTableView().getItems().get(getIndex());
                            setText(dataProvider.url().get());
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
                            setText(null);
                        }
                    }
                };
                return cell;
            }
        };
        refreshColumn.setCellFactory(refreshCellFactory);
        enabledColumn.setCellValueFactory((TableColumn.CellDataFeatures<DataProvider, Boolean> tc) -> {
            final DataProvider dataProvider = tc.getValue();
            SimpleBooleanProperty isEnabled = new SimpleBooleanProperty(!dataProvider.getStatus().equals(ResourceStatus.Disabled));
            //doesn't work
//            isEnabled.addListener((obs, old, isNowActive) -> {
//                if (isNowActive) {
//                    dataProvider.setStatus(ResourceStatus.NotInitialized);
//                } else {
//                    dataProvider.setStatus(ResourceStatus.Disabled);
//                }
//            });
            return isEnabled;
        });
        enabledColumn.setCellFactory(p -> {
            ReferenceHoldingCheckBox<DataProvider> checkBox = new ReferenceHoldingCheckBox();
            TableCell<DataProvider, Boolean> tableCell = new TableCell<DataProvider, Boolean>() {
                @Override
                protected void updateItem(Boolean item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setGraphic(null);
                    } else {
                        DataProvider dataProvider = getTableView().getItems().get(getIndex());
                        checkBox.setReference(dataProvider);
                        setGraphic(checkBox);
                        checkBox.setSelected(item);
                    }
                }
            };

            checkBox.addEventFilter(MouseEvent.MOUSE_PRESSED, new EventHandler<MouseEvent>() {
                @Override
                public void handle(MouseEvent event) {
                    event.consume();
                    DataProvider dataProvider = checkBox.getReference();
                    if (dataProvider != null) {
                        if (checkBox.isSelected()) {
                            disableDataProvider(dataProvider);
                        } else {
                            enableDataProvider(dataProvider);
                        }
                        checkBox.setSelected(!checkBox.isSelected());
                    }
                }
            });

            tableCell.setAlignment(Pos.CENTER);
            tableCell.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
            return tableCell;
        });
    }

    public void disableDataProvider(DataProvider dataProvider) {
        quickloadSiteManager.disableDataProvider(dataProvider);
    }

    public void enableDataProvider(DataProvider dataProvider) {
        dataProvider.setStatus(ResourceStatus.NotInitialized);
        quickloadSiteManager.initializeDataProvider(dataProvider);
    }

    private void initializeActionButtons() {
        BooleanBinding selectedItemIsNull = Bindings.isNull(repoTable.getSelectionModel().selectedItemProperty());
        BooleanBinding isRemovable = Bindings.and(selectedItemIsNull.not(), selectedItemIsNull);
        upBtn.disableProperty().bind(selectedItemIsNull);
        upBtn.setTooltip(new Tooltip("Increase the priority of using sequence from this server when it conflicts with another source"));
        downBtn.disableProperty().bind(selectedItemIsNull);
        downBtn.setTooltip(new Tooltip("Decrease the priority of using sequence from this server when it conflicts with another source"));
        repoTable.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<DataProvider>() {
            @Override
            public void changed(ObservableValue<? extends DataProvider> observable, DataProvider oldValue, DataProvider newValue) {
                Platform.runLater(() -> {
                    if (newValue != null) {
                        removeBtn.setDisable(!newValue.isEditable());
                        editBtn.setDisable(!newValue.isEditable());
                        enterPasswordBtn.setDisable(!newValue.isEditable());
                    } else {
                        removeBtn.setDisable(true);
                        editBtn.setDisable(true);
                        enterPasswordBtn.setDisable(true);
                    }
                });
            }
        });

        upBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                DataProvider selectedItem = repoTable.getSelectionModel().getSelectedItem();
                int indexOf = repoItems.indexOf(selectedItem);
                if (indexOf > 0) {
                    final DataProvider dataProviderAbove = repoItems.get(indexOf - 1);
                    int currentLoadPriority = selectedItem.loadPriority().get();
                    selectedItem.loadPriority().setValue(dataProviderAbove.loadPriority().get());
                    dataProviderAbove.loadPriority().set(currentLoadPriority);
                    Collections.sort(repoItems, new DataProviderComparator());
                }
            });
        });
        downBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                DataProvider selectedItem = repoTable.getSelectionModel().getSelectedItem();
                int indexOf = repoItems.indexOf(selectedItem);
                if (indexOf + 1 < repoItems.size()) {
                    final DataProvider dataProviderBelow = repoItems.get(indexOf + 1);
                    int currentLoadPriority = selectedItem.loadPriority().get();
                    selectedItem.loadPriority().setValue(dataProviderBelow.loadPriority().get());
                    dataProviderBelow.loadPriority().set(currentLoadPriority);
                    Collections.sort(repoItems, new DataProviderComparator());
                }
            });
        });
        addBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                AddEditeQuickoadModal addEditeQuickoadModal = new AddEditeQuickoadModal(Optional.empty(), dataProviderFactory, repoItems.stream().mapToInt(item -> item.loadPriority().get()).max().orElse(5));
            });
        });
        editBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                DataProvider selectedItem = repoTable.getSelectionModel().getSelectedItem();
                AddEditeQuickoadModal addEditeQuickoadModal = new AddEditeQuickoadModal(Optional.of(selectedItem), dataProviderFactory, repoItems.stream().mapToInt(item -> item.loadPriority().get()).max().orElse(5));
                urlColumn.setVisible(false);
                urlColumn.setVisible(true);
                nameColumn.setVisible(false);
                nameColumn.setVisible(true);
            });
        });
        enterPasswordBtn.setOnAction(action -> {
            DataProvider selectedItem = repoTable.getSelectionModel().getSelectedItem();
        });
        removeBtn.setOnAction(action -> {
            Platform.runLater(() -> {
                DataProvider selectedItem = repoTable.getSelectionModel().getSelectedItem();
                if (selectedItem.isEditable()) {
                    quickloadSiteManager.removeDataProvider(selectedItem);
                    BaseDataProvider.getDataProviderNodeIfExist(selectedItem.url().get()).ifPresent(node -> {
                        try {
                            node.removeNode();
                        } catch (BackingStoreException ex) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    });
                }
            });
        });
    }

    @Reference
    public void setQuickloadDataProviderFactory(QuickloadDataProviderFactory quickloadDataProviderFactory) {
        this.dataProviderFactory = quickloadDataProviderFactory;
    }

}
