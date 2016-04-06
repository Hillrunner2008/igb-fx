/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes;

import java.util.Optional;
import org.lorainelab.igb.data.model.Feature;

/**
 *
 * @author jeckstei
 * @param <T>
 */
public interface Shape<T extends Feature> {

    default int getWidth() {
        return -1;
    }

    default int getOffset() {
        return -1;
    }

    default void setOffset(int offset) {
    }

    default Optional<T> getModel() {
        return Optional.empty();
    }
}
