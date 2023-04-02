package com.treeyh.raindrop.config;

import lombok.Data;

@Data
public class RaindropDbConfig {

    private String dbType;

    // 数据库驱动
    private String jdbcDriver;

    // 数据库Url jdbc:mysql://{host}:{port}/{dbName}?{params}
    private String dbUrl;

    private String dbName;

    private String dbPassword;
}
