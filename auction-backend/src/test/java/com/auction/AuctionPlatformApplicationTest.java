package com.auction;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class AuctionPlatformApplicationTest {

    @Test
    void contextLoads() {
        // Verifies that the entire Spring application context starts without errors.
        // This catches wiring issues, missing beans, and invalid configuration.
    }
}
