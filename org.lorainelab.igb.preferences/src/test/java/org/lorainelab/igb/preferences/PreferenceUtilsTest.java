package org.lorainelab.igb.preferences;

import java.util.prefs.Preferences;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author dcnorris
 */
public class PreferenceUtilsTest {

    private static final Logger LOG = LoggerFactory.getLogger(PreferenceUtilsTest.class);

    @Test
    public void testInsertValue() {
        Preferences packagePrefsNode = PreferenceUtils.getPackagePrefsNode(PreferenceUtils.class);
        packagePrefsNode.put("Test", "Testing");
    }
}
