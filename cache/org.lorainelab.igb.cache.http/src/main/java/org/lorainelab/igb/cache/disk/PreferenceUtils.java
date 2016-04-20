/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.cache.disk;

import java.io.File;
import java.net.URL;
import java.util.prefs.Preferences;

/**
 *
 * @author jeckstei
 */
public class PreferenceUtils {
    static String CONFIRM_BEFORE_CACHE_SEQUENCE_IN_BACKGROUND;
    static boolean default_confirm_before_cache_sequence_in_background = false;

    public static String getCacheRequestKey(URL url) {
        return "";
    }

    public static Preferences getCachePrefsNode() {
        return Preferences.userRoot().node("org.lorainelab.igb.cache.root");
    }

    public static Preferences getCacheRequestNode() {
        return Preferences.userRoot().node("org.lorainelab.igb.cache.request");
    }
    
    public static File getDataDir() {
        String dataDir = System.getProperty("karaf.data");
        if(dataDir == null) {
            dataDir = System.getProperty("user.home");
        }
        return new File(dataDir);
    }
}
