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
    private static String DATA_HOME_DIR;

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
        if (DATA_HOME_DIR == null) {
            boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
            String igbHomeDirName = ".igbfx";
            if (IS_WINDOWS) {
                DATA_HOME_DIR = System.getenv("AppData") + File.separator + "IGB_FX";
            } else {
                DATA_HOME_DIR = System.getProperty("user.home") + File.separator + igbHomeDirName;
            }
            File igbDataHomeFile = new File(DATA_HOME_DIR);
            igbDataHomeFile.mkdir();
        }
        return new File(DATA_HOME_DIR + File.separator);
    }

}
