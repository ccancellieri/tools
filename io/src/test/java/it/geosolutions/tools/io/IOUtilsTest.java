/**
 * 
 */
package it.geosolutions.tools.io;

import it.geosolutions.tools.io.file.IOUtils;

import java.io.File;

import junit.framework.Assert;

import org.geotools.test.TestData;
import org.junit.Test;


/**
 * @author Carlo Cancellieri - carlo.cancellieri@geo-solutions.it
 *
 */
public class IOUtilsTest extends Assert{
    
    @Test
    public void lockingDirectory() throws Exception{
        // 
        final File dir= new File(TestData.file(this, "."),"test");
        Assert.assertTrue(dir.exists()?true:dir.mkdir());
        assertTrue(IOUtils.acquireLock(this, dir));
    }

}
