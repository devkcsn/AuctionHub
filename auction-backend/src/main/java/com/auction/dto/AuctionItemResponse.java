package com.auction.dto;

import com.auction.entity.AuctionItem;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuctionItemResponse {
    private Long id;
    private String title;
    private String description;
    private BigDecimal startingPrice;
    private BigDecimal reservePrice;
    private BigDecimal currentPrice;
    private BigDecimal minBidIncrement;
    private AuctionItem.AuctionStatus status;
    private AuctionItem.Category category;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private List<String> imageUrls;
    private Long sellerId;
    private String sellerUsername;
    private Long winnerId;
    private String winnerUsername;
    private Integer bidCount;
    private Integer viewCount;
    private Boolean featured;
    private Long timeRemainingSeconds;
    private LocalDateTime createdAt;
}
