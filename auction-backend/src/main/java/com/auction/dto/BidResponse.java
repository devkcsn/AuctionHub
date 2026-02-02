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
public class BidResponse {
    private Long id;
    private BigDecimal amount;
    private Long auctionItemId;
    private String auctionTitle;
    private Long bidderId;
    private String bidderUsername;
    private Boolean autoBid;
    private LocalDateTime createdAt;
}
