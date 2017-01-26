package lorainelab.igb.bookmarks;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Reference;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.io.IOException;
import java.net.URL;
import java.util.Map;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.beans.value.WeakChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.WeakListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Tab;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DataFormat;
import javafx.scene.input.DragEvent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.TransferMode;
import javafx.scene.layout.AnchorPane;
import javafx.util.Callback;
import org.lorainelab.igb.bookmarks.data.Bookmark;
import org.lorainelab.igb.bookmarks.data.BookmarkFolder;
import org.lorainelab.igb.bookmarks.manager.BookmarkManager;
import org.lorainelab.igb.tabs.api.TabDockingPosition;
import org.lorainelab.igb.tabs.api.TabProvider;
import static org.lorainelab.igb.utils.FXUtilities.runAndWait;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Devdatta Kulkarni
 */
@Component(immediate = true)
public class BookmarksTab implements TabProvider {

    private static final String TAB_TITLE = "Bookmarks";
    private static final String DO_BOOKMARK_TOOLTIP = "Create a new bookmark";
    private static final String DELETE_BOOKMARK_TOOLTIP = "Delete selected bookmark";
    private static final String CREATE_BOOKMARK_FOLDER_TOOLTIP = "Create folder for bookmarks";
    private final int TAB_WEIGHT = 10;
    private final Tab bookmarksTab;
    private BookmarkManager bookmarkManager;
    private Bookmark selectedBookmark;

    @FXML
    private AnchorPane tabContent;
    @FXML
    private TreeView<Bookmark> bookMarkTree;
    @FXML
    private TableColumn key;
    @FXML
    private TableColumn value;
    @FXML
    private TableView bookmarkInfoTable;
    @FXML
    private Button doBookmark;
    @FXML
    private Button deleteBookmark;
    @FXML
    private TextArea bookmarkDescTextArea;
    @FXML
    private Button addFolder;
    @FXML
    private TextField bookmarkNameTextField;

    public BookmarksTab() {
        this.bookmarksTab = new Tab(TAB_TITLE);
        final URL resource = BookmarksTab.class.getClassLoader().getResource("BookmarksTab.fxml");
        FXMLLoader fxmlLoader = new FXMLLoader(resource);
        fxmlLoader.setClassLoader(this.getClass().getClassLoader());
        fxmlLoader.setController(this);
        runAndWait(() -> {
            try {
                fxmlLoader.load();
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        });
    }

    @Activate
    public void activate() {
        bookmarksTab.setContent(tabContent);
        runAndWait(() -> {
            init();
            setData();
        });
    }

    public void init() {

        doBookmark.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.BOOKMARK));
        doBookmark.setTooltip(new Tooltip(DO_BOOKMARK_TOOLTIP));
        doBookmark.setOnAction(ae -> {
            bookmarkManager.createBookmark(selectedBookmark);
        });

