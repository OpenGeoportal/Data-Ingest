package org.opengeoportal.dataingest.api.download;

import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocumentList;
import org.opengeoportal.dataingest.api.solr.SolrClient;
import org.opengeoportal.dataingest.api.solr.SolrJClient;
import org.opengeoportal.dataingest.exception.MetadataException;
import org.opengeoportal.dataingest.exception.NoMetadataException;
import org.opengeoportal.dataingest.utils.FileNameUtils;
import org.opengeoportal.dataingest.utils.ShapefilePackage;
import org.opengeoportal.dataingest.utils.ZipUtils;
import org.springframework.http.HttpHeaders;
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
import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.UUID;

/**
 * Created by joana on 12/05/17.
 */

public class DownloadWrapper {

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
    public DownloadWrapper() {
        this.rest = new RestTemplate();
        this.headers = new HttpHeaders();
    }

    public final File getFile(final String workspace, final String dataset, String localSolrUrl, final
    String uri, final String getFullFilePath)
        throws Exception {

        File f = null;

        try {
            String strMetadataFilePath = createXmlFileFromTypeName(workspace, dataset, localSolrUrl);

            ShapefilePackage shapefilePackage = new ShapefilePackage(new File(getFullFilePath));

            HashMap<String, String> hFileNames = new HashMap<String, String>();

            File[] directoryListing = shapefilePackage.getUnzipDir().listFiles();
            if (directoryListing != null) {
                for (File child : directoryListing) {
                    hFileNames.put(child.getName(), child.getPath());
                }
            }

            hFileNames.put(dataset + ".shp.xml", strMetadataFilePath);

            // Create temp dir
            String tempDir = System.getProperty
                ("java.io.tmpdir") + "/" + ".dataingest-zip-" + UUID.randomUUID();
            Boolean success = (new File(tempDir)).mkdirs();
            if (!success) throw new Exception("Could not create temporary directory for zip file, on: " + tempDir);

            f = ZipUtils.createZip(hFileNames, FileNameUtils.getFullPathZipFile(tempDir, dataset));

            shapefilePackage.dispose();
            //Cleanup xml file
            File fm = new File(strMetadataFilePath);
            fm.delete();

        } catch (NoMetadataException ex){
            return new File(getFullFilePath);
        }

        return f;
    }


    private String createXmlFileFromTypeName(String WorkspaceName, String Name, String localSolrUrl)
        throws SolrServerException, ParserConfigurationException, IOException, SAXException,
        TransformerException, SolrServerException, MetadataException, NoMetadataException {

        SolrClient solrClient = new SolrJClient(localSolrUrl);
        QueryResponse qr = solrClient.searchForDataset(WorkspaceName, Name);
        //QueryResponse qr = solrClient.searchForDataset("cite", "SDE2.MATWN_3764_B6N44_1852_P5");
        SolrDocumentList docs = qr.getResults();

        if (docs.getNumFound() == 0)
            throw new NoMetadataException("Did not find any records for " + WorkspaceName + ":" + Name);

        String str = docs.get(0).getFieldValue("FgdcText").toString();

        //Fix, until we find a better solution for reading the contents of the solr doc
        StringBuilder sb = new StringBuilder(str);
        sb.deleteCharAt(0);
        sb.deleteCharAt(str.length() - 2);
        str = sb.toString();

        //System.out.println(str);

        // Just in case, check that the xml is correct: remove from Production?
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(new InputSource(new StringReader(str)));

        // Write the parsed document to an xml file
        TransformerFactory transformerFactory = TransformerFactory.newInstance();
        Transformer transformer = transformerFactory.newTransformer();
        DOMSource source = new DOMSource(doc);

        File temp = File.createTempFile("dataingest-metadata-", ".tmp");
        StreamResult result = new StreamResult(temp);
        transformer.transform(source, result);

        return temp.getAbsolutePath();
    }
}
