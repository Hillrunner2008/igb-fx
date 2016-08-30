package org.lorainelab.igb.utils;

import com.google.common.base.Charsets;

/**
 *
 * @author dcnorris
 */
//for rarely referenced large strings... 
public class StringWrapper {

    private byte[] bytes;

    public StringWrapper(String s) {
        bytes = s.getBytes(Charsets.ISO_8859_1);
    }

    public String getString() {
        return new String(bytes, Charsets.ISO_8859_1);
    }
}
