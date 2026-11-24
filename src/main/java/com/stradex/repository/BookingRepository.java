package com.stradex.repository;

import com.stradex.model.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.time.LocalDate;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    List<Booking> findAllByOrderByIdDesc();

    void deleteByVehicleId(Long vehicleId);

    /** True when the vehicle already has a non-cancelled booking overlapping the given dates. */
    @Query("""
            select count(b) > 0 from Booking b
            where b.vehicle.id = :vehicleId
              and b.status <> 'CANCELLED'
              and b.startDate < :endDate
              and b.endDate > :startDate
            """)
    boolean hasOverlap(@Param("vehicleId") Long vehicleId,
                       @Param("startDate") LocalDate startDate,
                       @Param("endDate") LocalDate endDate);
}
