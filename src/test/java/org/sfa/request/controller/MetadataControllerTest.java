package org.sfa.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfa.request.dto.MetadataFormRequestDto;
import org.sfa.request.dto.ReqAddInfoMetadataTreeDto;
import org.sfa.request.service.api.MetadataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {MetadataController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class MetadataControllerTest {

    @MockBean
    private MetadataService metadataService;

    @Autowired
    private MetadataController metadataController;

    private final ObjectMapper mapper = new ObjectMapper();

    // POST /metadata/form
    @Test
    void testGetMetadataForm() throws Exception {
        when(metadataService.getMetadataFormByCategoryId("1.2"))
                .thenReturn(new ReqAddInfoMetadataTreeDto());

        MetadataFormRequestDto requestDto = new MetadataFormRequestDto();
        requestDto.setCatId("1.2");

        MockMvcBuilders.standaloneSetup(metadataController).build()
                .perform(post("/metadata/form")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk());

        verify(metadataService).getMetadataFormByCategoryId("1.2");
    }

    // GET /metadata
    @Test
    void testGetAllMetadataWithItems() throws Exception {
        when(metadataService.getAllMetadataWithItems())
                .thenReturn(Collections.singletonList(new ReqAddInfoMetadataTreeDto()));

        MockMvcBuilders.standaloneSetup(metadataController).build()
                .perform(get("/metadata"))
                .andExpect(status().isOk());

        verify(metadataService).getAllMetadataWithItems();
    }
}