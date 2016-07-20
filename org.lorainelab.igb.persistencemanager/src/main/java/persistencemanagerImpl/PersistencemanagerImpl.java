package persistencemanagerImpl;

import aQute.bnd.annotation.component.Activate;
import aQute.bnd.annotation.component.Component;
import aQute.bnd.annotation.component.ConfigurationPolicy;
import aQute.bnd.annotation.component.Reference;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.osgi.service.jdbc.DataSourceFactory;
import org.lorainelab.igb.persistencemanager.PersistenceManager;
import org.slf4j.LoggerFactory;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Devdatta Kulkarni
 */
public class PersistencemanagerImpl implements PersistenceManager {

    private static final String DBNAME = "data/database.sqlite";
    private static final String KEY_COLUMN_NAME = "key";
    private static final String VALUE_COLUMN_NAME = "value";

//    private Connection connection;
    private String tableName;
    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(PersistencemanagerImpl.class);
    private DataSourceFactory dataSourceFactory;
    private DataSource ds;


    public PersistencemanagerImpl(String tableName,DataSourceFactory dataSourceFactory) {
        this.tableName = tableName;
        this.dataSourceFactory = dataSourceFactory;
        initDB();
    }

    private void initDB() {
        LOG.trace("init creating table");
        Properties props = new Properties();
        props.put("databaseName", DBNAME);
        String sql = "CREATE TABLE IF NOT EXISTS " + tableName
                + " (" + KEY_COLUMN_NAME + " TEXT PRIMARY KEY NOT NULL,"
                + " " + VALUE_COLUMN_NAME + " TEXT NOT NULL)";
        System.out.println("before try datasrc factory: " + dataSourceFactory);
        try {
            ds = dataSourceFactory.createDataSource(props);
            ds.getConnection();
        } catch (Exception ex) {
            System.out.println("exception");
            ex.printStackTrace();
        }
        System.out.println("after try");

        try (Connection dsConnection = ds.getConnection()) {
            ds = dataSourceFactory.createDataSource(props);
            try (Statement stmt = dsConnection.createStatement()) {
                stmt.executeUpdate(sql);
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            Logger.getLogger(PersistencemanagerImpl.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void put(String key, String value) {
        try (Connection dsConnection = ds.getConnection()) {
            dsConnection.setAutoCommit(false);
            String sql = "INSERT OR REPLACE INTO " + getTableName() + " (" + KEY_COLUMN_NAME + "," + VALUE_COLUMN_NAME + ") VALUES (?,?)";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, key);
                stmt.setString(2, value);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
            }
            dsConnection.commit();
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public Optional<String> get(String key) {
        String value = null;
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "SELECT * FROM " + getTableName() + " WHERE " + KEY_COLUMN_NAME + "=?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, key);
                ResultSet result = stmt.executeQuery();
                if(!result.next()){
                    return Optional.empty();
                }
                value = result.getString(VALUE_COLUMN_NAME);
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
                return Optional.empty();
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
            return Optional.empty();
        }
        return Optional.of(value);
    }

    @Override
    public boolean containsKey(String key) {
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "SELECT COUNT(*) FROM " + getTableName() + " WHERE " + KEY_COLUMN_NAME + "=?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, key);
                ResultSet result = stmt.executeQuery();
                return result.getInt(1) > 0;
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return false;
    }

    @Override
    public void remove(String key) {
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "DELETE FROM " + getTableName() + " WHERE key=?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, key);
                stmt.execute();
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void clear() {
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "DROP TABLE " + getTableName();
            try (Statement stmt = dsConnection.createStatement()) {
                stmt.execute(sql);
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void putAll(Map<String, String> valueMap) {
        try (Connection dsConnection = ds.getConnection()) {
            dsConnection.setAutoCommit(false);
            String sql = "INSERT OR REPLACE INTO " + getTableName() + " (" + KEY_COLUMN_NAME + "," + VALUE_COLUMN_NAME + ") VALUES (?,?)";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                valueMap.forEach((key, value) -> {
                    try {
                        stmt.setString(1, key);
                        stmt.setString(2, value);
                        stmt.addBatch();
                    } catch (SQLException ex) {
                        LOG.trace("sql called: " + sql);
                        Logger.getLogger(PersistencemanagerImpl.class.getName()).log(Level.SEVERE, null, ex);
                    }
                });
                stmt.executeBatch();
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
            }
            dsConnection.commit();
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Override
    public HashMap<String, String> getAll() {
        HashMap<String, String> valueMap = new HashMap<String, String>();
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "SELECT * FROM " + getTableName();
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    valueMap.put(result.getString(KEY_COLUMN_NAME), result.getString(VALUE_COLUMN_NAME));
                }
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
                return valueMap;
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return valueMap;
    }

    @Override
    public HashMap<String, String> getAllLike(String pattern) {
        HashMap<String, String> valueMap = new HashMap<String, String>();
        try (Connection dsConnection = ds.getConnection()) {
            String sql = "SELECT * FROM " + getTableName() + " WHERE "+KEY_COLUMN_NAME+" LIKE ?";
            try (PreparedStatement stmt = dsConnection.prepareStatement(sql)) {
                stmt.setString(1, "%"+pattern+"%");
                ResultSet result = stmt.executeQuery();
                while (result.next()) {
                    valueMap.put(result.getString(KEY_COLUMN_NAME), result.getString(VALUE_COLUMN_NAME));
                }
            } catch (SQLException ex) {
                LOG.trace("sql called: " + sql);
                LOG.error(ex.getMessage(), ex);
                return valueMap;
            }
        } catch (SQLException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        return valueMap;
    }

    private String getTableName() {
        return tableName;
    }

}
