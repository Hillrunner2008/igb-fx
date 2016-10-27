/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.bookmarks.data;

import java.util.Map;
import java.util.Optional;
import javafx.beans.property.StringProperty;
import javafx.collections.ObservableList;
import javafx.scene.image.ImageView;

/**
 *
 * @author Devdatta Kulkarni
 */
public interface Bookmark {
    
    void setName(String name);
    StringProperty getName();
    Optional<Map<String,String>> getDetails();
    StringProperty getDescription();
    void setDescription(String newDescription);
    Optional<ImageView> getImageView();
    
    Optional<ObservableList<Bookmark>> getChildren();
    void removeChild(Bookmark child);
    void addChild(Bookmark child);
    void addChild(int index,Bookmark child);
    Bookmark getParent();
    boolean isLeaf(); 
    boolean isExpanded();
    void setExpanded(boolean state);
    void setParent(Bookmark bookmark);
}
