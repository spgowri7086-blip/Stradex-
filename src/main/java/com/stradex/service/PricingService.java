package com.stradex.service;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Rental price rule: number of days between pickup and return x daily rate. */
@Service
public class PricingService {

    public long rentalDays(LocalDate startDate, LocalDate endDate) {
        if (startDate == null || endDate == null) return 0;
        return Math.max(0, ChronoUnit.DAYS.between(startDate, endDate));
    }

    public double total(LocalDate startDate, LocalDate endDate, double pricePerDay) {
        return rentalDays(startDate, endDate) * pricePerDay;
    }
}
