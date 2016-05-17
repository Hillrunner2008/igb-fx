package org.lorainelab.igb.toolbar.api;

import javafx.scene.Node;
import javafx.scene.control.Button;

/**
 *
 * @author dcnorris
 */
public class WeightedButton extends Button {

    int weight;

    public WeightedButton(int weight) {
        this.weight = weight;
    }

    public WeightedButton(int weight, String text) {
        super(text);
        this.weight = weight;
    }

    public WeightedButton(int weight, String text, Node graphic) {
        super(text, graphic);
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }
}
