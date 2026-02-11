package com.auction.controller;

import com.auction.dto.NotificationResponse;
import com.auction.entity.Notification;
import com.auction.entity.User;
import com.auction.security.CustomUserDetailsService;
import com.auction.security.JwtTokenProvider;
import com.auction.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@DisplayName("NotificationController Tests")
class NotificationControllerTest {

    @Autowired private MockMvc mockMvc;

    @MockitoBean private NotificationService notificationService;
    @MockitoBean private JwtTokenProvider tokenProvider;
    @MockitoBean private CustomUserDetailsService userDetailsService;

    private CustomUserDetailsService.CustomUserPrincipal principal;
    private NotificationResponse sampleNotif;

    @BeforeEach
    void setUp() {
        User testUser = User.builder()
                .id(1L).username("testuser").email("t@e.com").password("p")
                .role(User.Role.USER).enabled(true).build();
        principal = new CustomUserDetailsService.CustomUserPrincipal(testUser);

        sampleNotif = NotificationResponse.builder()
                .id(10L)
                .title("You've been outbid!")
                .message("Someone bid $200 on your item.")
                .type(Notification.NotificationType.OUTBID)
                .auctionItemId(100L)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Nested
    @DisplayName("GET /api/notifications")
    class GetNotificationsTests {

        @Test
        @DisplayName("Should return user notifications")
        void shouldReturnNotifications() throws Exception {
            Page<NotificationResponse> page = new PageImpl<>(List.of(sampleNotif));
            when(notificationService.getUserNotifications(eq(1L), any())).thenReturn(page);

            mockMvc.perform(get("/api/notifications")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].title").value("You've been outbid!"));
        }

        @Test
        @DisplayName("Should return 401 when not authenticated")
        void shouldReturn401() throws Exception {
            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread")
    class GetUnreadTests {

        @Test
        @DisplayName("Should return unread notifications")
        void shouldReturnUnread() throws Exception {
            Page<NotificationResponse> page = new PageImpl<>(List.of(sampleNotif));
            when(notificationService.getUnreadNotifications(eq(1L), any())).thenReturn(page);

            mockMvc.perform(get("/api/notifications/unread")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content[0].read").value(false));
        }
    }

    @Nested
    @DisplayName("GET /api/notifications/unread-count")
    class GetUnreadCountTests {

        @Test
        @DisplayName("Should return unread count")
        void shouldReturnCount() throws Exception {
            when(notificationService.getUnreadCount(1L)).thenReturn(5L);

            mockMvc.perform(get("/api/notifications/unread-count")
                            .with(user(principal)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").value(5));
        }
    }

    @Nested
    @DisplayName("PUT /api/notifications/{id}/read")
    class MarkAsReadTests {

        @Test
        @DisplayName("Should mark notification as read")
        void shouldMarkAsRead() throws Exception {
            mockMvc.perform(put("/api/notifications/10/read")
                            .with(user(principal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Notification marked as read"));

            verify(notificationService).markAsRead(10L, 1L);
        }
    }

    @Nested
    @DisplayName("PUT /api/notifications/read-all")
    class MarkAllAsReadTests {

        @Test
        @DisplayName("Should mark all notifications as read")
        void shouldMarkAllAsRead() throws Exception {
            mockMvc.perform(put("/api/notifications/read-all")
                            .with(user(principal))
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("All notifications marked as read"));

            verify(notificationService).markAllAsRead(1L);
        }
    }
}
