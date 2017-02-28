package org.opengeoportal.dataingest.api;

import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opengeoportal.dataingest.api.fileCache.FileManager;
import org.opengeoportal.dataingest.exception.FileLockedException;

public class FileManagerTest {
    
       
    @Test
    public void getFileLifeCycle() throws Exception {

        FileManager fm1 = new FileManager(System.getProperty("java.io.tmpdir")+"/file1", true);
        FileManager fm2 = new FileManager(System.getProperty("java.io.tmpdir")+"/file2", true);
        
        fm1.lock();
        fm2.lock();    
        
        assertTrue(fm2.isLocked());
        assertTrue(fm2.isLocked());
        
        fm1.unlock();
        
        assertTrue(!fm1.isLocked());
        assertTrue(fm2.isLocked());
        
        fm2.unlock();
        
        assertTrue(!fm1.isLocked());
        assertTrue(!fm2.isLocked());
        
        fm1.lock();
        
        try {
            fm1.removeFile();
            // not ok
            assertTrue(false);
        } catch (FileLockedException fl) {
            // ok
        }
        
        fm1.unlock();
        
        fm1.removeFile();
        fm2.removeFile();
        
        
        
    }

}
