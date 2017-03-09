package org.opengeoportal.dataingest.utils;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.opengeoportal.dataingest.exception.UncompressStrategyException;

/**
 * Class to manage the uncompresss of tar files.
 *
 * @author Jose García
 */

public class UncompressStrategyTar implements UncompressStrategy {

    protected TarArchiveInputStream createTarArchiveInputStream(File file) throws Exception {
      return new TarArchiveInputStream(
              new BufferedInputStream(
                      new FileInputStream(file)));
    }

    @Override
    public void uncompress(File file, File uncompressDir) throws UncompressStrategyException {
        uncompressDir.mkdir();
        TarArchiveInputStream tarIn = null;
        OutputStream out = null;

        try {
            tarIn = createTarArchiveInputStream(file);

            TarArchiveEntry tarEntry = tarIn.getNextTarEntry();

            while (tarEntry != null) {
                // create a file with the same name as the tarEntry
                File destPath = new File(uncompressDir, tarEntry.getName());

                if (tarEntry.isDirectory()) {
                    destPath.mkdirs();

                } else {
                    destPath.createNewFile();

                    try {
                        out = new FileOutputStream(destPath);
                        IOUtils.copy(tarIn, out);
                    } finally {
                        IOUtils.closeQuietly(out);
                    }

                }

                tarEntry = tarIn.getNextTarEntry();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            FileUtils.deleteQuietly(uncompressDir);
            throw new UncompressStrategyException(ex.getMessage());

        } finally {
            IOUtils.closeQuietly(tarIn);
        }
    }
}