        deleteBookmark.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.TRASH));
        deleteBookmark.setTooltip(new Tooltip(DELETE_BOOKMARK_TOOLTIP));
        deleteBookmark.setOnAction(ae -> {
            if (selectedBookmark == null || selectedBookmark.equals(bookmarkManager.getRootBookmark())) {
                return;
            }
            selectedBookmark.getParent().removeChild(selectedBookmark);
        });

        addFolder.setGraphic(new FontAwesomeIconView(FontAwesomeIcon.FOLDER));
        addFolder.setTooltip(new Tooltip(CREATE_BOOKMARK_FOLDER_TOOLTIP));
        addFolder.setOnAction(ae -> {
            if (selectedBookmark == null) {
                bookmarkManager.getRootBookmark().addChild(new BookmarkFolder("Folder"));
            } else {
                selectedBookmark.addChild(new BookmarkFolder("Folder"));
            }
        });

        value.setCellFactory(new Callback<TableColumn<String, String>, TableCell<String, String>>() {
            @Override
            public TableCell<String, String> call(TableColumn<String, String> param) {
                return new TextFieldTableCell<String, String>() {
                    @Override
                    public void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty); //To change body of generated methods, choose Tools | Templates.
                        if (!empty) {
                            setTooltip(new Tooltip(item));
                        }
                    }

                };
            }

        });

        key.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {
            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                return new SimpleStringProperty(p.getValue().getKey());
            }
        });

        value.setCellValueFactory(new Callback<TableColumn.CellDataFeatures<Map.Entry<String, String>, String>, ObservableValue<String>>() {

            @Override
            public ObservableValue<String> call(TableColumn.CellDataFeatures<Map.Entry<String, String>, String> p) {
                return new SimpleStringProperty(p.getValue().getValue());
            }
        });

        bookmarkNameTextField.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (selectedBookmark != null) {
                selectedBookmark.setName(newValue);
            }
        });

        bookmarkDescTextArea.textProperty().addListener((ObservableValue<? extends String> observable, String oldValue, String newValue) -> {
            if (selectedBookmark != null) {
                selectedBookmark.setDescription(newValue);
            }
        });

        bookMarkTree.setCellFactory(new Callback<TreeView<Bookmark>, TreeCell<Bookmark>>() {
            @Override
            public TreeCell<Bookmark> call(TreeView<Bookmark> param) {
                TreeCell t = new CustomTreeCell(param);
                return t;
            }
        });

        bookMarkTree.getSelectionModel().selectedItemProperty().addListener((ObservableValue<? extends TreeItem<Bookmark>> observable, TreeItem<Bookmark> oldValue, TreeItem<Bookmark> newValue) -> {
            if (newValue != null) {
                selectedBookmark = newValue.getValue();
                populateDetails(selectedBookmark);
            } else {
                selectedBookmark = null;
            }
        });
    }

    private void setData() {
        Bookmark root = bookmarkManager.getRootBookmark();
        TreeItem<Bookmark> rootItem = new TreeItem<>(root);
        root.setParent(null);
        rootItem.setExpanded(true);
        TreeItem<Bookmark> item = addBookmarks(root);
        Platform.runLater(() -> {
            bookMarkTree.setRoot(item);
            bookMarkTree.layout();
        });
    }

    private void populateDetails(Bookmark bookmark) {
        bookmarkNameTextField.setText(bookmark.getName().get());
        bookmarkDescTextArea.setText(bookmark.getDescription().get());
        if (bookmark.getDetails().isPresent()) {
            ObservableList<Map.Entry<String, String>> items = FXCollections.observableArrayList(bookmark.getDetails().get().entrySet());
            bookmarkInfoTable.setItems(items);
        } else {
            bookmarkInfoTable.setItems(FXCollections.observableArrayList());
        }
    }

    private ListChangeListener bookmarkChildrenChangeListener = (ListChangeListener<Bookmark>) (ListChangeListener.Change<? extends Bookmark> c) -> {
        setData();
    };
    private WeakListChangeListener bookmarkChildrenWeakChangeListener = new WeakListChangeListener(bookmarkChildrenChangeListener);

    private TreeItem<Bookmark> addBookmarks(Bookmark bookmark) {
        TreeItem item = new TreeItem(bookmark);
        treeItemExpandedPropertyListener = (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
            bookmark.setExpanded(newValue);
        };
        item.expandedProperty().addListener(new WeakChangeListener<>(treeItemExpandedPropertyListener));
        bookmark.getChildren().ifPresent((ObservableList<Bookmark> data) -> {
            data.stream().forEach(b -> {
                if (b.getChildren() != null) {
                    item.getChildren().add(addBookmarks(b));
                }
            });
            data.removeListener(bookmarkChildrenWeakChangeListener);
            data.addListener(bookmarkChildrenWeakChangeListener);
            item.setExpanded(bookmark.isExpanded());
        });
        return item;
    }
    private ChangeListener<Boolean> treeItemExpandedPropertyListener;

    @Override
    public Tab getTab() {
        return bookmarksTab;
    }

    @Override
    public TabDockingPosition getTabDockingPosition() {
        return TabDockingPosition.RIGHT;
    }

    @Override
    public int getTabWeight() {
        return TAB_WEIGHT;
    }

    @Reference
    public void setBookmarkManager(BookmarkManager bookmarkManager) {
        this.bookmarkManager = bookmarkManager;
    }

    static Bookmark draggedBookmark;

    class CustomTreeCell extends TreeCell<Bookmark> {

        Bookmark bookmark;
        TreeView<Bookmark> parent;

        public CustomTreeCell(TreeView<Bookmark> param) {
            parent = param;

            setOnDragDetected((MouseEvent event) -> {
                if (bookmark == null) {
                    return;
                }
                draggedBookmark = bookmark;
                Dragboard dragBoard = startDragAndDrop(TransferMode.MOVE);
                ClipboardContent content = new ClipboardContent();
                content.put(DataFormat.PLAIN_TEXT, bookmark.getName().get());
                dragBoard.setContent(content);
                event.consume();
            });

            setOnDragOver((DragEvent dragEvent) -> {
                if (bookmark != draggedBookmark) {
                    dragEvent.acceptTransferModes(TransferMode.MOVE);
                }
                if (dragEvent.getDragboard().hasString()) {
                    String valueToMove = dragEvent.getDragboard().getString();
                }
                dragEvent.consume();
            });

            setOnDragDropped((DragEvent event) -> {
                if (bookmark == null) {
                    return;
                }
                if (draggedBookmark != null) {
                    Bookmark p = bookmark;
                    while ((p = p.getParent()) != null) {
                        if (p.equals(draggedBookmark)) {
                            Platform.runLater(() -> {
                                Alert dlg = new Alert(Alert.AlertType.INFORMATION);
                                dlg.setWidth(600);
                                dlg.setTitle("Operation not allowed");
                                dlg.setHeaderText("Operation not allowed");
                                dlg.setContentText("Cannot move parent into its child");
                                dlg.show();
                            });
                            return;
                        }
                    }
                    draggedBookmark.getParent().getChildren().get().remove(draggedBookmark);
                    if (!bookmark.isLeaf()) {
                        bookmark.addChild(draggedBookmark);
                        draggedBookmark.setParent(bookmark);
                    } else {
                        bookmark.getParent().getChildren().ifPresent((ObservableList<Bookmark> children) -> {
                            int position = children.indexOf(bookmark);
                            children.add(position + 1, draggedBookmark);
                            draggedBookmark.setParent(bookmark.getParent());
                        });
                    }
                }
                event.consume();
            });

            setOnMouseClicked((MouseEvent event) -> {
                if (bookmark != null && bookmark.isLeaf() && event.getClickCount() > 1) {
                    bookmarkManager.restoreBookmark(bookmark);
                    event.consume();
                }
            });

        }

        @Override
        protected void updateItem(Bookmark item, boolean empty) {
            super.updateItem(item, empty);
            bookmark = item;
            if (empty || bookmark == null) {
                setText(null);
                setGraphic(null);
            } else {
                bookmarkNameChangeListener = new ChangeListener<String>() {
                    @Override
                    public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                        setText(newValue);
                    }
                };
                bookmark.getName().addListener(new WeakChangeListener<>(bookmarkNameChangeListener));
                setText(getItem() == null ? "" : getItem().getName().get());
            }
        }
        private ChangeListener<String> bookmarkNameChangeListener;
    }
}
