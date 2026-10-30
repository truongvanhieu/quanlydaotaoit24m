package com.example.quanlydaotao.n1.dto;

import java.util.UUID;

public class PermissionResponse {

    private UUID id;
    private String code;
    private String name;
    private String module;
    private String description;

    public PermissionResponse() {
    }

    public PermissionResponse(
            UUID id,
            String code,
            String name,
            String module,
            String description) {
        this.id = id;
        this.code = code;
        this.name = name;
        this.module = module;
        this.description = description;
    }

    public UUID getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getModule() {
        return module;
    }

    public String getDescription() {
        return description;
    }
}
