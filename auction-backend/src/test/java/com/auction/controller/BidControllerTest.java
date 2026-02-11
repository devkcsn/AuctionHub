package com.auction.controller;

import com.auction.dto.BidRequest;
import com.auction.dto.BidResponse;
import com.auction.entity.User;
import com.auction.security.CustomUserDetailsService;
import com.auction.security.JwtTokenProvider;
import com.auction.service.BidService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BidController.class)
@DisplayName("BidController Tests")
class BidControllerTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;

    @MockitoBean private BidService bidService;
    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    private CustomUserDetailsService.CustomUserPrincipal principal;
    private BidResponse sampleBidResponse;

    @BeforeEach
    void setUp() {
        User bidder = User.builder()
                .id(2L).username("bidder").email("b@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();
        principal = new CustomUserDetailsService.CustomUserPrincipal(bidder);

        sampleBidResponse = BidResponse.builder()
                .id(100L)
                .amount(new BigDecimal("200.00"))
                .auctionItemId(10L)
                .auctionTitle("Test Auction")
                .bidderId(2L)
                .bidderUsername("bidder")
                .autoBid(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("POST /api/bids")
    class PlaceBidTests {

        @Test
        @DisplayName("Should place bid successfully")
        void shouldPlaceBid() throws Exception {
            when(bidService.placeBid(eq(2L), any(BidRequest.class))).thenReturn(sampleBidResponse);

            BidRequest request = new BidRequest();
            request.setAuctionItemId(10L);
            request.setAmount(new BigDecimal("200.00"));

            mockMvc.perform(post("/api/bids")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Bid placed successfully"))
                    .andExpect(jsonPath("$.data.amount").value(200.00))
                    .andExpect(jsonPath("$.data.bidderUsername").value("bidder"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401() throws Exception {
            BidRequest request = new BidRequest();
            request.setAuctionItemId(10L);
            request.setAmount(new BigDecimal("200.00"));

            mockMvc.perform(post("/api/bids")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("Should return 400 for missing auction id")
        void shouldReturn400ForMissingAuctionId() throws Exception {
            BidRequest request = new BidRequest();
            request.setAmount(new BigDecimal("200.00"));

            mockMvc.perform(post("/api/bids")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 400 for missing amount")
        void shouldReturn400ForMissingAmount() throws Exception {
            BidRequest request = new BidRequest();
            request.setAuctionItemId(10L);

            mockMvc.perform(post("/api/bids")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/bids/auction/{auctionId}")
    class GetAuctionBidsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return bids for auction (public)")
        void shouldReturnBids() throws Exception {
            Page<BidResponse> page = new PageImpl<>(List.of(sampleBidResponse));
            when(bidService.getAuctionBids(eq(10L), any())).thenReturn(page);

            mockMvc.perform(get("/api/bids/auction/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].amount").value(200.00));
        }
    }

    @Nested
    @DisplayName("GET /api/bids/my-bids")
    class GetMyBidsTests {

        @Test
        @DisplayName("Should return user's bids when authenticated")
        void shouldReturnMyBids() throws Exception {
            Page<BidResponse> page = new PageImpl<>(List.of(sampleBidResponse));
            when(bidService.getUserBids(eq(2L), any())).thenReturn(page);

            mockMvc.perform(get("/api/bids/my-bids")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].bidderUsername").value("bidder"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/bids/my-bids"))
                    .andExpect(status().isUnauthorized());
        }
    }
}
