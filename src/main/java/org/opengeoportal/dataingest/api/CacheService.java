package org.opengeoportal.dataingest.api;

import org.opengeoportal.dataingest.exception.GeoServerDataStoreException;
import org.opengeoportal.dataingest.exception.NoDataFoundOnGeoserverException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * Spring boot logger.
     */
    private final Logger log = LoggerFactory.getLogger(this.getClass());
    /**
     * The GeoServer URL (from the application.properties).
     */
    @Value("${geoserver.url}")
    private String geoserverUrl;

    /**
     * Fills cache on startup.
     *
     * @throws Exception
     */
    //  @PostConstruct
    public void fillCache() throws Exception {
        try {
            log.info("Filling datasets list cache...");
            getDatasets(geoserverUrl);
            log.info("Cached filled.");
        } catch (Exception e) {
            throw new Exception("Could not initialize datasets list cache!");
        }
    }

    /**
     * Get datasets typenames, workspaces, names and titles, for a given GeoServer uri.
     * This cache caches the entire response as a single record. It does not have the ability
     * to remove individual entries from the cache.
     * This function is meant to be used with the getDataSetsForWorkspace controller.
     *
     * @param uri geoserver uri (it may include the filter for workspace).
     * @return hashmap with dataset (names, titles)
     * @throws Exception the exception
     * @see "getTypeNames"
     */
    @Cacheable(value = "titles", key = "#uri")
    public List<Map<String, String>> getDatasets(final String uri)
        throws Exception {

        log.info("Not using the cache");

        GeoserverDataStore ds = null;
        List<Map<String, String>> hDatasets = new ArrayList<Map<String, String>>();

        try {
            ds = new GeoserverDataStore(uri);

            String[] typenames = ds.typenames();
            for (final String typeName : typenames) {
                hDatasets.add(getDataset(uri, ds, typeName));
            }

        } catch (final java.lang.Exception e) {
            throw new Exception("Could not create WFS datastore " + "at: " + uri
                + ". Make sure it is up and "
                + "running and that the connection settings are correct!");
        }


        return hDatasets;
    }

    /**
     * This function gets the summary info for a given typename.
     * This is a part of the mechanism for caching the AllDatasets response.
     *
     * @param uri      an uri
     * @param ds       a datastore
     * @param typename a typename
     * @return summary info about the dataset
     * @throws Exception
     */
    @Cacheable(cacheNames = "summary", key = "#typename")
    public Map<String, String> getDataset(final String uri, GeoserverDataStore ds, final String typename)
        throws Exception {
        log.info("Not using the summary cache for typename:" + typename);
        return ds.getDataset(typename);
    }

    /**
     * Gets detailed info about a layer.
     *
     * @param uri          geoserver uri
     * @param workspace    workspace name
     * @param dataset      dataset name
     * @param bFeatureSize boolean to indicate if we want to include the featureSize in the layer properties
     * @return summary info about a layer, as hash table
     * @throws Exception the exception
     */
    @Cacheable(value = "info", key = "#uri.concat('-').concat(#workspace).concat(#dataset).concat(#bFeatureSize)")
    public HashMap<String, String> getInfo(final String uri,
                                           final String workspace, final String dataset, boolean bFeatureSize) throws
        Exception {

        log.info("Not using the cache");

        GeoserverDataStore ds = null;
        try {
            ds = new GeoserverDataStore(uri);
        } catch (final java.lang.Exception e) {
            throw new GeoServerDataStoreException("Could not create WFS datastore " + "at: " + uri
                + ". Make sure it is up and "
                + "running and that the connection settings are correct!");
        }
        try {
            return ds.getLayerInfo(workspace, dataset, bFeatureSize);
        } catch (final Exception e) {
            throw new NoDataFoundOnGeoserverException();
        }

    }

    /**
     * Clear one typename entry from the cache. This applies to the summary cache.
     *
     * @param typename a given type name
     */
    @Caching(evict = {@CacheEvict(value = "summary", key = "#typename")})
    public void clearCacheOne(final String typename) {
        log.info("Clearing the entry: '" + typename + "' from the datasets cache");
    }

    /**
     * Clean one entry from the getdataset info cache.
     *
     * @param uri          geoserver uri
     * @param workspace    workspace name
     * @param dataset      dataset name
     * @param bFeatureSize boolean to indicate if we want to include the featureSize in the layer properties
     */
    @Caching(evict = {@CacheEvict(value = "info", key = "#uri.concat('-').concat(#workspace).concat(#dataset).concat" +
        "(#bFeatureSize)")})
    public void clearInfoCache(final String uri, final String workspace, final String dataset, boolean bFeatureSize) {
        log.info("Clearing entry: '" + dataset + "' from the dataset info cache");
    }

    /**
     * Clear the cached response of the entire dataset list.
     */
    @Caching(evict = {@CacheEvict(value = "titles", allEntries = true)})
    public void clearCacheAll() {
        log.info("Clearing the dataset list cache");
    }

    /**
     * Dummy function to trigger cache eviction.
     *
     * @param uri       workspace name
     * @param workspace workspace name
     * @param dataset   dataset name
     */
    /*
    @Caching(evict = {@CacheEvict(value = "titles", allEntries = true),
        @CacheEvict(value = "info", key = "#uri.concat('-').concat(#workspace).concat(#dataset)")})
    public void clearCacheAll(final String uri, final String workspace,
                              final String dataset) {
        log.info("Clearing the cache");
    }*/

}
