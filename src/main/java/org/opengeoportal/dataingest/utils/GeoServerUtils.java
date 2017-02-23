package org.opengeoportal.dataingest.utils;

/**
 * Created by joana on 22/02/17.
 */
public class GeoServerUtils {

    public static String getTypeName(String workspace, String dataset){
        return workspace + ":" + dataset;
    }
}
