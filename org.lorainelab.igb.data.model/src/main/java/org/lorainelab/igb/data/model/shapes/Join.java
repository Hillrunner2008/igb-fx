/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import org.lorainelab.igb.data.model.Feature;

/**
 *
 * @author jeckstei
 * @param <T>
 */
public interface Join<T extends Feature> extends Shape {

    default Join[] join(Feature model, Shape[]... shapes) {
        return new Join[]{new Join() {
            @Override
            public List<Shape> getJoinItems() {
                return null;
            }

            @Override
            public Optional<Feature> getModel() {
                return Optional.ofNullable(model);
            }

            @Override
            public int getWidth() {
                return -1;
            }

            @Override
            public int getOffset() {
                return -1;
            }

            @Override
            public void setOffset(int offset) {
                //do nothing
            }
        }};
    }

    default List<Shape> getJoinItems() {
        return Collections.emptyList();
    }

}
