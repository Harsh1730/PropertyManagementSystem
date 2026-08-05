package com.harsh.propertymanagementsystem.property.repository;

import com.harsh.propertymanagementsystem.property.entity.Property;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PropertyRepository extends JpaRepository<Property, Long> {

}