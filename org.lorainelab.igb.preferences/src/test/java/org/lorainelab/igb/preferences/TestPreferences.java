/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferences;

import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author deva
 */
public class TestPreferences {

    private static Preferences modulePreferencesNode;

    public TestPreferences() {
    }

    @BeforeClass
    public static void setUpClass() {
        modulePreferencesNode = PreferenceUtils.getPackagePrefsNode(TestPreferences.class);
    }

    @AfterClass
    public static void tearDownClass() {
        Preferences node = modulePreferencesNode.node("demoNode");
        try {
            node.removeNode();
        } catch (BackingStoreException ex) {
            assertTrue(false);
        }
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() throws BackingStoreException {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void test() {
        try {
            Preferences node = modulePreferencesNode.node("demoNode");
            node.put("demoKey2", "demoValue");
            node.put("demoKey", "demoValue2");
            Preferences node2 = modulePreferencesNode.node("demoNode2");
            node2.put("demoKey2", "demoValue");
            node2.put("demoKey", "demoValue2");
            
            Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(n -> {
                        assertTrue(n.get("demoKey2", "").equals("demoValue"));
                        assertTrue(n.get("demoKey", "").equals("demoValue2"));
                    });
        } catch (BackingStoreException ex) {
            assertTrue(false);
        }
    }
}
