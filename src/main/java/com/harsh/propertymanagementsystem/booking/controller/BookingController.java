package com.harsh.propertymanagementsystem.booking.controller;

import com.harsh.propertymanagementsystem.booking.dto.BookingResponse;
import com.harsh.propertymanagementsystem.booking.dto.CreateBookingRequest;
import com.harsh.propertymanagementsystem.booking.service.BookingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(
            @Valid @RequestBody CreateBookingRequest request) {
        log.info("Received request to create booking for property {}", request.getPropertyId());
        return ResponseEntity.ok(bookingService.createBooking(request));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        log.info("Received request to get my bookings");
        return ResponseEntity.ok(bookingService.getMyBookings());
    }

    @GetMapping("/owner")
    public ResponseEntity<List<BookingResponse>> getOwnerBookings() {
        log.info("Received request to get owner bookings");
        return ResponseEntity.ok(bookingService.getOwnerBookings());
    }

    @GetMapping("/property/{propertyId}")
    public ResponseEntity<List<BookingResponse>> getBookingsForProperty(
            @PathVariable Long propertyId) {
        log.info("Received request to get bookings for property {}", propertyId);
        return ResponseEntity.ok(bookingService.getBookingsForProperty(propertyId));
    }

    @PatchMapping("/{id}/approve")
    public ResponseEntity<BookingResponse> approveBooking(@PathVariable Long id) {
        log.info("Received request to approve booking {}", id);
        return ResponseEntity.ok(bookingService.approveBooking(id));
    }

    @PatchMapping("/{id}/reject")
    public ResponseEntity<BookingResponse> rejectBooking(@PathVariable Long id) {
        log.info("Received request to reject booking {}", id);
        return ResponseEntity.ok(bookingService.rejectBooking(id));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<BookingResponse> cancelBooking(@PathVariable Long id) {
        log.info("Received request to cancel booking {}", id);
        return ResponseEntity.ok(bookingService.cancelBooking(id));
    }
}
