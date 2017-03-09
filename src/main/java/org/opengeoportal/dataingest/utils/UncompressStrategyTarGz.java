package org.opengeoportal.dataingest.utils;

import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;

import java.io.*;

/**
 * Class to manage the uncompresss of tar gzip files.
 *
 * @author Jose García
 */

public class UncompressStrategyTarGz extends  UncompressStrategyTar implements UncompressStrategy {

    @Override
    protected TarArchiveInputStream createTarArchiveInputStream(File file) throws Exception {
        return new TarArchiveInputStream(
                new GzipCompressorInputStream(
                        new BufferedInputStream(
                                new FileInputStream(file))));
    }
}
