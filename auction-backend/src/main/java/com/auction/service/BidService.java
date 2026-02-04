package com.auction.service;

import com.auction.dto.BidRequest;
import com.auction.dto.BidResponse;
import com.auction.dto.WebSocketMessage;
import com.auction.entity.AuctionItem;
import com.auction.entity.Bid;
import com.auction.entity.Notification;
import com.auction.entity.User;
import com.auction.exception.AuctionException;
import com.auction.exception.BadRequestException;
import com.auction.exception.ResourceNotFoundException;
import com.auction.repository.AuctionItemRepository;
import com.auction.repository.BidRepository;
import com.auction.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BidService {

    private static final Logger logger = LoggerFactory.getLogger(BidService.class);

    private final BidRepository bidRepository;
    private final AuctionItemRepository auctionItemRepository;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;
    private final NotificationService notificationService;

    @Value("${app.auction.snipe-protection-seconds:30}")
    private int snipeProtectionSeconds;

    @Value("${app.auction.auto-extend-minutes:5}")
    private int autoExtendMinutes;

    @Transactional
    public BidResponse placeBid(Long bidderId, BidRequest request) {
        User bidder = userRepository.findById(bidderId)
                .orElseThrow(() -> new ResourceNotFoundException("User", "id", bidderId));

        AuctionItem auctionItem = auctionItemRepository.findById(request.getAuctionItemId())
                .orElseThrow(() -> new ResourceNotFoundException("Auction", "id", request.getAuctionItemId()));

        // Validations
        validateBid(bidder, auctionItem, request.getAmount());

        // Create the bid
        Bid bid = Bid.builder()
                .amount(request.getAmount())
                .auctionItem(auctionItem)
                .bidder(bidder)
                .autoBid(request.getAutoBid() != null && request.getAutoBid())
                .maxAutoBidAmount(request.getMaxAutoBidAmount())
                .build();

        Bid savedBid = bidRepository.save(bid);

        // Update auction item
        auctionItem.setCurrentPrice(request.getAmount());
        auctionItem.setBidCount(auctionItem.getBidCount() + 1);

        // Snipe protection: extend auction if bid placed near the end
        long secondsRemaining = Duration.between(LocalDateTime.now(), auctionItem.getEndTime()).toSeconds();
        if (secondsRemaining > 0 && secondsRemaining <= snipeProtectionSeconds) {
            auctionItem.setEndTime(auctionItem.getEndTime().plusMinutes(autoExtendMinutes));
            logger.info("Snipe protection activated for auction {}, extended by {} minutes",
                    auctionItem.getId(), autoExtendMinutes);
        }

        auctionItemRepository.save(auctionItem);

        // Notify previous highest bidder they've been outbid
        notifyOutbidUsers(auctionItem, bidder, request.getAmount());

        // Send real-time WebSocket update
        sendBidUpdate(auctionItem, bidder, request.getAmount());

        logger.info("Bid placed: ${} on auction {} by {}", request.getAmount(), auctionItem.getId(), bidder.getUsername());

        return mapToResponse(savedBid);
    }

    @Transactional(readOnly = true)
    public Page<BidResponse> getAuctionBids(Long auctionItemId, Pageable pageable) {
        if (!auctionItemRepository.existsById(auctionItemId)) {
            throw new ResourceNotFoundException("Auction", "id", auctionItemId);
        }
        return bidRepository.findByAuctionItemIdOrderByAmountDesc(auctionItemId, pageable)
                .map(this::mapToResponse);
    }

    @Transactional(readOnly = true)
    public Page<BidResponse> getUserBids(Long userId, Pageable pageable) {
        return bidRepository.findByBidderIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::mapToResponse);
    }

    private void validateBid(User bidder, AuctionItem auctionItem, BigDecimal amount) {
        if (auctionItem.getStatus() != AuctionItem.AuctionStatus.ACTIVE) {
            throw new AuctionException("This auction is not currently active. Status: " + auctionItem.getStatus());
        }

        if (auctionItem.getEndTime().isBefore(LocalDateTime.now())) {
            throw new AuctionException("This auction has already ended");
        }

        if (auctionItem.getSeller().getId().equals(bidder.getId())) {
            throw new AuctionException("You cannot bid on your own auction");
        }

        BigDecimal minimumBid = auctionItem.getCurrentPrice().add(auctionItem.getMinBidIncrement());
        if (auctionItem.getBidCount() == 0) {
            minimumBid = auctionItem.getStartingPrice();
        }

        if (amount.compareTo(minimumBid) < 0) {
            throw new BadRequestException(
                    String.format("Bid amount must be at least $%s (current price $%s + minimum increment $%s)",
                            minimumBid, auctionItem.getCurrentPrice(), auctionItem.getMinBidIncrement()));
        }
    }

    private void notifyOutbidUsers(AuctionItem auctionItem, User currentBidder, BigDecimal newAmount) {
        List<Long> bidderIds = bidRepository.findDistinctBidderIds(auctionItem.getId());
        for (Long bidderId : bidderIds) {
            if (!bidderId.equals(currentBidder.getId())) {
                notificationService.createNotification(
                        bidderId,
                        "You've been outbid!",
                        String.format("Someone bid $%s on \"%s\". Place a higher bid to stay in the game!",
                                newAmount, auctionItem.getTitle()),
                        Notification.NotificationType.OUTBID,
                        auctionItem.getId()
                );
            }
        }
    }

    private void sendBidUpdate(AuctionItem auctionItem, User bidder, BigDecimal amount) {
        long timeRemaining = Math.max(0,
                Duration.between(LocalDateTime.now(), auctionItem.getEndTime()).toSeconds());

        WebSocketMessage message = WebSocketMessage.builder()
                .type("NEW_BID")
                .auctionItemId(auctionItem.getId())
                .currentPrice(amount)
                .bidCount(auctionItem.getBidCount())
                .bidderUsername(bidder.getUsername())
                .bidAmount(amount)
                .timeRemainingSeconds(timeRemaining)
                .message(String.format("%s bid $%s", bidder.getUsername(), amount))
                .timestamp(LocalDateTime.now())
                .build();

        messagingTemplate.convertAndSend("/topic/auction/" + auctionItem.getId(), message);
        messagingTemplate.convertAndSend("/topic/auctions", message);
    }

    private BidResponse mapToResponse(Bid bid) {
        return BidResponse.builder()
                .id(bid.getId())
                .amount(bid.getAmount())
                .auctionItemId(bid.getAuctionItem().getId())
                .auctionTitle(bid.getAuctionItem().getTitle())
                .bidderId(bid.getBidder().getId())
                .bidderUsername(bid.getBidder().getUsername())
                .autoBid(bid.getAutoBid())
                .createdAt(bid.getCreatedAt())
                .build();
    }
}
