/**
 * 
 */
package it.geosolutions.tools.file;

import it.geosolutions.tools.file.IOUtils;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;

import junit.framework.Assert;

import org.junit.Test;


/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class IOUtilsTest {
    
    @Test
    public void lockingDirectory() throws URISyntaxException, InterruptedException, IOException{
        // 
        final File dir= new File(new File(IOUtilsTest.class.getResource(".").toURI()),"test");
        Assert.assertTrue(dir.exists()?true:dir.mkdir());
        IOUtils.acquireLock(this, dir);
    }

}
