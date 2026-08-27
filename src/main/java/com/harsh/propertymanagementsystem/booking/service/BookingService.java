package com.harsh.propertymanagementsystem.booking.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.booking.dto.BookingResponse;
import com.harsh.propertymanagementsystem.booking.dto.CreateBookingRequest;
import com.harsh.propertymanagementsystem.booking.entity.BookingRequest;
import com.harsh.propertymanagementsystem.booking.entity.BookingStatus;
import com.harsh.propertymanagementsystem.booking.mapper.BookingMapper;
import com.harsh.propertymanagementsystem.booking.repository.BookingRequestRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.entity.LeaseStatus;
import com.harsh.propertymanagementsystem.lease.repository.LeaseRepository;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class BookingService {

    private final BookingRequestRepository bookingRepository;
    private final PropertyRepository propertyRepository;
    private final UserRepository userRepository;
    private final LeaseRepository leaseRepository;
    private final BookingMapper bookingMapper;

    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {
        User tenant = getCurrentUser();

        Property property = propertyRepository.findById(request.getPropertyId())
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + request.getPropertyId()));

        if (property.getOwner() != null && property.getOwner().getId().equals(tenant.getId())) {
            throw new IllegalArgumentException("Owners cannot book their own properties");
        }

        if (property.getStatus() != PropertyStatus.AVAILABLE) {
            throw new IllegalStateException("Property is not currently available for booking (Status: " + property.getStatus() + ")");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Booking start date must be before end date");
        }

        BigDecimal monthlyRent = request.getMonthlyRent() != null && request.getMonthlyRent().compareTo(BigDecimal.ZERO) > 0
                ? request.getMonthlyRent()
                : property.getRentAmount();

        BigDecimal securityDeposit = request.getSecurityDeposit() != null && request.getSecurityDeposit().compareTo(BigDecimal.ZERO) >= 0
                ? request.getSecurityDeposit()
                : property.getSecurityDeposit();

        BookingRequest booking = BookingRequest.builder()
                .property(property)
                .tenant(tenant)
                .owner(property.getOwner())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .monthlyRent(monthlyRent)
                .securityDeposit(securityDeposit)
                .message(request.getMessage())
                .status(BookingStatus.PENDING)
                .build();

        BookingRequest savedBooking = bookingRepository.save(booking);
        log.info("Tenant {} created booking #{} for property {}", tenant.getEmail(), savedBooking.getId(), property.getId());
        return bookingMapper.toResponse(savedBooking);
    }

    public List<BookingResponse> getMyBookings() {
        User tenant = getCurrentUser();
        return bookingRepository.findByTenantIdOrderByCreatedAtDesc(tenant.getId())
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    public List<BookingResponse> getOwnerBookings() {
        User owner = getCurrentUser();
        return bookingRepository.findByOwnerIdOrderByCreatedAtDesc(owner.getId())
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    public List<BookingResponse> getBookingsForProperty(Long propertyId) {
        User owner = getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You do not own this property");
        }

        return bookingRepository.findByPropertyIdOrderByCreatedAtDesc(propertyId)
                .stream()
                .map(bookingMapper::toResponse)
                .toList();
    }

    @Transactional
    public BookingResponse approveBooking(Long bookingId) {
        User owner = getCurrentUser();

        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking request not found with ID: " + bookingId));

        if (!booking.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to approve bookings for this property");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be approved (Current status: " + booking.getStatus() + ")");
        }

        Property property = booking.getProperty();

        // 1. Create and activate a Lease contract
        Lease lease = Lease.builder()
                .property(property)
                .tenant(booking.getTenant())
                .leaseStartDate(booking.getStartDate())
                .leaseEndDate(booking.getEndDate())
                .monthlyRent(booking.getMonthlyRent())
                .securityDeposit(booking.getSecurityDeposit())
                .rentDueDay(5)
                .status(LeaseStatus.ACTIVE)
                .build();

        Lease savedLease = leaseRepository.save(lease);

        // 2. Transition property status to OCCUPIED
        property.setStatus(PropertyStatus.OCCUPIED);
        propertyRepository.save(property);

        // 3. Mark booking as APPROVED and link lease
        booking.setStatus(BookingStatus.APPROVED);
        booking.setLease(savedLease);
        BookingRequest approvedBooking = bookingRepository.save(booking);

        // 4. Reject other pending bookings on this property
        List<BookingRequest> otherPending = bookingRepository.findByPropertyIdAndStatus(property.getId(), BookingStatus.PENDING);
        for (BookingRequest other : otherPending) {
            if (!other.getId().equals(bookingId)) {
                other.setStatus(BookingStatus.REJECTED);
                bookingRepository.save(other);
            }
        }

        log.info("Owner {} approved booking #{} - Activated Lease #{} for property {}",
                owner.getEmail(), bookingId, savedLease.getId(), property.getId());

        return bookingMapper.toResponse(approvedBooking);
    }

    @Transactional
    public BookingResponse rejectBooking(Long bookingId) {
        User owner = getCurrentUser();

        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking request not found with ID: " + bookingId));

        if (!booking.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to reject bookings for this property");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be rejected");
        }

        booking.setStatus(BookingStatus.REJECTED);
        BookingRequest saved = bookingRepository.save(booking);
        log.info("Owner {} rejected booking #{}", owner.getEmail(), bookingId);
        return bookingMapper.toResponse(saved);
    }

    @Transactional
    public BookingResponse cancelBooking(Long bookingId) {
        User tenant = getCurrentUser();

        BookingRequest booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new ResourceNotFoundException("Booking request not found with ID: " + bookingId));

        if (!booking.getTenant().getId().equals(tenant.getId())) {
            throw new AccessDeniedException("You are not authorized to cancel this booking request");
        }

        if (booking.getStatus() != BookingStatus.PENDING) {
            throw new IllegalStateException("Only PENDING bookings can be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
        BookingRequest saved = bookingRepository.save(booking);
        log.info("Tenant {} cancelled booking #{}", tenant.getEmail(), bookingId);
        return bookingMapper.toResponse(saved);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
