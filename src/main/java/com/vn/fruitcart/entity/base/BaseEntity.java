package com.vn.fruitcart.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;

import java.io.Serializable;
import java.time.Instant;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.CreatedBy;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedBy;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Getter
@Setter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseEntity implements Serializable {
  @CreatedBy
  @Column(name = "created_by", nullable = false, length = 50, updatable = false)
  private String createdBy;

  @CreatedDate
  @Column(name = "created_date", updatable = false)
  private Instant createdDate;

  @LastModifiedBy
  @Column(name = "last_modified_by", length = 50)
  private String lastModifiedBy;

  @LastModifiedDate
  @Column(name = "last_modified_date")
  private Instant lastModifiedDate;
}
