package com.harsh.propertymanagementsystem.auth.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RegisterResponce {
    private String msg;

    public String getMessage() {
        return msg;
    }
}
