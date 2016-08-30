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
import aQute.bnd.annotation.component.Reference;
import com.google.common.collect.Lists;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.UUID;
import javax.sql.DataSource;
import org.apache.commons.io.FileUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StoredField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexNotFoundException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.SimpleFSDirectory;
import org.apache.lucene.util.Version;
import org.lorainelab.igb.preferences.PreferenceUtils;
import org.lorainelab.igb.search.api.SearchService;
import org.lorainelab.igb.search.api.model.Document;
import org.lorainelab.igb.search.api.model.IndexIdentity;
import org.osgi.service.jdbc.DataSourceFactory;
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
    private DataSourceFactory dataSourceFactory;
    private DataSource ds;

    @Activate
    public void activate(Map<String, Object> properties) throws IOException {
        analyzer = new StandardAnalyzer();
        analyzer.setVersion(Version.LUCENE_6_0_0);
        indexRoot = PreferenceUtils.getApplicationDataDirectory() + File.separator + "lucene" + File.separator;
        initDb();
    }

    private void initDb() {
        try {
            Properties props = new Properties();
            props.put("databaseName", "data/search.sqlite");
            ds = dataSourceFactory.createDataSource(props);
            try (Connection dsConnection = ds.getConnection()) {
                try (Statement stmt = dsConnection.createStatement()) {
                    String sql = "CREATE TABLE search "
                            + "(id TEXT PRIMARY KEY NOT NULL,"
                            + " resource TEXT NOT NULL)";
                    stmt.executeUpdate(sql);
                } catch (SQLException ex) {
                    LOG.debug(ex.getMessage(), ex);
                }
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Deactivate
    public void deactivate() throws IOException {
        LOG.info("LuceneSearchService deactivated");
        analyzer.close();
    }

    @Reference(target = "(osgi.jdbc.driver.name=sqlite)")
    public void setDatasourceFactory(DataSourceFactory dataSourceFactory) {
        this.dataSourceFactory = dataSourceFactory;
    }

    @Override
    public void index(List<Document> documents, IndexIdentity indexIdentity) {
        LOG.debug("Creating index for {}", indexIdentity.getId());
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            try (IndexWriter writer = new IndexWriter(index, config)) {

                documents.stream().forEach((document) -> {
                    try {
                        org.apache.lucene.document.Document doc = new org.apache.lucene.document.Document();
                        document.getFields().keySet().stream().forEach(key -> {
                            switch (key) {
                                case "id":
                                    doc.add(new TextField(key, document.getFields().get(key), Field.Store.YES));
                                    break;
                                case "chromosomeId":
                                    doc.add(new TextField(key, document.getFields().get(key), Field.Store.YES));
                                    break;
                                case "source":
                                    doc.add(new TextField(key, document.getFields().get(key), Field.Store.NO));
                                    break;
                                default:
                                    doc.add(new StoredField(key, document.getFields().get(key)));
                                    break;

                            }

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
        LOG.debug("Finished index for {}", indexIdentity.getId());
    }

    @Override
    public List<Document> search(String query, IndexIdentity indexIdentity) {
        LOG.info("searching for: {}", query);
        List<Document> results = Lists.newArrayList();
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
            Query queryParser;
            try {
                queryParser = new MultiFieldQueryParser(new String[]{"id", "chromosomeId"}, analyzer).parse(query);
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
            } catch (IndexNotFoundException ex) {
                LOG.error("clearing index since it could not be found");
                clearIndex(indexIdentity);
            }
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return results;

    }

    @Override
    public void clearIndex(IndexIdentity indexIdentity) {
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "DELETE FROM search WHERE ID=?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, indexIdentity.getId());
                stmt.execute();
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
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

        try (Connection dsConnection = ds.getConnection()) {
            try (Statement stmt = dsConnection.createStatement()) {
                String sql = "DROP TABLE search";
                stmt.execute(sql);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        try {
            FileUtils.deleteDirectory(new File(indexRoot));
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<IndexIdentity> getResourceIndexIdentity(String resource) {
        String id;
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "SELECT * FROM search WHERE resource=?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, resource);
                ResultSet result = stmt.executeQuery();
                id = result.getString("id");
            }
        } catch (SQLException ex) {
            LOG.debug(ex.getMessage(), ex);
            return Optional.empty();
        }
        return Optional.of(new IndexIdentity(id));
    }

    @Override
    public void setResourceIndexIdentity(String resource, IndexIdentity indexIdentity) {
        try (Connection dsConnection = ds.getConnection()) {
            dsConnection.setAutoCommit(false);
            String sql = "INSERT INTO search (id,resource) VALUES (?,?)";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, indexIdentity.getId());
                stmt.setString(2, resource);
                stmt.executeUpdate();
            }
            dsConnection.commit();
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }

        try (Connection dsConnection = ds.getConnection()) {
            String sql = "SELECT * FROM search WHERE resource=?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, resource);
                ResultSet result = stmt.executeQuery();
                LOG.info(result.getString("id"));
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void clearByQuery(IndexIdentity indexIdentity, String field, String query) {
        try (Directory index = new SimpleFSDirectory(Paths.get(indexRoot + indexIdentity.getId()))) {
            IndexWriterConfig config = new IndexWriterConfig(analyzer);

            try (IndexWriter writer = new IndexWriter(index, config)) {
                BooleanQuery qry = new BooleanQuery.Builder()
                        .add(new TermQuery(new Term(field, QueryParser.escape(query))), BooleanClause.Occur.MUST)
                        .build();
                writer.deleteDocuments(qry);
            }
        } catch (Exception ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

}
