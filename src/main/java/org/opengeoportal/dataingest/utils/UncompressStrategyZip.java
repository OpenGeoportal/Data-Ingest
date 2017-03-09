package org.opengeoportal.dataingest.utils;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.ArchiveStreamFactory;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.utils.IOUtils;
import org.apache.commons.io.FileUtils;
import org.opengeoportal.dataingest.exception.UncompressStrategyException;

/**
 * Class to manage the uncompresss of zip files.
 *
 * @author Jose García
 */
public class UncompressStrategyZip implements UncompressStrategy {
    @Override
    public void uncompress(File file, File uncompressDir) throws UncompressStrategyException {
        InputStream is = null;
        OutputStream out = null;
        ArchiveInputStream in = null;

        try {
            is = new FileInputStream(file);
            in = new ArchiveStreamFactory().createArchiveInputStream(ArchiveStreamFactory.ZIP, is);
            ZipArchiveEntry entry = (ZipArchiveEntry) in.getNextEntry();

            while (entry!=null) {
                try {

                    String name = entry.getName();
                    name = name.replace('\\', '/');
                    File destinationFile = new File(uncompressDir, name);
                    if (name.endsWith("/")) {
                        if (!destinationFile.isDirectory() && !destinationFile.mkdirs()) {
                        }
                        entry = (ZipArchiveEntry) in.getNextEntry();
                        continue;
                    } else if (name.indexOf('/') != -1) {
                        // Create the the parent directory if it doesn't exist
                        File parentFolder = destinationFile.getParentFile();
                        if (!parentFolder.isDirectory()) {
                            if (!parentFolder.mkdirs()) {

                            }
                        }
                    }

                    out = new FileOutputStream(new File(uncompressDir.getAbsolutePath(), entry.getName()));
                    IOUtils.copy(in, out);
                } finally {
                    IOUtils.closeQuietly(out);
                }

                entry = (ZipArchiveEntry) in.getNextEntry();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            FileUtils.deleteQuietly(uncompressDir);
            throw new UncompressStrategyException(ex.getMessage());

        } finally {
            IOUtils.closeQuietly(out);
            IOUtils.closeQuietly(is);
            IOUtils.closeQuietly(in);

        }
    }
}
