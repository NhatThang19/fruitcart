package com.vn.fruitcart.entity.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSearchCriteriaReq {
    private String email;
    private Integer role;
    private Boolean isBlocked;
}
