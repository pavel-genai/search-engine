package com.searchengine;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exercises the IOException-handling branch in SearchController#indexDocument
 * by pointing the persist-path at a location whose parent cannot be created.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "search-engine.index.persist-path=/proc/cannot/create/index.ser"
})
class SearchControllerIoErrorTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void indexDocumentStillSucceedsWhenPersistenceFails() throws Exception {
        mockMvc.perform(post("/index")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"title\": \"IO Fail Doc\", \"text\": \"persisting this should fail but still return 200\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.docId").exists())
                .andExpect(jsonPath("$.message").value("Document indexed successfully"));
    }
}