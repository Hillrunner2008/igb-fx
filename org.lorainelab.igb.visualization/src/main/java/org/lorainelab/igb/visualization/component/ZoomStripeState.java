/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.visualization.component;

import org.lorainelab.igb.visualization.component.api.State;

/**
 *
 * @author jeckstei
 */
public class ZoomStripeState implements State {

    public static ZoomStripeState factory() {
        return new ZoomStripeState();
    }
}
