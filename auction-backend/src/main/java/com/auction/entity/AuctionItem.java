package com.auction.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "auction_items")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class AuctionItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal startingPrice;

    @Column(precision = 12, scale = 2)
    private BigDecimal reservePrice;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal currentPrice;

    @Column(precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal minBidIncrement = new BigDecimal("1.00");

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private AuctionStatus status = AuctionStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    private LocalDateTime endTime;

    @ElementCollection
    @CollectionTable(name = "auction_images", joinColumns = @JoinColumn(name = "auction_item_id"))
    @Column(name = "image_url")
    @Builder.Default
    private List<String> imageUrls = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private User winner;

    @OneToMany(mappedBy = "auctionItem", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("amount DESC")
    @Builder.Default
    private List<Bid> bids = new ArrayList<>();

    @Column(nullable = false)
    @Builder.Default
    private Integer bidCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer viewCount = 0;

    @Column(nullable = false)
    @Builder.Default
    private Boolean featured = false;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    public enum AuctionStatus {
        PENDING, ACTIVE, ENDED, CANCELLED, SOLD
    }

    public enum Category {
        ELECTRONICS, FASHION, HOME_GARDEN, SPORTS,
        COLLECTIBLES, ART, VEHICLES, JEWELRY,
        BOOKS, TOYS, MUSIC, OTHER
    }
}
