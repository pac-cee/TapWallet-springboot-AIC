package rw.ac.auca.tapwallet.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "tapwallet.security.enabled=false")
@AutoConfigureMockMvc
class ReportControllerTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void summaryAndCsvReflectSelectedPeriod() throws Exception {
        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":1,\"merchantId\":1,\"amount\":250,\"note\":\"report test\"}"))
                .andExpect(status().isCreated());

        String today = LocalDate.now().toString();
        mvc.perform(get("/api/reports/summary").param("from", today).param("to", today))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.from").value(today))
                .andExpect(jsonPath("$.totalUsers").value(greaterThanOrEqualTo(5)))
                .andExpect(jsonPath("$.totalPayments").value(greaterThanOrEqualTo(1)))
                .andExpect(jsonPath("$.completedVolume").isNumber())
                .andExpect(jsonPath("$.topMerchants[0].code").value("CAF-001"))
                .andExpect(jsonPath("$.dailyVolume[0].date").value(today));

        mvc.perform(get("/api/reports/payments.csv").param("from", today).param("to", today))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("attachment")))
                .andExpect(content().contentTypeCompatibleWith("text/csv"))
                .andExpect(content().string(containsString("report test")));
    }

    @Test
    void rejectsInvalidPeriod() throws Exception {
        mvc.perform(get("/api/reports/summary").param("from", "2026-10-11").param("to", "2026-10-01"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.message").value(containsString("cannot be after")));
    }
}
