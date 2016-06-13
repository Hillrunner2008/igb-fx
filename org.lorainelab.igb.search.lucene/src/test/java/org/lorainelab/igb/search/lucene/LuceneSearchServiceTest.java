/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.search.lucene;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.lorainelab.igb.search.api.model.Document;
import org.lorainelab.igb.search.api.model.IndexIdentity;

/**
 *
 * @author jeckstei
 */
public class LuceneSearchServiceTest {

    LuceneSearchService luceneSearchService;
    IndexIdentity indexIdentity;

    public LuceneSearchServiceTest() throws IOException {

    }

    @Before
    public void before() throws IOException {
        luceneSearchService = new LuceneSearchService();
        indexIdentity = luceneSearchService.generateIndexIndentity();
        Map<String, Object> properties = Maps.newHashMap();
        properties.put("index.path.root", System.getProperty("java.io.tmpdir") + File.separatorChar + "lucene");
        luceneSearchService.activate(properties);
        luceneSearchService.clearIndex(indexIdentity);
    }

    @After
    public void after() throws IOException {
        luceneSearchService.deleteAll();
        luceneSearchService.deactivate();
    }

    //@Test
    public void testSearch() {
        Document document1 = new Document();
        document1.getFields().put("id","foo123");

        Document document2 = new Document();
        document2.getFields().put("id","gene123");
        luceneSearchService.index(Lists.newArrayList(document1, document2), indexIdentity);

        List<Document> search = luceneSearchService.search("foo*", indexIdentity);
        Assert.assertTrue(search.size() == 1);
    }

}
