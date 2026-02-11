package com.auction.service;

import com.auction.dto.AuctionItemRequest;
import com.auction.dto.AuctionItemResponse;
import com.auction.entity.AuctionItem;
import com.auction.entity.User;
import com.auction.exception.AuctionException;
import com.auction.exception.BadRequestException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.exception.UnauthorizedException;
import com.auction.repository.AuctionItemRepository;
import com.auction.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AuctionService Tests")
class AuctionServiceTest {

    @Mock private AuctionItemRepository auctionItemRepository;
    @Mock private UserRepository userRepository;

    @InjectMocks private AuctionService auctionService;

    private User seller;
    private User otherUser;
    private AuctionItem activeAuction;
    private AuctionItem pendingAuction;
    private AuctionItemRequest validRequest;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L).username("seller").email("s@e.com").password("p")
                .fullName("Seller").role(User.Role.USER).enabled(true).build();

        otherUser = User.builder()
                .id(2L).username("other").email("o@e.com").password("p")
                .fullName("Other").role(User.Role.USER).enabled(true).build();

        activeAuction = AuctionItem.builder()
                .id(10L)
                .title("Active Auction")
                .description("An active auction item")
                .startingPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("150.00"))
                .minBidIncrement(new BigDecimal("5.00"))
                .category(AuctionItem.Category.ELECTRONICS)
                .status(AuctionItem.AuctionStatus.ACTIVE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .seller(seller)
                .bidCount(3)
                .viewCount(50)
                .featured(false)
                .build();

        pendingAuction = AuctionItem.builder()
                .id(20L)
                .title("Pending Auction")
                .description("A pending auction item")
                .startingPrice(new BigDecimal("200.00"))
                .currentPrice(new BigDecimal("200.00"))
                .minBidIncrement(new BigDecimal("1.00"))
                .category(AuctionItem.Category.ART)
                .status(AuctionItem.AuctionStatus.PENDING)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(5))
                .seller(seller)
                .bidCount(0)
                .viewCount(0)
                .featured(false)
                .build();

        validRequest = new AuctionItemRequest();
        validRequest.setTitle("New Auction");
        validRequest.setDescription("A brand new item");
        validRequest.setStartingPrice(new BigDecimal("50.00"));
        validRequest.setCategory(AuctionItem.Category.COLLECTIBLES);
        validRequest.setStartTime(LocalDateTime.now().plusHours(1));
        validRequest.setEndTime(LocalDateTime.now().plusDays(3));
    }

    @Nested
    @DisplayName("createAuction()")
    class CreateAuctionTests {

        @Test
        @DisplayName("Should create auction successfully")
        void shouldCreateAuction() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
            when(auctionItemRepository.save(any(AuctionItem.class))).thenAnswer(inv -> {
                AuctionItem item = inv.getArgument(0);
                item.setId(100L);
                return item;
            });

            AuctionItemResponse response = auctionService.createAuction(1L, validRequest);

            assertNotNull(response);
            assertEquals("New Auction", response.getTitle());
            assertEquals(new BigDecimal("50.00"), response.getStartingPrice());
            assertEquals(AuctionItem.AuctionStatus.PENDING, response.getStatus());
            assertEquals("seller", response.getSellerUsername());
            verify(auctionItemRepository).save(any(AuctionItem.class));
        }

        @Test
        @DisplayName("Should throw when seller not found")
        void shouldThrowWhenSellerNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> auctionService.createAuction(999L, validRequest));
        }

        @Test
        @DisplayName("Should throw when end time is before start time")
        void shouldThrowWhenEndBeforeStart() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
            validRequest.setEndTime(validRequest.getStartTime().minusHours(1));

            assertThrows(BadRequestException.class,
                    () -> auctionService.createAuction(1L, validRequest));
        }

        @Test
        @DisplayName("Should throw when duration is less than 1 hour")
        void shouldThrowWhenDurationTooShort() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
            validRequest.setEndTime(validRequest.getStartTime().plusMinutes(30));

            assertThrows(BadRequestException.class,
                    () -> auctionService.createAuction(1L, validRequest));
        }

        @Test
        @DisplayName("Should throw when reserve price < starting price")
        void shouldThrowWhenReserveLessThanStarting() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
            validRequest.setReservePrice(new BigDecimal("10.00")); // less than starting 50

            assertThrows(BadRequestException.class,
                    () -> auctionService.createAuction(1L, validRequest));
        }

        @Test
        @DisplayName("Should set status to ACTIVE if start time is in the past")
        void shouldSetActiveIfStartTimeInPast() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
            validRequest.setStartTime(LocalDateTime.now().minusHours(1));
            validRequest.setEndTime(LocalDateTime.now().plusDays(1));
            when(auctionItemRepository.save(any(AuctionItem.class))).thenAnswer(inv -> {
                AuctionItem item = inv.getArgument(0);
                item.setId(101L);
                return item;
            });

            AuctionItemResponse response = auctionService.createAuction(1L, validRequest);
            assertEquals(AuctionItem.AuctionStatus.ACTIVE, response.getStatus());
        }
    }

    @Nested
    @DisplayName("getAuction()")
    class GetAuctionTests {

        @Test
        @DisplayName("Should return auction by id")
        void shouldGetAuction() {
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));

            AuctionItemResponse response = auctionService.getAuction(10L);

            assertNotNull(response);
            assertEquals(10L, response.getId());
            assertEquals("Active Auction", response.getTitle());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException for non-existent id")
        void shouldThrowForNonExistent() {
            when(auctionItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> auctionService.getAuction(999L));
        }
    }

    @Nested
    @DisplayName("getAuctionAndIncrementViews()")
    class GetAuctionAndIncrementViewsTests {

        @Test
        @DisplayName("Should increment view count")
        void shouldIncrementViews() {
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));
            when(auctionItemRepository.save(any(AuctionItem.class))).thenReturn(activeAuction);

            int viewsBefore = activeAuction.getViewCount();
            AuctionItemResponse response = auctionService.getAuctionAndIncrementViews(10L);

            assertEquals(viewsBefore + 1, activeAuction.getViewCount());
            verify(auctionItemRepository).save(activeAuction);
        }
    }

    @Nested
    @DisplayName("getActiveAuctions()")
    class GetActiveAuctionsTests {

        @Test
        @DisplayName("Should return page of active auctions")
        void shouldReturnActiveAuctions() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.findByStatus(eq(AuctionItem.AuctionStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.getActiveAuctions(PageRequest.of(0, 10));

            assertEquals(1, result.getTotalElements());
            assertEquals("Active Auction", result.getContent().get(0).getTitle());
        }
    }

    @Nested
    @DisplayName("searchAuctions()")
    class SearchAuctionsTests {

        @Test
        @DisplayName("Should search with keyword")
        void shouldSearchWithKeyword() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.searchActiveAuctions(eq("laptop"), any(Pageable.class)))
                    .thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.searchAuctions("laptop", PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should fall back to active auctions for null keyword")
        void shouldFallBackForNullKeyword() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.findByStatus(eq(AuctionItem.AuctionStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.searchAuctions(null, PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should fall back to active auctions for empty keyword")
        void shouldFallBackForEmptyKeyword() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.findByStatus(eq(AuctionItem.AuctionStatus.ACTIVE), any(Pageable.class)))
                    .thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.searchAuctions("   ", PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("updateAuction()")
    class UpdateAuctionTests {

        @Test
        @DisplayName("Should update pending auction successfully")
        void shouldUpdatePendingAuction() {
            when(auctionItemRepository.findById(20L)).thenReturn(Optional.of(pendingAuction));
            when(auctionItemRepository.save(any(AuctionItem.class))).thenReturn(pendingAuction);

            AuctionItemRequest updateReq = new AuctionItemRequest();
            updateReq.setTitle("Updated Title");

            AuctionItemResponse response = auctionService.updateAuction(20L, 1L, updateReq);
            assertEquals("Updated Title", pendingAuction.getTitle());
        }

        @Test
        @DisplayName("Should throw UnauthorizedException for wrong seller")
        void shouldThrowForWrongSeller() {
            when(auctionItemRepository.findById(20L)).thenReturn(Optional.of(pendingAuction));

            AuctionItemRequest updateReq = new AuctionItemRequest();
            updateReq.setTitle("Hack Attempt");

            assertThrows(UnauthorizedException.class,
                    () -> auctionService.updateAuction(20L, 999L, updateReq));
        }

        @Test
        @DisplayName("Should not allow update of active auction")
        void shouldNotUpdateActiveAuction() {
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));

            AuctionItemRequest updateReq = new AuctionItemRequest();
            updateReq.setTitle("Updated");

            assertThrows(AuctionException.class,
                    () -> auctionService.updateAuction(10L, 1L, updateReq));
        }
    }

    @Nested
    @DisplayName("cancelAuction()")
    class CancelAuctionTests {

        @Test
        @DisplayName("Should cancel pending auction with no bids")
        void shouldCancelPendingAuction() {
            when(auctionItemRepository.findById(20L)).thenReturn(Optional.of(pendingAuction));
            when(auctionItemRepository.save(any(AuctionItem.class))).thenReturn(pendingAuction);

            assertDoesNotThrow(() -> auctionService.cancelAuction(20L, 1L));

            assertEquals(AuctionItem.AuctionStatus.CANCELLED, pendingAuction.getStatus());
            verify(auctionItemRepository).save(pendingAuction);
        }

        @Test
        @DisplayName("Should throw for wrong seller on cancel")
        void shouldThrowForWrongSellerOnCancel() {
            when(auctionItemRepository.findById(20L)).thenReturn(Optional.of(pendingAuction));

            assertThrows(UnauthorizedException.class,
                    () -> auctionService.cancelAuction(20L, 999L));
        }

        @Test
        @DisplayName("Should not cancel ended auction")
        void shouldNotCancelEndedAuction() {
            AuctionItem ended = AuctionItem.builder()
                    .id(30L).title("Ended").description("d")
                    .startingPrice(new BigDecimal("10")).currentPrice(new BigDecimal("10"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.ENDED)
                    .startTime(LocalDateTime.now().minusDays(3))
                    .endTime(LocalDateTime.now().minusDays(1))
                    .seller(seller).bidCount(0).viewCount(0).featured(false).build();

            when(auctionItemRepository.findById(30L)).thenReturn(Optional.of(ended));

            assertThrows(AuctionException.class,
                    () -> auctionService.cancelAuction(30L, 1L));
        }

        @Test
        @DisplayName("Should not cancel auction with bids")
        void shouldNotCancelAuctionWithBids() {
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));
            // activeAuction has bidCount=3

            assertThrows(AuctionException.class,
                    () -> auctionService.cancelAuction(10L, 1L));
        }
    }

    @Nested
    @DisplayName("mapToResponse()")
    class MapToResponseTests {

        @Test
        @DisplayName("Should map all fields correctly")
        void shouldMapAllFields() {
            AuctionItemResponse response = auctionService.mapToResponse(activeAuction);

            assertEquals(10L, response.getId());
            assertEquals("Active Auction", response.getTitle());
            assertEquals("An active auction item", response.getDescription());
            assertEquals(new BigDecimal("100.00"), response.getStartingPrice());
            assertEquals(new BigDecimal("150.00"), response.getCurrentPrice());
            assertEquals(new BigDecimal("5.00"), response.getMinBidIncrement());
            assertEquals(AuctionItem.AuctionStatus.ACTIVE, response.getStatus());
            assertEquals(AuctionItem.Category.ELECTRONICS, response.getCategory());
            assertEquals(1L, response.getSellerId());
            assertEquals("seller", response.getSellerUsername());
            assertNull(response.getWinnerId());
            assertNull(response.getWinnerUsername());
            assertEquals(3, response.getBidCount());
            assertEquals(50, response.getViewCount());
            assertFalse(response.getFeatured());
            assertTrue(response.getTimeRemainingSeconds() > 0);
        }

        @Test
        @DisplayName("Should set timeRemaining to 0 for non-active auction")
        void shouldSetZeroTimeForNonActive() {
            AuctionItemResponse response = auctionService.mapToResponse(pendingAuction);
            assertEquals(0L, response.getTimeRemainingSeconds());
        }

        @Test
        @DisplayName("Should include winner info when present")
        void shouldIncludeWinnerInfo() {
            activeAuction.setWinner(otherUser);
            AuctionItemResponse response = auctionService.mapToResponse(activeAuction);
            assertEquals(2L, response.getWinnerId());
            assertEquals("other", response.getWinnerUsername());
        }
    }

    @Nested
    @DisplayName("Query delegation methods")
    class QueryDelegationTests {

        @Test
        @DisplayName("getAuctionsByCategory should delegate to repository")
        void shouldDelegateGetByCategory() {
            Page<AuctionItem> page = new PageImpl<>(Collections.emptyList());
            when(auctionItemRepository.findByStatusAndCategory(any(), any(), any())).thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.getAuctionsByCategory(
                    AuctionItem.Category.ART, PageRequest.of(0, 10));

            assertNotNull(result);
            verify(auctionItemRepository).findByStatusAndCategory(
                    AuctionItem.AuctionStatus.ACTIVE, AuctionItem.Category.ART, PageRequest.of(0, 10));
        }

        @Test
        @DisplayName("getSellerAuctions should delegate to repository")
        void shouldDelegateGetSellerAuctions() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.findBySellerId(eq(1L), any())).thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.getSellerAuctions(1L, PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("getFeaturedAuctions should delegate to repository")
        void shouldDelegateGetFeatured() {
            when(auctionItemRepository.findFeaturedAuctions()).thenReturn(List.of(activeAuction));

            List<AuctionItemResponse> result = auctionService.getFeaturedAuctions();
            assertEquals(1, result.size());
        }

        @Test
        @DisplayName("getEndingSoonAuctions should delegate to repository")
        void shouldDelegateGetEndingSoon() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.findEndingSoonAuctions(any())).thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.getEndingSoonAuctions(PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("getMostPopularAuctions should delegate to repository")
        void shouldDelegateGetMostPopular() {
            Page<AuctionItem> page = new PageImpl<>(List.of(activeAuction));
            when(auctionItemRepository.findMostPopularAuctions(any())).thenReturn(page);

            Page<AuctionItemResponse> result = auctionService.getMostPopularAuctions(PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }
    }
}
