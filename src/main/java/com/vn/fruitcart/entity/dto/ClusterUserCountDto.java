package com.vn.fruitcart.entity.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ClusterUserCountDto {
    private String clusterName;
    private long userCount;
}
