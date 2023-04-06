package com.treeyh.raindrop.model;


public enum  ETimeUnit {
    Millisecond(1),
    Second(2),
    Minuter(3),
    Hour(4),
    Day(5);

    private Integer type;
    private ETimeUnit(Integer type){
        this.type = type;
    }

    public Integer getType() {
        return this.type;
    }
}
