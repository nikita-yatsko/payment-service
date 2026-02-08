package com.payment.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.WireMockServer;
import com.payment.service.PaymentServiceApplication;
import com.payment.service.model.dto.PaymentRequest;
import com.payment.service.model.entity.Payment;
import com.payment.service.model.enums.PaymentStatus;
import com.payment.service.repository.PaymentRepository;
import com.payment.service.security.model.CustomUserDetails;
import org.bson.types.Decimal128;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;


@ActiveProfiles("test")
@SpringBootTest(classes = PaymentServiceApplication.class)
@AutoConfigureMockMvc
@EnableWireMock
@TestInstance(TestInstance.Lifecycle.PER_METHOD)
public class PaymentControllerTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PaymentRepository paymentRepository;

    @InjectWireMock
    private WireMockServer wireMockServer;

    private Payment payment1, payment2;

    @BeforeEach
    public void setup() {
        paymentRepository.deleteAll();

        payment1 = new Payment();
        payment1.setOrderId("1");
        payment1.setUserId("1");
        payment1.setPaymentAmount(Decimal128.parse("100.0"));
        payment1.setStatus(PaymentStatus.SUCCESS);
        payment1.setTimestamp(Instant.parse("2025-05-01T00:00:00Z"));

        payment2 = new Payment();
        payment2.setOrderId("2");
        payment2.setUserId("1");
        payment2.setPaymentAmount(Decimal128.parse("200.0"));
        payment2.setStatus(PaymentStatus.FAILED);
        payment2.setTimestamp(Instant.parse("2025-06-03T00:00:00Z"));

        paymentRepository.save(payment1);
        paymentRepository.save(payment2);
    }

    @Test
    public void createPaymentReturn201Created() throws Exception {
        // Given:
        PaymentRequest request = new PaymentRequest();
        request.setOrderId("1");
        request.setUserId("1");
        request.setPaymentAmount(BigDecimal.valueOf(100.0));

        CustomUserDetails principal = new CustomUserDetails(1, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        wireMockServer.resetAll();

        wireMockServer.stubFor(
                get(urlPathEqualTo("/api/v1.0/random"))
                        .willReturn(aResponse()
                                .withStatus(200)
                                .withHeader("Content-Type", "application/json")
                                .withBody("[2]"))
        );

        // When:
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .post("/api/payment/create")
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // Then:
        result.andExpect(MockMvcResultMatchers.status().isCreated())
                .andExpect(MockMvcResultMatchers.jsonPath("$.orderId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.userId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.paymentAmount").value(100.0));

    }

    @Test
    public void getPaymentsByAnyByOrderIdReturn200Ok() throws Exception {
        // Given:
        CustomUserDetails principal = new CustomUserDetails(1, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/payment/byAny")
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON)
                .param("orderId", "1"));


        // Then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].orderId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].userId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].paymentAmount").value(BigDecimal.valueOf(100.0)));

    }

    @Test
    public void getPaymentsByAnyByUserIdReturn200Ok() throws Exception {
        // Given:
        CustomUserDetails principal = new CustomUserDetails(1, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/payment/byAny")
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON)
                .param("userId", "1"));

        // Then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].userId").value("1"));
    }

    @Test
    public void getPaymentsByAnyByStatusReturn200Ok() throws Exception {
        // Given:
        CustomUserDetails principal = new CustomUserDetails(1, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/payment/byAny")
                .with(authentication(auth))
                .accept(MediaType.APPLICATION_JSON)
                .param("status", "SUCCESS"));

        // Then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.size()").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].orderId").value("1"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.[0].status").value("SUCCESS"));
    }

    @Test
    public void getTotalSumReturn200Ok() throws Exception {
        // Given:
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2025-12-31T23:59:59Z");

        CustomUserDetails principal = new CustomUserDetails(1, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/payment/summary")
                .with(authentication(auth))
                .param("userId", "1")
                .param("from", from.toString())
                .param("to", to.toString())
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(100.0));

    }

    @Test
    public void getTotalSumAllReturn200Ok() throws Exception {
        // Given:
        Instant from = Instant.parse("2025-01-01T00:00:00Z");
        Instant to = Instant.parse("2025-12-31T23:59:59Z");

        CustomUserDetails principal = new CustomUserDetails(1, "ADMIN");
        Authentication auth = new UsernamePasswordAuthenticationToken(
                principal,
                null,
                List.of(new SimpleGrantedAuthority("ROLE_ADMIN"))
        );

        // When:
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders
                .get("/api/payment/summary/all")
                .with(authentication(auth))
                .param("from", from.toString())
                .param("to", to.toString())
                .accept(MediaType.APPLICATION_JSON));

        // Then:
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$").value(100.0));

    }
}
