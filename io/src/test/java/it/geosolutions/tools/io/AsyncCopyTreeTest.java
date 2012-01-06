package it.geosolutions.tools.io;

import it.geosolutions.tools.commons.file.Path;
import it.geosolutions.tools.io.file.CopyTree;

import java.io.File;
import java.util.Collection;
import java.util.concurrent.CompletionService;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorCompletionService;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.geotools.test.TestData;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AsyncCopyTreeTest {

    File destMount;

    File testFile;

    CompletionService<File> cs;

    ExecutorService ex;

    private final static Logger LOGGER = LoggerFactory.getLogger(AsyncCopyTreeTest.class);

    @Before
    public void setUp() throws Exception {

        destMount =new File(TestData.file(this,"."), "test-data2");
        if (!destMount.exists()) {
            new File(destMount, "collector").mkdirs();
        }
        Assert.assertTrue(destMount.exists());

        testFile = TestData.file(this,
                "collector/569_RS1_20100707050441.046.SCN8.NEAR.1.00000_PRO/RS1_20100707050441.046.SCN8.NEAR.1.00000_geo8.xml");

        ex = Executors.newFixedThreadPool(2);

        if (ex == null || ex.isTerminated()) {
            throw new IllegalArgumentException(
                    "Unable to run asynchronously using a terminated or null ThreadPoolExecutor");
        }

        cs = new ExecutorCompletionService<File>(ex);
    }

    @After
    public void tearDown() throws Exception {
        if (ex != null)
            ex.shutdown();
        
        FileUtils.deleteDirectory(destMount);

    }

    @Test
    public void asyncCopyTreeTest() throws Exception {
        LOGGER.info("START: asyncCopyTreeTest");
        File srcMount = TestData.file(this, ".");
        CopyTree act = new CopyTree(FileFilterUtils.or(FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);
        Collection<Future<File>> list = act.copy();
        int workSize = list.size();
        try {
            while (workSize-- > 0) {
                Future<File> future = cs.take();
                try {
                    LOGGER.info("copied file: " + future.get());
                } catch (ExecutionException e) {
                    LOGGER.info(e.getLocalizedMessage(),e);
                    Assert.fail();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.info(e.getLocalizedMessage(),e);
            Assert.fail();
        }
        LOGGER.info("STOP: asyncCopyTreeTest");
    }

    @Test
    public void asyncCopyTreeStopTest() throws Exception{
        LOGGER.info("START: asyncCopyTreeStopTest");
        File srcMount = TestData.file(this, ".");
        CopyTree act = new CopyTree(
                FileFilterUtils.or(FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.fileFileFilter()), 
                cs, 
                srcMount, 
                destMount);
        Collection<Future<File>> list = act.copy();
        int workSize = list.size();
        try {
            while (workSize-- > 0) {
                if (workSize == 2) {
                    act.setCancelled(true);
                }
                Future<File> future = cs.take();
                try {
                    if (workSize <= 2)
                        LOGGER.info("[STOPPED] copied file: " + future.get());
                    else
                        LOGGER.info("copied file: " + future.get());
                } catch (ExecutionException e) {

                    LOGGER.info(e.getLocalizedMessage(),e);
                    Assert.fail();
                }
            }
        } catch (InterruptedException e) {
            LOGGER.info(e.getLocalizedMessage(),e);

        }
        LOGGER.info("STOP: asyncCopyTreeStopTest");
    }

    @Test
    public void asyncStopCopyTest() throws Exception{
        LOGGER.info("BEGIN: asyncStopCopyTest");
        File srcMount = TestData.file(this, ".");
        final CopyTree act = new CopyTree(FileFilterUtils.or(FileFilterUtils.directoryFileFilter(),
                FileFilterUtils.fileFileFilter()), cs, srcMount, destMount);

        final Thread copier = new Thread(new Runnable() {
            public void run() {
                act.setCancelled(true);
                Assert.assertNull("Returned list should be null", act.copy());
            }
        });

        copier.start();

        try {
            copier.join();
        } catch (InterruptedException e) {
            LOGGER.info(e.getLocalizedMessage(),e);
            Assert.fail();
        }

        LOGGER.info("STOP: asyncStopCopyTest");
    }

    @Test
    public void rebaseTest() throws Exception {
        LOGGER.info("BEGIN: rebaseTest");
        // rebase a file
        LOGGER.info("Before: " + testFile);
        File srcMount = TestData.file(this, ".");
        File rebasedFile = Path.rebaseFile(srcMount, destMount, testFile);
        rebasedFile.getParentFile().mkdirs();
        Assert.assertTrue(testFile.renameTo(rebasedFile));
        LOGGER.info("After: " + rebasedFile);        

        // TEST: File is not in the mount point dir
        Assert.assertFalse(testFile.exists());
        
        // move it back
        LOGGER.info("Before: " + rebasedFile);
        testFile = Path.rebaseFile(destMount, srcMount, rebasedFile);
        testFile.getParentFile().mkdirs();
        Assert.assertTrue(rebasedFile.renameTo(testFile));
        LOGGER.info("After: " + testFile.getAbsolutePath());

        // TEST: File is not in the mount point dir
        Assert.assertFalse(rebasedFile.exists());

        LOGGER.info("END: rebaseTest");
    }
    /*
     * @Test
     * 
     * @Ignore public void testFileFilter() { LOGGER.info("START: testFileFilter"); XStream xstream
     * = new XStream(new DomDriver()); FileFilter filter = FileFilterUtils.or(
     * FileFilterUtils.directoryFileFilter(), FileFilterUtils.fileFileFilter()); FileFilter filter2
     * = FileFilterUtils.and(FileFilterUtils .asFileFilter(filter), FileFilterUtils
     * .notFileFilter(FileFilterUtils.nameFileFilter("*.lck", IOCase.INSENSITIVE)));
     * LOGGER.info("FILTER: " + xstream.toXML(filter2)); FileInputStream fis = null; try { fis = new
     * FileInputStream(new File( "src/test/resources/FileFilter.xml")); Object obj =
     * xstream.fromXML(fis); LOGGER.info("FILTER OBJ: " + obj.toString()); } catch
     * (FileNotFoundException e) { e.printStackTrace(); Assert.fail(); } catch (XStreamException e)
     * { e.printStackTrace(); Assert.fail(); } catch (Exception e) { e.printStackTrace();
     * Assert.fail(); } finally { IOUtils.closeQuietly(fis); } LOGGER.info("STOP: testFileFilter");
     * }
     */
}
