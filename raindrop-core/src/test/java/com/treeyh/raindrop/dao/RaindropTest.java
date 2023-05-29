package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.BaseTest;
import com.treeyh.raindrop.Raindrop;
import com.treeyh.raindrop.config.RaindropConfig;
import com.treeyh.raindrop.dao.mysql.RaindropWorkerMySqlDAO;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.utils.DateUtils;
import com.treeyh.raindrop.utils.Utils;
import com.treeyh.raindrop.worker.RaindropSnowflakeWorker;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.*;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
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

//    @Test
//    public void testInitFullWorker() {
//        RaindropConfig config = BaseTest.getTestSecondConfig();
//
//        long workerCount = config.getServiceMaxWorkId() - config.getServiceMinWorkId() + 1;
//        for (long i = 0; i < workerCount ; i++) {
//            Raindrop.getInstance().Init(config);
//        }
//        log.info("next init should fail.");
//        Raindrop.getInstance().Init(config);
//        Assertions.fail("The above should not be able to initialize");
//    }

    @Test
    public void testInitRepeatabilityWorker() {
        RaindropConfig config = BaseTest.getTestSecondConfig();

        Raindrop.getInstance().Init(config);
        long workerId = RaindropSnowflakeWorker.getInstance().getWorkerId();

        Raindrop.getInstance().Init(config);
        long workerId2 = RaindropSnowflakeWorker.getInstance().getWorkerId();

        Assertions.assertEquals(workerId, workerId2);
    }

    @Test
    public void testNewIdBenchmark() {
//        Raindrop.getInstance().Init(BaseTest.getTestMillisecondConfig());

        Raindrop.getInstance().Init(BaseTest.getTestSecondConfig());

        Utils.sleep(3 * 1000L);

        CountDownLatch countDownLatch = new CountDownLatch(5);

        ThreadPoolExecutor executor = new ThreadPoolExecutor(10, 10, 10, TimeUnit.SECONDS, new LinkedBlockingQueue<>());

        for (int i =0 ; i < 5; i++){
            Runnable runnable = new Runnable() {
                @Override
                public void run() {
                    try {
                        BaseTest.benchmarkNewId(1, 10000000, true);
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
        int index = 0;
        while (true) {
            index++;
            Utils.sleep(100);
            long time = System.currentTimeMillis();

            try {
                long id = Raindrop.getInstance().newId();
                if (index % 300 == 0) {
                    log.info(DateUtils.date2Str(new Date(time)) + " id:" + id);
                }
            } catch (RaindropException e) {
                log.error(e.getMessage(), e);
            }
            if (time > endTime) {
                break;
            }
        }
    }
}
