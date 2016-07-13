package org.lorainelab.igb.menu.api.model;

import javafx.scene.Node;
import javafx.scene.control.MenuItem;

/**
 *
 * @author dcnorris
 */
public class WeightedMenuItem extends MenuItem implements WeightedMenuEntry {

    int weight;

    public WeightedMenuItem(int weight) {
        this.weight = weight;
    }

    public WeightedMenuItem(int weight, String text) {
        super(text);
        this.weight = weight;
    }

    public WeightedMenuItem(int weight, String text, Node graphic) {
        super(text, graphic);
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

    @Override
    public MenuItem getMenuEntry() {
        return this;
    }

}
