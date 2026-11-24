package com.stradex.service;

import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PricingServiceTest {
    private final PricingService pricing = new PricingService();

    @Test
    void threeDaysAtEighteenHundredIsFiveThousandFourHundred() {
        assertEquals(5400.0, pricing.total(LocalDate.of(2026, 12, 1), LocalDate.of(2026, 12, 4), 1800));
    }

    @Test
    void returnBeforePickupCostsNothing() {
        assertEquals(0.0, pricing.total(LocalDate.of(2026, 12, 4), LocalDate.of(2026, 12, 1), 1800));
    }
}
