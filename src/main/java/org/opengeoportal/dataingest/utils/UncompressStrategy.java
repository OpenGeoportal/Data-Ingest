package org.opengeoportal.dataingest.utils;

import java.io.File;

import org.opengeoportal.dataingest.exception.UncompressStrategyException;

/**
 * The Interface UncompressStrategy.
 */
public interface UncompressStrategy {

    /**
     * Uncompress.
     *
     * @param file
     *            the file
     * @param uncompressDir
     *            the uncompress dir
     * @throws UncompressStrategyException
     *             the uncompress strategy exception
     */
    void uncompress(File file, File uncompressDir)
            throws UncompressStrategyException;
}
