package com.auction.repository;

import com.auction.entity.AuctionItem;
import com.auction.entity.Bid;
import com.auction.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AuctionItemRepository Tests")
class AuctionItemRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private AuctionItemRepository auctionItemRepository;

    private User seller;
    private User bidder;
    private AuctionItem activeAuction;
    private AuctionItem pendingAuction;
    private AuctionItem endedAuction;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .username("seller1")
                .email("seller@example.com")
                .password("password")
                .fullName("Seller One")
                .build();
        entityManager.persistAndFlush(seller);

        bidder = User.builder()
                .username("bidder1")
                .email("bidder@example.com")
                .password("password")
                .fullName("Bidder One")
                .build();
        entityManager.persistAndFlush(bidder);

        activeAuction = AuctionItem.builder()
                .title("Active Electronics Auction")
                .description("A brand new laptop for auction")
                .startingPrice(new BigDecimal("500.00"))
                .currentPrice(new BigDecimal("750.00"))
                .category(AuctionItem.Category.ELECTRONICS)
                .status(AuctionItem.AuctionStatus.ACTIVE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusDays(2))
                .seller(seller)
                .bidCount(5)
                .viewCount(100)
                .featured(true)
                .build();
        entityManager.persistAndFlush(activeAuction);

        pendingAuction = AuctionItem.builder()
                .title("Pending Art Piece")
                .description("An original painting")
                .startingPrice(new BigDecimal("1000.00"))
                .currentPrice(new BigDecimal("1000.00"))
                .category(AuctionItem.Category.ART)
                .status(AuctionItem.AuctionStatus.PENDING)
                .startTime(LocalDateTime.now().plusDays(1))
                .endTime(LocalDateTime.now().plusDays(5))
                .seller(seller)
                .build();
        entityManager.persistAndFlush(pendingAuction);

        endedAuction = AuctionItem.builder()
                .title("Ended Jewelry Auction")
                .description("Diamond ring")
                .startingPrice(new BigDecimal("2000.00"))
                .currentPrice(new BigDecimal("3500.00"))
                .category(AuctionItem.Category.JEWELRY)
                .status(AuctionItem.AuctionStatus.ENDED)
                .startTime(LocalDateTime.now().minusDays(7))
                .endTime(LocalDateTime.now().minusDays(1))
                .seller(seller)
                .winner(bidder)
                .bidCount(12)
                .build();
        entityManager.persistAndFlush(endedAuction);
    }

    @Nested
    @DisplayName("findByStatus")
    class FindByStatusTests {

        @Test
        @DisplayName("Should find all active auctions")
        void shouldFindActiveAuctions() {
            Page<AuctionItem> result = auctionItemRepository.findByStatus(
                    AuctionItem.AuctionStatus.ACTIVE, PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
            assertEquals("Active Electronics Auction", result.getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Should find pending auctions")
        void shouldFindPendingAuctions() {
            Page<AuctionItem> result = auctionItemRepository.findByStatus(
                    AuctionItem.AuctionStatus.PENDING, PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty page for cancelled status")
        void shouldReturnEmptyForCancelled() {
            Page<AuctionItem> result = auctionItemRepository.findByStatus(
                    AuctionItem.AuctionStatus.CANCELLED, PageRequest.of(0, 10));
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findByCategory")
    class FindByCategoryTests {

        @Test
        @DisplayName("Should find auctions by category")
        void shouldFindByCategory() {
            Page<AuctionItem> result = auctionItemRepository.findByCategory(
                    AuctionItem.Category.ELECTRONICS, PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
            assertEquals("Active Electronics Auction", result.getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Should return empty page for category with no auctions")
        void shouldReturnEmptyForEmptyCategory() {
            Page<AuctionItem> result = auctionItemRepository.findByCategory(
                    AuctionItem.Category.VEHICLES, PageRequest.of(0, 10));
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findByStatusAndCategory")
    class FindByStatusAndCategoryTests {

        @Test
        @DisplayName("Should find active electronics auctions")
        void shouldFindActiveByCategory() {
            Page<AuctionItem> result = auctionItemRepository.findByStatusAndCategory(
                    AuctionItem.AuctionStatus.ACTIVE, AuctionItem.Category.ELECTRONICS,
                    PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty when status doesn't match category")
        void shouldReturnEmptyWhenStatusMismatch() {
            Page<AuctionItem> result = auctionItemRepository.findByStatusAndCategory(
                    AuctionItem.AuctionStatus.ACTIVE, AuctionItem.Category.ART,
                    PageRequest.of(0, 10));
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findBySellerId")
    class FindBySellerIdTests {

        @Test
        @DisplayName("Should find all auctions by seller")
        void shouldFindBySeller() {
            Page<AuctionItem> result = auctionItemRepository.findBySellerId(
                    seller.getId(), PageRequest.of(0, 10));
            assertEquals(3, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty for non-existent seller")
        void shouldReturnEmptyForNonExistentSeller() {
            Page<AuctionItem> result = auctionItemRepository.findBySellerId(
                    9999L, PageRequest.of(0, 10));
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Custom Queries")
    class CustomQueryTests {

        @Test
        @DisplayName("Should find expired auctions")
        void shouldFindExpiredAuctions() {
            // Create an auction that should be expired (ACTIVE but endTime in the past)
            AuctionItem expired = AuctionItem.builder()
                    .title("Expired Auction")
                    .description("Should be ended")
                    .startingPrice(new BigDecimal("10.00"))
                    .currentPrice(new BigDecimal("10.00"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.ACTIVE)
                    .startTime(LocalDateTime.now().minusDays(3))
                    .endTime(LocalDateTime.now().minusMinutes(5))
                    .seller(seller)
                    .build();
            entityManager.persistAndFlush(expired);

            List<AuctionItem> result = auctionItemRepository.findExpiredAuctions(
                    AuctionItem.AuctionStatus.ACTIVE, LocalDateTime.now());

            assertFalse(result.isEmpty());
            assertTrue(result.stream().anyMatch(a -> a.getTitle().equals("Expired Auction")));
        }

        @Test
        @DisplayName("Should find auctions to start")
        void shouldFindAuctionsToStart() {
            // The pendingAuction starts in the future, so it shouldn't appear
            List<AuctionItem> result = auctionItemRepository.findAuctionsToStart(
                    AuctionItem.AuctionStatus.PENDING, LocalDateTime.now());
            assertTrue(result.isEmpty());

            // Create a pending auction whose startTime is in the past
            AuctionItem readyToStart = AuctionItem.builder()
                    .title("Ready to Start")
                    .description("Should be started")
                    .startingPrice(new BigDecimal("10.00"))
                    .currentPrice(new BigDecimal("10.00"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.PENDING)
                    .startTime(LocalDateTime.now().minusMinutes(5))
                    .endTime(LocalDateTime.now().plusDays(1))
                    .seller(seller)
                    .build();
            entityManager.persistAndFlush(readyToStart);

            result = auctionItemRepository.findAuctionsToStart(
                    AuctionItem.AuctionStatus.PENDING, LocalDateTime.now());
            assertEquals(1, result.size());
            assertEquals("Ready to Start", result.get(0).getTitle());
        }

        @Test
        @DisplayName("Should search active auctions by keyword")
        void shouldSearchActiveAuctions() {
            Page<AuctionItem> result = auctionItemRepository.searchActiveAuctions(
                    "laptop", PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
            assertEquals("Active Electronics Auction", result.getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Should search by title (case insensitive)")
        void shouldSearchCaseInsensitive() {
            Page<AuctionItem> result = auctionItemRepository.searchActiveAuctions(
                    "ELECTRONICS", PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty for non-matching search keyword")
        void shouldReturnEmptyForNonMatchingSearch() {
            Page<AuctionItem> result = auctionItemRepository.searchActiveAuctions(
                    "nonexistentxyz", PageRequest.of(0, 10));
            assertEquals(0, result.getTotalElements());
        }

        @Test
        @DisplayName("Should find featured auctions")
        void shouldFindFeaturedAuctions() {
            List<AuctionItem> result = auctionItemRepository.findFeaturedAuctions();
            assertEquals(1, result.size());
            assertTrue(result.get(0).getFeatured());
        }

        @Test
        @DisplayName("Should find most popular auctions ordered by bid count")
        void shouldFindMostPopular() {
            Page<AuctionItem> result = auctionItemRepository.findMostPopularAuctions(
                    PageRequest.of(0, 10));
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should find ending soon auctions ordered by end time")
        void shouldFindEndingSoon() {
            Page<AuctionItem> result = auctionItemRepository.findEndingSoonAuctions(
                    PageRequest.of(0, 10));
            assertFalse(result.isEmpty());
        }

        @Test
        @DisplayName("Should find won auctions for user")
        void shouldFindWonAuctions() {
            Page<AuctionItem> result = auctionItemRepository.findWonAuctions(
                    bidder.getId(), PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
            assertEquals("Ended Jewelry Auction", result.getContent().get(0).getTitle());
        }

        @Test
        @DisplayName("Should find auctions by bidder")
        void shouldFindAuctionsByBidder() {
            // Place a bid first
            Bid bid = Bid.builder()
                    .amount(new BigDecimal("800.00"))
                    .auctionItem(activeAuction)
                    .bidder(bidder)
                    .autoBid(false)
                    .build();
            entityManager.persistAndFlush(bid);

            Page<AuctionItem> result = auctionItemRepository.findAuctionsByBidder(
                    bidder.getId(), PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("Pagination")
    class PaginationTests {

        @Test
        @DisplayName("Should paginate results correctly")
        void shouldPaginateCorrectly() {
            // Add more active auctions
            for (int i = 0; i < 5; i++) {
                AuctionItem item = AuctionItem.builder()
                        .title("Bulk Auction " + i)
                        .description("Bulk description " + i)
                        .startingPrice(new BigDecimal("10.00"))
                        .currentPrice(new BigDecimal("10.00"))
                        .category(AuctionItem.Category.OTHER)
                        .status(AuctionItem.AuctionStatus.ACTIVE)
                        .startTime(LocalDateTime.now().minusHours(1))
                        .endTime(LocalDateTime.now().plusDays(1))
                        .seller(seller)
                        .build();
                entityManager.persist(item);
            }
            entityManager.flush();

            // Page 0, size 3 — should return 3 of 6 active auctions
            Page<AuctionItem> page0 = auctionItemRepository.findByStatus(
                    AuctionItem.AuctionStatus.ACTIVE, PageRequest.of(0, 3));
            assertEquals(3, page0.getContent().size());
            assertEquals(6, page0.getTotalElements());
            assertEquals(2, page0.getTotalPages());

            // Page 1, size 3
            Page<AuctionItem> page1 = auctionItemRepository.findByStatus(
                    AuctionItem.AuctionStatus.ACTIVE, PageRequest.of(1, 3));
            assertEquals(3, page1.getContent().size());
        }
    }
}
