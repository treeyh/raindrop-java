package com.treeyh.raindrop.config;


import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.consts.ErrorConsts;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.utils.DateUtils;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.Objects;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Builder
@ToString
@Data
@Slf4j
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
    private int servicePort;

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
    private int timeStampLength;

    /**
     * PriorityEqualCodeWorkId 优先相同code的workerId(毫秒，秒单位场景下生效)，默认：false。code格式为：{内网ip}:{ServicePort}#{Mac地址}
     */
    private boolean priorityEqualCodeWorkId;

    /**
     * WorkIdLength 工作节点 id 长度，取值范围 4 - 10 位.
     */
    private int workIdLength;

    /**
     * ServiceMinWorkId 服务的最小工作节点 id，默认 1，需在 workIdLength 的定义范围内，最大值最小值用于不同数据中心的隔离。
     */
    private long serviceMinWorkId;

    /**
     * ServiceMaxWorkId 服务的最大工作节点 id，默认 workIdLength 的最大值，需在 workIdLength 的定义范围内。
     */
    private long serviceMaxWorkId;

    /**
     * TimeBackBitValue 时间回拨位初始值，支持 `0` 或 `1`，默认： `0`；
     */
    private int timeBackBitValue;

    /**
     *  EndBitsLength 可选预留位长度，支持`0`-`5`, 如果不需要可以设置为 `0`, 建议设置为 `1`
     */
    private int endBitsLength;

    /**
     * EndBitsValue 可选预留位的值，默认： `0`
     */
    private int endBitValue;


    /**
     * 最大端口
     */
    private final static int maxPort = 65535;

    /**
     * 验证配置
     * @throws RaindropException
     */
    public void checkConfig() throws RaindropException {
        String mode = this.idMode.toLowerCase();
        if (!Objects.equals(mode, Consts.ID_MODE_SNOWFLAKE) && !Objects.equals(mode, Consts.ID_MODE_NUMBER_SECTION)) {
            this.idMode = Consts.ID_MODE_SNOWFLAKE;
        }

        if (this.servicePort < 0 || this.servicePort > maxPort) {
            log.error(ErrorConsts.SERVER_PORT_ERROR + "; servicePort:" + this.servicePort);
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.SERVER_PORT_ERROR);
        }

        this.checkTimeUnitConfig();

        this.checkWorkIdConfig();

        if (System.currentTimeMillis() < this.startTimeStamp.getTime()) {
            log.error(ErrorConsts.START_TIMESTAMP_ERROR + "; startTimeStamp:"+ DateUtils.date2Str(this.startTimeStamp));
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.START_TIMESTAMP_ERROR);
        }

        if (!Objects.equals(this.timeBackBitValue, 0) && !Objects.equals(this.timeBackBitValue, 1)) {
            log.error(ErrorConsts.TIME_BACK_BIT_VALUE_ERROR+"; timeBackBitValue:"+this.timeBackBitValue);
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_BACK_BIT_VALUE_ERROR);
        }

        if (this.endBitsLength < 0 || this.endBitsLength > 5) {
            log.error(ErrorConsts.END_BITS_LENGTH_ERROR + "; endBitsLength:"+ this.endBitsLength);
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.END_BITS_LENGTH_ERROR);
        } else if (this.endBitsLength > 0) {
            Integer maxEndBitsValue = (1 << this.endBitsLength) -1;
            if (this.endBitValue > maxEndBitsValue || this.endBitsLength < 0) {
                log.error(ErrorConsts.END_BITS_VALUE_ERROR + "; endBitValue:" + this.endBitValue);
                throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.END_BITS_VALUE_ERROR);
            }
        } else {
            this.endBitValue = 0;
        }

        Integer seqLength = Consts.ID_BIT_LENGTH - this.timeStampLength - this.workIdLength - Consts.ID_TIME_BACK_BIT_LENGTH - this.endBitsLength;

        if (seqLength < 1) {
            log.error(ErrorConsts.SEQUENCE_LENGTH_ERROR + "; seqLength:"+ seqLength);
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.SEQUENCE_LENGTH_ERROR);
        }
    }

    /**
     * 验证TimeUnit
     * @throws RaindropException
     */
    private void checkTimeUnitConfig() throws RaindropException {

        switch (this.timeUnit) {
            case Millisecond:
                if(this.timeStampLength < 41 || this.timeStampLength > 55) {
                    log.error(ErrorConsts.TIME_LENGTH_ERROR_MILLISECOND + "; timeStampLength:" + this.timeStampLength);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_LENGTH_ERROR_MILLISECOND);
                }
                break;
            case Second:
                if(this.timeStampLength < 31 || this.timeStampLength > 55) {
                    log.error(ErrorConsts.TIME_LENGTH_ERROR_SECOND + "; timeStampLength:" + this.timeStampLength);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_LENGTH_ERROR_SECOND);
                }
                break;
            case Minuter:
                if(this.timeStampLength < 25 || this.timeStampLength > 50) {
                    log.error(ErrorConsts.TIME_LENGTH_ERROR_MINUTE + "; timeStampLength:" + this.timeStampLength);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_LENGTH_ERROR_MINUTE);
                }
                break;
            case Hour:
                if(this.timeStampLength < 19 || this.timeStampLength > 45) {
                    log.error(ErrorConsts.TIME_LENGTH_ERROR_HOUR + "; timeStampLength:" + this.timeStampLength);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_LENGTH_ERROR_HOUR);
                }
                break;
            case Day:
                if(this.timeStampLength < 15 || this.timeStampLength > 40) {
                    log.error(ErrorConsts.TIME_LENGTH_ERROR_DAY + "; timeStampLength:" + this.timeStampLength);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_LENGTH_ERROR_DAY);
                }
                break;
            default:
                log.error(ErrorConsts.TIME_LENGTH_ERROR+"; timeUnit:"+this.timeUnit.getType());
                throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.TIME_LENGTH_ERROR);
        }
    }


    /**
     * 验证WorkId
     * @throws RaindropException
     */
    private void checkWorkIdConfig() throws RaindropException {
        if (this.workIdLength < 3 || this.workIdLength > 10) {
            log.error(ErrorConsts.WORK_ID_LENGTH_ERROR + "; workIdLength:" + this.workIdLength);
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_LENGTH_ERROR);
        }

        if (this.serviceMinWorkId > this.serviceMaxWorkId) {
            log.error(ErrorConsts.SERVICE_MAX_MIN_WORK_ID_ERROR + "; serviceMinWorkId:" +
                    this.serviceMinWorkId+ "; serviceMaxWorkId:"+this.serviceMaxWorkId);
            throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.SERVICE_MAX_MIN_WORK_ID_ERROR);
        }

        switch (this.workIdLength) {
            case 3:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 7) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_3 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_3);
                }
                break;
            case 4:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 15) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_4 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_4);
                }
                break;
            case 5:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 31) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_5 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_5);
                }
                break;
            case 6:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 63) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_6 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_6);
                }
                break;
            case 7:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 127) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_7 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_7);
                }
                break;
            case 8:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 255) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_8 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_8);
                }
                break;
            case 9:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 511) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_9 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_9);
                }
                break;
            case 10:
                if(this.serviceMinWorkId < 1 || this.serviceMaxWorkId > 1023) {
                    log.error(ErrorConsts.WORK_ID_RANGE_ERROR_10 + "; serviceMinWorkId:" + this.serviceMinWorkId+
                            "; serviceMaxWorkId:"+ this.serviceMaxWorkId);
                    throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR_10);
                }
                break;
            default:
                log.error(ErrorConsts.WORK_ID_RANGE_ERROR + "; workIdLength:" + this.workIdLength);
                throw new RaindropException(ErrorConsts.CHECK_CONFIG_ERROR, ErrorConsts.WORK_ID_RANGE_ERROR);
        }
    }
}
