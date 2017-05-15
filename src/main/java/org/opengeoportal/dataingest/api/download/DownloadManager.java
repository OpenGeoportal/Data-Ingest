package org.opengeoportal.dataingest.api.download;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.opengeoportal.dataingest.api.solr.SolrClient;
import org.opengeoportal.dataingest.api.solr.SolrJClient;
import org.opengeoportal.dataingest.exception.MetadataException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.ZipUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;

/**
 * Created by joana on 12/05/17.
 */

public class DownloadManager {

    /**
     * rest template.
     */
    private final RestTemplate rest;
    /**
     * http headers.
     */
    private final HttpHeaders headers;

    /**
     * Constructor of the download client.
     */
    public DownloadManager() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
    }

    public final File getFile(final String workspace, final String dataset, String localSolrUrl, final
    String uri,
                              final String getFullFilePath)
        throws Exception {

        String strMetadataFilePath = createXmlFileFromTypeName(workspace, dataset, localSolrUrl);

        HashMap<String, String> hFileNames = new HashMap<String, String>();
        hFileNames.put(getFullFilePath, workspace + "_" + dataset + ".zip");
        hFileNames.put(strMetadataFilePath, workspace + "_" + dataset + ".xml");

        File f = ZipUtils.createZip(hFileNames, FileNameUtils.getFullPathZipFile(System.getProperty
                ("java.io.tmpdir"),
            workspace, dataset));

        //Cleanup xml file
        File fm = new File(strMetadataFilePath);
        fm.delete();

        final HttpEntity<String> requestEntity = new HttpEntity<String>("",
            headers);
        final ResponseEntity<byte[]> responseEntity = rest.exchange(uri,
            HttpMethod.GET, requestEntity, byte[].class);
        final MediaType contentType = responseEntity.getHeaders()
            .getContentType();
        if (contentType.getType().equals("text")
            && contentType.getSubtype().equals("xml")) {
            throw new java.io.IOException(
                "Resource '" + strMetadataFilePath + "' not " + "found! ");
        }

        return f;
    }


    private String createXmlFileFromTypeName(String WorkspaceName, String Name, String localSolrUrl)
        throws SolrServerException, ParserConfigurationException, IOException, SAXException,
        TransformerException, SolrServerException, MetadataException {

        SolrClient solrClient = new SolrJClient(localSolrUrl);
        QueryResponse qr = solrClient.searchForDataset("cite",
            "SDE2.MATWN_3764_B6N44_1852_P5"); // TODO: Update this to Production
        SolrDocumentList docs = qr.getResults();
        String str = docs.get(0).getFieldValue("FgdcText").toString();

        //Fix, until we find a better solution for reading the contents of the solr doc
        StringBuilder sb = new StringBuilder(str);
        sb.deleteCharAt(0);
        sb.deleteCharAt(str.length() - 2);
        str = sb.toString();

        System.out.println(str);

        // Just in case, check that the xml is correct: remove from Production?
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(str)));

        // Write the parsed document to an xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File temp = File.createTempFile("metadata", ".xml");
        StreamResult result = new StreamResult(temp);
        transformer.transform(source, result);

        return temp.getAbsolutePath();
    }
}
