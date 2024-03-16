package com.galaxy.galaxy_drive.model.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.FieldDefaults;

@NoArgsConstructor
@AllArgsConstructor
@Entity
@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    Long id;
    @Column(name = "username",nullable = false, unique = true)
    String userName;
    @Column(nullable = false)
    String password;
    @Column(name = "firstname")
    String firstName;
    @Column(name = "lastname")
    String lastName;
    @Enumerated(value = EnumType.STRING)
    Role role;
    @Enumerated(value = EnumType.STRING)
    SignupMethod signupMethod;

}
