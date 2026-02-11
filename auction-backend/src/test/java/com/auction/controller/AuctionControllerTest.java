package com.auction.controller;

import com.auction.dto.AuctionItemRequest;
import com.auction.dto.AuctionItemResponse;
import com.auction.entity.AuctionItem;
import com.auction.entity.User;
import com.auction.security.CustomUserDetailsService;
import com.auction.security.JwtTokenProvider;
import com.auction.service.AuctionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
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

@WebMvcTest(AuctionController.class)
@DisplayName("AuctionController Tests")
class AuctionControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private AuctionService auctionService;
    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    private ObjectMapper objectMapper;
    private AuctionItemResponse sampleResponse;
    private CustomUserDetailsService.CustomUserPrincipal principal;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        User testUser = User.builder()
                .id(1L).username("seller").email("s@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();
        principal = new CustomUserDetailsService.CustomUserPrincipal(testUser);

        sampleResponse = AuctionItemResponse.builder()
                .id(10L)
                .title("Test Auction")
                .description("A test item")
                .startingPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("150.00"))
                .minBidIncrement(new BigDecimal("5.00"))
                .status(AuctionItem.AuctionStatus.ACTIVE)
                .category(AuctionItem.Category.ELECTRONICS)
                .startTime(LocalDateTime.now().minusDays(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .sellerId(1L)
                .sellerUsername("seller")
                .bidCount(3)
                .viewCount(50)
                .featured(false)
                .timeRemainingSeconds(172800L)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/auctions/{id}")
    class GetAuctionTests {

        @Test
        @WithMockUser
        @DisplayName("Should return auction by id (public endpoint)")
        void shouldReturnAuction() throws Exception {
            when(auctionService.getAuctionAndIncrementViews(10L)).thenReturn(sampleResponse);

            mockMvc.perform(get("/api/auctions/10"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(10))
                    .andExpect(jsonPath("$.data.title").value("Test Auction"))
                    .andExpect(jsonPath("$.data.sellerUsername").value("seller"));
        }
    }

    @Nested
    @DisplayName("GET /api/auctions")
    class GetActiveAuctionsTests {

        @Test
        @WithMockUser
        @DisplayName("Should return page of active auctions (public)")
        void shouldReturnActiveAuctions() throws Exception {
            Page<AuctionItemResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(auctionService.getActiveAuctions(any())).thenReturn(page);

            mockMvc.perform(get("/api/auctions")
                            .param("page", "0")
                            .param("size", "12"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].title").value("Test Auction"));
        }
    }

    @Nested
    @DisplayName("GET /api/auctions/search")
    class SearchAuctionsTests {

        @Test
        @WithMockUser
        @DisplayName("Should search auctions by keyword")
        void shouldSearchAuctions() throws Exception {
            Page<AuctionItemResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(auctionService.searchAuctions(eq("laptop"), any())).thenReturn(page);

            mockMvc.perform(get("/api/auctions/search")
                            .param("keyword", "laptop"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].title").value("Test Auction"));
        }
    }

    @Nested
    @DisplayName("GET /api/auctions/category/{category}")
    class GetByCategoryTests {

        @Test
        @WithMockUser
        @DisplayName("Should return auctions by category")
        void shouldReturnByCategory() throws Exception {
            Page<AuctionItemResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(auctionService.getAuctionsByCategory(eq(AuctionItem.Category.ELECTRONICS), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/auctions/category/ELECTRONICS"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/auctions/featured")
    class GetFeaturedTests {

        @Test
        @WithMockUser
        @DisplayName("Should return featured auctions")
        void shouldReturnFeatured() throws Exception {
            when(auctionService.getFeaturedAuctions()).thenReturn(List.of(sampleResponse));

            mockMvc.perform(get("/api/auctions/featured"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data[0].title").value("Test Auction"));
        }
    }

    @Nested
    @DisplayName("POST /api/auctions")
    class CreateAuctionTests {

        @Test
        @DisplayName("Should create auction for authenticated user")
        void shouldCreateAuction() throws Exception {
            when(auctionService.createAuction(eq(1L), any(AuctionItemRequest.class)))
                    .thenReturn(sampleResponse);

            AuctionItemRequest request = new AuctionItemRequest();
            request.setTitle("New Auction");
            request.setDescription("A brand new item");
            request.setStartingPrice(new BigDecimal("50.00"));
            request.setCategory(AuctionItem.Category.COLLECTIBLES);
            request.setStartTime(LocalDateTime.now().plusHours(1));
            request.setEndTime(LocalDateTime.now().plusDays(3));

            mockMvc.perform(post("/api/auctions")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Auction created successfully"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401WhenNotAuth() throws Exception {
            AuctionItemRequest request = new AuctionItemRequest();
            request.setTitle("Test");
            request.setDescription("Desc");
            request.setStartingPrice(new BigDecimal("10"));
            request.setCategory(AuctionItem.Category.OTHER);
            request.setStartTime(LocalDateTime.now().plusHours(1));
            request.setEndTime(LocalDateTime.now().plusDays(1));

            mockMvc.perform(post("/api/auctions")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("PUT /api/auctions/{id}")
    class UpdateAuctionTests {

        @Test
        @DisplayName("Should update auction for authenticated seller")
        void shouldUpdateAuction() throws Exception {
            when(auctionService.updateAuction(eq(10L), eq(1L), any(AuctionItemRequest.class)))
                    .thenReturn(sampleResponse);

            AuctionItemRequest request = new AuctionItemRequest();
            request.setTitle("Updated Title");
            request.setDescription("Updated Desc");
            request.setStartingPrice(new BigDecimal("100"));
            request.setCategory(AuctionItem.Category.ELECTRONICS);
            request.setStartTime(LocalDateTime.now().plusHours(1));
            request.setEndTime(LocalDateTime.now().plusDays(3));

            mockMvc.perform(put("/api/auctions/10")
                            .with(user(principal))
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Auction updated"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/auctions/{id}")
    class CancelAuctionTests {

        @Test
        @DisplayName("Should cancel auction for authenticated seller")
        void shouldCancelAuction() throws Exception {
            mockMvc.perform(delete("/api/auctions/10")
                            .with(user(principal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Auction cancelled"));
        }
    }

    @Nested
    @DisplayName("GET /api/auctions/my-auctions")
    class MyAuctionsTests {

        @Test
        @DisplayName("Should return seller's own auctions")
        void shouldReturnMyAuctions() throws Exception {
            Page<AuctionItemResponse> page = new PageImpl<>(List.of(sampleResponse));
            when(auctionService.getSellerAuctions(eq(1L), any())).thenReturn(page);

            mockMvc.perform(get("/api/auctions/my-auctions")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].sellerId").value(1));
        }
    }
}
