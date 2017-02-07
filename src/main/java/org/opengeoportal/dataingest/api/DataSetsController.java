package org.opengeoportal.dataingest.api;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.HashMap;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.opengeoportal.dataingest.api.download.LocalDownloadService;
import org.opengeoportal.dataingest.exception.FileNotReadyException;
import org.opengeoportal.dataingest.exception.PageNotFoundException;
import org.opengeoportal.dataingest.utils.DatasetsPageWrapper;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.ResultSortedPaginator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import it.geosolutions.geoserver.rest.GeoServerRESTPublisher;
import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTDataStore;
import it.geosolutions.geoserver.rest.decoder.RESTFeatureType;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;

/**
 * Creates a resource controller which handles a GET request for '/datasets' and
 * returns a DataSet resource.
 *
 * @author Joana Simoes
 * @version 1.0
 * @since 2017-01-13
 */
@SuppressWarnings("checkstyle:TodoComment")
@Controller
public class DataSetsController {

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

  /**
   * The page size for dataset results (from the application.properties).
   */
  @Value("${param.dataset.pagesize}")
  private int pageSize;

  /**
   * localDownloadService.
   */
  @Autowired
  private LocalDownloadService localDownloadService;

  /**
   * Gets the data sets from all workspaces.
   *
   * @param page
   *          the page
   * @param request
   *          the request
   * @return the data sets
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = { "/datasets",
      "/datasets/page/{page}" }, method = RequestMethod.GET)
  @ResponseBody
  public final DatasetsPageWrapper getDataSets(
      @PathVariable(value = "page", required = false) Integer page,
      HttpServletRequest request) throws Exception {

    return getPaginatedDataSets(page, request, null);
  }

  /**
   * Gets the data sets for workspace.
   *
   * @param workspace
   *          the workspace
   * @param page
   *          the page
   * @param request
   *          the request
   * @return the data sets for workspace
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = { "/workspaces/{workspace}/datasets",
      "/workspaces/{workspace}/datasets/page/{page}" }, method = RequestMethod.GET)
  @ResponseBody
  public final DatasetsPageWrapper getDataSetsForWorkspace(
      @PathVariable(value = "workspace") String workspace,
      @PathVariable(value = "page", required = false) Integer page,
      HttpServletRequest request) throws Exception {

    return getPaginatedDataSets(page, request, workspace);
  }

  /**
   * Gets the paginated data sets.
   *
   * @param page
   *          the page
   * @param request
   *          the request
   * @param workspace
   *          the workspace
   * @return the paginated data sets
   * @throws Exception
   *           the exception
   */
  private DatasetsPageWrapper getPaginatedDataSets(Integer page,
      final HttpServletRequest request, String workspace)
      throws Exception {
    try {

      boolean reloadResults = false;

      if (request.getParameter("pageSize") != null
          && StringUtils.isNumeric(request.getParameter("pageSize"))) {
        this.pageSize = Integer.parseInt(request.getParameter("pageSize"));
      }

      if (workspace == null || StringUtils.isEmpty(workspace)) {
        workspace = "*";
      }

      if (page == null) {
        page = 1;
        reloadResults = true;
      }

      if (request.getSession().getAttribute("w_" + workspace) == null) {
        reloadResults = true;
      }

      // pagination
      ResultSortedPaginator paginator = null;

      if (!reloadResults) {
        // manages with HtppSession the navigation between pages
        paginator = (ResultSortedPaginator) request.getSession()
            .getAttribute("w_" + workspace);
        paginator.setPageSize(this.pageSize);
      } else {
        // get data from geoserver
        final GeoserverDataStore gds = new GeoserverDataStore();
        HashMap<String, String> resultMap = null;

        if (workspace.equals("*")) {
          resultMap = gds.getLayerTitles(geoserverUrl);
        } else {
          resultMap = gds.getLayerTitles(geoserverUrl, workspace);
        }
        // Setup the paginator
        paginator = new ResultSortedPaginator(resultMap, this.pageSize, true);
      }

      if (page > paginator.getMaxPages()) {
        throw new PageNotFoundException();
      }

      paginator.setPage(page);

      return new DatasetsPageWrapper(paginator.getHashMapForPage(),
          paginator.getList().size(), paginator.getPage(),
          paginator.getMaxPages());

    } catch (final Exception ex) {
      ex.printStackTrace();
      throw ex;
    }
  }

