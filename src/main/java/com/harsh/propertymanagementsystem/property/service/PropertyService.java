package com.harsh.propertymanagementsystem.property.service;

import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.mapper.PropertyMapper;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

@RequiredArgsConstructor
public class PropertyService {
    private final PropertyRepository propertyRepository;
    private final PropertyMapper propertyMapper;
    private final UserRepository userRepository;
    public PropertyResponse createProperty(CreatePropertyRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User owner = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));
        Property property = propertyMapper.toEntity(request);
        property.setOwner(owner);
        property.setStatus(PropertyStatus.AVAILABLE);
        Property savedProperty = propertyRepository.save(property);
        return propertyMapper.toResponse(savedProperty);
    }

}
