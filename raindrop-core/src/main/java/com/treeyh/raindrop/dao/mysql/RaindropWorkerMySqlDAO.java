package com.treeyh.raindrop.dao.mysql;

import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.dao.IRaindropWorkerDAO;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.model.RaindropWorkerPO;
import com.treeyh.raindrop.utils.DateUtils;
import com.treeyh.raindrop.utils.StrUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Date;
import java.util.List;

@Slf4j
public class RaindropWorkerMySqlDAO implements IRaindropWorkerDAO {

    private Connection conn;

    @Override
    public void initConn(RaindropDbConfig dbConfig) throws ClassNotFoundException, SQLException {

        if (StrUtils.isNotEmpty(dbConfig.getJdbcDriver())) {
            Class.forName(dbConfig.getJdbcDriver());
        }

        HikariDataSource ds = new HikariDataSource();
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
        try {
            conn = ds.getConnection();
        } catch (SQLException e) {
            log.error("init conn fail. "+ e.getMessage(), e);
            throw e;
        }
    }

    private static String sqlGetNow = "SELECT NOW() as now;";
    @Override
    public Date getNowTime() throws SQLException {
        Statement statement = conn.createStatement();

        ResultSet rs = statement.executeQuery(sqlGetNow);
        String date = null;
        while (rs.next()) {
            date = rs.getString("now");
            break;
        }

        if (StrUtils.isNotEmpty(date)) {
            return DateUtils.str2Date(date);
        }
        return null;
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
