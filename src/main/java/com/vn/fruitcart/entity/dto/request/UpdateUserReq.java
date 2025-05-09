package com.vn.fruitcart.entity.dto.request;

import com.vn.fruitcart.util.constant.GenderEnum;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserReq {
    private Long id;

    @NotBlank(message = "Username cannot be blank")
    @Size(min = 3, max = 50, message = "Username must be between 3 and 50 characters")
    private String username;

    @Pattern(regexp = "(^$|[0-9]{10,15})", message = "Phone number must be 10-15 digits")
    private String phone;

    private String address;

    private String avatar;

    private GenderEnum gender;

    @NotNull(message = "Enabled cannot be null")
    private boolean active = true;

    private String role;
}
