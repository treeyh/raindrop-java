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

@Slf4j
public class RaindropWorkerMySqlDAO implements IRaindropWorkerDAO {

    private HikariDataSource ds;

    private static final  String columnName = "value";
    private static final String sqlGetNow = "SELECT NOW() as "+columnName+";";

    private static final String sqlGetDbName = "SELECT DATABASE() as "+columnName+";";

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
            if (null != statement) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
            if(null != conn) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return date;
    }

    private String getDbName() {
        Connection conn = null;
        PreparedStatement statement = null;
        String dbName = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sqlGetNow);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                dbName = rs.getString(columnName);
                break;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            if (null != statement) {
                try {
                    statement.close();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        return dbName;
    }

    @Override
    public Boolean existTable() {
        return null;
    }

    @Override
    public Boolean initTable() {
        return null;
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
