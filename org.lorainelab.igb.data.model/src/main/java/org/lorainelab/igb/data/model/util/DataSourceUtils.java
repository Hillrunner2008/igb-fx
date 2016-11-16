/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.util;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author dcnorris
 */
public interface DataSourceUtils {

    InputStream getStreamFor(final String path, boolean forceCache) throws IOException;

}
