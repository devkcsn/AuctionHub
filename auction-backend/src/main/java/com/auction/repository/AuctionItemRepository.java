package com.auction.repository;

import com.auction.entity.AuctionItem;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuctionItemRepository extends JpaRepository<AuctionItem, Long> {

    Page<AuctionItem> findByStatus(AuctionItem.AuctionStatus status, Pageable pageable);

    Page<AuctionItem> findByCategory(AuctionItem.Category category, Pageable pageable);

    Page<AuctionItem> findByStatusAndCategory(AuctionItem.AuctionStatus status,
                                               AuctionItem.Category category, Pageable pageable);

    Page<AuctionItem> findBySellerId(Long sellerId, Pageable pageable);

    @Query("SELECT a FROM AuctionItem a WHERE a.status = :status AND a.endTime <= :now")
    List<AuctionItem> findExpiredAuctions(@Param("status") AuctionItem.AuctionStatus status,
                                          @Param("now") LocalDateTime now);

    @Query("SELECT a FROM AuctionItem a WHERE a.status = :status AND a.startTime <= :now")
    List<AuctionItem> findAuctionsToStart(@Param("status") AuctionItem.AuctionStatus status,
                                          @Param("now") LocalDateTime now);

    @Query("SELECT a FROM AuctionItem a WHERE a.status = 'ACTIVE' AND " +
           "(LOWER(a.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(a.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<AuctionItem> searchActiveAuctions(@Param("keyword") String keyword, Pageable pageable);

    @Query("SELECT a FROM AuctionItem a WHERE a.featured = true AND a.status = 'ACTIVE' ORDER BY a.endTime ASC")
    List<AuctionItem> findFeaturedAuctions();

    @Query("SELECT a FROM AuctionItem a WHERE a.status = 'ACTIVE' ORDER BY a.bidCount DESC")
    Page<AuctionItem> findMostPopularAuctions(Pageable pageable);

    @Query("SELECT a FROM AuctionItem a WHERE a.status = 'ACTIVE' ORDER BY a.endTime ASC")
    Page<AuctionItem> findEndingSoonAuctions(Pageable pageable);

    @Query("SELECT DISTINCT a FROM AuctionItem a JOIN a.bids b WHERE b.bidder.id = :userId")
    Page<AuctionItem> findAuctionsByBidder(@Param("userId") Long userId, Pageable pageable);

    @Query("SELECT a FROM AuctionItem a WHERE a.winner.id = :userId")
    Page<AuctionItem> findWonAuctions(@Param("userId") Long userId, Pageable pageable);
}
