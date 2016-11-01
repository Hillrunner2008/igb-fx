/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.bookmarks.data;

import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIconView;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.image.ImageView;

/**
 *
 * @author Devdatta Kulkarni
 */
public class BookmarkData implements Bookmark {

    private StringProperty name;
    private Bookmark parent;
    private Map<String, String> details;

    private StringProperty description;

    public BookmarkData() {
        name = new SimpleStringProperty();
        description = new SimpleStringProperty();
        details = new HashMap<String, String>();
    }

    public void setDetails(Map<String, String> details) {
        this.details = details;
    }

    public BookmarkData(String name) {
        this();
        this.name.set(name);
    }

    public void setName(StringProperty name) {
        this.name = name;
    }

    @Override
    public StringProperty getName() {
        return name;
    }

    @Override
    public Optional<Node> getImageView() {
        return Optional.empty();
    }

    @Override
    public void removeChild(Bookmark bookmark) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void addChild(Bookmark bookmark) {
        getParent().addChild(bookmark);
    }

    @Override
    public Bookmark getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return true;
    }

    @Override
    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public Optional<ObservableList<Bookmark>> getChildren() {
        return Optional.empty();
    }

    @Override
    public void setParent(Bookmark bookmark) {
        this.parent = bookmark;
    }

    @Override
    public Optional<Map<String, String>> getDetails() {
        return Optional.of(details);
    }

    @Override
    public void addChild(int index, Bookmark bookmark) {
        getParent().addChild(index, bookmark);
    }

    @Override
    public StringProperty getDescription() {
        return description;
    }

    @Override
    public void setDescription(String newDescription) {
        description.set(newDescription);
    }

    @Override
    public boolean isExpanded() {
        return false;
    }

    @Override
    public void setExpanded(boolean state) {
    }

}
