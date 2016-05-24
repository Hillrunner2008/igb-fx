/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.search.lucene;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Deactivate;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import org.apache.commons.io.FileUtils;
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
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.Document;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
@Component(configurationPolicy = ConfigurationPolicy.require)
public class LuceneSearchService implements SearchService {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(LuceneSearchService.class);
    private StandardAnalyzer analyzer;
    private String indexRoot;
    private Preferences preferences;
    private String demoOnly; //TODO: remove
    

    @Activate
    public void activate(Map<String, Object> properties) throws IOException {
        analyzer = new StandardAnalyzer();
        analyzer.setVersion(Version.LUCENE_6_0_0);
        indexRoot = (String) properties.get("index.path.root") + File.separator + "lucene" + File.separator;
        LOG.info("prop: {}", indexRoot);
        preferences = PreferenceUtils.getDefaultPrefsNode().node("org.lorainelab.igb.search.lucene.root");
    }

    @Deactivate
    public void deactivate() throws IOException {
        analyzer.close();
    }

    @Override
    public void index(List<Document> documents, IndexIdentity indexIdentity) {
        LOG.info("Creating index for {}", indexIdentity.getId());
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            try (IndexWriter writer = new IndexWriter(index, config)) {
                documents.stream().forEach((document) -> {
                    try {
                        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                        document.getFields().keySet().stream().forEach(key -> {
                            doc.add(new TextField(key, document.getFields().get(key), Field.Store.YES));
                        });
                        LOG.debug("indexing {}", doc.get("id"));
                        writer.addDocument(doc);
                    } catch (Exception ex) {
                        LOG.error(ex.getMessage(), ex);
                    }
                });
            }

        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        LOG.info("Finished index for {}", indexIdentity.getId());
    }

    @Override
    public List<Document> search(String query, IndexIdentity indexIdentity) {
        LOG.info("searching for: {}", query);
        List<Document> results = Lists.newArrayList();
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
            Query queryParser;
            try {
                queryParser = new QueryParser("id", analyzer).parse(query);
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
                return results;
            }
            int hitsPerPage = 10;
            try (IndexReader reader = DirectoryReader.open(index)) {
                IndexSearcher searcher = new IndexSearcher(reader);
                TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage);
                searcher.search(queryParser, collector);
                ScoreDoc[] hits = collector.topDocs().scoreDocs;
                for (int i = 0; i < hits.length; ++i) {
                    int docId = hits[i].doc;
                    org.apache.lucene.document.Document d = searcher.doc(docId);
                    Document result = new Document();
                    d.getFields().stream().forEach(key -> {
                        result.getFields().put(key.name(), d.get(key.name()));
                    });
                    LOG.debug("result: {}", d.get("id"));
                    results.add(result);
                }
                reader.close();
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return results;

    }

    @Override
    public void clearIndex(IndexIdentity indexIdentity) {
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
            preferences.remove(indexIdentity.getId());
            IndexWriterConfig config = new IndexWriterConfig(analyzer);
            try (IndexWriter writer = new IndexWriter(index, config)) {
                writer.deleteAll();
            } catch (Exception ex) {
                LOG.error(ex.getMessage(), ex);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public IndexIdentity generateIndexIndentity() {
        return new IndexIdentity(UUID.randomUUID().toString());
    }

    @Override
    public void deleteAll() {
        try {
            preferences.clear();
            FileUtils.deleteDirectory(new File(indexRoot));
        } catch (BackingStoreException | IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<IndexIdentity> getResourceIndexIdentity(String resource) {
        if(resource == null && demoOnly != null) {
            resource = demoOnly;
        } else if(resource == null && demoOnly == null) {
            return Optional.empty();
        }
        String value = preferences.get(resource, "");
        if(Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }
        return Optional.of(new IndexIdentity(value));
    }

    @Override
    public void setResourceIndexIdentity(String resource, IndexIdentity indexIdentity) {
        demoOnly = resource;
        preferences.put(resource, indexIdentity.getId());
    }

}
