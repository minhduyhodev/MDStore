package com.mdstore.catalog.web;

import com.mdstore.common.web.ErrorCode;
import com.mdstore.common.web.GlobalExceptionHandler;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(CatalogController.class)
@ContextConfiguration(classes = {CatalogController.class, GlobalExceptionHandler.class})
class CatalogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void getCatalog_notImplemented_returnsInternalServerErrorWithEnvelope() throws Exception {
        mockMvc.perform(get("/api/catalog"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.error.code").value(ErrorCode.INTERNAL_SERVER_ERROR.name()))
                .andExpect(jsonPath("$.error.message").value(ErrorCode.INTERNAL_SERVER_ERROR.getDefaultMessage()));
    }
}
