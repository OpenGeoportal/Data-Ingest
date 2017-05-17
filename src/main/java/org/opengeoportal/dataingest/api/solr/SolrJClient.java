package org.opengeoportal.dataingest.api.solr;

/**
 * Created by joana on 12/05/17.
 */

import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.opengeoportal.dataingest.exception.MetadataException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

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
        MetadataException {

        SolrQuery query = new SolrQuery();
        query.setQuery("Name" + ":" + Name);
        query.addField("FgdcText");
        query.addFilterQuery("WorkspaceName" + ":" + WorkspaceName);
        query.setRows(1);

        QueryResponse response = null;
        try {
            response = solrClient.query(query);
        } catch (SolrServerException e) {
            throw new MetadataException(e.getMessage());
        } catch (IOException e) {
            throw new MetadataException(e.getMessage());
        }
        return response;

    }

}
