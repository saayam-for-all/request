package org.sfa.request.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.sfa.request.dto.HelpCategoryDto;
import org.sfa.request.dto.HelpCategoryMapDto;
import org.sfa.request.model.entity.HelpCategory;
import org.sfa.request.service.api.HelpCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.aot.DisabledInAotMode;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ContextConfiguration(classes = {HelpCategoryController.class})
@ExtendWith(SpringExtension.class)
@DisabledInAotMode
class HelpCategoryControllerTest {

    @MockBean
    private HelpCategoryService helpCategoryService;

    @Autowired
    private HelpCategoryController helpCategoryController;

    private final ObjectMapper mapper = new ObjectMapper();

    // GET /helpCategories
    @Test
    void testGetAllHierarchical() throws Exception {
        when(helpCategoryService.getAllHierarchicalCategories())
                .thenReturn(Collections.singletonList(new HelpCategoryDto()));

        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(get("/helpCategories"))
                .andExpect(status().isOk());

        verify(helpCategoryService).getAllHierarchicalCategories();
    }

    // POST /helpCategories/parent - valid
    @Test
    void testGetByParentId_valid() throws Exception {
        when(helpCategoryService.getChildMappingsByParentId("1"))
                .thenReturn(Collections.singletonList(new HelpCategoryMapDto()));

        Map<String, String> body = new HashMap<>();
        body.put("parentId", "1");

        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(post("/helpCategories/parent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(helpCategoryService).getChildMappingsByParentId("1");
    }

    // POST /helpCategories/parent - missing parentId
    @Test
    void testGetByParentId_missingParentId() throws Exception {
        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(post("/helpCategories/parent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new HashMap<>())))
                .andExpect(status().isBadRequest());
    }

    // POST /helpCategories/parent - no children found
    @Test
    void testGetByParentId_noChildren() throws Exception {
        when(helpCategoryService.getChildMappingsByParentId("999"))
                .thenReturn(Collections.emptyList());

        Map<String, String> body = new HashMap<>();
        body.put("parentId", "999");

        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(post("/helpCategories/parent")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNoContent());
    }

    // POST /helpCategories/byId - valid
    @Test
    void testGetByCatId_valid() throws Exception {
        when(helpCategoryService.getCategoriesByCatId("1.2"))
                .thenReturn(Collections.singletonList(new HelpCategory()));

        Map<String, String> body = new HashMap<>();
        body.put("catId", "1.2");

        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(post("/helpCategories/byId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isOk());

        verify(helpCategoryService).getCategoriesByCatId("1.2");
    }

    // POST /helpCategories/byId - missing catId
    @Test
    void testGetByCatId_missingCatId() throws Exception {
        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(post("/helpCategories/byId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(new HashMap<>())))
                .andExpect(status().isBadRequest());
    }

    // POST /helpCategories/byId - not found
    @Test
    void testGetByCatId_notFound() throws Exception {
        when(helpCategoryService.getCategoriesByCatId("999"))
                .thenReturn(Collections.emptyList());

        Map<String, String> body = new HashMap<>();
        body.put("catId", "999");

        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(post("/helpCategories/byId")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(mapper.writeValueAsString(body)))
                .andExpect(status().isNotFound());
    }

    // GET /helpCategories/categoryMap
    @Test
    void testGetHelpCategoriesTree() throws Exception {
        Map<String, List<String>> tree = new HashMap<>();
        tree.put("1", List.of("1.1", "1.2"));
        when(helpCategoryService.getHelpCategoriesTree()).thenReturn(tree);

        MockMvcBuilders.standaloneSetup(helpCategoryController).build()
                .perform(get("/helpCategories/categoryMap"))
                .andExpect(status().isOk());

        verify(helpCategoryService).getHelpCategoriesTree();
    }
}