package com.auction.repository;

import com.auction.entity.Bid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BidRepository extends JpaRepository<Bid, Long> {

    Page<Bid> findByAuctionItemIdOrderByAmountDesc(Long auctionItemId, Pageable pageable);

    List<Bid> findByAuctionItemIdOrderByAmountDesc(Long auctionItemId);

    Page<Bid> findByBidderIdOrderByCreatedAtDesc(Long bidderId, Pageable pageable);

    @Query("SELECT b FROM Bid b WHERE b.auctionItem.id = :auctionItemId ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findHighestBid(@Param("auctionItemId") Long auctionItemId);

    @Query("SELECT COUNT(DISTINCT b.bidder.id) FROM Bid b WHERE b.auctionItem.id = :auctionItemId")
    Integer countDistinctBidders(@Param("auctionItemId") Long auctionItemId);

    @Query("SELECT b FROM Bid b WHERE b.auctionItem.id = :auctionItemId AND b.bidder.id = :bidderId " +
           "ORDER BY b.amount DESC LIMIT 1")
    Optional<Bid> findHighestBidByUser(@Param("auctionItemId") Long auctionItemId,
                                        @Param("bidderId") Long bidderId);

    @Query("SELECT DISTINCT b.bidder.id FROM Bid b WHERE b.auctionItem.id = :auctionItemId")
    List<Long> findDistinctBidderIds(@Param("auctionItemId") Long auctionItemId);
}
