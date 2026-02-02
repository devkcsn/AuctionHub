package com.auction.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private String fullName;
    private String phone;
    private String email;
    private String avatarUrl;
}
