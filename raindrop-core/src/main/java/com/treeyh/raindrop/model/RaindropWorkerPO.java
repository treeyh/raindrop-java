package com.treeyh.raindrop.model;

import lombok.*;

import java.util.Date;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
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
