package com.stradex.controller;

import com.stradex.model.Booking;
import com.stradex.model.Vehicle;
import com.stradex.repository.BookingRepository;
import com.stradex.repository.VehicleRepository;
import com.stradex.service.PricingService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Pattern;

@Controller
public class WebsiteController {
    private static final List<String> STATUSES = List.of("PENDING", "CONFIRMED", "CANCELLED");
    private static final Pattern EMAIL = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final VehicleRepository vehicles;
    private final BookingRepository bookings;
    private final PricingService pricing;

    public WebsiteController(VehicleRepository vehicles, BookingRepository bookings, PricingService pricing) {
        this.vehicles = vehicles;
        this.bookings = bookings;
        this.pricing = pricing;
    }

    private Vehicle findVehicle(Long id) {
        return vehicles.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Vehicle not found"));
    }

    // ---------- Customer pages ----------

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("vehicles", vehicles.findByAvailableTrueOrderByPricePerDayAsc());
        return "index";
    }

    @GetMapping("/book/{id}")
    public String bookingPage(@PathVariable Long id, Model model) {
        model.addAttribute("vehicle", findVehicle(id));
        return "booking";
    }

    @PostMapping("/book/{id}")
    public String createBooking(@PathVariable Long id,
                                @RequestParam String customerName,
                                @RequestParam String email,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                RedirectAttributes redirect) {
        Vehicle vehicle = findVehicle(id);
        String name = customerName == null ? "" : customerName.trim();
        String mail = email == null ? "" : email.trim().toLowerCase();

        String error = null;
        if (name.isEmpty() || name.length() > 100) {
            error = "Please enter your name (up to 100 characters).";
        } else if (mail.length() > 255 || !EMAIL.matcher(mail).matches()) {
            error = "Please enter a valid email address.";
        } else if (!vehicle.isAvailable() || startDate == null || endDate == null
                || startDate.isBefore(LocalDate.now()) || !endDate.isAfter(startDate)) {
            error = "Please choose valid rental dates and an available vehicle.";
        } else if (bookings.hasOverlap(vehicle.getId(), startDate, endDate)) {
            error = "Sorry, this vehicle is already booked for some of those dates. Please pick other dates.";
        }
        if (error != null) {
            redirect.addFlashAttribute("error", error);
            redirect.addFlashAttribute("customerName", name);
            redirect.addFlashAttribute("email", mail);
            return "redirect:/book/" + id;
        }

        Booking booking = new Booking();
        booking.setVehicle(vehicle);
        booking.setCustomerName(name);
        booking.setEmail(mail);
        booking.setStartDate(startDate);
        booking.setEndDate(endDate);
        // Price is always calculated on the server: days x daily rate.
        booking.setTotalAmount(pricing.total(startDate, endDate, vehicle.getPricePerDay()));
        booking.setStatus("PENDING");
        bookings.save(booking);

        redirect.addFlashAttribute("success", "Your booking request has been submitted! We'll confirm it soon.");
        return "redirect:/#vehicles";
    }

    // ---------- Admin pages (no login, as in the original project) ----------

    @GetMapping("/admin")
    public String admin(Model model) {
        model.addAttribute("vehicles", vehicles.findAllByOrderByIdAsc());
        model.addAttribute("bookings", bookings.findAllByOrderByIdDesc());
        model.addAttribute("statuses", STATUSES);
        if (!model.containsAttribute("newVehicle")) {
            model.addAttribute("newVehicle", new Vehicle());
        }
        return "admin";
    }

    @PostMapping("/admin/vehicles")
    public String addVehicle(@ModelAttribute("newVehicle") Vehicle vehicle, RedirectAttributes redirect) {
        String imageUrl = vehicle.getImageUrl() == null ? "" : vehicle.getImageUrl().trim();
        if (isBlank(vehicle.getName()) || isBlank(vehicle.getType()) || isBlank(vehicle.getRegistrationNumber())) {
            redirect.addFlashAttribute("error", "Please fill in the vehicle name, type and registration number.");
        } else if (vehicle.getPricePerDay() < 0) {
            redirect.addFlashAttribute("error", "Price can't be negative.");
        } else if (!imageUrl.isEmpty() && !imageUrl.matches("^(https?://|/).*")) {
            redirect.addFlashAttribute("error", "Image URL must start with http:// or https://");
        } else {
            vehicle.setId(null);
            vehicle.setName(vehicle.getName().trim());
            vehicle.setType(vehicle.getType().trim());
            vehicle.setRegistrationNumber(vehicle.getRegistrationNumber().trim());
            vehicle.setImageUrl(imageUrl.isEmpty() ? null : imageUrl);
            vehicle.setAvailable(true);
            vehicles.save(vehicle);
            redirect.addFlashAttribute("success", "Vehicle added.");
            return "redirect:/admin";
        }
        redirect.addFlashAttribute("newVehicle", vehicle);
        return "redirect:/admin";
    }

    @PostMapping("/admin/vehicles/{id}/delete")
    @Transactional
    public String deleteVehicle(@PathVariable Long id, RedirectAttributes redirect) {
        Vehicle vehicle = findVehicle(id);
        bookings.deleteByVehicleId(vehicle.getId());
        vehicles.delete(vehicle);
        redirect.addFlashAttribute("success", vehicle.getName() + " deleted.");
        return "redirect:/admin";
    }

    @PostMapping("/admin/bookings/{id}/status")
    public String updateBookingStatus(@PathVariable Long id, @RequestParam String status, RedirectAttributes redirect) {
        Booking booking = bookings.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Booking not found"));
        if (STATUSES.contains(status)) {
            booking.setStatus(status);
            bookings.save(booking);
            redirect.addFlashAttribute("success", "Booking marked " + status.toLowerCase() + ".");
        }
        return "redirect:/admin";
    }

    private static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }
}
