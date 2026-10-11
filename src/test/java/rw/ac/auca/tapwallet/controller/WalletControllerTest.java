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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "tapwallet.security.enabled=false")
@AutoConfigureMockMvc
class WalletControllerTest {

    @Autowired
    private MockMvc mvc;

    private String newCustomer(String tag) throws Exception {
        String created = mvc.perform(post("/api/users").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"fullName\":\"Wallet " + tag + "\",\"email\":\"w" + tag + "@test.rw\",\"phone\":\"+2507883" + tag + "\",\"role\":\"CUSTOMER\"}"))
                .andExpect(status().isCreated()).andReturn().getResponse().getContentAsString();
        return created.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");
    }

    @Test
    void openTopUpWithdrawUpdateAndClose() throws Exception {
        String userId = newCustomer("00001");
        String wallet = mvc.perform(post("/api/wallets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":" + userId + ",\"openingBalance\":0,\"currency\":\"RWF\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.balance").value(0))
                .andReturn().getResponse().getContentAsString();
        String id = wallet.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");

        mvc.perform(post("/api/wallets/" + id + "/top-up").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":2500}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(2500.00));

        mvc.perform(post("/api/wallets/" + id + "/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":9999}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("Insufficient funds")));

        mvc.perform(put("/api/wallets/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currency\":\"USD\",\"status\":\"ACTIVE\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.currency").value(containsString("RWF only")));

        mvc.perform(post("/api/wallets/" + id + "/withdraw").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":2500}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.balance").value(0.00));

        mvc.perform(get("/api/wallets/by-owner/" + userId)).andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(Integer.parseInt(id)));

        mvc.perform(delete("/api/wallets/" + id)).andExpect(status().isNoContent());
        mvc.perform(get("/api/wallets/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void oneWalletPerUser() throws Exception {
        mvc.perform(post("/api/wallets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":2,\"openingBalance\":0,\"currency\":\"RWF\"}"))
                .andExpect(status().isConflict());
    }

    @Test
    void onlyCustomersOwnWallets() throws Exception {
        mvc.perform(post("/api/wallets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":4,\"openingBalance\":0,\"currency\":\"RWF\"}"))
                .andExpect(status().isUnprocessableEntity());
    }

    @Test
    void frozenWalletCannotTransact() throws Exception {
        String userId = newCustomer("00002");
        String wallet = mvc.perform(post("/api/wallets").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"ownerId\":" + userId + ",\"openingBalance\":100,\"currency\":\"RWF\"}"))
                .andReturn().getResponse().getContentAsString();
        String id = wallet.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");

        mvc.perform(put("/api/wallets/" + id).contentType(MediaType.APPLICATION_JSON)
                        .content("{\"currency\":\"RWF\",\"status\":\"FROZEN\"}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.status").value("FROZEN"));

        mvc.perform(post("/api/wallets/" + id + "/top-up").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":10}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("frozen")));
    }

    @Test
    void rejectsNonPositiveAmount() throws Exception {
        mvc.perform(post("/api/wallets/1/top-up").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":0}"))
                .andExpect(status().isBadRequest());
    }
}
