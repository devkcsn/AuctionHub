package com.auction.controller;

import com.auction.dto.ApiResponse;
import com.auction.dto.BidRequest;
import com.auction.dto.BidResponse;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.BidService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bids")
@RequiredArgsConstructor
public class BidController {

    private final BidService bidService;

    @PostMapping
    public ResponseEntity<ApiResponse<BidResponse>> placeBid(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @Valid @RequestBody BidRequest request) {
        BidResponse response = bidService.placeBid(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Bid placed successfully", response));
    }

    @GetMapping("/auction/{auctionId}")
    public ResponseEntity<ApiResponse<Page<BidResponse>>> getAuctionBids(
            @PathVariable Long auctionId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponse> response = bidService.getAuctionBids(auctionId, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-bids")
    public ResponseEntity<ApiResponse<Page<BidResponse>>> getMyBids(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<BidResponse> response = bidService.getUserBids(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
