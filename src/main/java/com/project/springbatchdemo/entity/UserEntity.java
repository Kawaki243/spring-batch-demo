package com.project.springbatchdemo.entity;


import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "tbl_users")
@Data
public class UserEntity {

    @Id
    private Long id;

    private String userId;
    private String firstName;
    private String lastName;
    private String gender;
    private String email;
    private String phone;
    private String dateOfBirth;
    private String jobTitle;


}
