/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.menu.api.model;

import java.util.List;
import org.lorainelab.igb.visualization.model.Glyph;

/**
 * ## AnnotationContextEvent
 * A class containing contextual information about the right
 * click event (e.g. selection information).
 * @author dcnorris
 * @module.info context-menu-api
 */
public class AnnotationContextEvent {

    private final List<Glyph> selectedItems;

    public AnnotationContextEvent(List<Glyph> selectedItems) {
        this.selectedItems = selectedItems;
    }

    /**
     *
     * @return list of currently selected SeqSymmetry objects
     */
    public List<Glyph> getSelectedItems() {
        return selectedItems;
    }

}
