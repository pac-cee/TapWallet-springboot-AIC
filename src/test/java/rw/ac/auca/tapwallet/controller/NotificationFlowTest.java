package rw.ac.auca.tapwallet.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import rw.ac.auca.tapwallet.event.InMemoryNotificationPublisher;
import rw.ac.auca.tapwallet.event.NotificationChannel;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "tapwallet.security.enabled=false")
@AutoConfigureMockMvc
class NotificationFlowTest {

    @Autowired MockMvc mvc;
    @Autowired InMemoryNotificationPublisher publisher;

    @Test
    void paymentPublishesEmailAndSmsEvents() throws Exception {
        int before = publisher.publishedEvents().size();

        mvc.perform(post("/api/payments").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"walletId\":1,\"merchantId\":1,\"amount\":100,\"note\":\"notification test\"}"))
                .andExpect(status().isCreated());

        var newEvents = publisher.publishedEvents().subList(before, before + 2);
        assertThat(newEvents).extracting(event -> event.channel())
                .containsExactly(NotificationChannel.EMAIL, NotificationChannel.SMS);
        assertThat(newEvents).allMatch(event -> event.type().equals("payment.completed"));
    }
}
