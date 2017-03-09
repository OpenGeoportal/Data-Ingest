package org.opengeoportal.dataingest.utils;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.sevenz.SevenZArchiveEntry;
import org.apache.commons.compress.archivers.sevenz.SevenZFile;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.opengeoportal.dataingest.exception.UncompressStrategyException;

/**
 * Class to manage the uncompresss of 7z files.
 *
 * @author Jose García
 */

public class UncompressStrategy7z implements UncompressStrategy {
    @Override
    public void uncompress(File file, File uncompressDir) throws UncompressStrategyException {
        SevenZFile sevenZFile = null;
        OutputStream out = null;

        try {
            sevenZFile = new SevenZFile(file);
            SevenZArchiveEntry entry = sevenZFile.getNextEntry();

            while(entry!=null){
                byte[] btoRead = new byte[1024];
                BufferedOutputStream bout =
                        new BufferedOutputStream(new FileOutputStream(new File(uncompressDir.getAbsolutePath(), entry.getName())));

                try {
                    int len = 0;

                    while ((len = sevenZFile.read(btoRead)) != -1) {
                        bout.write(btoRead, 0, len);
                    }

                    btoRead = null;
                } finally {
                    IOUtils.closeQuietly(bout);
                }

                entry = sevenZFile.getNextEntry();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            FileUtils.deleteQuietly(uncompressDir);
            throw new UncompressStrategyException(ex.getMessage());

        } finally {
            IOUtils.closeQuietly(sevenZFile);
        }
    }
}
