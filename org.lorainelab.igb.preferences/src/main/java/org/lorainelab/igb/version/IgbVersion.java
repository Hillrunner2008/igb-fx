/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.version;

/**
 *
 * @author Devdatta Kulkarni
 */
public class IgbVersion {

    private final static int MAJOR_VERSION = 9;
    private final static int MINOR_VERSION = 0;

    public static String getVersion() {
        return MAJOR_VERSION + "." + MINOR_VERSION;
    }

    public static int getMajorVersion() {
        return MAJOR_VERSION;
    }

    public static int getMinorVersion() {
        return MINOR_VERSION;
    }

}
