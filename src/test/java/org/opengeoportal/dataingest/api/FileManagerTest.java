package org.opengeoportal.dataingest.api;

import org.junit.Test;
import org.opengeoportal.dataingest.api.fileCache.FileManager;
import org.opengeoportal.dataingest.exception.FileLockedException;

import static org.junit.Assert.assertTrue;

public class FileManagerTest {

    private long maxAllowableLockTime = 3600;

    @Test
    public void getFileLifeCycle() throws Exception {

        FileManager fm1 = new FileManager(System.getProperty("java.io.tmpdir") + "/file1", true, maxAllowableLockTime);
        FileManager fm2 = new FileManager(System.getProperty("java.io.tmpdir") + "/file2", true, maxAllowableLockTime);

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
