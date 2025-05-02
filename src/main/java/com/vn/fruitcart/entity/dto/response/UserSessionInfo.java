package com.vn.fruitcart.entity.dto.response;

import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserSessionInfo implements Serializable {
  private long id;
  private String email;
  private String username;
  private String avatar;
}
