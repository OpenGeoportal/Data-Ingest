package org.opengeoportal;

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Created by joana on 13/01/17.
 */
public class DataSetsTest {
    @Test
    public void getContent() throws Exception {

        DataSets DS = new DataSets("This is a list of datasets");

        String str = DS.getContent();
        assertTrue(str != null && !str.isEmpty());

    }

}
