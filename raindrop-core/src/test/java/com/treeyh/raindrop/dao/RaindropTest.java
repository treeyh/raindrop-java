package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.BaseTest;
import com.treeyh.raindrop.Raindrop;
import com.treeyh.raindrop.dao.mysql.RaindropWorkerMySqlDAO;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.utils.DateUtils;
import com.treeyh.raindrop.utils.Utils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.sql.SQLException;

@Slf4j
public class RaindropTest {
    @BeforeAll
    public static void initTest() {
        BaseTest.initTestDataSource();
    }

    @BeforeEach
    public void dropTable() {
        BaseTest.dropTable();
    }

    @Test
    public void testInit() {
        Raindrop.getInstance().Init(BaseTest.getTestMillisecondConfig());

        Utils.sleep(1200 * 1000L);

        Assertions.assertTrue(true);
    }

    @Test
    public void testNewId() {

        Raindrop.getInstance().Init(BaseTest.getTestMillisecondConfig());

        long endTime = System.currentTimeMillis() + (60 * 60 * 1000L);
        Utils.sleep(3000L);
        while (true) {
            Utils.sleep(100);
            long time = System.currentTimeMillis();

            try {
                long id = Raindrop.getInstance().newId();
                log.info(DateUtils.date2Str(new Date(time)) + " id:" + id);
            } catch (RaindropException e) {
                log.error(e.getMessage(), e);
            }
            if (time > endTime) {
                break;
            }
        }
    }
}
