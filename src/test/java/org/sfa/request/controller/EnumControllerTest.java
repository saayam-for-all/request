package org.sfa.request.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfa.request.dto.EnumsResponse;
import org.sfa.request.service.api.EnumService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ContextConfiguration(classes = {EnumController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class EnumControllerTest {

    @MockBean
    private EnumService enumService;

    @Autowired
    private EnumController enumController;

    @Test
    void testGetEnums() throws Exception {
        when(enumService.getAllEnums()).thenReturn(new EnumsResponse());

        MockMvcBuilders.standaloneSetup(enumController)
                .build()
                .perform(MockMvcRequestBuilders.get("/dev/requests/v0.0.1/enums"))
                .andExpect(MockMvcResultMatchers.status().isOk());

        verify(enumService).getAllEnums();
    }
}