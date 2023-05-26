package com.treeyh.raindrop.model;


/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
public enum  ETimeUnit {
    /**
     * 间隔单位，毫秒
     */
    Millisecond(1),
    /**
     * 间隔单位，秒
     */
    Second(2),
    /**
     * 间隔单位，分钟
     */
    Minuter(3),
    /**
     * 间隔单位，小时
     */
    Hour(4),
    /**
     * 间隔单位，天
     */
    Day(5);

    private Integer type;
    private ETimeUnit(Integer type){
        this.type = type;
    }

    public Integer getType() {
        return this.type;
    }
}
