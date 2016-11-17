/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.quickload.util;

import java.io.IOException;
import java.util.List;
import org.junit.Assert;
import org.junit.Test;
import org.lorainelab.igb.quickload.model.QuickloadFile;

/**
 *
 * @author dcnorris
 */
public class AnnotsParserTest {

    @Test
    public void getQuickloadFileListTest() throws IOException {
        AnnotsParser parser = new AnnotsParser();
        final List<QuickloadFile> quickloadFileList = parser.getQuickloadFileList(AnnotsParserTest.class.getClassLoader().getResourceAsStream("annots-1.xml"));
        Assert.assertEquals(3, quickloadFileList.size());
        quickloadFileList.stream().forEach(file -> {
            Assert.assertTrue("", !file.getProps().isEmpty());
        });
    }

}
