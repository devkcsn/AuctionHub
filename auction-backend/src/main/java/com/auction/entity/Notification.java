package com.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "notifications")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private NotificationType type;

    @Column(name = "auction_item_id")
    private Long auctionItemId;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean read = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public enum NotificationType {
        BID_PLACED, OUTBID, AUCTION_WON, AUCTION_LOST,
        AUCTION_STARTING, AUCTION_ENDING, AUCTION_CANCELLED,
        PAYMENT_RECEIVED, SYSTEM
    }
}
