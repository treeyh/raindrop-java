package com.treeyh.raindrop.utils;

import java.util.Objects;

public class StrUtils {

    public static Boolean isEmpty(String val){
        return null == val || Objects.equals(val, "");
    }

    public static Boolean isNotEmpty(String val) {
        return !isEmpty(val);
    }
}
