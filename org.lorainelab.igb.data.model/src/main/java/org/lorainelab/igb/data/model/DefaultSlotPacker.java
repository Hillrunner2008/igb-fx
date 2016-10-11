package org.lorainelab.igb.data.model;

import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;

/**
 *
 * @author dcnorris
 */
public class DefaultSlotPacker {

    //negative strand
    public static double packTopToBottom(int slot) {
        return slot * SLOT_HEIGHT;
    }

    //positive strand
    public static double packBottomToTop(int slot, double trackHeight) {
        return (trackHeight - (slot * SLOT_HEIGHT)) - SLOT_HEIGHT;
    }

}
