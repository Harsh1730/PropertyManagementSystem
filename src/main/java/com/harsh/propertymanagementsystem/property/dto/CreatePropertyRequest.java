package com.harsh.propertymanagementsystem.property.dto;

import com.harsh.propertymanagementsystem.property.entity.PropertyType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Data
public class CreatePropertyRequest {
    @NotBlank
    private String propertyName;

    private String description;

    @NotBlank
    private String address;

    @NotBlank
    private String city;

    @NotBlank
    private String state;

    @NotBlank
    private String country;

    @NotBlank
    private String postalCode;

    @NotNull
    private PropertyType propertyType;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal rentAmount;

    @NotNull
    @DecimalMin("0.0")
    private BigDecimal securityDeposit;

    @NotNull
    @Positive
    private Integer totalUnits;
}
