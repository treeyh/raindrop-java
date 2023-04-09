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
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

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
    public void testNewIdBenchmark() {

//        Raindrop.getInstance().Init(BaseTest.getTestMillisecondConfig());

        Raindrop.getInstance().Init(BaseTest.getTestSecondConfig());

        Utils.sleep(3 * 1000L);

        CountDownLatch countDownLatch = new CountDownLatch(1);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (int i =0 ; i < 1; i++){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        benchmarkNewId(1, 10000000, true);
                    } catch (RaindropException e) {
                        log.error(e.getMessage(), e);
                    } finally {
                        countDownLatch.countDown();
                    }
                }
            };
            executor.submit(runnable);
        }
        //关闭线程处理
        try {
            countDownLatch.await();
        } catch (InterruptedException e) {
            log.error(e.getMessage(), e);
        }
        //关闭线程池
        executor.shutdown();
    }

    @Test
    public void testNewIdLongTime() {

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

    /**
     *
     */
    private void benchmarkNewId(int index, int count, boolean logFlag) throws RaindropException {
        Map<Long, Boolean> idMap = new HashMap<>();

        long start = System.currentTimeMillis();

        for (int i = 0; i < count; i ++) {
            Long id = Raindrop.getInstance().newId();

            if (null != idMap.getOrDefault(id, null)) {
                log.error(String.format("benchmarkNewId duplicate id generated: %d", id));
            }
            idMap.put(id, true);
            if (logFlag && i%100000 == 0) {
                log.info(String.format("benchmarkNewId new id index: %d id: %d ", i, id));
            }
        }

        long end = System.currentTimeMillis();
        log.info(String.format("index:%d, count:%d, time:%d", index, count, (end- start)));
    }
}
