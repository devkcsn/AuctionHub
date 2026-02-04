package com.auction.service;

import com.auction.dto.WebSocketMessage;
import com.auction.entity.AuctionItem;
import com.auction.entity.Bid;
import com.auction.entity.Notification;
import com.auction.repository.AuctionItemRepository;
import com.auction.repository.BidRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuctionSchedulerService {

    private static final Logger logger = LoggerFactory.getLogger(AuctionSchedulerService.class);

    private final AuctionItemRepository auctionItemRepository;
    private final BidRepository bidRepository;
    private final NotificationService notificationService;
    private final SimpMessagingTemplate messagingTemplate;

    /**
     * Start pending auctions whose start time has passed
     */
    @Scheduled(fixedRate = 10000) // Every 10 seconds
    @Transactional
    public void startPendingAuctions() {
        List<AuctionItem> auctionsToStart = auctionItemRepository
                .findAuctionsToStart(AuctionItem.AuctionStatus.PENDING, LocalDateTime.now());

        for (AuctionItem auction : auctionsToStart) {
            auction.setStatus(AuctionItem.AuctionStatus.ACTIVE);
            auctionItemRepository.save(auction);

            logger.info("Auction {} '{}' is now ACTIVE", auction.getId(), auction.getTitle());

            // Broadcast auction started
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("AUCTION_STARTED")
                    .auctionItemId(auction.getId())
                    .currentPrice(auction.getCurrentPrice())
                    .message("Auction '" + auction.getTitle() + "' is now live!")
                    .timestamp(LocalDateTime.now())
                    .build();
            messagingTemplate.convertAndSend("/topic/auctions", message);
        }
    }

    /**
     * End active auctions whose end time has passed
     */
    @Scheduled(fixedRate = 5000) // Every 5 seconds
    @Transactional
    public void endExpiredAuctions() {
        List<AuctionItem> expiredAuctions = auctionItemRepository
                .findExpiredAuctions(AuctionItem.AuctionStatus.ACTIVE, LocalDateTime.now());

        for (AuctionItem auction : expiredAuctions) {
            Optional<Bid> highestBid = bidRepository.findHighestBid(auction.getId());

            if (highestBid.isPresent()) {
                Bid winningBid = highestBid.get();
                auction.setWinner(winningBid.getBidder());

                // Check if reserve price was met
                if (auction.getReservePrice() != null &&
                    winningBid.getAmount().compareTo(auction.getReservePrice()) < 0) {
                    auction.setStatus(AuctionItem.AuctionStatus.ENDED);
                    logger.info("Auction {} ended - reserve price not met", auction.getId());

                    // Notify all bidders
                    notifyAuctionEndedReserveNotMet(auction);
                } else {
                    auction.setStatus(AuctionItem.AuctionStatus.SOLD);
                    logger.info("Auction {} sold to {} for ${}",
                            auction.getId(), winningBid.getBidder().getUsername(), winningBid.getAmount());

                    // Notify winner
                    notificationService.createNotification(
                            winningBid.getBidder().getId(),
                            "Congratulations! You won!",
                            String.format("You won the auction \"%s\" with a bid of $%s!",
                                    auction.getTitle(), winningBid.getAmount()),
                            Notification.NotificationType.AUCTION_WON,
                            auction.getId()
                    );

                    // Notify seller
                    notificationService.createNotification(
                            auction.getSeller().getId(),
                            "Your auction has sold!",
                            String.format("Your auction \"%s\" has sold for $%s to %s!",
                                    auction.getTitle(), winningBid.getAmount(),
                                    winningBid.getBidder().getUsername()),
                            Notification.NotificationType.PAYMENT_RECEIVED,
                            auction.getId()
                    );

                    // Notify losers
                    notifyLosers(auction, winningBid.getBidder().getId());
                }
            } else {
                auction.setStatus(AuctionItem.AuctionStatus.ENDED);
                logger.info("Auction {} ended with no bids", auction.getId());
            }

            auctionItemRepository.save(auction);

            // Broadcast auction ended
            WebSocketMessage message = WebSocketMessage.builder()
                    .type("AUCTION_ENDED")
                    .auctionItemId(auction.getId())
                    .currentPrice(auction.getCurrentPrice())
                    .message("Auction '" + auction.getTitle() + "' has ended!")
                    .timestamp(LocalDateTime.now())
                    .build();
            messagingTemplate.convertAndSend("/topic/auction/" + auction.getId(), message);
            messagingTemplate.convertAndSend("/topic/auctions", message);
        }
    }

    private void notifyLosers(AuctionItem auction, Long winnerId) {
        List<Long> bidderIds = bidRepository.findDistinctBidderIds(auction.getId());
        for (Long bidderId : bidderIds) {
            if (!bidderId.equals(winnerId) && !bidderId.equals(auction.getSeller().getId())) {
                notificationService.createNotification(
                        bidderId,
                        "Auction ended",
                        String.format("The auction \"%s\" has ended. Unfortunately, you didn't win this time.",
                                auction.getTitle()),
                        Notification.NotificationType.AUCTION_LOST,
                        auction.getId()
                );
            }
        }
    }

    private void notifyAuctionEndedReserveNotMet(AuctionItem auction) {
        List<Long> bidderIds = bidRepository.findDistinctBidderIds(auction.getId());
        for (Long bidderId : bidderIds) {
            notificationService.createNotification(
                    bidderId,
                    "Auction ended - Reserve not met",
                    String.format("The auction \"%s\" has ended but the reserve price was not met.",
                            auction.getTitle()),
                    Notification.NotificationType.AUCTION_LOST,
                    auction.getId()
            );
        }

        notificationService.createNotification(
                auction.getSeller().getId(),
                "Auction ended - Reserve not met",
                String.format("Your auction \"%s\" has ended but the reserve price was not met.",
                        auction.getTitle()),
                Notification.NotificationType.AUCTION_ENDING,
                auction.getId()
        );
    }
}
