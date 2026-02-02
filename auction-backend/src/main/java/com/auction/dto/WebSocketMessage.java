package com.auction.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WebSocketMessage {
    private String type;
    private Long auctionItemId;
    private BigDecimal currentPrice;
    private Integer bidCount;
    private String bidderUsername;
    private BigDecimal bidAmount;
    private Long timeRemainingSeconds;
    private String message;
    private LocalDateTime timestamp;
}
