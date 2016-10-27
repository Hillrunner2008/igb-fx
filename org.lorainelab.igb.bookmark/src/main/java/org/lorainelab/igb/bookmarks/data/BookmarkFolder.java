/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.bookmarks.data;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;

/**
 *
 * @author Devdatta Kulkarni
 */
public class BookmarkFolder implements Bookmark {

    private StringProperty name;
    private StringProperty description;
    ObservableList<Bookmark> children;
    Bookmark parent;
    boolean expanded;

    public BookmarkFolder() {
        name = new SimpleStringProperty();
        children = FXCollections.observableArrayList();
        description = new SimpleStringProperty();
    }

    public BookmarkFolder(String name) {
        this();
        this.name.set(name);
    }

    public StringProperty getName() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    @Override
    public String toString() {
        return name.get();
    }

    @Override
    public Optional<ImageView> getImageView() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public void removeChild(Bookmark bookmark) {
        children.remove(bookmark);
        bookmark.setParent(null);
    }

    @Override
    public void addChild(Bookmark bookmark) {
        children.add(bookmark);
        bookmark.setParent(this);
    }

    @Override
    public Bookmark getParent() {
        return parent;
    }

    @Override
    public boolean isLeaf() {
        return false;
    }

    @Override
    public Optional<ObservableList<Bookmark>> getChildren() {
        return Optional.of(children);
    }

    @Override
    public void setParent(Bookmark bookmark) {
        this.parent = bookmark;
    }

    @Override
    public Optional<Map<String, String>> getDetails() {
        return Optional.empty();
    }

    @Override
    public void addChild(int index, Bookmark bookmark) {
        children.add(index, bookmark);
        bookmark.setParent(this);
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
        return expanded;
    }

    @Override
    public void setExpanded(boolean state) {
        expanded = state;
    }

}
