package org.opengeoportal;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.servlet.http.HttpServletResponse;

import org.opengeoportal.dataingest.download.LocalDownloadService;
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
 * Creates a resource controller
 * which handles a GET request
 * for '/datasets' and returns a
 * DataSet resource.
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
     * Lists data set names for all workspaces.
     *
     * @return dataset in a RESTLayerList.
     */
    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    @ResponseBody
    public final HashMap<String, String> getDataSets() throws Exception {
        try {

            GeoserverDataStore gds = new GeoserverDataStore();
            return gds.getLayerTitles(geoserverUrl);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Lists data sets names for a given workspace.
     *
     * @param workspace the required datavset
     * @return String datasets list of datasets.
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets",
        method = RequestMethod.GET)
    @ResponseBody
    public final HashMap<String, String> getDataSetsForWorkspace(
        @PathVariable(value = "workspace") final String workspace)
        throws Exception {

        try {

            GeoserverDataStore gds = new GeoserverDataStore();
            return gds.getLayerTitles(geoserverUrl, workspace);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Gives detailed information about one given dataset.
     *
     * @param workspace given workspace
     * @param dataset   given dataset
     * @return String dataset info, as a set of properties.
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}",
        method = RequestMethod.GET)
    @ResponseBody
    public final HashMap<String, String> getDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset) throws
        Exception {

        try {

            GeoserverDataStore gds = new GeoserverDataStore();
            return gds.getLayerInfo(geoserverUrl, workspace, dataset);

        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    /**
     * Download a ZIP file with the requested dataset.
     *
     * @param workspace the needed workspace
     * @param dataset   the needed dataset
     * @param response  http response
     * @throws Exception
     */
    @RequestMapping(value =
        "/workspaces/{workspace}/datasets/{dataset}/download",
        method = RequestMethod.GET)
    @ResponseBody
    public final void download(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset,
        final HttpServletResponse response) throws Exception {

        String uri = geoserverUrl
            + "/" + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
            + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";

        try {

            WFSClient client = new WFSClient();

            String fileName = workspace + "_" + dataset + ".zip";
            File file = client.getFile(uri, fileName);

            response.setContentType("application/force-download");
            response.addHeader("Content-Length",
                Long.toString(file.length()));
            response.setHeader("Content-Transfer-Encoding",
                "binary");
            response.setHeader("Content-Disposition", "attachment;"
                +
                " filename=\""
                + fileName);

            // get your file as InputStream
            InputStream is = new FileInputStream(file);
            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());

            response.flushBuffer();
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }

    }

    /**
     * Deletes a given dataset. It first unpublishes the layer and then removes
     * the datastore. The underlying dataset is not purged.
     * @param workspace given workspace
     * @param dataset given dataset
     * @throws Exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}",
        method = RequestMethod.DELETE)
    @ResponseBody
    public final void deleteDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset)
        throws Exception {

        GeoServerRESTReader reader = new GeoServerRESTReader(geoserverUrl,
            geoserverUsername, geoserverPassword);

        GeoServerRESTPublisher publisher = new GeoServerRESTPublisher(
            geoserverUrl, geoserverUsername, geoserverPassword);

        RESTLayer layer = reader.getLayer(workspace, dataset);
        RESTFeatureType featureType = reader.getFeatureType(layer);
        RESTDataStore data = reader.getDatastore(featureType);

        //Unpublish feature type
        if (!publisher.unpublishFeatureType(workspace, data.getName(),
            dataset)) {
            throw new Exception("Could not unpublish featuretype " + dataset
                + " on store " + data.getName());
        }
        //Remove data store
        if (!publisher.removeDatastore(workspace,
            data.getName(), true)) {
            throw new Exception("Could not remove store " + data.getName());
        }
        publisher.reload();
    }

    /**
     * Uploads a given dataset.
     * @param workspace given workspace
     * @param dataset given dataset
     * @throws Exception
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}",
        method = RequestMethod.POST)
    @ResponseBody
    public final void uploadDataSet(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset)
        throws Exception {

        //TODO
    }
    
    /**
     * Only for test purpose
     *
     * @param workspace the needed workspace
     * @param dataset   the needed dataset
     * @param response  http response
     * @throws Exception
     */
    @RequestMapping(value =
        "/workspaces/{workspace}/datasets/{dataset}/download/test",
        method = RequestMethod.GET)
    @ResponseBody
    public final void testDownload(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset,
        final HttpServletResponse response) throws Exception {
        System.out.println("Inizio.");
        LocalDownloadService localDownloadService = new LocalDownloadService();
        localDownloadService.getFile(workspace, dataset);
        System.out.println("Fine.");
    }

}