package com.auction.controller;

import com.auction.dto.BidRequest;
import com.auction.dto.BidResponse;
import com.auction.security.CustomUserDetailsService;
import com.auction.service.BidService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class WebSocketController {

    private final BidService bidService;

    @MessageMapping("/bid")
    public void handleBid(@Payload BidRequest bidRequest, SimpMessageHeaderAccessor headerAccessor) {
        UsernamePasswordAuthenticationToken auth =
                (UsernamePasswordAuthenticationToken) headerAccessor.getUser();
        if (auth != null) {
            CustomUserDetailsService.CustomUserPrincipal principal =
                    (CustomUserDetailsService.CustomUserPrincipal) auth.getPrincipal();
            bidService.placeBid(principal.getId(), bidRequest);
        }
    }
}
