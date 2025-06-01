package com.vn.fruitcart.entity.base;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
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
public abstract class BaseEntity {
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

  @Column(name = "is_deleted", columnDefinition = "BOOLEAN DEFAULT FALSE")
  private boolean isDeleted = false;

  public boolean isDeleted() {
    return isDeleted;
  }

  public void setDeleted(boolean deleted) {
    isDeleted = deleted;
  }
}
