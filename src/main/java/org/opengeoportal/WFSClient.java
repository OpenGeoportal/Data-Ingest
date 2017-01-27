package org.opengeoportal;

import org.apache.commons.io.IOUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.io.FileOutputStream;

/**
 * Created by joana on 16/01/17.
 */
public class WFSClient {
    /**
     * rest template.
     */
    private RestTemplate rest;
    /**
     * http headers.
     */
    private HttpHeaders headers;
    /**
     * http status.
     */
    private HttpStatus status;

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
     * @param uri      address of the service
     * @param fileName name of the created file
     * @return file with the dataset
     * @throws Exception
     */
    public final File getFile(final String uri,
                              final String fileName) throws Exception {

        HttpEntity<String> requestEntity = new HttpEntity<String>("",
            headers);
        ResponseEntity<byte[]> responseEntity = rest.exchange(uri,
            HttpMethod.GET, requestEntity, byte[].class);
        MediaType contentType
            = responseEntity.getHeaders().getContentType();
        if (contentType.getType().equals("text") && contentType.getSubtype()
            .equals("xml")) {
            throw new java.io.IOException("Resource '" + fileName + "' not "
                + "found! ");
        }
        this.status = responseEntity.getStatusCode();
        File out = new File(
            System.getProperty("java.io.tmpdir") + "/" + fileName);
        FileOutputStream fos = new FileOutputStream(out);
        IOUtils.write(responseEntity.getBody(), fos);
        return out;
    }

    /**
     * Get feature type for a given workspace.
     * @param uri geoserver url
     * @param workspace given workspace
     * @return feature type as string
     * @throws Exception
     */
    public final String getFeatureType(final String uri, final String workspace)
        throws
        Exception {

        HttpEntity<String> requestEntity = new HttpEntity<String>("",
            headers);

        ResponseEntity<String> responseEntity = rest.exchange(uri
                + "/rest/workspaces/" + workspace + "/featuretypes.xml",
            HttpMethod.GET, requestEntity, String.class);

        return responseEntity.getBody();

    }
}




