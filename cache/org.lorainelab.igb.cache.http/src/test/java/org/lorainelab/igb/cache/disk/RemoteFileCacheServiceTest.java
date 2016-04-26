/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package org.lorainelab.igb.cache.disk;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigInteger;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.lorainelab.igb.cache.api.CacheStatus;
import org.lorainelab.igb.stage.provider.api.StageProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author jeckstei
 */
public class RemoteFileCacheServiceTest {

    private static final Logger LOG = LoggerFactory.getLogger(RemoteFileCacheServiceTest.class);

    RemoteFileDiskCacheService remoteFileService;

    public RemoteFileCacheServiceTest() {

    }

    public static class AsNonApp extends Application {

        public static Stage stage;

        @Override
        public void start(Stage primaryStage) throws Exception {
            AsNonApp.stage = primaryStage;
            HBox layout = new HBox(10);
            primaryStage.setScene(new Scene(layout));
            primaryStage.show();
        }
    }

    @Before
    public void before() throws InterruptedException {
        try {
            remoteFileService = new RemoteFileDiskCacheService();
            remoteFileService.activate();
            new Thread(() -> {
                Application.launch(AsNonApp.class, new String[0]);

            }).start();

            Thread.sleep(1000);
            remoteFileService.setStageProvider(new StageProvider() {
                private Stage stage;

                @Override
                public Stage getStage() {

                    return AsNonApp.stage;

                }
            });
            RemoteFileDiskCacheService.DATA_DIR = "/home/jeckstei/.igb/fileCache/";
            File cacheDir = new File(RemoteFileDiskCacheService.DATA_DIR);
            FileUtils.forceMkdir(cacheDir);
            FileUtils.cleanDirectory(cacheDir);
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
    }

    @Test
    @Ignore
    public void testGetCacheStatus() {
        try {
            URL url = new URL("http://bioviz.org/quickload/A_thaliana_Jan_2004/chr2.bnib");
            remoteFileService.clearAllCaches();
            remoteFileService.getFilebyUrl(url, false);
            CacheStatus cacheStatus = remoteFileService.getCacheStatus(url);
            Assert.assertTrue(cacheStatus.isDataExists());
        } catch (MalformedURLException ex) {
            LOG.error(ex.getMessage(), ex);
        }

    }

    @Ignore
    @Test
    public void testEnforceEvictionPolicies() throws MalformedURLException {

        remoteFileService.clearAllCaches();
        remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_gambiae_Feb_2003/A_gambiae_Feb_2003.2bit"), false);
        BigInteger size = remoteFileService.getCacheSizeInMB();
        Assert.assertTrue(size.compareTo(BigInteger.ZERO) >= 1);
        try {
            FileUtils.writeStringToFile(new File(RemoteFileDiskCacheService.DATA_DIR + "aWdicXVpY2tsb2FkLm9yZy9BX2dhbWJpYWVfRmViXzIwMDMvQV9nYW1iaWFlX0ZlYl8yMDAzLjJiaXQ=/cache/data.cacheLastUpdate"), "0");
        } catch (IOException ex) {
            LOG.error(ex.getMessage(), ex);
        }
        remoteFileService.enforceEvictionPolicies();
        size = remoteFileService.getCacheSizeInMB();
        Assert.assertTrue(size.compareTo(BigInteger.ZERO) <= 0);

        remoteFileService.clearAllCaches();
        remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_gambiae_Feb_2003/A_gambiae_Feb_2003.2bit"), false);
        remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_lyrata_Apr_2011/A_lyrata_Apr_2011.2bit"), false);
        remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_thaliana_Jan_2004/chr2.bnib"), false);
        size = remoteFileService.getCacheSizeInMB().divide(BigInteger.valueOf(1000000));
        Assert.assertFalse(size.compareTo(RemoteFileDiskCacheService.DEFAULT_MAX_CACHE_SIZE_MB) <= 0);
        remoteFileService.enforceEvictionPolicies();
        size = remoteFileService.getCacheSizeInMB().divide(BigInteger.valueOf(1000000));
        Assert.assertTrue(size.compareTo(RemoteFileDiskCacheService.DEFAULT_MAX_CACHE_SIZE_MB) <= 0);
    }

    @Ignore
    @Test
    public void testGetFilebyUrl() throws MalformedURLException {
        Optional<InputStream> is = remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_gambiae_Feb_2003/A_gambiae_Feb_2003.2bit"), false);
        remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_lyrata_Apr_2011/A_lyrata_Apr_2011.2bit"), false);
        remoteFileService.getFilebyUrl(new URL("http://igbquickload.org/A_thaliana_Jan_2004/chr2.bnib"), false);
        Assert.assertTrue(is.isPresent());
    }

    @Ignore
    @Test
    public void testPromptCacheInBackground() throws MalformedURLException, InterruptedException {
        remoteFileService.promptToCacheInBackground(new URL("http://igbquickload.org/A_gambiae_Feb_2003/A_gambiae_Feb_2003.2bit"), true);
        Thread.sleep(100000);
    }

    @Ignore
    @Test
    public void testFileSizeLimit() throws MalformedURLException {
        remoteFileService.clearAllCaches();
        Optional<InputStream> is = remoteFileService.getFilebyUrl(new URL("http://localhost/index.html"), false);
        Assert.assertTrue(is.isPresent());
        BigInteger size = remoteFileService.getCacheSizeInMB();
        Assert.assertTrue(size.compareTo(BigInteger.ZERO) == 0);
    }

    @Ignore
    @Test
    public void testClearAllCaches() {
        remoteFileService.clearAllCaches();
        Assert.assertTrue((new File(RemoteFileDiskCacheService.DATA_DIR)).exists());
    }

    @Ignore
    @Test
    public void testClearCacheByUrl() throws MalformedURLException {
        URL url = new URL("http://igbquickload.org/A_gambiae_Feb_2003/A_gambiae_Feb_2003.2bit");
        Optional<InputStream> is = remoteFileService.getFilebyUrl(url, false);
        Assert.assertTrue(remoteFileService.cacheExists(url));
        remoteFileService.clearCacheByUrl(url);
        Assert.assertFalse(remoteFileService.cacheExists(url));
    }

    @Ignore
    @Test
    public void testGetCacheSize() throws MalformedURLException {
        URL url = new URL("http://igbquickload.org/A_gambiae_Feb_2003/A_gambiae_Feb_2003.2bit");
        Optional<InputStream> is = remoteFileService.getFilebyUrl(url, false);
        BigInteger size = remoteFileService.getCacheSizeInMB();
        Assert.assertTrue(size.compareTo(BigInteger.ZERO) > 0);
        LOG.info("size: " + size.toString());
        remoteFileService.clearAllCaches();
        size = remoteFileService.getCacheSizeInMB();
        LOG.info("size: " + size.toString());
        Assert.assertTrue(size.compareTo(BigInteger.ZERO) == 0);

    }

    @Ignore
    @Test
    public void testGetCacheSizePerformance() throws MalformedURLException {
        long startTime = System.nanoTime();
        BigInteger size = remoteFileService.getCacheSizeInMB();
        long endTime = System.nanoTime();
        long duration = (endTime - startTime);
        LOG.info("time (ms): " + duration / 1000000 + ", size (MB): " + size.divide(new BigInteger("1000000")));
        //10GB ~ 40ms on SSD
    }

    @Ignore
    @Test
    public void testMD5CalculationPerformance() {
        try {
            File file1 = new File("/home/jeckstei/tmp/igb/data1.dat");
            File file2 = new File("/home/jeckstei/tmp/igb/data2.dat");
            File file3 = new File("/home/jeckstei/tmp/igb/data3.dat");
            long startTime1 = System.nanoTime();
            String md5Calculated1 = convertByteArrayToHexString(
                    DigestUtils.md5(new FileInputStream(file1)));
            long endTime1 = System.nanoTime();
            long duration1 = (endTime1 - startTime1);
            LOG.info("hash: " + md5Calculated1 + "time (ms): " + duration1 / 1000000);

            long startTime2 = System.nanoTime();
            String md5Calculated2 = convertByteArrayToHexString(
                    DigestUtils.md5(new FileInputStream(file2)));
            long endTime2 = System.nanoTime();
            long duration2 = (endTime2 - startTime2);
            LOG.info("hash: " + md5Calculated2 + "time (ms): " + duration2 / 1000000);

            long startTime3 = System.nanoTime();
            String md5Calculated3 = convertByteArrayToHexString(
                    DigestUtils.md5(new FileInputStream(file3)));
            long endTime3 = System.nanoTime();
            long duration3 = (endTime3 - startTime3);
            LOG.info("hash: " + md5Calculated3 + "time (ms): " + duration3 / 1000000);

        } catch (Exception e) {
            LOG.error("Error calculating hash: " + e.getMessage(), e);
        }

        /*
         150 MB ~ 2015-06-16 09:32:01 INFO  RemoteFileCacheServiceTest:103 - hash: 50ae04f69743921dd6082dfe978672adtime (ms): 461
         700 MB ~ 2015-06-16 09:32:03 INFO  RemoteFileCacheServiceTest:110 - hash: b1035e2bd6751bf29260791d75798350time (ms): 1860
         1.4 GB ~ 2015-06-16 09:32:06 INFO  RemoteFileCacheServiceTest:117 - hash: dbb4b453aa37a81247182b0794f49827time (ms): 3750
         */
    }

    private static String convertByteArrayToHexString(byte[] arrayBytes) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < arrayBytes.length; i++) {
            sb.append(Integer.toString((arrayBytes[i] & 0xff) + 0x100, 16).substring(1));
        }
        return sb.toString();
    }

}
