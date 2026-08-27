package com.harsh.propertymanagementsystem.property.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyImage;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.mapper.PropertyMapper;
import com.harsh.propertymanagementsystem.property.repository.PropertyImageRepository;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import com.harsh.propertymanagementsystem.review.repository.OwnerReviewRepository;
import com.harsh.propertymanagementsystem.review.repository.PropertyReviewRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor
@Service
public class PropertyService {

    private final PropertyRepository propertyRepository;
    private final PropertyImageRepository propertyImageRepository;
    private final PropertyReviewRepository propertyReviewRepository;
    private final OwnerReviewRepository ownerReviewRepository;
    private final PropertyMapper propertyMapper;
    private final UserRepository userRepository;

    @Transactional
    public PropertyResponse createProperty(CreatePropertyRequest request) {
        User owner = getCurrentUser();

        // 1. Create Property
        Property property = propertyMapper.toEntity(request);
        property.setOwner(owner);
        property.setStatus(PropertyStatus.AVAILABLE);
        if (property.getImages() == null) {
            property.setImages(new ArrayList<>());
        }

        // 2. Save property
        Property savedProperty = propertyRepository.save(property);

        // 3. Process and persist images in property_images table
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            for (MultipartFile file : request.getImages()) {
                if (file == null || file.isEmpty()) {
                    continue;
                }

                try {
                    PropertyImage propertyImage = PropertyImage.builder()
                            .imageData(file.getBytes())
                            .fileName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .property(savedProperty)
                            .build();

                    PropertyImage savedImage = propertyImageRepository.save(propertyImage);
                    savedProperty.getImages().add(savedImage);
                } catch (IOException e) {
                    log.error("Failed to read image file bytes: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("Failed to process property image: " + file.getOriginalFilename(), e);
                }
            }
        }

        log.info("Created property '{}' with id {} and {} images for owner {}",
                savedProperty.getPropertyName(), savedProperty.getId(), savedProperty.getImages().size(), owner.getEmail());

        return mapToResponseWithRatings(savedProperty);
    }

    @Transactional
    public PropertyResponse addImagesToProperty(Long propertyId, List<MultipartFile> images) {
        User owner = getCurrentUser();
        Property property = propertyRepository.findById(propertyId)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with ID: " + propertyId));

        if (!property.getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to add images to this property");
        }

        if (images != null && !images.isEmpty()) {
            for (MultipartFile file : images) {
                if (file == null || file.isEmpty()) continue;

                try {
                    PropertyImage propertyImage = PropertyImage.builder()
                            .imageData(file.getBytes())
                            .fileName(file.getOriginalFilename())
                            .contentType(file.getContentType())
                            .fileSize(file.getSize())
                            .property(property)
                            .build();

                    PropertyImage savedImage = propertyImageRepository.save(propertyImage);
                    property.getImages().add(savedImage);
                } catch (IOException e) {
                    log.error("Failed to upload additional image: {}", file.getOriginalFilename(), e);
                    throw new RuntimeException("Failed to upload image: " + file.getOriginalFilename(), e);
                }
            }
        }

        log.info("Added {} images to property #{} by owner {}",
                images != null ? images.size() : 0, propertyId, owner.getEmail());

        return mapToResponseWithRatings(property);
    }

    @Transactional
    public void deletePropertyImage(Long imageId) {
        User owner = getCurrentUser();
        PropertyImage image = propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Property image not found with ID: " + imageId));

        if (!image.getProperty().getOwner().getId().equals(owner.getId())) {
            throw new AccessDeniedException("You are not authorized to delete this property image");
        }

        propertyImageRepository.delete(image);
        log.info("Deleted property image #{} by owner {}", imageId, owner.getEmail());
    }

    @Transactional(readOnly = true)
    public PropertyImage getPropertyImage(Long imageId) {
        return propertyImageRepository.findById(imageId)
                .orElseThrow(() -> new ResourceNotFoundException("Property image not found with ID: " + imageId));
    }

    @Transactional(readOnly = true)
    public List<PropertyImage> getImagesByPropertyId(Long propertyId) {
        return propertyImageRepository.findByPropertyId(propertyId);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getMyProperties() {
        User owner = getCurrentUser();
        return propertyRepository.findByOwnerId(owner.getId())
                .stream()
                .map(this::mapToResponseWithRatings)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getMyProperty(Long id) {
        if (id == null) {
            return getMyProperties();
        }
        return propertyRepository.findByOwnerId(id)
                .stream()
                .map(this::mapToResponseWithRatings)
                .toList();
    }

    @Transactional(readOnly = true)
    public PropertyResponse getPropertyById(Long id) {
        Property property = propertyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Property not found with id: " + id));
        return mapToResponseWithRatings(property);
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getAllProperties() {
        return propertyRepository.findAll()
                .stream()
                .map(this::mapToResponseWithRatings)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PropertyResponse> getAvailableProperties() {
        return propertyRepository.findByStatus(PropertyStatus.AVAILABLE)
                .stream()
                .map(this::mapToResponseWithRatings)
                .toList();
    }

    private PropertyResponse mapToResponseWithRatings(Property property) {
        Double propAvg = propertyReviewRepository.findAverageRatingByPropertyId(property.getId());
        long count = propertyReviewRepository.countByPropertyId(property.getId());

        Double ownerAvg = null;
        if (property.getOwner() != null) {
            ownerAvg = ownerReviewRepository.findAverageRatingByOwnerId(property.getOwner().getId());
        }

        Double roundedPropAvg = propAvg != null ? roundToOneDecimal(propAvg) : null;
        Double roundedOwnerAvg = ownerAvg != null ? roundToOneDecimal(ownerAvg) : null;

        return propertyMapper.toResponse(property, roundedPropAvg, (int) count, roundedOwnerAvg);
    }

    private Double roundToOneDecimal(Double val) {
        return BigDecimal.valueOf(val)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));
    }
}
