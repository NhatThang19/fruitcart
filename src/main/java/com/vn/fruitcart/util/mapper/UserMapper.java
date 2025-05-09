package com.vn.fruitcart.util.mapper;

import com.vn.fruitcart.entity.User;
import com.vn.fruitcart.entity.dto.request.UpdateUserReq;

public class UserMapper {
    public static UpdateUserReq convertToUpdateUserReq(User user) {
        UpdateUserReq updateUserReq = new UpdateUserReq();
        updateUserReq.setId(user.getId());
        updateUserReq.setUsername(user.getUsername());
        updateUserReq.setPhone(user.getPhone());
        updateUserReq.setAddress(user.getAddress());
        updateUserReq.setAvatar(user.getAvatar());
        updateUserReq.setGender(user.getGender());
        updateUserReq.setActive(user.isActive());
        if (user.getRole() != null) {
            updateUserReq.setRole(user.getRole().getName());
        }
        return updateUserReq;
    }
}
