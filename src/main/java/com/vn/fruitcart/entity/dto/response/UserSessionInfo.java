package com.vn.fruitcart.entity.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSessionInfo {
  private long id;
  private String email;
  private String username;
  private String avatar;
}
