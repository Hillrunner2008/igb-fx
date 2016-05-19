/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.search.lucene;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.Deactivate;
import com.google.common.collect.Lists;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.Document;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@Component
public class LuceneSearchService implements SearchService {
    
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LuceneSearchService.class);
    private StandardAnalyzer analyzer;
    private Directory index;
    private IndexWriterConfig config;
    private IndexWriter w;
    
    @Activate
    public void activate() throws IOException {
        analyzer = new StandardAnalyzer();
        analyzer.setVersion(Version.LUCENE_6_0_0);
        
        index = new SimpleFSDirectory(Paths.get("/tmp/lucene"));
        
        config = new IndexWriterConfig(analyzer);
        w = new IndexWriter(index, config);
    }
    
    @Deactivate
    public void deactivate() throws IOException {
        w.close();
        index.close();
        analyzer.close();
    }
    
    @Override
    public void index(Document document) {
        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
        doc.add(new TextField("id", document.getId(), Field.Store.YES));
        doc.add(new TextField("name", document.getName(), Field.Store.YES));
        try {
            w.addDocument(doc);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }
    
    @Override
    public List<Document> search(String query) {
        List<Document> results = Lists.newArrayList();
        try {
            Query q = null;
            try {
                q = new QueryParser("id", analyzer).parse(query);
            } catch (Exception e) {
                LOG.error(e.getMessage(), e);
            }

            int hitsPerPage = 10;
            IndexReader reader = DirectoryReader.open(index);
            IndexSearcher searcher = new IndexSearcher(reader);
            TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
            searcher.search(q, collector);
            ScoreDoc[] hits = collector.topDocs().scoreDocs;
            
            for (int i = 0; i < hits.length; ++i) {
                int docId = hits[i].doc;
                org.apache.lucene.document.Document d = searcher.doc(docId);
                Document result = new Document();
                result.setId(d.get("id"));
                result.setName(d.get("name"));
                results.add(result);
            }
            reader.close();
            return results;
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return results;
    }
    
}
