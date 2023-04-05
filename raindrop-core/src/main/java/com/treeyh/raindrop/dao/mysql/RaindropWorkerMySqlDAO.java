package com.treeyh.raindrop.dao.mysql;

import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.dao.IRaindropWorkerDAO;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.model.RaindropWorkerPO;
import com.treeyh.raindrop.utils.DateUtils;
import com.treeyh.raindrop.utils.StrUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Slf4j
public class RaindropWorkerMySqlDAO implements IRaindropWorkerDAO {

    private HikariDataSource ds;

    private static final String tableName = "soc_raindrop_worker";

    private static final  String columnName = "value";
    private static final String sqlGetNow = "SELECT NOW() AS "+columnName+";";

    private static final String sqlGetDbName = "SELECT DATABASE() AS "+columnName+";";

    private static final String sqlExistTable = "SELECT count(*) AS "+columnName+" FROM information_schema.tables " +
            "WHERE table_schema = ? AND table_name = ? AND table_type = ?";

    private static final String sqlInitTable = "CREATE TABLE `" + tableName + "` (\n" +
            "\t`id` bigint NOT NULL,\n" +
            "\t`code` varchar(128) COLLATE utf8mb4_general_ci NOT NULL DEFAULT '',\n" +
            "\t`time_unit` tinyint NOT NULL DEFAULT '2',\n" +
            "\t`heartbeat_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "\t`create_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP,\n" +
            "\t`update_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,\n" +
            "\t`version` bigint NOT NULL DEFAULT '1',\n" +
            "\t`del_flag` tinyint NOT NULL DEFAULT '2',\n" +
            "\tPRIMARY KEY (`id`),\n" +
            "\tKEY `idx_soc_raindrop_worker_heartbeat_time` (`heartbeat_time`),\n" +
            "\tKEY `idx_soc_raindrop_worker_code` (`code`)\n" +
            "\t) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;";

    @Override
    public void initConn(RaindropDbConfig dbConfig) throws ClassNotFoundException, SQLException {

        if (StrUtils.isNotEmpty(dbConfig.getJdbcDriver())) {
            Class.forName(dbConfig.getJdbcDriver());
        }

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

    private void close(Connection conn, Statement statement) {
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

    @Override
    public Date getNowTime() {
        Connection conn = null;
        PreparedStatement statement = null;
        Date date = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlGetNow);
            ResultSet rs = statement.executeQuery();
            String d = null;
            while (rs.next()) {
                d = rs.getString(columnName);
                break;
            }

            if (StrUtils.isNotEmpty(d)) {
                date = DateUtils.str2Date(d);
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return date;
    }

    private String getDbName() {
        Connection conn = null;
        PreparedStatement statement = null;
        String dbName = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlGetDbName);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                dbName = rs.getString(columnName);
                break;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return dbName;
    }

    @Override
    public Boolean existTable() {

        String dbName = getDbName();
        if (StrUtils.isEmpty(dbName)) {
            return false;
        }

        Connection conn = null;
        PreparedStatement statement = null;
        Integer count = 0;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlExistTable);
            statement.setString(1, dbName);
            statement.setString(2, tableName);
            statement.setString(3, "BASE TABLE");
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                count = rs.getInt(columnName);
                break;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return Objects.equals(count, 1);
    }

    @Override
    public Boolean initTable() {

        Connection conn = null;
        PreparedStatement statement = null;
        Boolean result =false;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlInitTable);
            result = statement.execute();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return result;
    }

    @Override
    public Boolean initWorkers() {
        return null;
    }

    @Override
    public RaindropWorkerPO getBeforeWorker(String code, ETimeUnit timeUnit) {
        return null;
    }

    @Override
    public List<RaindropWorkerPO> queryFreeWorkers(Date heartbeatTime) {
        return null;
    }

    @Override
    public RaindropWorkerPO activateWorker(Long id, String code, ETimeUnit timeUnit, Long version) {
        return null;
    }

    @Override
    public RaindropWorkerPO heartbeatWorker(RaindropWorkerPO raindropWorkerPO) {
        return null;
    }

    @Override
    public RaindropWorkerPO getWorkerById(Long id) {
        return null;
    }
}
