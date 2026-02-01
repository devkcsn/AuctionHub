package com.auction.controller;

import com.auction.dto.*;
import com.auction.entity.AuctionItem;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.AuctionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auctions")
@RequiredArgsConstructor
public class AuctionController {

    private final AuctionService auctionService;

    @PostMapping
    public ResponseEntity<ApiResponse<AuctionItemResponse>> createAuction(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @Valid @RequestBody AuctionItemRequest request) {
        AuctionItemResponse response = auctionService.createAuction(currentUser.getId(), request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Auction created successfully", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionItemResponse>> getAuction(@PathVariable Long id) {
        AuctionItemResponse response = auctionService.getAuctionAndIncrementViews(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getActiveAuctions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        Sort sort = sortDir.equalsIgnoreCase("asc") ? Sort.by(sortBy).ascending() : Sort.by(sortBy).descending();
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<AuctionItemResponse> response = auctionService.getActiveAuctions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> searchAuctions(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemResponse> response = auctionService.searchAuctions(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getByCategory(
            @PathVariable AuctionItem.Category category,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemResponse> response = auctionService.getAuctionsByCategory(category, pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/featured")
    public ResponseEntity<ApiResponse<List<AuctionItemResponse>>> getFeaturedAuctions() {
        List<AuctionItemResponse> response = auctionService.getFeaturedAuctions();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/ending-soon")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getEndingSoon(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemResponse> response = auctionService.getEndingSoonAuctions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/popular")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getMostPopular(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemResponse> response = auctionService.getMostPopularAuctions(pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-auctions")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getMyAuctions(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuctionItemResponse> response = auctionService.getSellerAuctions(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-bids")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getMyBidAuctions(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemResponse> response = auctionService.getMyBiddingAuctions(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/won")
    public ResponseEntity<ApiResponse<Page<AuctionItemResponse>>> getWonAuctions(
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<AuctionItemResponse> response = auctionService.getWonAuctions(currentUser.getId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AuctionItemResponse>> updateAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser,
            @Valid @RequestBody AuctionItemRequest request) {
        AuctionItemResponse response = auctionService.updateAuction(id, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("Auction updated", response));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> cancelAuction(
            @PathVariable Long id,
            @AuthenticationPrincipal CustomUserDetailsService.CustomUserPrincipal currentUser) {
        auctionService.cancelAuction(id, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("Auction cancelled", null));
    }
}
