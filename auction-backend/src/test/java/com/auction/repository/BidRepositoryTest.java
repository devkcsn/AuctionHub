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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("BidRepository Tests")
class BidRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private BidRepository bidRepository;

    private User seller;
    private User bidder1;
    private User bidder2;
    private AuctionItem auction;

    @BeforeEach
    void setUp() {
        seller = User.builder()
                .username("seller").email("seller@e.com").password("pass").build();
        entityManager.persistAndFlush(seller);

        bidder1 = User.builder()
                .username("bidder1").email("b1@e.com").password("pass").build();
        entityManager.persistAndFlush(bidder1);

        bidder2 = User.builder()
                .username("bidder2").email("b2@e.com").password("pass").build();
        entityManager.persistAndFlush(bidder2);

        auction = AuctionItem.builder()
                .title("Test Auction")
                .description("Test Description")
                .startingPrice(new BigDecimal("100.00"))
                .currentPrice(new BigDecimal("300.00"))
                .category(AuctionItem.Category.ELECTRONICS)
                .status(AuctionItem.AuctionStatus.ACTIVE)
                .startTime(LocalDateTime.now().minusHours(1))
                .endTime(LocalDateTime.now().plusDays(1))
                .seller(seller)
                .bidCount(3)
                .build();
        entityManager.persistAndFlush(auction);

        // Create bids: bidder1 bids 200, bidder2 bids 250, bidder1 bids 300
        Bid bid1 = Bid.builder()
                .amount(new BigDecimal("200.00"))
                .auctionItem(auction)
                .bidder(bidder1)
                .autoBid(false)
                .build();
        entityManager.persistAndFlush(bid1);

        Bid bid2 = Bid.builder()
                .amount(new BigDecimal("250.00"))
                .auctionItem(auction)
                .bidder(bidder2)
                .autoBid(false)
                .build();
        entityManager.persistAndFlush(bid2);

        Bid bid3 = Bid.builder()
                .amount(new BigDecimal("300.00"))
                .auctionItem(auction)
                .bidder(bidder1)
                .autoBid(false)
                .build();
        entityManager.persistAndFlush(bid3);
    }

    @Nested
    @DisplayName("findByAuctionItemIdOrderByAmountDesc")
    class FindByAuctionTests {

        @Test
        @DisplayName("Should find all bids for auction sorted by amount desc (pageable)")
        void shouldFindBidsPageable() {
            Page<Bid> result = bidRepository.findByAuctionItemIdOrderByAmountDesc(
                    auction.getId(), PageRequest.of(0, 10));

            assertEquals(3, result.getTotalElements());
            List<Bid> bids = result.getContent();
            // First should be highest
            assertEquals(new BigDecimal("300.00"), bids.get(0).getAmount());
            assertEquals(new BigDecimal("250.00"), bids.get(1).getAmount());
            assertEquals(new BigDecimal("200.00"), bids.get(2).getAmount());
        }

        @Test
        @DisplayName("Should find all bids for auction sorted by amount desc (list)")
        void shouldFindBidsList() {
            List<Bid> bids = bidRepository.findByAuctionItemIdOrderByAmountDesc(auction.getId());

            assertEquals(3, bids.size());
            assertEquals(new BigDecimal("300.00"), bids.get(0).getAmount());
        }

        @Test
        @DisplayName("Should return empty for non-existent auction")
        void shouldReturnEmptyForNonExistentAuction() {
            Page<Bid> result = bidRepository.findByAuctionItemIdOrderByAmountDesc(
                    9999L, PageRequest.of(0, 10));
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findByBidderIdOrderByCreatedAtDesc")
    class FindByBidderTests {

        @Test
        @DisplayName("Should find all bids by bidder1")
        void shouldFindBidsByBidder1() {
            Page<Bid> result = bidRepository.findByBidderIdOrderByCreatedAtDesc(
                    bidder1.getId(), PageRequest.of(0, 10));
            assertEquals(2, result.getTotalElements());
        }

        @Test
        @DisplayName("Should find all bids by bidder2")
        void shouldFindBidsByBidder2() {
            Page<Bid> result = bidRepository.findByBidderIdOrderByCreatedAtDesc(
                    bidder2.getId(), PageRequest.of(0, 10));
            assertEquals(1, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findHighestBid")
    class FindHighestBidTests {

        @Test
        @DisplayName("Should find highest bid for auction")
        void shouldFindHighestBid() {
            Optional<Bid> result = bidRepository.findHighestBid(auction.getId());

            assertTrue(result.isPresent());
            assertEquals(new BigDecimal("300.00"), result.get().getAmount());
            assertEquals("bidder1", result.get().getBidder().getUsername());
        }

        @Test
        @DisplayName("Should return empty when auction has no bids")
        void shouldReturnEmptyWhenNoBids() {
            AuctionItem noBidAuction = AuctionItem.builder()
                    .title("No Bid Auction")
                    .description("No bids here")
                    .startingPrice(new BigDecimal("50.00"))
                    .currentPrice(new BigDecimal("50.00"))
                    .category(AuctionItem.Category.OTHER)
                    .status(AuctionItem.AuctionStatus.ACTIVE)
                    .startTime(LocalDateTime.now().minusHours(1))
                    .endTime(LocalDateTime.now().plusDays(1))
                    .seller(seller)
                    .build();
            entityManager.persistAndFlush(noBidAuction);

            Optional<Bid> result = bidRepository.findHighestBid(noBidAuction.getId());
            assertTrue(result.isEmpty());
        }
    }

    @Nested
    @DisplayName("countDistinctBidders")
    class CountDistinctBiddersTests {

        @Test
        @DisplayName("Should count 2 distinct bidders")
        void shouldCountDistinctBidders() {
            Integer count = bidRepository.countDistinctBidders(auction.getId());
            assertEquals(2, count);
        }
    }

    @Nested
    @DisplayName("findHighestBidByUser")
    class FindHighestBidByUserTests {

        @Test
        @DisplayName("Should find highest bid by bidder1")
        void shouldFindHighestByBidder1() {
            Optional<Bid> result = bidRepository.findHighestBidByUser(
                    auction.getId(), bidder1.getId());

            assertTrue(result.isPresent());
            assertEquals(new BigDecimal("300.00"), result.get().getAmount());
        }

        @Test
        @DisplayName("Should find highest bid by bidder2")
        void shouldFindHighestByBidder2() {
            Optional<Bid> result = bidRepository.findHighestBidByUser(
                    auction.getId(), bidder2.getId());

            assertTrue(result.isPresent());
            assertEquals(new BigDecimal("250.00"), result.get().getAmount());
        }
    }

    @Nested
    @DisplayName("findDistinctBidderIds")
    class FindDistinctBidderIdsTests {

        @Test
        @DisplayName("Should return both bidder IDs")
        void shouldReturnAllDistinctBidderIds() {
            List<Long> ids = bidRepository.findDistinctBidderIds(auction.getId());

            assertEquals(2, ids.size());
            assertTrue(ids.contains(bidder1.getId()));
            assertTrue(ids.contains(bidder2.getId()));
        }
    }
}
