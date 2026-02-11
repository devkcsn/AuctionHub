package com.auction.service;

import com.auction.entity.AuctionItem;
import com.auction.entity.Bid;
import com.auction.entity.Notification;
import com.auction.entity.User;
import com.auction.repository.AuctionItemRepository;
import com.auction.repository.BidRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
@DisplayName("AuctionSchedulerService Tests")
class AuctionSchedulerServiceTest {

    @Mock private AuctionItemRepository auctionItemRepository;
    @Mock private BidRepository bidRepository;
    @Mock private NotificationService notificationService;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private AuctionSchedulerService schedulerService;

    private User seller;
    private User winner;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .id(1L).username("seller").email("s@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();

        winner = User.builder()
                .id(2L).username("winner").email("w@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();
    }

    @Nested
    @DisplayName("startPendingAuctions()")
    class StartPendingAuctionsTests {

        @Test
        @DisplayName("Should start pending auctions whose start time has passed")
        void shouldStartPendingAuctions() {
            AuctionItem pending = AuctionItem.builder()
                    .id(10L).title("Ready to Start").description("d")
                    .startingPrice(new BigDecimal("100")).currentPrice(new BigDecimal("100"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.PENDING)
                    .startTime(LocalDateTime.now().minusMinutes(5))
                    .endTime(LocalDateTime.now().plusDays(1))
                    .seller(seller).bidCount(0).viewCount(0).featured(false)
                    .build();

            when(auctionItemRepository.findAuctionsToStart(
                    eq(AuctionItem.AuctionStatus.PENDING), any(LocalDateTime.class)))
                    .thenReturn(List.of(pending));
            when(auctionItemRepository.save(any())).thenReturn(pending);

            schedulerService.startPendingAuctions();

            assertEquals(AuctionItem.AuctionStatus.ACTIVE, pending.getStatus());
            verify(auctionItemRepository).save(pending);
            verify(messagingTemplate).convertAndSend(eq("/topic/auctions"), any(Object.class));
        }

        @Test
        @DisplayName("Should do nothing when no pending auctions to start")
        void shouldDoNothingWhenNoPending() {
            when(auctionItemRepository.findAuctionsToStart(any(), any()))
                    .thenReturn(Collections.emptyList());

            schedulerService.startPendingAuctions();

            verify(auctionItemRepository, never()).save(any());
            verify(messagingTemplate, never()).convertAndSend(anyString(), any(Object.class));
        }
    }

    @Nested
    @DisplayName("endExpiredAuctions()")
    class EndExpiredAuctionsTests {

        @Test
        @DisplayName("Should end auction and set SOLD status when highest bid meets reserve")
        void shouldEndAndSoldWhenReserveMet() {
            AuctionItem expired = AuctionItem.builder()
                    .id(20L).title("Expired Auction").description("d")
                    .startingPrice(new BigDecimal("100")).currentPrice(new BigDecimal("500"))
                    .reservePrice(new BigDecimal("200"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.ACTIVE)
                    .startTime(LocalDateTime.now().minusDays(3))
                    .endTime(LocalDateTime.now().minusMinutes(5))
                    .seller(seller).bidCount(5).viewCount(10).featured(false)
                    .build();

            Bid highBid = Bid.builder()
                    .id(1L).amount(new BigDecimal("500.00"))
                    .auctionItem(expired).bidder(winner).autoBid(false).build();

            when(auctionItemRepository.findExpiredAuctions(
                    eq(AuctionItem.AuctionStatus.ACTIVE), any(LocalDateTime.class)))
                    .thenReturn(List.of(expired));
            when(bidRepository.findHighestBid(20L)).thenReturn(Optional.of(highBid));
            when(auctionItemRepository.save(any())).thenReturn(expired);
            when(bidRepository.findDistinctBidderIds(20L)).thenReturn(List.of(2L, 3L));

            schedulerService.endExpiredAuctions();

            assertEquals(AuctionItem.AuctionStatus.SOLD, expired.getStatus());
            assertEquals(winner, expired.getWinner());
            verify(auctionItemRepository).save(expired);

            // Should notify winner (AUCTION_WON)
            verify(notificationService).createNotification(
                    eq(2L), anyString(), anyString(),
                    eq(Notification.NotificationType.AUCTION_WON), eq(20L));

            // Should notify seller (PAYMENT_RECEIVED)
            verify(notificationService).createNotification(
                    eq(1L), anyString(), anyString(),
                    eq(Notification.NotificationType.PAYMENT_RECEIVED), eq(20L));

            // Should notify loser bidder 3 (AUCTION_LOST)
            verify(notificationService).createNotification(
                    eq(3L), anyString(), anyString(),
                    eq(Notification.NotificationType.AUCTION_LOST), eq(20L));

            // WebSocket broadcast
            verify(messagingTemplate).convertAndSend(eq("/topic/auction/20"), any(Object.class));
            verify(messagingTemplate).convertAndSend(eq("/topic/auctions"), any(Object.class));
        }

        @Test
        @DisplayName("Should end auction with ENDED status when reserve not met")
        void shouldEndWithEndedStatusWhenReserveNotMet() {
            AuctionItem expired = AuctionItem.builder()
                    .id(30L).title("Reserve Not Met").description("d")
                    .startingPrice(new BigDecimal("100")).currentPrice(new BigDecimal("150"))
                    .reservePrice(new BigDecimal("500")) // Reserve is 500, highest bid is 150
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.ACTIVE)
                    .startTime(LocalDateTime.now().minusDays(3))
                    .endTime(LocalDateTime.now().minusMinutes(5))
                    .seller(seller).bidCount(2).viewCount(5).featured(false)
                    .build();

            Bid lowBid = Bid.builder()
                    .id(2L).amount(new BigDecimal("150.00"))
                    .auctionItem(expired).bidder(winner).autoBid(false).build();

            when(auctionItemRepository.findExpiredAuctions(any(), any()))
                    .thenReturn(List.of(expired));
            when(bidRepository.findHighestBid(30L)).thenReturn(Optional.of(lowBid));
            when(auctionItemRepository.save(any())).thenReturn(expired);
            when(bidRepository.findDistinctBidderIds(30L)).thenReturn(List.of(2L));

            schedulerService.endExpiredAuctions();

            assertEquals(AuctionItem.AuctionStatus.ENDED, expired.getStatus());
        }

        @Test
        @DisplayName("Should end auction with ENDED status when no bids")
        void shouldEndWithNoBids() {
            AuctionItem expired = AuctionItem.builder()
                    .id(40L).title("No Bids").description("d")
                    .startingPrice(new BigDecimal("100")).currentPrice(new BigDecimal("100"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.ACTIVE)
                    .startTime(LocalDateTime.now().minusDays(3))
                    .endTime(LocalDateTime.now().minusMinutes(5))
                    .seller(seller).bidCount(0).viewCount(0).featured(false)
                    .build();

            when(auctionItemRepository.findExpiredAuctions(any(), any()))
                    .thenReturn(List.of(expired));
            when(bidRepository.findHighestBid(40L)).thenReturn(Optional.empty());
            when(auctionItemRepository.save(any())).thenReturn(expired);

            schedulerService.endExpiredAuctions();

            assertEquals(AuctionItem.AuctionStatus.ENDED, expired.getStatus());
            assertNull(expired.getWinner());
        }

        @Test
        @DisplayName("Should do nothing when no expired auctions")
        void shouldDoNothingWhenNoExpired() {
            when(auctionItemRepository.findExpiredAuctions(any(), any()))
                    .thenReturn(Collections.emptyList());

            schedulerService.endExpiredAuctions();

            verify(auctionItemRepository, never()).save(any());
        }
    }
}
