/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.preferences;

import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.lorainelab.igb.version.IgbVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class PreferenceUtils {

    private static final Logger LOG = LoggerFactory.getLogger(PreferenceUtils.class);
    private static final boolean IS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");
    private static final String ROOT_PREFERENCE_NODE_NAME = "org/lorainelab/igb";
    private static String DATA_HOME_DIR;

    public static Preferences getDefaultPrefsNode() {
        return NbPreferences.userRootImpl();
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
            Arrays.stream(getDefaultPrefsNode().keys()).forEach(key -> getDefaultPrefsNode().remove(key));
            Arrays.stream(getDefaultPrefsNode().childrenNames())
                    .map(nodeName -> getDefaultPrefsNode().node(nodeName))
                    .forEach(node -> {
                        try {
                            node.removeNode();
                        } catch (BackingStoreException ex) {
                            LOG.error(ex.getMessage(), ex);
                        }
                    });
        } catch (BackingStoreException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    public static File getApplicationDataDirectory() {
        if (DATA_HOME_DIR == null) {
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

    public static File getPreferenceConfigDirectory() {
        File applicationDataDirectory = getApplicationDataDirectory();
        File preferenceConfigDirectory = new File(applicationDataDirectory.getPath() + File.separator + IgbVersion.getVersion() + File.separator + "preferences");
        preferenceConfigDirectory.mkdir();
        return preferenceConfigDirectory;
    }

    private static String getPreferenceNodeName(String name) {
        final String replace = name.replace('.', '/');
        return replace;//replace.replaceFirst(ROOT_PREFERENCE_NODE_NAME + "/", "");
    }

}
