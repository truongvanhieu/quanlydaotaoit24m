package com.example.quanlydaotao.n7.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity(name = "ScheduleEmployee")
@Table(name = "employees")
public class ScheduleEmployee {

    @Id
    private UUID id;

    @Column(name = "full_name")
    private String fullName;

    @Column(name = "code")
    private String code;
}
