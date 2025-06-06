package com.vn.fruitcart.entity;

import com.vn.fruitcart.entity.base.BaseEntity;
import com.vn.fruitcart.util.constant.GenderEnum;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class User extends BaseEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "first_name", nullable = false, length = 50)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 50)
  private String lastName;

  @Column(name = "email", nullable = false, unique = true, length = 100)
  private String email;

  @Column(name = "password", nullable = false)
  private String password;

  @Column(name = "phone", length = 15)
  private String phone;

  @Column(name = "address", length = 255)
  private String address;

  @Enumerated(EnumType.STRING)
  @Column(name = "gender")
  private GenderEnum gender;

  @Column(name = "birth_date")
  private LocalDate birthDate;

  @Column(name = "avatar_url", length = 255)
  private String avatarUrl;

  @Column(name = "is_blocked", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
  private Boolean isBlocked = false;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "role_id", nullable = false)
  private Role role;

  public String getFullName() {
    String ho = this.firstName != null ? this.firstName.trim() : "";
    String ten = this.lastName != null ? this.lastName.trim() : "";

    if (ho.isEmpty() && ten.isEmpty()) {
      return "";
    }
    if (ho.isEmpty()) {
      return ten;
    }
    if (ten.isEmpty()) {
      return ho;
    }

    return ho + " " + ten;
  }
}
