package org.opengeoportal.dataingest.api.download;

import org.opengeoportal.dataingest.api.GeoserverDataStore;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileNotFoundException;

/**
 * The Class LocalDownloadService.
 */
@Component
public class LocalDownloadService {

  /**
   * The GeoServer URL (from the application.properties).
   */
  @Value("${geoserver.url}")
  private String geoserverUrl;

  /**
   * The GeoServer Username (from the application.properties).
   */
  @Value("${geoserver.username}")
  private String geoserverUsername;

  /**
   * The GeoServer Password (from the application.properties).
   */
  @Value("${geoserver.password}")
  private String geoserverPassword;

  /** The context. */
  @Autowired
  private ApplicationContext context;

  /**
   * Check if the file is already on the filesystem.
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   * @return true if the file is already on the filesystem (until it's better
   *         defined the caching strategy)
   */
  private boolean isFilePresent(final String workspace, final String dataset) {

    final File f = new File(
        FileNameUtils.getFullPathZipFile(workspace, dataset));
    if (f.exists() && !f.isDirectory()) {
      return true;
    }

    return false;
  }

  /**
   * Check if data exists.
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   * @return true
   */
  private boolean isAValidRequest(final String workspace,
      final String dataset) {

    /*
     * Find a better way than asking directly for the file (some method in
     * org.geotools.data.store.ContentDataStore?)
     */

    try {

      final GeoserverDataStore gds = new GeoserverDataStore(geoserverUrl);
      return (gds.getLayerInfo(geoserverUrl, workspace, dataset) != null);

    } catch (final Exception ex) {
      ex.printStackTrace();
      return false;
    }
  }

  /**
   * Gets the file from remote.
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   */
  private void getFileFromRemote(final String workspace, final String dataset) {
    final JmsTemplate jmsTemplate = context.getBean(JmsTemplate.class);
    jmsTemplate.convertAndSend("fileRequestsQueue",
        new DownloadRequest(workspace, dataset));
  }

  /**
   * This is the method that we use for getting the file. Consider to cache this
   * method.
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   * @return The requested file
   * @throws FileNotFoundException
   *           File not found on OGS
   * @throws FileNotReadyException
   *           File not ready locally
   */
  public final File getFile(final String workspace, final String dataset)
      throws FileNotFoundException, FileNotReadyException {

    if (!this.isAValidRequest(workspace, dataset)) {
      throw new FileNotFoundException();
    }

    if (this.isFilePresent(workspace, dataset)) {
      return new File(FileNameUtils.getFullPathZipFile(workspace, dataset));
    } else {
      getFileFromRemote(workspace, dataset);
      throw new FileNotReadyException();
    }
  }

  /**
   * This is the method force the download of the file, ignoring the cache .
   *
   * @param workspace
   *          the workspace
   * @param dataset
   *          the dataset
   * @throws FileNotFoundException
   *           File not found on OGS
   */
  public final void forceGetFile(final String workspace, final String dataset)
      throws FileNotFoundException {
    this.getFileFromRemote(workspace, dataset);
  }

}
