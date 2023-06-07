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

    /**
     * 数据库用户名
     */
    private String dbName;

    /**
     * 数据库密码
     */
    private String dbPassword;

    /**
     * 工作表名
     */
    private String tableName;
}
