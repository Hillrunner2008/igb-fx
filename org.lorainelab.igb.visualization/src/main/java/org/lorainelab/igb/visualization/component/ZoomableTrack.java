/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import org.lorainelab.igb.visualization.component.api.Component;

/**
 *
 * @author jeckstei
 */
public class ZoomableTrack extends Component {

    @Override
    public List<Component> render() {
        return Lists.newArrayList();
    }

    @Override
    public Component beforeComponentReady() {
        return this;
    }

}
