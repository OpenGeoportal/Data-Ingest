package org.opengeoportal;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by joana on 13/01/17.
 */
@RunWith(SpringRunner.class)
@WebMvcTest(DataSetsController.class)
public class DataSetsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void testGetDataSets() throws Exception {
        this.mockMvc.perform(MockMvcRequestBuilders.get("/datasets").accept
            (MediaType.parseMediaType("application/json;charset=UTF-8")))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json;charset=UTF-8"));
    }

}
