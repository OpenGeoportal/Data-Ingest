package org.opengeoportal.dataingest.api.download;

import java.io.File;
import java.io.FileOutputStream;

import org.apache.commons.io.IOUtils;
import org.opengeoportal.dataingest.api.fileCache.FileManager;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

/**
 * Created by joana on 16/01/17.
 */
public class WFSClient {
    /**
     * rest template.
     */
    private final RestTemplate rest;
    /**
     * http headers.
     */
    private final HttpHeaders headers;

    /**
     * Constructor of the wfs client.
     */
    public WFSClient() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
    }

    /**
     * Get a file from a WFS request.
     *
     * @param uri                  address of the service
     * @param getFullFilePath      name with path of the file
     * @param maxAllowableLockTime Max allowable lock time in seconds 1 hour is recommended for bigger
     * @return file with the dataset
     * @throws Exception the exception
     */
    public final File getFile(final String uri, final String getFullFilePath, final long maxAllowableLockTime)
        throws Exception {

        final HttpEntity<String> requestEntity = new HttpEntity<String>("",
            headers);
        final ResponseEntity<byte[]> responseEntity = rest.exchange(uri,
            HttpMethod.GET, requestEntity, byte[].class);
        final MediaType contentType = responseEntity.getHeaders()
            .getContentType();
        if (contentType.getType().equals("text")
            && contentType.getSubtype().equals("xml")) {
            throw new java.io.IOException(
                "Resource '" + getFullFilePath + "' not " + "found! ");
        }
        final FileManager out = new FileManager(getFullFilePath, true, maxAllowableLockTime);
        try {
            out.lock();
            final FileOutputStream fos = new FileOutputStream(out.getFile());
            IOUtils.write(responseEntity.getBody(), fos);
        } finally {
            out.unlock();
        }
        return out.getFile();
    }

    /**
     * Get a file sizefrom a WFS request.
     *
     * @param geoserverUrl the geoserver url.
     * @param workspace    a workspace.
     * @param dataset      a dataset.
     * @return file size in bytes
     * @throws Exception
     */
    public final long getFileSize(final String geoserverUrl, final String workspace, final String dataset) throws
        Exception {

        final String uri = geoserverUrl + workspace
            + "/ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
            + workspace + ":" + dataset + "&outputFormat=SHAPE-ZIP";

        final HttpEntity<String> requestEntity = new HttpEntity<String>("",
            headers);
        final ResponseEntity<byte[]> responseEntity = rest.exchange(uri,
            HttpMethod.HEAD, requestEntity, byte[].class);

        return responseEntity.getHeaders().getContentLength();

    }

    /**
     * Get feature type for a given workspace.
     *
     * @param uri       geoserver url
     * @param workspace given workspace
     * @return feature type as string
     * @throws Exception the exception
     */
    public final String getFeatureType(final String uri, final String workspace)
        throws Exception {

        final HttpEntity<String> requestEntity = new HttpEntity<String>("",
            headers);

        final ResponseEntity<String> responseEntity = rest.exchange(
            uri + "/rest/workspaces/" + workspace + "/featuretypes.xml",
            HttpMethod.GET, requestEntity, String.class);

        return responseEntity.getBody();

    }
}
