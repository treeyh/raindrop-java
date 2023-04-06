package com.treeyh.raindrop.consts;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
public class ErrorConsts {

    /**
     * 验证配置文件错误
     */
    public final static Integer CHECK_CONFIG_ERROR = 1001;

    /**
     * 数据库初始化失败
     */
    public final static Integer INIT_DB_ERROR = 1002;

    /**
     * 初始化表结构失败
     */
    public final static Integer INIT_TABLE_ERROR = 1003;

    /**
     * 校验服务器时间和db时间间隔超过阈值
     */
    public final static Integer DATABASE_SERVER_TIME_INTERVAL_ERROR = 1004;


    /**
     * 服务端口设置错误
     */
    public final static String SERVER_PORT_ERROR = "ServicePort range between 0 and 65535";

    /**
     * 时间位长度错误，毫秒
     */
    public final static String TIME_LENGTH_ERROR_MILLISECOND = "When TimeUnit is millisecond, TimeLength must be between 41 and 55";

    /**
     * 时间位长度错误，秒
     */
    public final static String TIME_LENGTH_ERROR_SECOND = "When TimeUnit is second, TimeLength must be between 31 and 55";

    /**
     * 时间位长度错误，分钟
     */
    public final static String TIME_LENGTH_ERROR_MINUTE = "When TimeUnit is minute, TimeLength must be between 25 and 50";

    /**
     * 时间位长度错误，小时
     */
    public final static String TIME_LENGTH_ERROR_HOUR = "When TimeUnit is hour, TimeLength must be between 19 and 45";

    /**
     * 时间位长度错误，天
     */
    public final static String TIME_LENGTH_ERROR_DAY = "When TimeUnit is day, TimeLength must be between 15 and 40";

    /**
     * 时间类型取值错误
     */
    public final static String TIME_LENGTH_ERROR = "TimeUnit has the wrong value range";

    /**
     * workid长度
     */
    public final static String WORK_ID_LENGTH_ERROR = "WorkIdLength takes values between 3 and 10";

    /**
     * 服务最大工作id，
     */
    public final static String SERVICE_MAX_MIN_WORK_ID_ERROR = "ServiceMaxWorkId must be greater than ServiceMinWorkId";

    /**
     * workid区间错误，3
     */
    public final static String WORK_ID_RANGE_ERROR_3 = "When WorkIdLength is 3, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 7";

    /**
     * workid区间错误，4
     */
    public final static String WORK_ID_RANGE_ERROR_4 = "When WorkIdLength is 4, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 15";

    /**
     * workid区间错误，5
     */
    public final static String WORK_ID_RANGE_ERROR_5 = "When WorkIdLength is 5, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 31";

    /**
     * workid区间错误，6
     */
    public final static String WORK_ID_RANGE_ERROR_6 = "When WorkIdLength is 6, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 63";

    /**
     * workid区间错误，7
     */
    public final static String WORK_ID_RANGE_ERROR_7 = "When WorkIdLength is 7, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 127";

    /**
     * workid区间错误，8
     */
    public final static String WORK_ID_RANGE_ERROR_8 = "When WorkIdLength is 8, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 255";

    /**
     * workid区间错误，9
     */
    public final static String WORK_ID_RANGE_ERROR_9 = "When WorkIdLength is 9, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 511";

    /**
     * workid区间错误，10
     */
    public final static String WORK_ID_RANGE_ERROR_10 = "When WorkIdLength is 10, ServiceMinWorkId and ServiceMaxWorkId take values in the range of 1 to 1023";

    /**
     * workIdLength 取值超过区间
     */
    public final static String WORK_ID_RANGE_ERROR = "WorkIdLength has the wrong value range";

    /**
     * 开始时间戳超过当前时间
     */
    public final static String START_TIMESTAMP_ERROR = "StartTimeStamp is greater than the current time";

    /**
     * 时间回溯标识错误
     */
    public final static String TIME_BACK_BIT_VALUE_ERROR = "TimeBackBitValue value is 0 or 1";

    /**
     * 预留位长度错误
     */
    public final static String END_BITS_LENGTH_ERROR = "EndBitsLength needs to be between 0 and 5";

    /**
     * 预留值错误
     */
    public final static String END_BITS_VALUE_ERROR = "EndBitsValue The value range is not in the EndBitsLength range";

    /**
     * 流水号长度错误
     */
    public final static String SEQUENCE_LENGTH_ERROR = "Sequence number occupies at least 1 bit";
}
