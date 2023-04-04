package com.treeyh.raindrop;

import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.consts.Consts;

public class BaseTest {

    public static RaindropDbConfig getDefaultRaindropDbConfig() {
        return RaindropDbConfig.builder().dbType(Consts.DB_TYPE_MYSQL)
                .dbUrl("jdbc:mysql://192.168.80.137:3306/raindrop_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=UTC")
                .dbPassword("mysqlpw")
//                .dbUrl("jdbc:mysql://192.168.0.134:3306/raindrop_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=UTC")
//                .dbPassword("7Dv_v2VxnZ8PgG26f")
                .dbName("root")

                .build();
    }
}
