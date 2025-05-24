package com.vn.fruitcart.entity.dto.response;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSessionInfo implements Serializable {
  private Long userId;
  private String email;
  private String fullName;
  private String avatar;
  private String role;
}
