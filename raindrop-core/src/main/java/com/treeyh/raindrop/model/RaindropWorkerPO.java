package com.treeyh.raindrop.model;

import lombok.*;

import java.util.Date;

@Data
@ToString
@RequiredArgsConstructor
public class RaindropWorkerPO {
    private long id;

    private String code;

    private int timeUnit;

    private Date heartbeatTime;

    private Date createTime;

    private Date updateTime;

    private long version;

    private int delFlag;
}
