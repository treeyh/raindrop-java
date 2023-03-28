package com.treeyh.raindrop.model;

import lombok.Data;

import java.util.Date;

@Data
public class RaindropWorkerPO {
    private Long id;

    private String code;

    private Integer timeUnit;

    private Date heartbeatTime;

    private Date createTime;

    private Date updateTime;

    private Long version;

    private Integer delFlag;
}
