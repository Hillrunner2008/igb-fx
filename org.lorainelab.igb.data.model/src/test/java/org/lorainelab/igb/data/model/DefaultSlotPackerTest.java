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
        assertEquals(minY, 30, 0);
        minY = packTopToBottom(2);
        assertEquals(minY, 60, 0);
    }

    @Test
    public void testPackBottomToTop() {
        double minY = packBottomToTop(0, 100);
        assertEquals(minY, 70, 0);
        minY = packBottomToTop(1, 100);
        assertEquals(minY, 40, 0);
        minY = packBottomToTop(2, 100);
        assertEquals(minY, 10, 0);
    }

}
