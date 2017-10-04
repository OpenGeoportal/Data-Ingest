package org.opengeoportal.dataingest.api;

import org.opengeoportal.dataingest.api.fileCache.LRUFileCache;
import org.opengeoportal.dataingest.exception.GenericCacheException;
import org.opengeoportal.dataingest.exception.GeoServerDataStoreException;
import org.opengeoportal.dataingest.exception.NoDataFoundOnGeoserverException;
import org.opengeoportal.dataingest.utils.GeoServerUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Component;

import javax.inject.Inject;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

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
     * Geoserver url.
     */
    private String geoserverUrl;

    /**
     * Logger.
     */
    private final Logger log = LoggerFactory.getLogger(CacheService.class);

    /**
     * List of typenames, to be read by the cache.
     */
    private Set<String> typenames = null;

    /**
     * A file cache, following the LRU eviction policy.
     */
    @Autowired
    private LRUFileCache fileCache;


    @SuppressWarnings({"unchecked", "rawtypes"})
    @Inject
    public CacheService(@Value("${geoserver.url}") String uri) {
        this.geoserverUrl = uri;
        try {
            this.log.info("Filling datasets list cache...");
            if (this.typenames == null) {
                try {
                    final GeoserverDataStore ds = new GeoserverDataStore(
                        this.geoserverUrl);
                    this.typenames = new HashSet(
                        Arrays.asList(ds.typenames()));
                } catch (final IOException e) {
                    e.printStackTrace();
                    throw new Exception(
                        "Could not initialize typenames list");
                }
            }
            this.log.info("Cached filled.");
        } catch (final Exception e) {
            this.log.error("ERROR in CACHESERVICE ", e);
        }
    }

    @Caching(evict = { @CacheEvict(value = "summary", key = "#typename") })
    public void clearDataSetCache(final String uri, final String typename) {
        this.log.info("Clearing entry: '" + typename
            + "' from the dataset info cache");
    }

    /**
     * Clean one entry from the getdataset info cache.
     *
     * @param uri
     *            geoserver uri
     * @param workspace
     *            workspace name
     * @param dataset
     *            dataset name
     * @param bFeatureSize
     *            boolean to indicate if we want to include the featureSize in
     *            the layer properties
     */
    @Caching(evict = {
        @CacheEvict(value = "info", key = "#workspace.concat(#dataset).concat"
            + "(#bFeatureSize)") })
    public void clearInfoCache(final String uri, final String workspace,
                               final String dataset, final boolean bFeatureSize) {
        this.log.info("Clearing entry: '" + dataset
            + "' from the dataset info cache");
    }

    /**
     * This function gets the summary info for a given typename. This is a part
     * of the mechanism for caching the AllDatasets response.
     *
     * @param uri
     *            an uri
     * @param ds
     *            a datastore
     * @param typename
     *            a typename
     * @return summary info about the dataset
     * @throws Exception
     */
    @Cacheable(cacheNames = "summary", key = "#typename")
    public Map<String, String> getDataset(final GeoserverDataStore ds,
                                          final String typename) throws Exception {
        this.log.info("Not using the summary cache for typename:" + typename);
        return ds.getDataset(typename);
    }

    public LRUFileCache getFileCache() {
        return this.fileCache;
    }

    /**
     * Gets detailed info about a layer.
     *
     * @param workspace
     *            workspace name
     * @param dataset
     *            dataset name
     * @param bFeatureSize
     *            boolean to indicate if we want to include the featureSize in
     *            the layer properties
     * @return summary info about a layer, as hash table
     * @throws Exception
     *             the exception
     */
    @Cacheable(value = "info", key = "#workspace.concat(#dataset).concat(#bFeatureSize)")
    public HashMap<String, String> getInfo(final String workspace,
                                           final String dataset, final boolean bFeatureSize) throws Exception {

        this.log.info("Not using the cache");

        GeoserverDataStore ds = null;
        try {
            ds = new GeoserverDataStore(this.geoserverUrl);
        } catch (final java.lang.Exception e) {
            throw new GeoServerDataStoreException(
                "Could not create WFS datastore " + "at: "
                    + this.geoserverUrl + ". Make sure it is up and "
                    + "running and that the connection settings are correct!");
        }
        try {
            return ds.getLayerInfo(workspace, dataset, bFeatureSize);
        } catch (final Exception e) {
            throw new NoDataFoundOnGeoserverException();
        }

    }


    public Set<String> getTypenames() throws Exception {
        return this.typenames;
    }

    /**
     * This function updates the various caches, when a dataset is removed. Its
     * meant to be called by the update and delete events.
     *
     * @param workspace
     *            a workspace
     * @param dataset
     *            a dataset
     * @throws Exception
     */
    public void updateCachesOnDelete(final String workspace,
                                     final String dataset) throws Exception {

        // Clear this entry from getdataset info
        this.clearInfoCache(this.geoserverUrl, workspace, dataset, true);
        this.clearInfoCache(this.geoserverUrl, workspace, dataset, false);

        final String typename = GeoServerUtils.getTypeName(workspace, dataset);
        // removes this from the typename array
        synchronized (this.getTypenames()) {
            this.getTypenames().remove(typename);
        }

        final String typeName = GeoServerUtils.getTypeName(workspace, dataset);
        try {
            if (this.fileCache.isCached(typeName)) {
                this.fileCache.remove(typeName);
            }
        } catch (final Exception e) {
            e.printStackTrace();
            throw new GenericCacheException(
                "A problem occurred while updating the cache.");
        }
    }


    /**
     * This function updates the various caches, when a dataset is uploaded. Its
     * meant to be called by the upload and update events.
     *
     * @param workspace
     *            a workspace
     * @param dataset
     *            a dataset
     * @throws GenericCacheException
     */
    public void updateCachesOnUpload(final String workspace,
                                     final String dataset) throws GenericCacheException {

        try {

            // With the other caches, we insert the record manually
            final String typeName = GeoServerUtils.getTypeName(workspace,
                dataset);
            synchronized (this.getTypenames()) {
                this.getTypenames().add(typeName);
            }

            final GeoserverDataStore ds = new GeoserverDataStore(
                this.geoserverUrl);
            try {
                this.getDataset(ds, typeName);
            } catch (final GeoServerDataStoreException gDSex) {
                throw new Exception();
            }

        } catch (final Exception e) {
            e.printStackTrace();
            throw new GenericCacheException(
                "A problem occurred while updating the cache.");
        }
    }
}

