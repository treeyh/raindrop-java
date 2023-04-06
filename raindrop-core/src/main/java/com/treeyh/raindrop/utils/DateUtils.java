package com.treeyh.raindrop.utils;

import lombok.extern.slf4j.Slf4j;

import java.text.SimpleDateFormat;
import java.util.Date;

@Slf4j
public class DateUtils {

    public static Date str2Date(String val) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        Date date = null;
        try {
            date = sdf1.parse(val);
        }catch (Exception e){
            log.error("str2Date error. "+ e.getMessage(), e);
        }
        return date;
    }

    public static String date2Str(Date date) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf1.format(date);
    }
}
