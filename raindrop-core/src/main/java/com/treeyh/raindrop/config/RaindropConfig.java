package com.treeyh.raindrop.config;


import com.treeyh.raindrop.model.ETimeUnit;
import lombok.Data;

import java.util.Date;

@Data
public class RaindropConfig {

    /**
     * IdMode Id生成模式， Snowflake：雪花算法；NumberSection：号段模式，目前仅支持Snowflake
     */
    private String idMode;

    /**
     * DbConfig 数据库配置
     */
    private RaindropDbConfig dbConfig;

    /**
     * ServicePort 服务端口
     */
    private Integer servicePort;

    /**
     * TimeUnit 时间戳单位, 1：毫秒（可能会有闰秒问题）；2：秒，默认；3：分钟；4：小时，间隔过大不建议选择；5：天，间隔过大不建议选择；
     */
    private ETimeUnit timeUnit;

    /**
     * StartTimeStamp 起始时间，时间戳从该时间开始计时，
     */
    private Date startTimeStamp;

    /**
     * TimeStampLength 时间戳位数
     */
    private Integer timeStampLength;

    /**
     * PriorityEqualCodeWorkId 优先相同code的workerId(毫秒，秒单位场景下生效)，默认：false。code格式为：{内网ip}:{ServicePort}#{Mac地址}
     */
    private Boolean priorityEqualCodeWorkId;

    /**
     * WorkIdLength 工作节点 id 长度，取值范围 4 - 10 位.
     */
    private Integer workIdLength;

    /**
     * ServiceMinWorkId 服务的最小工作节点 id，默认 1，需在 workIdLength 的定义范围内，最大值最小值用于不同数据中心的隔离。
     */
    private Long serviceMinWorkId;

    /**
     * ServiceMaxWorkId 服务的最大工作节点 id，默认 workIdLength 的最大值，需在 workIdLength 的定义范围内。
     */
    private Long serviceMaxWorkId;

    /**
     * TimeBackBitValue 时间回拨位初始值，支持 `0` 或 `1`，默认： `0`；
     */
    private Integer timeBackBitValue;

    /**
     *  EndBitsLength 可选预留位长度，支持`0`-`5`, 如果不需要可以设置为 `0`, 建议设置为 `1`
     */
    private Integer endBitsLength;

    /**
     * EndBitsValue 可选预留位的值，默认： `0`
     */
    private Integer endBitValue;

}
