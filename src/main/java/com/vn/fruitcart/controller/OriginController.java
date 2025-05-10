package com.vn.fruitcart.controller;

import org.springframework.stereotype.Controller;

import com.vn.fruitcart.service.OriginService;

@Controller
public class OriginController {
    private final OriginService originService;

    public OriginController(OriginService originService) {
        this.originService = originService;
    }
}
