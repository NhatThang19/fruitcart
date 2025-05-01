package com.vn.fruitcart.controller;

import com.vn.fruitcart.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/users")
public class UserController {
  private final UserService userService;

  public UserController(UserService userService){
    this.userService = userService;
  }
}
