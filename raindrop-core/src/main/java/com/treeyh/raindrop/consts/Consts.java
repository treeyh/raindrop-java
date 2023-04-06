package com.treeyh.raindrop.consts;

public class Consts {

    /**
     * 数据库类型mysql
     */
    public final static String DB_TYPE_MYSQL = "mysql";
    /**
     * 数据库类型 postgresql
     */
    public final static String DB_TYPE_POSTGRESQL = "postgresql";


    /**
     * 项目名
     */

    public final static String PROJECT_NAME = "raindrop";

    /**
     * Id模式，雪花
     */
    public final static String ID_MODE_SNOWFLAKE = "snowflake";

    /**
     * Id模式，号段
     */
    public final static String ID_MODE_NUMBER_SECTION = "numbersection";


    /**
     * id长度
     */
    public final static Integer ID_BIT_LENGTH = 63;

    /**
     * 时间回溯长度
     */
    public final static Integer ID_TIME_BACK_BIT_LENGTH=1;


    /**
     * 服务器与DB时间允许间隔，毫秒
     */
    public final static Long DATABASE_TIME_INTERVAL = 30L * 1000L;

    /**
     * 数据库心跳时间间隔，毫秒
     */
    public final static Long HEARTBEAT_TIME_INTERVAL = 30L * 1000L;

}
