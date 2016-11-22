package org.lorainelab.igb.quickload;

import javafx.scene.control.CheckBox;

/**
 *
 * @author dcnorris
 */
public class ReferenceHoldingCheckBox<S> extends CheckBox {

    S reference;

    public S getReference() {
        return reference;
    }

    public void setReference(S reference) {
        this.reference = reference;
    }

}