  /**
   * Gives detailed information about one given dataset.
   *
   * @param workspace
   *          given workspace
   * @param dataset
   *          given dataset
   * @return String dataset info, as a set of properties.
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.GET)
  @ResponseBody
  public final HashMap<String, String> getDataSet(
      @PathVariable(value = "workspace") final String workspace,
      @PathVariable(value = "dataset") final String dataset) throws Exception {

    try {

      final GeoserverDataStore gds = new GeoserverDataStore();
      return gds.getLayerInfo(geoserverUrl, workspace, dataset);

    } catch (final Exception ex) {
      ex.printStackTrace();
      throw ex;
    }

  }

  /**
   * Deletes a given dataset. It just unpublishes the layer leaving intact the
   * datastore. The underlying dataset is not purged.
   *
   * @param workspace
   *          given workspace
   * @param dataset
   *          given dataset
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.DELETE)
  @ResponseBody
  public final void deleteDataSet(
      @PathVariable(value = "workspace") final String workspace,
      @PathVariable(value = "dataset") final String dataset) throws Exception {

    final GeoServerRESTReader reader = new GeoServerRESTReader(geoserverUrl,
        geoserverUsername, geoserverPassword);

    final GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
        geoserverUrl, geoserverUsername, geoserverPassword);

    final RESTLayer layer = reader.getLayer(workspace, dataset);
    final RESTFeatureType featureType = reader.getFeatureType(layer);
    final RESTDataStore data = reader.getDatastore(featureType);

    // Unpublish feature type
    if (!publisher.unpublishFeatureType(workspace, data.getName(), dataset)) {
      throw new Exception("Could not unpublish featuretype " + dataset
          + " on store " + data.getName());
    }
    publisher.reload();
  }

  /**
   * Uploads a given dataset.
   *
   * @param workspace
   *          given workspace
   * @param dataset
   *          given dataset
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}", method = RequestMethod.POST)
  @ResponseBody
  public final void uploadDataSet(
      @PathVariable(value = "workspace") final String workspace,
      @PathVariable(value = "dataset") final String dataset) throws Exception {

    // TODO
  }

  /**
   * Download a ZIP file with the requested dataset.
   *
   * @param workspace
   *          the needed workspace
   * @param dataset
   *          the needed dataset
   * @param response
   *          http response
   * @throws Exception
   *           the exception
   */
  @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}/download", method = RequestMethod.GET)
  @ResponseBody
  public final void download(
      @PathVariable(value = "workspace") final String workspace,
      @PathVariable(value = "dataset") final String dataset,
      final HttpServletResponse response) throws Exception {
    File file = null;
    try {
      file = localDownloadService.getFile(workspace, dataset);
    } catch (final FileNotFoundException fnfex) {
      throw fnfex;
    } catch (final FileNotReadyException fnrex) {
      response.setStatus(HttpServletResponse.SC_ACCEPTED);
      final PrintWriter out = response.getWriter();
      response.setContentType("text/html");
      out.println("File not yet ready.");
      response.flushBuffer();
      return;

    }

    // Setting up the headers:
    // The content type
    response.setContentType("application/force-download");
    // The file size
    response.addHeader("Content-Length", Long.toString(file.length()));
    // binary encoding
    response.setHeader("Content-Transfer-Encoding", "binary");
    // Filename
    response.setHeader("Content-Disposition", "attachment;filename=\""
        + FileNameUtils.getZipFileName(workspace, dataset));

    // get your file as InputStream
    final InputStream is = new FileInputStream(file);
    // copy it to response's OutputStream
    org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());

    response.flushBuffer();

  }
}
