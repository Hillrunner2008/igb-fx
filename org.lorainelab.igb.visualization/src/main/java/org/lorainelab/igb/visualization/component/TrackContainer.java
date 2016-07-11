/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import com.google.common.collect.Lists;
import java.util.List;
import org.lorainelab.igb.visualization.component.api.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class TrackContainer extends Component {

    private static final Logger LOG = LoggerFactory.getLogger(TrackContainer.class);
    
    @Override
    public List<Component> render() {
        LOG.info("render");
        return Lists.newArrayList();
    }

    @Override
    public Component beforeComponentReady() {
        return this;
    }
    
}
