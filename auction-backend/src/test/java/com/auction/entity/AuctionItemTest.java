package com.auction.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("AuctionItem Entity Tests")
class AuctionItemTest {

    @Nested
    @DisplayName("Builder & Defaults")
    class BuilderTests {

        @Test
        @DisplayName("Should build auction item with all fields")
        void shouldBuildAuctionItemWithAllFields() {
            User seller = User.builder().id(1L).username("seller").email("s@e.com").password("p").build();
            LocalDateTime start = LocalDateTime.now().plusHours(1);
            LocalDateTime end = start.plusDays(3);

            AuctionItem item = AuctionItem.builder()
                    .id(100L)
                    .title("Vintage Watch")
                    .description("A rare vintage watch from 1960.")
                    .startingPrice(new BigDecimal("250.00"))
                    .reservePrice(new BigDecimal("500.00"))
                    .currentPrice(new BigDecimal("250.00"))
                    .minBidIncrement(new BigDecimal("5.00"))
                    .status(AuctionItem.AuctionStatus.PENDING)
                    .category(AuctionItem.Category.JEWELRY)
                    .startTime(start)
                    .endTime(end)
                    .seller(seller)
                    .featured(true)
                    .build();

            assertEquals(100L, item.getId());
            assertEquals("Vintage Watch", item.getTitle());
            assertEquals("A rare vintage watch from 1960.", item.getDescription());
            assertEquals(new BigDecimal("250.00"), item.getStartingPrice());
            assertEquals(new BigDecimal("500.00"), item.getReservePrice());
            assertEquals(new BigDecimal("250.00"), item.getCurrentPrice());
            assertEquals(new BigDecimal("5.00"), item.getMinBidIncrement());
            assertEquals(AuctionItem.AuctionStatus.PENDING, item.getStatus());
            assertEquals(AuctionItem.Category.JEWELRY, item.getCategory());
            assertEquals(start, item.getStartTime());
            assertEquals(end, item.getEndTime());
            assertEquals(seller, item.getSeller());
            assertTrue(item.getFeatured());
        }

        @Test
        @DisplayName("Should set correct default values")
        void shouldSetDefaultValues() {
            AuctionItem item = AuctionItem.builder()
                    .title("Test Item")
                    .description("Desc")
                    .startingPrice(new BigDecimal("10.00"))
                    .currentPrice(new BigDecimal("10.00"))
                    .category(AuctionItem.Category.OTHER)
                    .startTime(LocalDateTime.now())
                    .endTime(LocalDateTime.now().plusHours(2))
                    .build();

            assertEquals(AuctionItem.AuctionStatus.PENDING, item.getStatus());
            assertEquals(new BigDecimal("1.00"), item.getMinBidIncrement());
            assertEquals(0, item.getBidCount());
            assertEquals(0, item.getViewCount());
            assertFalse(item.getFeatured());
            assertNotNull(item.getImageUrls());
            assertTrue(item.getImageUrls().isEmpty());
            assertNotNull(item.getBids());
            assertTrue(item.getBids().isEmpty());
        }
    }

    @Nested
    @DisplayName("Getters & Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("Should update all mutable fields")
        void shouldUpdateAllFields() {
            AuctionItem item = new AuctionItem();

            item.setTitle("Updated Title");
            item.setDescription("Updated Description");
            item.setCurrentPrice(new BigDecimal("999.99"));
            item.setBidCount(15);
            item.setViewCount(100);
            item.setStatus(AuctionItem.AuctionStatus.SOLD);
            item.setFeatured(true);

            List<String> images = new ArrayList<>(List.of("img1.jpg", "img2.jpg"));
            item.setImageUrls(images);

            assertEquals("Updated Title", item.getTitle());
            assertEquals("Updated Description", item.getDescription());
            assertEquals(new BigDecimal("999.99"), item.getCurrentPrice());
            assertEquals(15, item.getBidCount());
            assertEquals(100, item.getViewCount());
            assertEquals(AuctionItem.AuctionStatus.SOLD, item.getStatus());
            assertTrue(item.getFeatured());
            assertEquals(2, item.getImageUrls().size());
        }

        @Test
        @DisplayName("Should set winner")
        void shouldSetWinner() {
            AuctionItem item = new AuctionItem();
            User winner = User.builder().id(2L).username("winner").email("w@e.com").password("p").build();

            assertNull(item.getWinner());
            item.setWinner(winner);
            assertEquals(winner, item.getWinner());
            assertEquals(2L, item.getWinner().getId());
        }
    }

    @Nested
    @DisplayName("Enums")
    class EnumTests {

        @Test
        @DisplayName("AuctionStatus should have all expected values")
        void auctionStatusValues() {
            AuctionItem.AuctionStatus[] statuses = AuctionItem.AuctionStatus.values();
            assertEquals(5, statuses.length);

            assertNotNull(AuctionItem.AuctionStatus.valueOf("PENDING"));
            assertNotNull(AuctionItem.AuctionStatus.valueOf("ACTIVE"));
            assertNotNull(AuctionItem.AuctionStatus.valueOf("ENDED"));
            assertNotNull(AuctionItem.AuctionStatus.valueOf("CANCELLED"));
            assertNotNull(AuctionItem.AuctionStatus.valueOf("SOLD"));
        }

        @Test
        @DisplayName("Category should have all expected values")
        void categoryValues() {
            AuctionItem.Category[] cats = AuctionItem.Category.values();
            assertEquals(12, cats.length);

            assertNotNull(AuctionItem.Category.valueOf("ELECTRONICS"));
            assertNotNull(AuctionItem.Category.valueOf("FASHION"));
            assertNotNull(AuctionItem.Category.valueOf("HOME_GARDEN"));
            assertNotNull(AuctionItem.Category.valueOf("SPORTS"));
            assertNotNull(AuctionItem.Category.valueOf("COLLECTIBLES"));
            assertNotNull(AuctionItem.Category.valueOf("ART"));
            assertNotNull(AuctionItem.Category.valueOf("VEHICLES"));
            assertNotNull(AuctionItem.Category.valueOf("JEWELRY"));
            assertNotNull(AuctionItem.Category.valueOf("BOOKS"));
            assertNotNull(AuctionItem.Category.valueOf("TOYS"));
            assertNotNull(AuctionItem.Category.valueOf("MUSIC"));
            assertNotNull(AuctionItem.Category.valueOf("OTHER"));
        }
    }
}
