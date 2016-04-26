/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.shapes;

import com.google.common.collect.Sets;
import java.util.stream.Stream;

/**
 *
 * @author jeckstei
 */
public interface Shapes {

    default Stream<Shape> shapes(Shape... shapes) {
        return Sets.newHashSet(shapes).stream();
    }
;
}
