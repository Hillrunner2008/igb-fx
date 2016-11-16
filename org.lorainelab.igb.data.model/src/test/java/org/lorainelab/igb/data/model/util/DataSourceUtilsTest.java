/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.data.model.util;

import com.google.common.base.Charsets;
import com.google.common.io.CharStreams;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import org.junit.Test;

/**
 *
 * @author dcnorris
 */
public class DataSourceUtilsTest {

    @Test
    public void getStreamForHttpTest() throws IOException {
        InputStream streamFor = DataSourceUtilsImpl.getStreamFor("http://www.igbquickload.org/pollen/contents.txt");
        String contentTxt = CharStreams.toString(new InputStreamReader(streamFor, Charsets.UTF_8));
        System.out.println(contentTxt);
    }
}
