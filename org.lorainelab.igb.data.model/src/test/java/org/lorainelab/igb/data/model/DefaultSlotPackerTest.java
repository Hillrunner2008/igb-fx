/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model;

import static org.junit.Assert.assertEquals;
import org.junit.Test;
import static org.lorainelab.igb.data.model.DefaultSlotPacker.packBottomToTop;
import static org.lorainelab.igb.data.model.DefaultSlotPacker.packTopToBottom;
import static org.lorainelab.igb.data.model.glyph.Glyph.SLOT_HEIGHT;

/**
 *
 * @author dcnorris
 */
public class DefaultSlotPackerTest {

    @Test
    public void testPackTopToBottom() {
        double minY = packTopToBottom(0);
        assertEquals(minY, 0, 0);
        minY = packTopToBottom(1);
        assertEquals(minY, SLOT_HEIGHT, 0);
        minY = packTopToBottom(2);
        assertEquals(minY, SLOT_HEIGHT * 2, 0);
    }

    @Test
    public void testPackBottomToTop() {
        final int height = 100;
        double minY = packBottomToTop(0, height);
        assertEquals(minY, height - SLOT_HEIGHT, 0);
        minY = packBottomToTop(1, height);
        assertEquals(minY, height - (SLOT_HEIGHT * 2), 0);
        minY = packBottomToTop(2, height);
        assertEquals(minY,  height - (SLOT_HEIGHT * 3), 0);
    }

}
