package rw.ac.auca.tapwallet.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MerchantControllerTest {

    @Autowired
    private MockMvc mvc;

    private String newMerchantUser(String tag) throws Exception {
        String created = mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Shop " + tag + "\",\"email\":\"m" + tag + "@test.rw\",\"phone\":\"+2507885" + tag + "\",\"role\":\"MERCHANT\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return created.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");
    }

    @Test
    void registerReadUpdateFreezeDelete() throws Exception {
        String userId = newMerchantUser("00001");
        String created = mvc.perform(post("/api/merchants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Test Bakery\",\"code\":\"BAK-101\",\"operatorId\":" + userId + "}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.code").value("BAK-101"))
                .andExpect(jsonPath("$.revenue").value(0))
                .andReturn().getResponse().getContentAsString();
        String id = created.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");

        mvc.perform(get("/api/merchants/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.operator.id").value(Integer.parseInt(userId)));

        mvc.perform(put("/api/merchants/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Test Bakery & Cafe\",\"code\":\"BAK-101\",\"operatorId\":" + userId + "}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.businessName").value("Test Bakery & Cafe"));

        mvc.perform(patch("/api/merchants/" + id + "/freeze")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));
        mvc.perform(patch("/api/merchants/" + id + "/activate")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ACTIVE"));

        mvc.perform(delete("/api/merchants/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/merchants/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsBadCodeFormat() throws Exception {
        mvc.perform(post("/api/merchants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Bad Code\",\"code\":\"CAFE1\",\"operatorId\":4}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.code").exists());
    }

    @Test
    void rejectsDuplicateCode() throws Exception {
        String userId = newMerchantUser("00002");
        mvc.perform(post("/api/merchants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Copy Cafe\",\"code\":\"CAF-001\",\"operatorId\":" + userId + "}"))
                .andExpect(status().isConflict());
    }

    @Test
    void operatorMustHaveMerchantRole() throws Exception {
        mvc.perform(post("/api/merchants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Alice Shop\",\"code\":\"ALC-001\",\"operatorId\":2}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void oneShopPerOperator() throws Exception {
        mvc.perform(post("/api/merchants").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"businessName\":\"Second Cafe\",\"code\":\"CAF-999\",\"operatorId\":4}"))
                .andExpect(status().isConflict());
    }
}
