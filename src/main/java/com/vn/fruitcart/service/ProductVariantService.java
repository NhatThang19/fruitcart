package com.vn.fruitcart.service;

import org.springframework.stereotype.Service;

import com.vn.fruitcart.repository.ProductVariantRepository;

@Service  
public class ProductVariantService {
    private final ProductVariantRepository productVariantRepository;

    public ProductVariantService(ProductVariantRepository pRepository){
        this.productVariantRepository = pRepository;
    }
}
