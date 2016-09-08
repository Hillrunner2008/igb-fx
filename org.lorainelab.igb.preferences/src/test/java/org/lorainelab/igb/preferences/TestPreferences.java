/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferences;

import java.util.Arrays;
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
    public void testPut() {
        Preferences node = modulePreferencesNode.node("demoNode");
        node.put("demoKey2", "demoValue");
    }
    
    @Test
    public void testGet() throws BackingStoreException {
        //Preferences node = modulePreferencesNode.node("demoNode");
        Arrays.stream(modulePreferencesNode.childrenNames())
                    .map(nodeName -> modulePreferencesNode.node(nodeName))
                    .forEach(node -> {
                        System.out.println(node.get("demoKey2", ""));
                    });
    }
    
    @Test
    public void testDelete() throws BackingStoreException {
        Preferences node = modulePreferencesNode.node("demoNode");
        node.removeNode();
    }
}
