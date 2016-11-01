/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.bookmarks.manager;

import org.lorainelab.igb.bookmarks.data.Bookmark;

/**
 *
 * @author Devdatta
 */
public interface BookmarkManager {
    
    Bookmark getRootBookmark();
    void createBookmark();
    void createBookmark(Bookmark relativePosition);
    void restoreBookmark(Bookmark b);
    
}
