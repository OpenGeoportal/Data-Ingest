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

    // private String solrUrl;
    private HttpSolrClient solrClient;

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    public SolrJClient(String solrUrl) {

        HttpSolrClient solr = new HttpSolrClient(solrUrl);
        this.solrClient = solr;
    }


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

        } catch (Exception e) {// If there are errors, we just skip the metadata
            //e.printStackTrace();
            throw new NoMetadataException(e.getMessage());
        }

        return response;

    }

}
