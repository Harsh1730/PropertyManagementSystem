package com.harsh.propertymanagementsystem.booking.repository;

import com.harsh.propertymanagementsystem.booking.entity.BookingRequest;
import com.harsh.propertymanagementsystem.booking.entity.BookingStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingRequestRepository extends JpaRepository<BookingRequest, Long> {

    List<BookingRequest> findByTenantIdOrderByCreatedAtDesc(Long tenantId);

    List<BookingRequest> findByOwnerIdOrderByCreatedAtDesc(Long ownerId);

    List<BookingRequest> findByPropertyIdOrderByCreatedAtDesc(Long propertyId);

    List<BookingRequest> findByPropertyIdAndStatus(Long propertyId, BookingStatus status);

    Optional<BookingRequest> findByIdAndOwnerId(Long id, Long ownerId);

    Optional<BookingRequest> findByIdAndTenantId(Long id, Long tenantId);
}
