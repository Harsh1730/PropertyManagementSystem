package com.harsh.propertymanagementsystem.property.controller;

import com.harsh.propertymanagementsystem.dashboard.dto.OwnerRentedPropertyResponse;
import com.harsh.propertymanagementsystem.dashboard.service.DashboardService;
import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.entity.PropertyImage;
import com.harsh.propertymanagementsystem.property.service.PropertyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.util.List;
import java.util.Map;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;
    private final DashboardService dashboardService;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> createProperty(
            @Valid @ModelAttribute CreatePropertyRequest request) {
        log.info("Received multipart request to create property: {}", request.getPropertyName());
        return ResponseEntity.ok(
                propertyService.createProperty(request));
    }

    @PostMapping(value = "/{id}/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<PropertyResponse> addImagesToProperty(
            @PathVariable Long id,
            @RequestParam("images") List<MultipartFile> images) {
        log.info("Received request to add {} images to property #{}", images != null ? images.size() : 0, id);
        return ResponseEntity.ok(propertyService.addImagesToProperty(id, images));
    }

    @DeleteMapping("/images/{imageId}")
    public ResponseEntity<Map<String, String>> deletePropertyImage(@PathVariable Long imageId) {
        log.info("Received request to delete property image #{}", imageId);
        propertyService.deletePropertyImage(imageId);
        return ResponseEntity.ok(Map.of("message", "Image deleted successfully"));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProperty(@PathVariable Long id) {
        log.info("Received request to delete property #{}", id);
        propertyService.deleteProperty(id);
        return ResponseEntity.ok(Map.of("message", "Property deleted successfully"));
    }

    @GetMapping("/images/{imageId}")
    public ResponseEntity<byte[]> getPropertyImage(@PathVariable Long imageId) {
        log.info("Received request to view property image: {}", imageId);
        PropertyImage image = propertyService.getPropertyImage(imageId);
        MediaType mediaType = MediaType.IMAGE_JPEG;
        if (image.getContentType() != null) {
            try {
                mediaType = MediaType.parseMediaType(image.getContentType());
            } catch (Exception ignored) {
            }
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + (image.getFileName() != null ? image.getFileName() : "image.jpg")
                                + "\"")
                .body(image.getImageData());
    }

    @GetMapping
    public ResponseEntity<List<PropertyResponse>> getAllProperties() {
        return ResponseEntity.ok(propertyService.getAllProperties());
    }

    @GetMapping("/my")
    public ResponseEntity<List<PropertyResponse>> getMyProperties() {
        log.info("Received request to get my properties");
        return ResponseEntity.ok(propertyService.getMyProperties());
    }

    @GetMapping("/rented")
    public ResponseEntity<List<OwnerRentedPropertyResponse>> getRentedProperties() {
        log.info("Received request to get rented properties");
        return ResponseEntity.ok(dashboardService.getOwnerRentedProperties());
    }

    @GetMapping("/available")
    public ResponseEntity<List<PropertyResponse>> getAvailableProperties() {
        log.info("Received request to get available properties");
        return ResponseEntity.ok(propertyService.getAvailableProperties());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PropertyResponse> getPropertyById(@PathVariable Long id) {
        log.info("Received request to get property by id: {}", id);
        return ResponseEntity.ok(propertyService.getPropertyById(id));
    }
}