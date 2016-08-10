/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferences;

import java.io.File;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.lorainelab.igb.menu.about.IgbVersion;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class PreferenceUtils {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PreferenceUtils.class);
    private static final String ROOT_PREFERENCE_NODE_NAME = "org/lorainelab/igb";
    private static String DATA_HOME_DIR;

    public static Preferences getDefaultPrefsNode() {
        String prefDirPath = getApplicationDataDirectory().getAbsolutePath() + File.separator + "preferences" + File.separator + IgbVersion.getVersion() + File.separator;
        System.setProperty("java.util.prefs.userRoot", prefDirPath);
        return Preferences.userRoot().node(ROOT_PREFERENCE_NODE_NAME);
    }

    public static Preferences getPackagePrefsNode(Class c) {
        final String packageName = getPreferenceNodeName(c.getPackage().getName());
        return getDefaultPrefsNode().node(packageName);
    }

    public static Preferences getClassPrefsNode(Class c) {
        final String className = getPreferenceNodeName(c.getCanonicalName());
        return getDefaultPrefsNode().node(className);
    }

    public static void clearAllPreferences() {
        try {
            getDefaultPrefsNode().removeNode();
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public static File getApplicationDataDirectory() {
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

    private static String getPreferenceNodeName(String name) {
        final String replace = name.replace('.', '/');
        return replace.replaceFirst(ROOT_PREFERENCE_NODE_NAME + "/", "");
    }

}
