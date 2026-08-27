package com.harsh.propertymanagementsystem;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import com.harsh.propertymanagementsystem.auth.entity.User;
import com.harsh.propertymanagementsystem.auth.repository.UserRepository;
import com.harsh.propertymanagementsystem.common.exception.ResourceNotFoundException;
import com.harsh.propertymanagementsystem.property.dto.CreatePropertyRequest;
import com.harsh.propertymanagementsystem.property.dto.PropertyResponse;
import com.harsh.propertymanagementsystem.property.entity.Property;
import com.harsh.propertymanagementsystem.property.entity.PropertyStatus;
import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import com.harsh.propertymanagementsystem.property.mapper.PropertyMapper;
import com.harsh.propertymanagementsystem.property.repository.PropertyRepository;
import com.harsh.propertymanagementsystem.property.service.PropertyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PropertyServiceTest {

    @Mock
    private PropertyRepository propertyRepository;

    @Mock
    private UserRepository userRepository;

    @Spy
    private PropertyMapper propertyMapper = new PropertyMapper();

    @InjectMocks
    private PropertyService propertyService;

    @Mock
    private SecurityContext securityContext;

    @Mock
    private Authentication authentication;

    private User testOwner;
    private Property testProperty;

    @BeforeEach
    void setUp() {
        testOwner = User.builder()
                .id(1L)
                .email("owner@test.com")
                .firstName("John")
                .lastName("Doe")
                .role(Role.OWNER)
                .build();

        testProperty = Property.builder()
                .id(10L)
                .propertyName("Sunset Villa")
                .address("123 Palm St")
                .city("Miami")
                .state("FL")
                .country("USA")
                .postalCode("33101")
                .propertyType(PropertyType.HOUSE)
                .status(PropertyStatus.AVAILABLE)
                .rentAmount(new BigDecimal("1500.00"))
                .securityDeposit(new BigDecimal("1500.00"))
                .totalUnits(1)
                .owner(testOwner)
                .build();
    }

    @Test
    void testCreateProperty() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("owner@test.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testOwner));
        when(propertyRepository.save(any(Property.class))).thenReturn(testProperty);

        CreatePropertyRequest request = new CreatePropertyRequest();
        request.setPropertyName("Sunset Villa");
        request.setAddress("123 Palm St");
        request.setCity("Miami");
        request.setState("FL");
        request.setCountry("USA");
        request.setPostalCode("33101");
        request.setPropertyType(PropertyType.HOUSE);
        request.setRentAmount(new BigDecimal("1500.00"));
        request.setSecurityDeposit(new BigDecimal("1500.00"));
        request.setTotalUnits(1);

        PropertyResponse response = propertyService.createProperty(request);

        assertNotNull(response);
        assertEquals("Sunset Villa", response.getPropertyName());
        assertEquals(PropertyStatus.AVAILABLE, response.getStatus());
        verify(propertyRepository).save(any(Property.class));
    }

    @Test
    void testGetMyProperties() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.getName()).thenReturn("owner@test.com");
        SecurityContextHolder.setContext(securityContext);

        when(userRepository.findByEmail("owner@test.com")).thenReturn(Optional.of(testOwner));
        when(propertyRepository.findByOwnerId(1L)).thenReturn(List.of(testProperty));

        List<PropertyResponse> properties = propertyService.getMyProperties();

        assertEquals(1, properties.size());
        assertEquals("Sunset Villa", properties.get(0).getPropertyName());
    }

    @Test
    void testGetPropertyById_NotFound() {
        when(propertyRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> propertyService.getPropertyById(99L));
    }
}
