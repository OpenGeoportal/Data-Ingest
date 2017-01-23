package org.opengeoportal;

import it.geosolutions.geoserver.rest.GeoServerRESTReader;
import it.geosolutions.geoserver.rest.decoder.RESTLayer;
import it.geosolutions.geoserver.rest.decoder.RESTLayerList;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestClientException;

import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;

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
     * Delivers a list of DataSets.
     *
     * @return dataset in a RESTLayerList.
     */
    @RequestMapping(value = "/datasets", method = RequestMethod.GET)
    @ResponseBody
    public final RESTLayerList getDataSets() {
        // TODO: Listed.
        // 1 - Improve the response with information
        // from GetCapabilities request.
        // 2 - check the availability of geoserver, and throw an
        // appropriated exception if its not available
        try {

            GeoServerRESTReader geoServerRESTReader
                = new GeoServerRESTReader(geoserverUrl,
                geoserverUsername, geoserverPassword);

            if (!geoServerRESTReader.existGeoserver()) {
                throw new RuntimeException("Could not connect to GeoServer " +
                    "at: " + geoserverUrl + ". Make sure it is up and " +
                    "running and that the connection settings are correct!");
            }

            return geoServerRESTReader.getLayers();

        } catch (MalformedURLException e) {
            throw new RuntimeException("Malformed URL ");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw ex;
        }
    }

    /**
     * Gives detailed information about one given dataset.
     *
     * @param workspace the needed workspace
     * @param dataset   the needed dataset
     * @return dataset in a RESTLayerList.
     */
    @RequestMapping(value = "/workspaces/{workspace}/datasets/{dataset}",
        method = RequestMethod.GET)
    @ResponseBody
    public final RESTLayer getDataSetsFromWorkspace(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset) {

        //TODO implement this
        return null;
    }

    /**
     * Download a ZIP file with the requested dataset.
     *
     * @param workspace the needed workspace
     * @param dataset   the needed dataset
     * @param response  http response
     */
    @RequestMapping(value =
        "/workspaces/{workspace}/datasets/{dataset}/download",
        method = RequestMethod.GET)
    @ResponseBody
    public final void download(
        @PathVariable(value = "workspace") final String workspace,
        @PathVariable(value = "dataset") final String dataset,
        final HttpServletResponse response) {

        //TODO: verify why the wrong dataset name still triggers a
        // (empty) download
        String uri = geoserverUrl
            + "/" + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
            + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";

        try {

            WFSClient client = new WFSClient();

            String fileName = workspace + "_" + dataset + ".zip";
            File file = client.getFile(uri, fileName);

            response.setContentType("application/force-download");
            response.addHeader("Content-Length", Long.toString(file.length()));
            response.setHeader("Content-Transfer-Encoding", "binary");
            response.setHeader("Content-Disposition", "attachment; filename=\""
                + fileName);

            // get your file as InputStream
            InputStream is = new FileInputStream(file);
            // copy it to response's OutputStream
            org.apache.commons.io.IOUtils.copy(is, response.getOutputStream());

            response.flushBuffer();
        } catch (RestClientException e1) {
            throw new RuntimeException("Resource not found! ");
        } catch (Exception ex) {
            ex.printStackTrace();
            throw new RuntimeException();
        }

    }


}
