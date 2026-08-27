package com.harsh.propertymanagementsystem.auth.service;

import com.harsh.propertymanagementsystem.auth.dto.RegisterRequest;
import com.harsh.propertymanagementsystem.auth.dto.RegisterResponce;
import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.exception.EmailAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.exception.PhoneAlreadyExistsException;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.booking.entity.BookingRequest;
import com.harsh.propertymanagementsystem.booking.repository.BookingRequestRepository;
import com.harsh.propertymanagementsystem.chat.entity.ChatMessage;
import com.harsh.propertymanagementsystem.chat.repository.ChatMessageRepository;
import com.harsh.propertymanagementsystem.lease.entity.Lease;
import com.harsh.propertymanagementsystem.lease.repository.LeaseRepository;
import com.harsh.propertymanagementsystem.maintenance.repository.MaintenanceRepository;
import com.harsh.propertymanagementsystem.payment.repository.PaymentRepository;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import com.harsh.propertymanagementsystem.review.repository.OwnerReviewRepository;
import com.harsh.propertymanagementsystem.review.repository.PropertyReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;
    private final PropertyRepository propertyRepository;
    private final PropertyReviewRepository propertyReviewRepository;
    private final OwnerReviewRepository ownerReviewRepository;
    private final LeaseRepository leaseRepository;
    private final PaymentRepository paymentRepository;
    private final MaintenanceRepository maintenanceRepository;
    private final BookingRequestRepository bookingRequestRepository;
    private final ChatMessageRepository chatMessageRepository;

    public User findById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    public User findByEmail(String email) {
        return repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("No User Found With Email: " + email));
    }

    public User findByPhoneNumber(String phoneNumber) {
        return repo.findByPhoneNumber(phoneNumber)
                .orElseThrow(() -> new UsernameNotFoundException("No User Found with Phone: " + phoneNumber));
    }

    public RegisterResponce register(RegisterRequest request) throws EmailAlreadyExistsException, PhoneAlreadyExistsException {
        if (repo.existsByEmail(request.getEmail())) {
            log.warn("Registration failed: Email already exists: {}", request.getEmail());
            throw new EmailAlreadyExistsException();
        }

        if (repo.existsByPhoneNumber(request.getPhoneNumber())) {
            log.warn("Registration failed: Phone number already exists: {}", request.getPhoneNumber());
            throw new PhoneAlreadyExistsException();
        }

        Role userRole = request.getRole() != null ? request.getRole() : Role.TENANT;

        User user = User.builder()
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .password(encoder.encode(request.getPassword()))
                .role(userRole)
                .accountLocked(false)
                .enabled(true)
                .build();

        repo.save(user);
        log.info("Successfully registered new user: {} with role: {}", user.getEmail(), user.getRole());
        return new RegisterResponce("User Registration Success");
    }

    @Transactional
    public void deleteCurrentUserAccount() {
        User user = getCurrentUser();
        Long userId = user.getId();
        log.info("Deleting user account #{} ({}) with role {}", userId, user.getEmail(), user.getRole());

        // 1. Delete all chat messages where user is sender or receiver
        chatMessageRepository.findAllMessagesForUser(userId)
                .forEach(chatMessageRepository::delete);

        // 2. Delete owner reviews involving this user
        ownerReviewRepository.findAll().stream()
                .filter(r -> (r.getOwner() != null && r.getOwner().getId().equals(userId)) ||
                             (r.getTenant() != null && r.getTenant().getId().equals(userId)))
                .forEach(ownerReviewRepository::delete);

        // 3. Delete property reviews written by this user
        propertyReviewRepository.findAll().stream()
                .filter(r -> r.getTenant() != null && r.getTenant().getId().equals(userId))
                .forEach(propertyReviewRepository::delete);

        // 4. Delete maintenance requests by tenant
        maintenanceRepository.findByTenantId(userId)
                .forEach(maintenanceRepository::delete);

        // 5. Delete booking requests created by tenant
        bookingRequestRepository.findByTenantIdOrderByCreatedAtDesc(userId)
                .forEach(bookingRequestRepository::delete);

        // 6. Delete payments by tenant
        paymentRepository.findByLeaseTenantId(userId)
                .forEach(paymentRepository::delete);

        // 7. Delete leases where user is tenant
        leaseRepository.findByTenantId(userId)
                .forEach(leaseRepository::delete);

        // 8. If user is owner, delete all owned properties and their dependent records
        List<Property> ownedProperties = propertyRepository.findByOwnerId(userId);
        for (Property property : ownedProperties) {
            Long propId = property.getId();

            // Unlink chat messages
            chatMessageRepository.findByPropertyId(propId).forEach(msg -> {
                msg.setProperty(null);
                msg.setBooking(null);
                chatMessageRepository.save(msg);
            });

            // Delete property reviews
            propertyReviewRepository.findByPropertyIdOrderByCreatedAtDesc(propId)
                    .forEach(propertyReviewRepository::delete);

            // Delete maintenance requests
            maintenanceRepository.findByPropertyId(propId)
                    .forEach(maintenanceRepository::delete);

            // Delete booking requests
            bookingRequestRepository.findByPropertyIdOrderByCreatedAtDesc(propId)
                    .forEach(bookingRequestRepository::delete);

            // Delete leases and payments
            List<Lease> propLeases = leaseRepository.findByPropertyId(propId);
            for (Lease lease : propLeases) {
                paymentRepository.findByLeaseId(lease.getId()).forEach(paymentRepository::delete);
                leaseRepository.delete(lease);
            }

            // Delete property
            propertyRepository.delete(property);
        }

        // 9. Delete the user
        repo.delete(user);
        log.info("User account #{} successfully deleted", userId);
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return repo.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
