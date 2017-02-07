package org.opengeoportal.dataingest.api;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.geotools.data.FeatureSource;
import org.geotools.data.ResourceInfo;
import org.geotools.data.wfs.WFSDataStore;
import org.geotools.data.wfs.WFSDataStoreFactory;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.cache.annotation.Cacheable;

/**
 * Created by joana on 27/01/17.
 */
public class GeoserverDataStore {
  /**
   * Geoserver connection timeout.
   */
  private static final int TIMEOUT = 15000;

  /**
   * Constructor of the Geoserver data store.
   */
  public GeoserverDataStore() {
  }

  /**
   * Creates a WFS data store. Do not call this function directly! Call
   * createDataStoreI, instead.
   *
   * @param uri
   *          geoserver url
   * @return WFS data store
   * @throws Exception
   *           the exception
   */
  @Cacheable(value = "datastore")
  public final WFSDataStore createDataStore(final String uri) throws Exception {

    final String getCapabilities = uri + "/wfs?REQUEST=GetCapabilities";

    final Map connectionParameters = new HashMap();
    connectionParameters.put("WFSDataStoreFactory:GET_CAPABILITIES_URL",
        getCapabilities);
    connectionParameters.put("WFSDataStoreFactory:TIMEOUT", TIMEOUT);

    try {
      final WFSDataStoreFactory dsf = new WFSDataStoreFactory();
      return dsf.createDataStore(connectionParameters);
    } catch (final java.net.ConnectException ce) {
      throw new Exception("Could not connect to GeoServer " + "at: " + uri
          + ". Make sure it is up and "
          + "running and that the connection settings are correct!");
    } catch (final Exception ex) {
      throw ex;
    }
  }

  /**
   * Lists layer names and titles, for a given data store.
   *
   * @param data
   *          data store
   * @return layer type names and titles as a hash table
   * @throws Exception
   *           the exception
   */
  public final HashMap<String, String> getTitlesForDataStore(
      final WFSDataStore data) throws Exception {

    try {
      final HashMap<String, String> hLayer = new HashMap<String, String>();
      final String[] typeNames = data.getTypeNames();

      for (final String typeName : typeNames) {
        final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = data
            .getFeatureSource(typeName);
        final ResourceInfo resourceInfo = featureSource.getInfo();
        hLayer.put(resourceInfo.getName(), resourceInfo.getTitle());
      }
      return hLayer;
    } catch (final IOException io) {
      throw new Exception("Could not read featuretype");
    } catch (final Exception ex) {
      throw ex;
    }
  }

  /**
   * Lists layer titles for all workspaces.
   *
   * @param uri
   *          geoserver url
   * @return layer type names and titles as a hash table
   * @throws Exception
   *           the exception
   */
  public final HashMap<String, String> getLayerTitles(final String uri)
      throws Exception {

    try {
      final WFSDataStore data = createDataStore(uri);
      return getTitlesForDataStore(data);
    } catch (final Exception ex) {
      throw ex;
    }
  }

  /**
   * Lists layer titles for a given workspace.
   *
   * @param uri
   *          geoserver url
   * @param workspace
   *          given workspace
   * @return layer names and titles as a hash table
   * @throws Exception
   *           the exception
   */
  public final HashMap<String, String> getLayerTitles(final String uri,
      final String workspace) throws Exception {

    try {
      final WFSDataStore data = createDataStore(uri + "/" + workspace);
      return getTitlesForDataStore(data);
    } catch (final Exception ex) {
      throw ex;
    }
  }

  /**
   * Get detailed info about one layer.
   *
   * @param uri
   *          geoserver url
   * @param workspace
   *          given workspace
   * @param dataset
   *          given dataset
   * @return hashtable with layer properties
   * @throws Exception
   *           the exception
   */
  public final HashMap<String, String> getLayerInfo(final String uri,
      final String workspace, final String dataset) throws Exception {

    final WFSDataStore data = createDataStore(uri);
    final HashMap<String, String> layerProps = new HashMap<String, String>();
    final String typeName = workspace + ":" + dataset;
    final FeatureSource<SimpleFeatureType, SimpleFeature> featureSource = data
        .getFeatureSource(typeName);
    final ResourceInfo resourceInfo = featureSource.getInfo();

    try {
      // Example properties
      layerProps.put("name", resourceInfo.getName()); // typename
      layerProps.put("title", resourceInfo.getTitle());
      layerProps.put("description", resourceInfo.getDescription());
      layerProps.put("crs", resourceInfo.getCRS().toWKT().toString());
      layerProps.put("keywords", resourceInfo.getKeywords().toString());
      return layerProps;

    } catch (final Exception e) {
      e.printStackTrace();
      throw new Exception("Could not read layer featuretype");
    }
  }

}