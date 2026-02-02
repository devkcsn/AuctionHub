package com.auction.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BidRequest {
    @NotNull(message = "Auction item ID is required")
    private Long auctionItemId;

    @NotNull(message = "Bid amount is required")
    @DecimalMin(value = "0.01", message = "Bid amount must be greater than 0")
    private BigDecimal amount;

    private Boolean autoBid = false;

    @DecimalMin(value = "0.01", message = "Max auto bid amount must be greater than 0")
    private BigDecimal maxAutoBidAmount;
}
