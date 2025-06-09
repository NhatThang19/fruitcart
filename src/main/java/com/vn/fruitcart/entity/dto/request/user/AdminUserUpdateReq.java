package com.vn.fruitcart.entity.dto.request.user;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AdminUserUpdateReq {
    @NotNull
    private Long userId;

    @NotNull(message = "Vai trò không được để trống.")
    private Long roleId;

    @NotNull(message = "Trạng thái chặn không được để trống.")
    private Boolean isBlocked;
}