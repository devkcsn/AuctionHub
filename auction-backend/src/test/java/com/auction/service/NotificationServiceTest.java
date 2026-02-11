package com.auction.service;

import com.auction.dto.NotificationResponse;
import com.auction.entity.Notification;
import com.auction.entity.User;
import com.auction.exception.ResourceNotFoundException;
import com.auction.repository.NotificationRepository;
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

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Tests")
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @Mock private UserRepository userRepository;
    @Mock private SimpMessagingTemplate messagingTemplate;

    @InjectMocks private NotificationService notificationService;

    private User user;
    private Notification unreadNotif;
    private Notification readNotif;

    @BeforeEach
    void setUp() {
        user = User.builder()
                .id(1L).username("testuser").email("t@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();

        unreadNotif = Notification.builder()
                .id(10L)
                .user(user)
                .title("You've been outbid!")
                .message("Someone bid $200 on \"Vintage Watch\".")
                .type(Notification.NotificationType.OUTBID)
                .auctionItemId(100L)
                .read(false)
                .build();

        readNotif = Notification.builder()
                .id(20L)
                .user(user)
                .title("Bid placed")
                .message("Your bid was placed.")
                .type(Notification.NotificationType.BID_PLACED)
                .auctionItemId(100L)
                .read(true)
                .build();
    }

    @Nested
    @DisplayName("createNotification()")
    class CreateNotificationTests {

        @Test
        @DisplayName("Should create notification and send via WebSocket")
        void shouldCreateAndBroadcast() {
            when(userRepository.findById(1L)).thenReturn(Optional.of(user));
            when(notificationRepository.save(any(Notification.class))).thenAnswer(inv -> {
                Notification n = inv.getArgument(0);
                n.setId(30L);
                return n;
            });

            notificationService.createNotification(
                    1L, "Test Title", "Test Message",
                    Notification.NotificationType.SYSTEM, 50L);

            verify(notificationRepository).save(any(Notification.class));
            verify(messagingTemplate).convertAndSendToUser(
                    eq("testuser"), eq("/queue/notifications"), any());
        }

        @Test
        @DisplayName("Should throw when user not found")
        void shouldThrowWhenUserNotFound() {
            when(userRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.createNotification(
                            999L, "Title", "Msg",
                            Notification.NotificationType.SYSTEM, null));
        }
    }

    @Nested
    @DisplayName("getUserNotifications()")
    class GetUserNotificationsTests {

        @Test
        @DisplayName("Should return all user notifications")
        void shouldReturnAll() {
            Page<Notification> page = new PageImpl<>(List.of(unreadNotif, readNotif));
            when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(1L), any()))
                    .thenReturn(page);

            Page<NotificationResponse> result = notificationService.getUserNotifications(
                    1L, PageRequest.of(0, 20));

            assertEquals(2, result.getTotalElements());
        }
    }

    @Nested
    @DisplayName("getUnreadNotifications()")
    class GetUnreadNotificationsTests {

        @Test
        @DisplayName("Should return only unread notifications")
        void shouldReturnUnread() {
            Page<Notification> page = new PageImpl<>(List.of(unreadNotif));
            when(notificationRepository.findByUserIdAndReadOrderByCreatedAtDesc(eq(1L), eq(false), any()))
                    .thenReturn(page);

            Page<NotificationResponse> result = notificationService.getUnreadNotifications(
                    1L, PageRequest.of(0, 20));

            assertEquals(1, result.getTotalElements());
            assertFalse(result.getContent().get(0).getRead());
        }
    }

    @Nested
    @DisplayName("getUnreadCount()")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should return correct unread count")
        void shouldReturnCount() {
            when(notificationRepository.countByUserIdAndRead(1L, false)).thenReturn(5L);

            Long count = notificationService.getUnreadCount(1L);
            assertEquals(5L, count);
        }
    }

    @Nested
    @DisplayName("markAsRead()")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read")
        void shouldMarkAsRead() {
            when(notificationRepository.findById(10L)).thenReturn(Optional.of(unreadNotif));
            when(notificationRepository.save(any(Notification.class))).thenReturn(unreadNotif);

            notificationService.markAsRead(10L, 1L);

            assertTrue(unreadNotif.getRead());
            verify(notificationRepository).save(unreadNotif);
        }

        @Test
        @DisplayName("Should throw when notification doesn't belong to user")
        void shouldThrowWhenWrongUser() {
            when(notificationRepository.findById(10L)).thenReturn(Optional.of(unreadNotif));

            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.markAsRead(10L, 999L));
        }

        @Test
        @DisplayName("Should throw when notification not found")
        void shouldThrowWhenNotFound() {
            when(notificationRepository.findById(999L)).thenReturn(Optional.empty());

            assertThrows(ResourceNotFoundException.class,
                    () -> notificationService.markAsRead(999L, 1L));
        }
    }

    @Nested
    @DisplayName("markAllAsRead()")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should delegate to repository")
        void shouldDelegate() {
            notificationService.markAllAsRead(1L);
            verify(notificationRepository).markAllAsRead(1L);
        }
    }
}
