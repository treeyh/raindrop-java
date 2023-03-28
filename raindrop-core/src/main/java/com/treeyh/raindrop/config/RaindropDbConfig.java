package com.treeyh.raindrop.config;

import lombok.Data;

@Data
public class RaindropDbConfig {

    private String dbType;

    private String dbIp;

    private Integer dbPort;

    private String user;

    private String password;
}
