package org.opengeoportal.dataingest.api.solr;

import org.apache.solr.client.solrj.response.QueryResponse;
import org.opengeoportal.dataingest.exception.NoMetadataException;

/**
 * Created by joana on 12/05/17.
 */
public interface SolrClient {
    /**
     * Searches for a geoserver dataset stored in solr, based on the typename.
     *
     * @param WorkspaceName dataset workspace
     * @param Name dataset name
     * @return result of the query
     */
    QueryResponse searchForDataset(String WorkspaceName, String Name) throws NoMetadataException;
}
