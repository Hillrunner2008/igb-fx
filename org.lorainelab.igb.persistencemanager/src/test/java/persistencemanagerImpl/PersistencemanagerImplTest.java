/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package persistencemanagerImpl;

import java.util.HashMap;
import java.util.Optional;
import org.lorainelab.igb.persistencemanager.PersistenceManager;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.osgi.service.jdbc.DataSourceFactory;

/**
 *
 * @author Devdatta Kulkarni
 */
public class PersistencemanagerImplTest {

    persistencemanagerFactory.PersistencemanagerFactory factory;
    PersistenceManager pm;
    
    public PersistencemanagerImplTest() {
    }

    @BeforeClass
    public static void setUpClass() {
    }

    @AfterClass
    public static void tearDownClass() {
    }

    @Before
    public void setUp() {
        
        pm = factory.getPersistenceManager("test").get();
        System.out.println("return from setup");
    }

    @After
    public void tearDown() {
        pm.dropTable();
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void testDbinsert() {
        pm.put("dummyKey", "dummyValue");
        assertTrue(pm.containsKey("dummyKey"));
    }
//
//    /**
//     * Test of put method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testPut() {
//        System.out.println("put");
//        String key = "";
//        String value = "";
//        PersistencemanagerImpl instance = null;
//        instance.put(key, value);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of get method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testGet() {
//        System.out.println("get");
//        String key = "";
//        PersistencemanagerImpl instance = null;
//        Optional<String> expResult = null;
//        Optional<String> result = instance.get(key);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of containsKey method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testContainsKey() {
//        System.out.println("containsKey");
//        String key = "";
//        PersistencemanagerImpl instance = null;
//        boolean expResult = false;
//        boolean result = instance.containsKey(key);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of remove method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testRemove() {
//        System.out.println("remove");
//        String key = "";
//        PersistencemanagerImpl instance = null;
//        instance.remove(key);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of dropTable method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testDropTable() {
//        System.out.println("dropTable");
//        PersistencemanagerImpl instance = null;
//        instance.dropTable();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of putAll method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testPutAll() {
//        System.out.println("putAll");
//        HashMap<String, String> valueMap = null;
//        PersistencemanagerImpl instance = null;
//        instance.putAll(valueMap);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAll method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testGetAll() {
//        System.out.println("getAll");
//        PersistencemanagerImpl instance = null;
//        HashMap<String, String> expResult = null;
//        HashMap<String, String> result = instance.getAll();
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of persistConnection method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testPersistConnection() {
//        System.out.println("persistConnection");
//        PersistencemanagerImpl instance = null;
//        instance.persistConnection();
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of getAllLike method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testGetAllLike() {
//        System.out.println("getAllLike");
//        String pattern = "";
//        PersistencemanagerImpl instance = null;
//        HashMap<String, String> expResult = null;
//        HashMap<String, String> result = instance.getAllLike(pattern);
//        assertEquals(expResult, result);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
//
//    /**
//     * Test of setDatasourceFactory method, of class PersistencemanagerImpl.
//     */
//    @Test
//    public void testSetDatasourceFactory() {
//        System.out.println("setDatasourceFactory");
//        DataSourceFactory dataSourceFactory = null;
//        PersistencemanagerImpl instance = null;
//        instance.setDatasourceFactory(dataSourceFactory);
//        // TODO review the generated test code and remove the default call to fail.
//        fail("The test case is a prototype.");
//    }
}
