package com.auction.dto;

import com.auction.entity.AuctionItem;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class AuctionItemRequest {
    @NotBlank(message = "Title is required")
    @Size(max = 200, message = "Title must not exceed 200 characters")
    private String title;

    @NotBlank(message = "Description is required")
    private String description;

    @NotNull(message = "Starting price is required")
    @DecimalMin(value = "0.01", message = "Starting price must be greater than 0")
    private BigDecimal startingPrice;

    @DecimalMin(value = "0.01", message = "Reserve price must be greater than 0")
    private BigDecimal reservePrice;

    private BigDecimal minBidIncrement;

    @NotNull(message = "Category is required")
    private AuctionItem.Category category;

    @NotNull(message = "Start time is required")
    private LocalDateTime startTime;

    @NotNull(message = "End time is required")
    private LocalDateTime endTime;

    private List<String> imageUrls;
}
