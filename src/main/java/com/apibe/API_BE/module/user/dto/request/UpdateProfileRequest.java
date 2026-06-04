package com.apibe.API_BE.module.user.dto.request;

import jakarta.validation.constraints.Pattern;
import lombok.*;
import org.hibernate.validator.constraints.URL;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateProfileRequest {

    private String name;

    @Pattern(regexp = "^\\+?[0-9\\s().-]{7,20}$", message = "Số điện thoại không hợp lệ")
    private String phone;

    @URL(message = "Avatar phải là URL hợp lệ")
    private String avatarUrl;
}
