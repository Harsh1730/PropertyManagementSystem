package com.harsh.propertymanagementsystem.auth.dto;

import com.harsh.propertymanagementsystem.auth.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginResponce {
    private String token;
    @Builder.Default
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String name;
    private Role role;
    private String msg;

    public String getAccessToken() {
        return token;
    }

    public String getJwt() {
        return token;
    }

    public String getTokenType() {
        return type;
    }

    public String getMessage() {
        return msg;
    }
}
