package rw.ac.auca.tapwallet.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "tapwallet.security.enabled=false")
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mvc;

    private static String body(String name, String email, String phone, String role) {
        return "{\"fullName\":\"" + name + "\",\"email\":\"" + email + "\",\"phone\":\"" + phone + "\",\"role\":\"" + role + "\"}";
    }

    @Test
    void createReadUpdateDelete() throws Exception {
        String created = mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Jane Doe", "jane@test.rw", "+250788111111", "CUSTOMER")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.email").value("jane@test.rw"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn().getResponse().getContentAsString();
        String id = created.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");

        mvc.perform(get("/api/users/" + id)).andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jane Doe"));

        mvc.perform(put("/api/users/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content(body("Jane Smith", "jane@test.rw", "+250788111111", "CUSTOMER")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.fullName").value("Jane Smith"));

        mvc.perform(patch("/api/users/" + id + "/freeze")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("FROZEN"));

        mvc.perform(delete("/api/users/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/users/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void rejectsInvalidBody() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"A\",\"email\":\"nope\",\"phone\":\"1\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.email").exists())
                .andExpect(jsonPath("$.fieldErrors.role").exists());
    }

    @Test
    void rejectsDuplicateEmailIgnoringCase() throws Exception {
        mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice Clone", "ALICE@tapwallet.rw", "+250788222222", "CUSTOMER")))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(containsString("already registered")));
    }

    @Test
    void cannotDeleteUserWhoOwnsAWallet() throws Exception {
        mvc.perform(delete("/api/users/2")).andExpect(status().isUnprocessableEntity());
    }

    @Test
    void cannotChangeRoleWhileOwningWallet() throws Exception {
        mvc.perform(put("/api/users/2").contentType(MediaType.APPLICATION_JSON)
                        .content(body("Alice Uwase", "alice@tapwallet.rw", "+250788000002", "MERCHANT")))
                .andExpect(status().isUnprocessableEntity());
    }
}
