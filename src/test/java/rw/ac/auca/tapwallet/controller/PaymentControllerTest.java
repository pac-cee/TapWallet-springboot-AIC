package rw.ac.auca.tapwallet.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PaymentControllerTest {

    @Autowired
    private MockMvc mvc;

    private static String pay(long walletId, long merchantId, String amount) {
        return "{\"walletId\":" + walletId + ",\"merchantId\":" + merchantId + ",\"amount\":" + amount + ",\"note\":\"test\"}";
    }

    @Test
    void paymentMovesMoneyAndReversalPutsItBack() throws Exception {
        String before = mvc.perform(get("/api/wallets/1")).andReturn().getResponse().getContentAsString();
        double balance = Double.parseDouble(before.replaceAll(".*\"balance\":([0-9.]+).*", "$1"));

        String created = mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content(pay(1, 1, "1500")))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.reference").value(containsString("TXN-")))
                .andExpect(jsonPath("$.wallet.balance").value(balance - 1500))
                .andReturn().getResponse().getContentAsString();
        String id = created.replaceFirst("^.*?\"id\":(\\d+).*$", "$1");

        mvc.perform(get("/api/payments/by-wallet/1")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(Integer.parseInt(id)));

        mvc.perform(post("/api/payments/" + id + "/reverse")).andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REVERSED"))
                .andExpect(jsonPath("$.wallet.balance").value(balance));

        mvc.perform(post("/api/payments/" + id + "/reverse"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("already been reversed")));
    }

    @Test
    void rejectsOverdraft() throws Exception {
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content(pay(2, 1, "999999")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("Insufficient funds")));
    }

    @Test
    void frozenMerchantDoesNotAcceptPayments() throws Exception {
        mvc.perform(patch("/api/merchants/2/freeze")).andExpect(status().isOk());
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content(pay(1, 2, "10")))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("not accepting payments")));
        mvc.perform(patch("/api/merchants/2/activate")).andExpect(status().isOk());
    }

    @Test
    void unknownWalletIs404() throws Exception {
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content(pay(999, 1, "10")))
                .andExpect(status().isNotFound());
    }

    @Test
    void rejectsZeroAmount() throws Exception {
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content(pay(1, 1, "0")))
                .andExpect(status().isBadRequest());
    }
}
