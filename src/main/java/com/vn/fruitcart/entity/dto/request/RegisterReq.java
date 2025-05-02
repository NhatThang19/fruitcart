package com.vn.fruitcart.entity.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RegisterReq {
    @Size(min = 3, max = 50, message = "Tên người dùng phải từ 3 đến 50 ký tự.")
    private String username;

    @NotBlank(message = "Email không được để trống.")
    @Email(message = "Email không hợp lệ.")
    private String email;

    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String password;
    
    @Size(min = 6, message = "Mật khẩu phải có ít nhất 6 ký tự.")
    private String rePassword;

}
