package com.treeyh.raindrop;

import com.treeyh.raindrop.config.RaindropConfig;
import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.utils.DateUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Slf4j
public class BaseTest {

    private static HikariDataSource ds;

    public static int servicePort = 65510;

    private static String tableName = "soc_raindrop_worker";

    public static long beginId = 10L;

    public static long endId = 15L;

    public static Date startTimeStamp = DateUtils.str2Date("2023-01-01 00:00:00");

    public static RaindropDbConfig getDefaultRaindropDbConfig() {
        return RaindropDbConfig.builder().dbType(Consts.DB_TYPE_MYSQL)
                .dbUrl("jdbc:mysql://192.168.40.211:3306/raindrop_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=UTC")
                .dbPassword("2Dv_v2VxnZ8PgG26f")
//                .dbUrl("jdbc:mysql://192.168.0.134:3306/raindrop_db?useUnicode=true&characterEncoding=UTF-8&autoReconnect=true&useSSL=false&zeroDateTimeBehavior=convertToNull&useSSL=true&serverTimezone=UTC")
//                .dbPassword("7Dv_v2VxnZ8PgG26f")
                .dbName("root")
                .dbType(Consts.DB_TYPE_MYSQL)
                .tableName(tableName)
                .build();
    }

    public static RaindropConfig getTestMillisecondConfig() {
        return RaindropConfig.builder()
                .dbConfig(getDefaultRaindropDbConfig())
                .idMode(Consts.ID_MODE_SNOWFLAKE)
                .servicePort(servicePort)
                .timeUnit(ETimeUnit.Millisecond)
                .startTimeStamp(startTimeStamp)
                .timeStampLength(44)
                .priorityEqualCodeWorkId(false)
                .workIdLength(4)
                .serviceMinWorkId(beginId)
                .serviceMaxWorkId(endId)
                .timeBackBitValue(0)
                .endBitsLength(1)
                .endBitValue(0)
                .build();
    }

    public static RaindropConfig getTestSecondConfig() {
        return RaindropConfig.builder()
                .dbConfig(getDefaultRaindropDbConfig())
                .idMode(Consts.ID_MODE_SNOWFLAKE)
                .servicePort(servicePort)
                .timeUnit(ETimeUnit.Second)
                .startTimeStamp(startTimeStamp)
                .timeStampLength(31)
                .priorityEqualCodeWorkId(false)
                .workIdLength(4)
                .serviceMinWorkId(beginId)
                .serviceMaxWorkId(endId)
                .timeBackBitValue(0)
                .endBitsLength(1)
                .endBitValue(0)
                .build();
    }

    /**
     * 初始化测试数据源
     */
    public static void initTestDataSource() {
        if (ds != null) {
            return;
        }

        RaindropDbConfig dbConfig = getDefaultRaindropDbConfig();

        ds = new HikariDataSource();
        ds.setJdbcUrl(dbConfig.getDbUrl());
        ds.setUsername(dbConfig.getDbName());
        ds.setPassword(dbConfig.getDbPassword());
        // HikariCP提供的优化设置
        ds.addDataSourceProperty("cachePrepStmts", "true");
        ds.addDataSourceProperty("prepStmtCacheSize", "250");
        ds.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        ds.addDataSourceProperty("useServerPrepStmts", "true");
        ds.addDataSourceProperty("cacheServerConfiguration", "true");
        ds.addDataSourceProperty("elideSetAutoCommits", "true");
        ds.addDataSourceProperty("maintainTimeStats", "false");

        ds.addDataSourceProperty("maximumPoolSize", 2);
        ds.addDataSourceProperty("minimumIdle", 2);
    }

    public static void dropTable() {
        String dropSql = "DROP TABLE soc_raindrop_worker;";

        Connection conn = null;
        PreparedStatement statement = null;
        Integer count = 0;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(dropSql);
            int result = statement.executeUpdate();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            if (null != statement) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
            if(null != conn) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
    }

    @Test
    public void testTest() {
        AtomicLong seq  = new AtomicLong();

        System.out.println(seq.incrementAndGet());
        System.out.println(seq.incrementAndGet());
        System.out.println(seq.incrementAndGet());
        System.out.println(seq.incrementAndGet());
        System.out.println(seq.incrementAndGet());
        System.out.println(seq.incrementAndGet());
        System.out.println(seq.incrementAndGet());

    }



    /**
     *
     */
    public static void benchmarkNewId(int index, int count, boolean logFlag) throws RaindropException {
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
