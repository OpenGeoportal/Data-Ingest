package org.opengeoportal.dataingest.utils;

import org.opengeoportal.dataingest.exception.UncompressStrategyException;

public class UncompressStrategyFactory {

    public static UncompressStrategy getUncompressStrategy(String type) throws UncompressStrategyException {
        if (type.equalsIgnoreCase("zip") || type.equalsIgnoreCase("shz") || type.equalsIgnoreCase("shp.zip")) {
            return new UncompressStrategyZip();
        } else if (type.equalsIgnoreCase("7z")) {
            return new UncompressStrategy7z();
        } else if (type.equalsIgnoreCase("tar")) {
            return new UncompressStrategyTar();
        } else if (type.equalsIgnoreCase("gz") || type.equalsIgnoreCase("tgz")) {
            return new UncompressStrategyTarGz();
        } else {
            throw new UncompressStrategyException("Format not supported");
        }
    }
}
