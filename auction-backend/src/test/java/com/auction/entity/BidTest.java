package com.auction.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Bid Entity Tests")
class BidTest {

    @Nested
    @DisplayName("Builder & Defaults")
    class BuilderTests {

        @Test
        @DisplayName("Should build bid with all fields")
        void shouldBuildBidWithAllFields() {
            User bidder = User.builder().id(1L).username("bidder").email("b@e.com").password("p").build();
            AuctionItem auction = AuctionItem.builder()
                    .id(10L).title("Item").description("Desc")
                    .startingPrice(new BigDecimal("100.00"))
                    .currentPrice(new BigDecimal("100.00"))
                    .category(AuctionItem.Category.ART)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now().plusDays(1))
                    .build();

            Bid bid = Bid.builder()
                    .id(1L)
                    .amount(new BigDecimal("150.00"))
                    .auctionItem(auction)
                    .bidder(bidder)
                    .autoBid(true)
                    .maxAutoBidAmount(new BigDecimal("300.00"))
                    .build();

            assertEquals(1L, bid.getId());
            assertEquals(new BigDecimal("150.00"), bid.getAmount());
            assertEquals(auction, bid.getAuctionItem());
            assertEquals(bidder, bid.getBidder());
            assertTrue(bid.getAutoBid());
            assertEquals(new BigDecimal("300.00"), bid.getMaxAutoBidAmount());
        }

        @Test
        @DisplayName("Should default autoBid to false")
        void shouldDefaultAutoBidToFalse() {
            Bid bid = Bid.builder()
                    .amount(new BigDecimal("100.00"))
                    .build();

            assertFalse(bid.getAutoBid(), "AutoBid should default to false");
        }
    }

    @Nested
    @DisplayName("Getters & Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("Should get and set all fields")
        void shouldGetAndSetFields() {
            Bid bid = new Bid();

            bid.setId(99L);
            bid.setAmount(new BigDecimal("500.00"));
            bid.setAutoBid(true);
            bid.setMaxAutoBidAmount(new BigDecimal("1000.00"));

            assertEquals(99L, bid.getId());
            assertEquals(new BigDecimal("500.00"), bid.getAmount());
            assertTrue(bid.getAutoBid());
            assertEquals(new BigDecimal("1000.00"), bid.getMaxAutoBidAmount());
        }

        @Test
        @DisplayName("Should set bidder and auction relationships")
        void shouldSetRelationships() {
            Bid bid = new Bid();
            User bidder = User.builder().id(5L).username("user5").email("u5@e.com").password("p").build();
            AuctionItem auction = new AuctionItem();
            auction.setId(20L);

            bid.setBidder(bidder);
            bid.setAuctionItem(auction);

            assertEquals(5L, bid.getBidder().getId());
            assertEquals(20L, bid.getAuctionItem().getId());
        }
    }
}
