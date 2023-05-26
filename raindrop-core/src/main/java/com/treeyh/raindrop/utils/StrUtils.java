package com.treeyh.raindrop.utils;

import java.util.Objects;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
public class StrUtils {

    public static Boolean isEmpty(String val){
        return null == val || Objects.equals(val, "");
    }

    public static Boolean isNotEmpty(String val) {
        return !isEmpty(val);
    }
}
