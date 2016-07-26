/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencemanagerImpl;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sql.DataSource;
import org.lorainelab.igb.persistencemanager.PersistenceManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.mockito.Mock;
import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import org.osgi.service.jdbc.DataSourceFactory;
import persistencemanagerFactory.PersistencemanagerFactory;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

/**
 *
 * @author Devdatta Kulkarni
 */
public class PersistencemanagerImplTest {

    PersistencemanagerFactory factory;
    PersistenceManager pm;
    HashMap<String, String> testMap = new HashMap<>();
    @Mock DataSourceFactory dataSourceFactory = mock(DataSourceFactory.class);
    @Mock DataSource dataSource = mock(DataSource.class);
    Connection connection;
    
    public PersistencemanagerImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {

    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp(){
        try {
            //load driver for sqlite           
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:test.db");
            MockitoAnnotations.initMocks(this);
            
            factory = new PersistencemanagerFactory();
            factory.setDatasourceFactory(dataSourceFactory);
            
            when(dataSourceFactory.createDataSource(any(Properties.class)))
                    .thenReturn(dataSource);
            when(dataSource.getConnection()).thenAnswer(x -> DriverManager.getConnection("jdbc:sqlite:test.db"));
            pm = factory.getPersistenceManager(PersistencemanagerImplTest.class).get();
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
            Logger.getLogger(PersistencemanagerImplTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            ex.printStackTrace();
            Logger.getLogger(PersistencemanagerImplTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @After
    public void tearDown() throws SQLException {
        pm.clear();
    }

    /**
     * Test of put method, of class PersistencemanagerImpl.
     */
    @Test
    public void testPut() throws SQLException {
        pm.put("key", "value");
        assertEquals(pm.get("key").get(), "value");
    }

    /**
     * Test of get method, of class PersistencemanagerImpl.
     */
    @Test
    public void testGet() {
        pm.put("key", "value");
        assertEquals(pm.get("key").get(), "value");
    }

    /**
     * Test of containsKey method, of class PersistencemanagerImpl.
     */
    @Test
    public void testContainsKey() {
        pm.put("key", "value");
        assertTrue(pm.containsKey("key"));
        assertFalse(pm.containsKey("randomKey"));
    }

    /**
     * Test of remove method, of class PersistencemanagerImpl.
     */
    @Test
    public void testRemove() {
        pm.put("key", "value");
        assertEquals(pm.get("key").get(), "value");
        pm.remove("key");
        assertFalse(pm.get("key").isPresent());
    }

    /**
     * Test of putAll method, of class PersistencemanagerImpl.
     */
    @Test
    public void testPutAll() {
        HashMap<String, String> map = new HashMap<>();
        initMap();
        pm.putAll(testMap);
        Map tempmap = pm.getAll();
        map.keySet().forEach((key) -> {
            assertEquals(map.get(key), tempmap.get(key));
        });
    }

    /**
     * Test of getAll method, of class PersistencemanagerImpl.
     */
    @Test
    public void testGetAll() {
        HashMap<String, String> map = new HashMap<>();
        initMap();
        pm.putAll(testMap);
        Map tempmap = pm.getAll();
        map.keySet().forEach((key) -> {
            assertEquals(map.get(key), tempmap.get(key));
        });
    }

    /**
     * Test of getAllLike method, of class PersistencemanagerImpl.
     */
    @Test
    public void testGetAllLike() {
        initMap();
        pm.putAll(testMap);
        Map<String, String> tempmap = pm.getAllLike("as");
        tempmap.keySet().forEach((key) -> {
            assertTrue(key.split("as").length > 1);
        });
    }

    private void initMap() {
        testMap.put("qwe", "qwe");
        testMap.put("asd", "zxc");
        testMap.put("zxc", "qwe");
        testMap.put("dsasf", "val");
    }

}
