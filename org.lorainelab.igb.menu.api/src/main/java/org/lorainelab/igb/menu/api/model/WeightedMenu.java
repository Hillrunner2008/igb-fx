package org.lorainelab.igb.menu.api.model;

import javafx.scene.Node;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuItem;

/**
 *
 * @author dcnorris
 */
public class WeightedMenu extends Menu {

    int weight;

    public WeightedMenu(int weight) {
        this.weight = weight;
    }

    public WeightedMenu(int weight, String text) {
        super(text);
        this.weight = weight;
    }

    public WeightedMenu(int weight, String text, Node graphic) {
        super(text, graphic);
        this.weight = weight;
    }

    public WeightedMenu(int weight, String text, Node graphic, MenuItem... items) {
        super(text, graphic, items);
        this.weight = weight;
    }

    public int getWeight() {
        return weight;
    }

}
