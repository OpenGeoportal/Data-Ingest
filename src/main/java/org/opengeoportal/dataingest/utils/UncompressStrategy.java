package org.opengeoportal.dataingest.utils;

import java.io.File;

import org.opengeoportal.dataingest.exception.UncompressStrategyException;

public interface UncompressStrategy {
    public void uncompress(File file, File uncompressDir) throws UncompressStrategyException;
}
