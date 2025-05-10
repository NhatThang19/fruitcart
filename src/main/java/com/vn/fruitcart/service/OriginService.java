package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.repository.OriginRepository;

@Service
public class OriginService {
    private final OriginRepository originRepository;

    public OriginService(OriginRepository originRepository) {
        this.originRepository = originRepository;
    }
}
