package org.opengeoportal.dataingest.api.solr;

/**
 * Created by joana on 12/05/17.
 */

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.opengeoportal.dataingest.exception.NoMetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class for retrieve info from a remote Solr instance.
 */
public class SolrJClient implements SolrClient {

    /**
     * Logger interface, to write messages.
     */
    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    /**
     * Solr client.
     */
    private HttpSolrClient solrClient;

    /**
     * Constructor.
     *
     * @param solrUrl Url of the SOLR instance
     */
    public SolrJClient(String solrUrl) {

        HttpSolrClient solr = new HttpSolrClient(solrUrl);
        this.solrClient = solr;
    }

    /**
     * Search for a dataset in SOLR.
     *
     * @param WorkspaceName Workspace name
     * @param Name Dataset name
     * @return A query response
     * @throws NoMetadataException
     */
    @Override
    public QueryResponse searchForDataset(String WorkspaceName, String Name) throws
        NoMetadataException {

        SolrQuery query = new SolrQuery();
        query.setQuery("Name" + ":" + Name);
        query.addField("FgdcText");
        query.addFilterQuery("WorkspaceName" + ":" + WorkspaceName);
        query.setRows(1);

        QueryResponse response = null;
        try {
            response = solrClient.query(query);

        } catch (Exception e) { // If there are errors, we just skip the metadata
            //e.printStackTrace();
            throw new NoMetadataException(e.getMessage());
        }

        return response;

    }

}
