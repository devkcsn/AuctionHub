package com.auction.service;

import com.auction.dto.BidRequest;
import com.auction.dto.BidResponse;
import com.auction.entity.AuctionItem;
import com.auction.entity.Bid;
import com.auction.entity.User;
import com.auction.exception.AuctionException;
import com.auction.exception.BadRequestException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.repository.AuctionItemRepository;
import com.auction.repository.BidRepository;
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
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BidService Tests")
class BidServiceTest {

    @Mock private BidRepository bidRepository;
    @Mock private AuctionItemRepository auctionItemRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;
    @Mock private NotificationService notificationService;

    @InjectMocks private BidService bidService;

    private User seller;
    private User bidder;
    private AuctionItem activeAuction;
    private BidRequest validBidRequest;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L).username("seller").email("s@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();

        bidder = User.builder()
                .id(2L).username("bidder").email("b@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();

        activeAuction = AuctionItem.builder()
                .id(10L)
                .title("Active Auction")
                .description("A test auction")
                .startingPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("150.00"))
                .minBidIncrement(new BigDecimal("5.00"))
                .category(AuctionItem.Category.ELECTRONICS)
                .status(AuctionItem.AuctionStatus.ACTIVE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .seller(seller)
                .bidCount(2)
                .viewCount(50)
                .featured(false)
                .build();

        validBidRequest = new BidRequest();
        validBidRequest.setAuctionItemId(10L);
        validBidRequest.setAmount(new BigDecimal("160.00"));
        validBidRequest.setAutoBid(false);
    }

    @Nested
    @DisplayName("placeBid()")
    class PlaceBidTests {

        @Test
        @DisplayName("Should place bid successfully")
        void shouldPlaceBid() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));
            when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> {
                Bid b = inv.getArgument(0);
                b.setId(100L);
                return b;
            });
            when(auctionItemRepository.save(any(AuctionItem.class))).thenReturn(activeAuction);
            when(bidRepository.findDistinctBidderIds(10L)).thenReturn(Collections.emptyList());

            BidResponse response = bidService.placeBid(2L, validBidRequest);

            assertNotNull(response);
            assertEquals(new BigDecimal("160.00"), response.getAmount());
            assertEquals("bidder", response.getBidderUsername());
            assertEquals("Active Auction", response.getAuctionTitle());
            assertEquals(10L, response.getAuctionItemId());

            // Verify auction price and bid count updated
            assertEquals(new BigDecimal("160.00"), activeAuction.getCurrentPrice());
            assertEquals(3, activeAuction.getBidCount());

            verify(bidRepository).save(any(Bid.class));
            verify(auctionItemRepository).save(activeAuction);
            verify(messagingTemplate, times(2)).convertAndSend(anyString(), any(Object.class));
        }

        @Test
        @DisplayName("Should throw when bidder not found")
        void shouldThrowWhenBidderNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bidService.placeBid(999L, validBidRequest));
        }

        @Test
        @DisplayName("Should throw when auction not found")
        void shouldThrowWhenAuctionNotFound() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            validBidRequest.setAuctionItemId(999L);
            when(auctionItemRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> bidService.placeBid(2L, validBidRequest));
        }

        @Test
        @DisplayName("Should throw when auction is not active")
        void shouldThrowWhenAuctionNotActive() {
            activeAuction.setStatus(AuctionItem.AuctionStatus.ENDED);
            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));

            assertThrows(AuctionException.class,
                    () -> bidService.placeBid(2L, validBidRequest));
        }

        @Test
        @DisplayName("Should throw when auction has ended (time-based)")
        void shouldThrowWhenAuctionExpired() {
            activeAuction.setEndTime(LocalDateTime.now().minusMinutes(5));
            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));

            assertThrows(AuctionException.class,
                    () -> bidService.placeBid(2L, validBidRequest));
        }

        @Test
        @DisplayName("Should throw when seller bids on own auction")
        void shouldThrowWhenSellerBidsOnOwn() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(seller));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));

            assertThrows(AuctionException.class,
                    () -> bidService.placeBid(1L, validBidRequest));
        }

        @Test
        @DisplayName("Should throw when bid amount is too low")
        void shouldThrowWhenBidTooLow() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));

            validBidRequest.setAmount(new BigDecimal("152.00")); // below 150 + 5 = 155

            assertThrows(BadRequestException.class,
                    () -> bidService.placeBid(2L, validBidRequest));
        }

        @Test
        @DisplayName("Should accept first bid equal to starting price when no bids exist")
        void shouldAcceptFirstBidAtStartingPrice() {
            activeAuction.setBidCount(0);
            activeAuction.setCurrentPrice(new BigDecimal("100.00"));

            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));
            when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> {
                Bid b = inv.getArgument(0);
                b.setId(101L);
                return b;
            });
            when(auctionItemRepository.save(any())).thenReturn(activeAuction);
            when(bidRepository.findDistinctBidderIds(10L)).thenReturn(Collections.emptyList());

            validBidRequest.setAmount(new BigDecimal("100.00")); // equal to starting price

            BidResponse response = bidService.placeBid(2L, validBidRequest);
            assertNotNull(response);
        }

        @Test
        @DisplayName("Should notify outbid users")
        void shouldNotifyOutbidUsers() {
            when(userRepository.findById(2L)).thenReturn(Optional.of(bidder));
            when(auctionItemRepository.findById(10L)).thenReturn(Optional.of(activeAuction));
            when(bidRepository.save(any(Bid.class))).thenAnswer(inv -> {
                Bid b = inv.getArgument(0);
                b.setId(102L);
                return b;
            });
            when(auctionItemRepository.save(any())).thenReturn(activeAuction);
            when(bidRepository.findDistinctBidderIds(10L)).thenReturn(List.of(3L, 4L));

            bidService.placeBid(2L, validBidRequest);

            // Should notify bidder 3 and 4 (not bidder 2 who placed the bid)
            verify(notificationService, times(2)).createNotification(
                    anyLong(), anyString(), anyString(), any(), eq(10L));
        }
    }

    @Nested
    @DisplayName("getAuctionBids()")
    class GetAuctionBidsTests {

        @Test
        @DisplayName("Should return page of bids for auction")
        void shouldReturnBids() {
            Bid bid = Bid.builder()
                    .id(1L).amount(new BigDecimal("160.00"))
                    .auctionItem(activeAuction).bidder(bidder)
                    .autoBid(false).build();

            when(auctionItemRepository.existsById(10L)).thenReturn(true);
            when(bidRepository.findByAuctionItemIdOrderByAmountDesc(eq(10L), any()))
                    .thenReturn(new PageImpl<>(List.of(bid)));

            Page<BidResponse> result = bidService.getAuctionBids(10L, PageRequest.of(0, 20));

            assertEquals(1, result.getTotalElements());
            assertEquals(new BigDecimal("160.00"), result.getContent().get(0).getAmount());
        }

        @Test
        @DisplayName("Should throw when auction does not exist")
        void shouldThrowWhenAuctionNotExists() {
            when(auctionItemRepository.existsById(999L)).thenReturn(false);

            assertThrows(ResourceNotFoundException.class,
                    () -> bidService.getAuctionBids(999L, PageRequest.of(0, 20)));
        }
    }

    @Nested
    @DisplayName("getUserBids()")
    class GetUserBidsTests {

        @Test
        @DisplayName("Should return page of user bids")
        void shouldReturnUserBids() {
            Bid bid = Bid.builder()
                    .id(1L).amount(new BigDecimal("160.00"))
                    .auctionItem(activeAuction).bidder(bidder)
                    .autoBid(false).build();

            when(bidRepository.findByBidderIdOrderByCreatedAtDesc(eq(2L), any()))
                    .thenReturn(new PageImpl<>(List.of(bid)));

            Page<BidResponse> result = bidService.getUserBids(2L, PageRequest.of(0, 20));
            assertEquals(1, result.getTotalElements());
        }
    }
}
