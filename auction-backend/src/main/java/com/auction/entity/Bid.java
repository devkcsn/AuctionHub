package com.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "bids")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Bid {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "auction_item_id", nullable = false)
    private AuctionItem auctionItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bidder_id", nullable = false)
    private User bidder;

    @Column(nullable = false)
    @Builder.Default
    private Boolean autoBid = false;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxAutoBidAmount;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
}
