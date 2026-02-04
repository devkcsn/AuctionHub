package com.auction.service;

import com.auction.dto.AuctionItemRequest;
import com.auction.dto.AuctionItemResponse;
import com.auction.entity.AuctionItem;
import com.auction.entity.User;
import com.auction.exception.AuctionException;
import com.auction.exception.BadRequestException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.exception.UnauthorizedException;
import com.auction.repository.AuctionItemRepository;
import com.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuctionService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionService.class);

    private final AuctionItemRepository auctionItemRepository;
    private final UserRepository userRepository;

    @Transactional
    public AuctionItemResponse createAuction(Long sellerId, AuctionItemRequest request) {
        User seller = userRepository.findById(sellerId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", sellerId));

        validateAuctionRequest(request);

        AuctionItem auctionItem = AuctionItem.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .startingPrice(request.getStartingPrice())
                .reservePrice(request.getReservePrice())
                .currentPrice(request.getStartingPrice())
                .minBidIncrement(request.getMinBidIncrement() != null ?
                        request.getMinBidIncrement() : new BigDecimal("1.00"))
                .category(request.getCategory())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .imageUrls(request.getImageUrls() != null ? request.getImageUrls() : List.of())
                .seller(seller)
                .status(request.getStartTime().isAfter(LocalDateTime.now()) ?
                        AuctionItem.AuctionStatus.PENDING : AuctionItem.AuctionStatus.ACTIVE)
                .build();

        AuctionItem saved = auctionItemRepository.save(auctionItem);
        logger.info("Auction created: {} by seller: {}", saved.getId(), seller.getUsername());
        return mapToResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuctionItemResponse getAuction(Long id) {
        AuctionItem item = auctionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", id));
        return mapToResponse(item);
    }

    @Transactional
    public AuctionItemResponse getAuctionAndIncrementViews(Long id) {
        AuctionItem item = auctionItemRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", id));
        item.setViewCount(item.getViewCount() + 1);
        auctionItemRepository.save(item);
        return mapToResponse(item);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getActiveAuctions(Pageable pageable) {
        return auctionItemRepository.findByStatus(AuctionItem.AuctionStatus.ACTIVE, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getAuctionsByCategory(AuctionItem.Category category, Pageable pageable) {
        return auctionItemRepository.findByStatusAndCategory(
                AuctionItem.AuctionStatus.ACTIVE, category, pageable).map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> searchAuctions(String keyword, Pageable pageable) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getActiveAuctions(pageable);
        }
        return auctionItemRepository.searchActiveAuctions(keyword.trim(), pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getSellerAuctions(Long sellerId, Pageable pageable) {
        return auctionItemRepository.findBySellerId(sellerId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getMyBiddingAuctions(Long userId, Pageable pageable) {
        return auctionItemRepository.findAuctionsByBidder(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getWonAuctions(Long userId, Pageable pageable) {
        return auctionItemRepository.findWonAuctions(userId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public List<AuctionItemResponse> getFeaturedAuctions() {
        return auctionItemRepository.findFeaturedAuctions().stream()
                .map(this::mapToResponse).toList();
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getEndingSoonAuctions(Pageable pageable) {
        return auctionItemRepository.findEndingSoonAuctions(pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuctionItemResponse> getMostPopularAuctions(Pageable pageable) {
        return auctionItemRepository.findMostPopularAuctions(pageable)
                .map(this::mapToResponse);
    }

    @Transactional
    public AuctionItemResponse updateAuction(Long auctionId, Long sellerId, AuctionItemRequest request) {
        AuctionItem item = auctionItemRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));

        if (!item.getSeller().getId().equals(sellerId)) {
            throw new UnauthorizedException("You are not the seller of this auction");
        }

        if (item.getStatus() != AuctionItem.AuctionStatus.PENDING) {
            throw new AuctionException("Cannot update an auction that is already " + item.getStatus());
        }

        if (request.getTitle() != null) item.setTitle(request.getTitle());
        if (request.getDescription() != null) item.setDescription(request.getDescription());
        if (request.getStartingPrice() != null) {
            item.setStartingPrice(request.getStartingPrice());
            item.setCurrentPrice(request.getStartingPrice());
        }
        if (request.getReservePrice() != null) item.setReservePrice(request.getReservePrice());
        if (request.getMinBidIncrement() != null) item.setMinBidIncrement(request.getMinBidIncrement());
        if (request.getCategory() != null) item.setCategory(request.getCategory());
        if (request.getStartTime() != null) item.setStartTime(request.getStartTime());
        if (request.getEndTime() != null) item.setEndTime(request.getEndTime());
        if (request.getImageUrls() != null) item.setImageUrls(request.getImageUrls());

        AuctionItem saved = auctionItemRepository.save(item);
        return mapToResponse(saved);
    }

    @Transactional
    public void cancelAuction(Long auctionId, Long sellerId) {
        AuctionItem item = auctionItemRepository.findById(auctionId)
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", auctionId));

        if (!item.getSeller().getId().equals(sellerId)) {
            throw new UnauthorizedException("You are not the seller of this auction");
        }

        if (item.getStatus() == AuctionItem.AuctionStatus.ENDED ||
            item.getStatus() == AuctionItem.AuctionStatus.SOLD) {
            throw new AuctionException("Cannot cancel an auction that has already ended");
        }

        if (item.getBidCount() > 0) {
            throw new AuctionException("Cannot cancel an auction that has active bids");
        }

        item.setStatus(AuctionItem.AuctionStatus.CANCELLED);
        auctionItemRepository.save(item);
        logger.info("Auction {} cancelled by seller {}", auctionId, sellerId);
    }

    private void validateAuctionRequest(AuctionItemRequest request) {
        if (request.getEndTime().isBefore(request.getStartTime())) {
            throw new BadRequestException("End time must be after start time");
        }
        if (Duration.between(request.getStartTime(), request.getEndTime()).toHours() < 1) {
            throw new BadRequestException("Auction must last at least 1 hour");
        }
        if (request.getReservePrice() != null &&
            request.getReservePrice().compareTo(request.getStartingPrice()) < 0) {
            throw new BadRequestException("Reserve price must be >= starting price");
        }
    }

    public AuctionItemResponse mapToResponse(AuctionItem item) {
        long timeRemaining = 0;
        if (item.getStatus() == AuctionItem.AuctionStatus.ACTIVE) {
            timeRemaining = Math.max(0, Duration.between(LocalDateTime.now(), item.getEndTime()).toSeconds());
        }

        return AuctionItemResponse.builder()
                .id(item.getId())
                .title(item.getTitle())
                .description(item.getDescription())
                .startingPrice(item.getStartingPrice())
                .reservePrice(item.getReservePrice())
                .currentPrice(item.getCurrentPrice())
                .minBidIncrement(item.getMinBidIncrement())
                .status(item.getStatus())
                .category(item.getCategory())
                .startTime(item.getStartTime())
                .endTime(item.getEndTime())
                .imageUrls(item.getImageUrls())
                .sellerId(item.getSeller().getId())
                .sellerUsername(item.getSeller().getUsername())
                .winnerId(item.getWinner() != null ? item.getWinner().getId() : null)
                .winnerUsername(item.getWinner() != null ? item.getWinner().getUsername() : null)
                .bidCount(item.getBidCount())
                .viewCount(item.getViewCount())
                .featured(item.getFeatured())
                .timeRemainingSeconds(timeRemaining)
                .createdAt(item.getCreatedAt())
                .build();
    }
}
