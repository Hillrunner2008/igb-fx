/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.view;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.lorainelab.igb.data.model.shapes.Shape;

/**
 *
 * @author jeckstei
 */
public interface Layer {

    default Layer layer(int start, Stream<Shape>... shapes) {
        return new Layer() {
            List<Shape> items = Arrays.asList(shapes).stream()
                    .flatMap(stream -> stream)
                    .map(shape -> {
                        shape.setOffset(shape.getOffset() + start);
                        return shape;
                    })
                    .collect(Collectors.toList());

            @Override
            public int getStart() {
                return start;
            }

            @Override
            public List<Shape> getItems() {
                return items;
            }
        };
    }

    default List<Shape> getItems() {
        return Collections.emptyList();
    }

    default int getStart() {
        return -1;
    }

}
