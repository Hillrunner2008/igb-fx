/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.view;

import org.lorainelab.igb.data.model.Feature;
import org.lorainelab.igb.data.model.shapes.Composition;
import org.lorainelab.igb.data.model.shapes.Join;
import org.lorainelab.igb.data.model.shapes.Shapes;

/**
 *
 * @author dcnorris
 * @param <T>
 */
public interface Renderer<T extends Feature> extends Shapes, Join, Layer, Composition {

    public Composition render(T t);

}
