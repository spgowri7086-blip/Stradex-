package com.stradex.config;

import com.stradex.model.Vehicle;
import com.stradex.repository.VehicleRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import java.util.List;

/** Adds six sample vehicles the first time the app starts with an empty database. */
@Component
public class SampleDataLoader implements CommandLineRunner {
    private final VehicleRepository vehicles;

    public SampleDataLoader(VehicleRepository vehicles) {
        this.vehicles = vehicles;
    }

    @Override
    public void run(String... args) {
        if (vehicles.count() > 0) return;
        vehicles.saveAll(List.of(
                new Vehicle("City Hatch", "Car", "KA 01 AB 1234", 1800, "/images/vehicle-hatchback.jpg"),
                new Vehicle("Comfort Sedan", "Car", "KA 02 CD 5678", 2600, "/images/vehicle-sedan.jpg"),
                new Vehicle("Trail SUV 7-Seater", "SUV", "KA 03 EF 9012", 3900, "/images/vehicle-suv.jpg"),
                new Vehicle("Group Van 12-Seater", "Van", "KA 04 GH 3456", 5200, "/images/vehicle-van.jpg"),
                new Vehicle("Mint Scooter", "Bike", "KA 05 IJ 7890", 450, "/images/vehicle-scooter.jpg"),
                new Vehicle("Night Cruiser", "Bike", "KA 06 KL 2345", 1200, "/images/vehicle-motorcycle.jpg")
        ));
    }
}
