/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.cache.disk;

import java.net.URL;
import java.util.prefs.Preferences;
import org.lorainelab.igb.preferences.PreferenceUtils;

/**
 *
 * @author jeckstei
 */
public class PreferencesManager {

    static String CONFIRM_BEFORE_CACHE_SEQUENCE_IN_BACKGROUND;
    static boolean default_confirm_before_cache_sequence_in_background = false;

    public static String getCacheRequestKey(URL url) {
        return "";
    }

    public static Preferences getCachePrefsNode() {
        return PreferenceUtils.getPackagePrefsNode(PreferencesManager.class).node("org.lorainelab.igb.cache.root");
    }

    public static Preferences getCacheRequestNode() {
        return PreferenceUtils.getPackagePrefsNode(PreferencesManager.class).node("org.lorainelab.igb.cache.request");
    }

}
