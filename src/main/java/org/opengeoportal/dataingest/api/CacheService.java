package org.opengeoportal.dataingest.api;

import org.opengeoportal.dataingest.exception.NoDataFoundOnGeoserverException;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;

/**
 * This service class acts as a proxy between the controller and
 * GeoServerDataStore class. It manages the caching of the methods, using Spring
 * cache.
 * <p>
 * * Created by joana on 08/02/17.
 */
@Component
public class CacheService {

    /**
     * Get datasets names and titles, for a given GeoServer uri.
     *
     * @param uri
     *            geoserver uri (it may include the filter for workspace).
     * @return hashmap with dataset (names, titles)
     * @throws Exception
     */
    @Cacheable(value = "titles", key = "#uri")
    public HashMap<String, String> getTitles(String uri) throws Exception {

        System.out.println("Not using the cache");

        GeoserverDataStore ds = null;
        try {
            ds = new GeoserverDataStore(uri);
        } catch (java.lang.Exception e) {
            throw new Exception("Could not create WFS datastore " + "at: " + uri
                    + ". Make sure it is up and "
                    + "running and that the connection settings are correct!");
        }

        return ds.titles();
    }

    /**
     * Gets detailed info about a layer.
     *
     * @param uri
     *            geoserver uri
     * @param workspace
     *            workspace name
     * @param dataset
     *            dataset name
     * @return summary info about a layer, as hash table
     * @throws Exception
     */
    @Cacheable(value = "info", key = "#uri.concat('-').concat(#workspace).concat(#dataset)")
    public HashMap<String, String> getInfo(String uri, String workspace,
            String dataset) throws Exception {

        System.out.println("Not using the cache");

        GeoserverDataStore ds = null;
        try {
            ds = new GeoserverDataStore(uri);
        } catch (java.lang.Exception e) {
            throw new Exception("Could not create WFS datastore " + "at: " + uri
                    + ". Make sure it is up and "
                    + "running and that the connection settings are correct!");
        }
        try {
            return ds.getLayerInfo(workspace, dataset);
        } catch (IOException e) {
           throw new NoDataFoundOnGeoserverException();
        }

    }

    /**
     * Dummy function to trigger cache eviction.
     *
     * @param uri
     *            workspace name
     * @param workspace
     *            workspace name
     * @param dataset
     *            dataset name
     */
    @Caching(evict = { @CacheEvict(value = "titles", allEntries = true),
            @CacheEvict(value = "info", key = "#uri.concat('-').concat(#workspace).concat(#dataset)") })
    public void clearCache(String uri, String workspace, String dataset) {
        System.out.println("Clearing the cache");
    }

}
