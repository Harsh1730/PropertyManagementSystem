package com.harsh.propertymanagementsystem.property.controller;


import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.service.PropertyService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequiredArgsConstructor
@RestController
@RequestMapping("/properties")
public class PropertyController {

    private final PropertyService propertyService;

    @PostMapping
    public PropertyResponse createProperty(
            @RequestBody CreatePropertyRequest request
    ) {
        return propertyService.createProperty(request);
    }
}