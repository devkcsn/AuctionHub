package com.auction.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Notification Entity Tests")
class NotificationTest {

    @Nested
    @DisplayName("Builder & Defaults")
    class BuilderTests {

        @Test
        @DisplayName("Should build notification with all fields")
        void shouldBuildWithAllFields() {
            User user = User.builder().id(1L).username("user1").email("u1@e.com").password("p").build();

            Notification notif = Notification.builder()
                    .id(1L)
                    .user(user)
                    .title("You've been outbid!")
                    .message("Someone bid $200 on \"Vintage Watch\".")
                    .type(Notification.NotificationType.OUTBID)
                    .auctionItemId(10L)
                    .read(false)
                    .build();

            assertEquals(1L, notif.getId());
            assertEquals(user, notif.getUser());
            assertEquals("You've been outbid!", notif.getTitle());
            assertEquals("Someone bid $200 on \"Vintage Watch\".", notif.getMessage());
            assertEquals(Notification.NotificationType.OUTBID, notif.getType());
            assertEquals(10L, notif.getAuctionItemId());
            assertFalse(notif.getRead());
        }

        @Test
        @DisplayName("Should default read to false")
        void shouldDefaultReadToFalse() {
            Notification notif = Notification.builder()
                    .title("Test")
                    .message("Test message")
                    .type(Notification.NotificationType.SYSTEM)
                    .build();

            assertFalse(notif.getRead(), "Read should default to false");
        }
    }

    @Nested
    @DisplayName("Getters & Setters")
    class GetterSetterTests {

        @Test
        @DisplayName("Should mark notification as read")
        void shouldMarkAsRead() {
            Notification notif = Notification.builder()
                    .title("Test")
                    .message("msg")
                    .type(Notification.NotificationType.BID_PLACED)
                    .build();

            assertFalse(notif.getRead());
            notif.setRead(true);
            assertTrue(notif.getRead());
        }

        @Test
        @DisplayName("Should set and get all fields")
        void shouldSetAndGetFields() {
            Notification notif = new Notification();

            notif.setId(42L);
            notif.setTitle("Auction Won");
            notif.setMessage("Congratulations!");
            notif.setType(Notification.NotificationType.AUCTION_WON);
            notif.setAuctionItemId(99L);
            notif.setRead(true);

            assertEquals(42L, notif.getId());
            assertEquals("Auction Won", notif.getTitle());
            assertEquals("Congratulations!", notif.getMessage());
            assertEquals(Notification.NotificationType.AUCTION_WON, notif.getType());
            assertEquals(99L, notif.getAuctionItemId());
            assertTrue(notif.getRead());
        }
    }

    @Nested
    @DisplayName("NotificationType Enum")
    class NotificationTypeTests {

        @Test
        @DisplayName("Should have all expected notification types")
        void shouldHaveAllTypes() {
            Notification.NotificationType[] types = Notification.NotificationType.values();
            assertEquals(9, types.length);

            assertNotNull(Notification.NotificationType.valueOf("BID_PLACED"));
            assertNotNull(Notification.NotificationType.valueOf("OUTBID"));
            assertNotNull(Notification.NotificationType.valueOf("AUCTION_WON"));
            assertNotNull(Notification.NotificationType.valueOf("AUCTION_LOST"));
            assertNotNull(Notification.NotificationType.valueOf("AUCTION_STARTING"));
            assertNotNull(Notification.NotificationType.valueOf("AUCTION_ENDING"));
            assertNotNull(Notification.NotificationType.valueOf("AUCTION_CANCELLED"));
            assertNotNull(Notification.NotificationType.valueOf("PAYMENT_RECEIVED"));
            assertNotNull(Notification.NotificationType.valueOf("SYSTEM"));
        }
    }
}
