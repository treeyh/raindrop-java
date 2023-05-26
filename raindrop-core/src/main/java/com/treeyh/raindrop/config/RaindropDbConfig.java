package com.treeyh.raindrop.config;

import lombok.*;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Data
@ToString
@Builder
public class RaindropDbConfig {

    private String dbType;

    /**
     * 数据库驱动
     */
    private String jdbcDriver;

    /**
     * 数据库Url jdbc:mysql://{host}:{port}/{dbName}?{params}
     */
    private String dbUrl;

    private String dbName;

    private String dbPassword;
}
