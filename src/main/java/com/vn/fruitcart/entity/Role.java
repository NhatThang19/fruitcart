package com.vn.fruitcart.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "roles")
public class Role {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(name = "name", nullable = false, unique = true, length = 50)
  private String name;

  @Column(name = "description", length = 255)
  private String description;

  @OneToMany(mappedBy = "role", fetch = FetchType.LAZY)
  @Builder.Default
  private List<User> users = new ArrayList<>();

  public Role(String name) {
    this.name = name;
  }
}
