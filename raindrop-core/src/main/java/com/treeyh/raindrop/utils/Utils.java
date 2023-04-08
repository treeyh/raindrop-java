package com.treeyh.raindrop.utils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class Utils {

    public static void sleep(long time) {
        try {
            Thread.sleep(time);
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
    }
}
