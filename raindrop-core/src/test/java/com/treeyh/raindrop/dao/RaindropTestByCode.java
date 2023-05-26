package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.BaseTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
public class RaindropTestByCode {

    @BeforeAll
    public static void initTest() {
        BaseTest.initTestDataSource();
    }

    @BeforeEach
    public void dropTable() {
        BaseTest.dropTable();
    }


}
