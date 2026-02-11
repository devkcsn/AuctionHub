package com.auction.repository;

import com.auction.entity.Notification;
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

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("NotificationRepository Tests")
class NotificationRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private NotificationRepository notificationRepository;

    private User user;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .username("notifuser")
                .email("notif@example.com")
                .password("password")
                .build();
        entityManager.persistAndFlush(user);

        // Create 3 unread and 2 read notifications
        for (int i = 0; i < 3; i++) {
            Notification notif = Notification.builder()
                    .user(user)
                    .title("Unread Notification " + i)
                    .message("Unread message " + i)
                    .type(Notification.NotificationType.OUTBID)
                    .auctionItemId((long) (i + 1))
                    .read(false)
                    .build();
            entityManager.persist(notif);
        }

        for (int i = 0; i < 2; i++) {
            Notification notif = Notification.builder()
                    .user(user)
                    .title("Read Notification " + i)
                    .message("Read message " + i)
                    .type(Notification.NotificationType.BID_PLACED)
                    .auctionItemId((long) (i + 10))
                    .read(true)
                    .build();
            entityManager.persist(notif);
        }
        entityManager.flush();
    }

    @Nested
    @DisplayName("findByUserIdOrderByCreatedAtDesc")
    class FindByUserIdTests {

        @Test
        @DisplayName("Should find all notifications for user")
        void shouldFindAllNotifications() {
            Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    user.getId(), PageRequest.of(0, 20));
            assertEquals(5, result.getTotalElements());
        }

        @Test
        @DisplayName("Should return empty for non-existent user")
        void shouldReturnEmptyForNonExistentUser() {
            Page<Notification> result = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    9999L, PageRequest.of(0, 20));
            assertEquals(0, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("findByUserIdAndReadOrderByCreatedAtDesc")
    class FindByUserIdAndReadTests {

        @Test
        @DisplayName("Should find only unread notifications")
        void shouldFindUnreadNotifications() {
            Page<Notification> result = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(
                    user.getId(), false, PageRequest.of(0, 20));
            assertEquals(3, result.getTotalElements());
            result.getContent().forEach(n -> assertFalse(n.getRead()));
        }

        @Test
        @DisplayName("Should find only read notifications")
        void shouldFindReadNotifications() {
            Page<Notification> result = notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(
                    user.getId(), true, PageRequest.of(0, 20));
            assertEquals(2, result.getTotalElements());
            result.getContent().forEach(n -> assertTrue(n.getRead()));
        }
    }

    @Nested
    @DisplayName("countByUserIdAndRead")
    class CountTests {

        @Test
        @DisplayName("Should count 3 unread notifications")
        void shouldCountUnread() {
            Long count = notificationRepository.countByUserIdAndRead(user.getId(), false);
            assertEquals(3, count);
        }

        @Test
        @DisplayName("Should count 2 read notifications")
        void shouldCountRead() {
            Long count = notificationRepository.countByUserIdAndRead(user.getId(), true);
            assertEquals(2, count);
        }

        @Test
        @DisplayName("Should count 0 for non-existent user")
        void shouldCountZeroForNonExistentUser() {
            Long count = notificationRepository.countByUserIdAndRead(9999L, false);
            assertEquals(0, count);
        }
    }

    @Nested
    @DisplayName("markAllAsRead")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all unread notifications as read")
        void shouldMarkAllAsRead() {
            // Verify 3 unread before
            assertEquals(3, notificationRepository.countByUserIdAndRead(user.getId(), false));

            notificationRepository.markAllAsRead(user.getId());
            entityManager.flush();
            entityManager.clear();

            // Verify 0 unread after
            assertEquals(0, notificationRepository.countByUserIdAndRead(user.getId(), false));
            // All 5 should now be read
            assertEquals(5, notificationRepository.countByUserIdAndRead(user.getId(), true));
        }

        @Test
        @DisplayName("Should not affect other users' notifications")
        void shouldNotAffectOtherUsers() {
            User otherUser = User.builder()
                    .username("otheruser")
                    .email("other@example.com")
                    .password("pass")
                    .build();
            entityManager.persistAndFlush(otherUser);

            Notification otherNotif = Notification.builder()
                    .user(otherUser)
                    .title("Other's notification")
                    .message("Should stay unread")
                    .type(Notification.NotificationType.SYSTEM)
                    .read(false)
                    .build();
            entityManager.persistAndFlush(otherNotif);

            notificationRepository.markAllAsRead(user.getId());
            entityManager.flush();
            entityManager.clear();

            // Other user's notification should still be unread
            assertEquals(1, notificationRepository.countByUserIdAndRead(otherUser.getId(), false));
        }
    }
}
