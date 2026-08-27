package com.harsh.propertymanagementsystem.property.repository;

import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {
    List<Property> findByOwnerId(Long ownerId);

    List<Property> findByOwnerIdAndStatus(
            Long ownerId,
            PropertyStatus status);

    List<Property> findByStatus(PropertyStatus status);
}