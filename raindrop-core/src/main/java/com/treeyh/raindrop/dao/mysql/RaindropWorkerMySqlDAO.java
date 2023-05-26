package com.treeyh.raindrop.dao.mysql;

import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.dao.AbstractRaindropWorkerDAO;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.model.RaindropWorkerPO;
import com.treeyh.raindrop.utils.DateUtils;
import com.treeyh.raindrop.utils.StrUtils;
import com.zaxxer.hikari.HikariDataSource;
import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Slf4j
public class RaindropWorkerMySqlDAO extends AbstractRaindropWorkerDAO {

    private HikariDataSource ds;

    private static final String tableName = "soc_raindrop_worker";

    private static final  String columnName = "value";
    private static final String sqlGetNow = "SELECT NOW() AS "+columnName+";";

    private static final String sqlGetDbName = "SELECT DATABASE() AS "+columnName+";";

    private static final String sqlExistTable = "SELECT count(*) AS "+columnName+" FROM information_schema.tables " +
            "WHERE table_schema = ? AND table_name = ? AND table_type = ?";

    private static final String sqlPreSelectRow = "SELECT `id`, `code`, `time_unit`, `heartbeat_time`, `create_time`, " +
            "`update_time`, `version`, `del_flag` FROM " + tableName + " WHERE `del_flag` = 2 ";
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
    public Boolean initTableWorkers(Long beginId, Long endId) {
        if (beginId > endId) {
            log.error("endId must be greater than beginId");
            return false;
        }

        List<String> ls = new ArrayList<>();
        int count = 0;
        for (Long i = beginId; i<=endId; i++) {
            ls.add("("+i.toString()+", '2023-01-01 00:00:00')");
            count++;
        }
        String sql = " INSERT INTO " + tableName + "(`id`, `heartbeat_time`) VALUES " + String.join(",", ls) + ";";

        Connection conn = null;
        Statement statement = null;
        Boolean result =false;
        try {
            conn = ds.getConnection();

            statement = conn.createStatement();
            statement.addBatch(sqlInitTable);
            statement.addBatch(sql);

            int[] results = statement.executeBatch();
            if (Objects.equals(results.length, 2) &&
                    Objects.equals(results[1], count)) {
                result = true;
            }
            statement.clearBatch();
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return result;
    }

    @Override
    public RaindropWorkerPO getBeforeWorker(String code) {

        String sql = sqlPreSelectRow + " AND `code` = ? ORDER BY `id` asc LIMIT 0,1 ";

        Connection conn = null;
        PreparedStatement statement = null;
        RaindropWorkerPO po = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sql);
            statement.setString(1, code);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                po = buildRaindropWorker(rs);
                break;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return po;
    }

    private RaindropWorkerPO buildRaindropWorker(ResultSet rs) {
        RaindropWorkerPO po = new RaindropWorkerPO();
        try {
            po.setId(rs.getLong("id"));
            po.setCode(rs.getString("code"));
            po.setTimeUnit(rs.getInt("time_unit"));
            po.setHeartbeatTime(DateUtils.str2Date(rs.getString("heartbeat_time")));
            po.setCreateTime(DateUtils.str2Date(rs.getString("create_time")));
            po.setUpdateTime(DateUtils.str2Date(rs.getString("update_time")));
            po.setVersion(rs.getLong("version"));
            po.setDelFlag(rs.getInt("del_flag"));
            return po;
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public List<RaindropWorkerPO> queryFreeWorkers(Date heartbeatTime) {
        String sql = sqlPreSelectRow + " AND `heartbeat_time` < ? ";

        Connection conn = null;
        PreparedStatement statement = null;
        List<RaindropWorkerPO> pos = new ArrayList<>();
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sql);
            statement.setString(1, DateUtils.date2Str(heartbeatTime));
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                pos.add(buildRaindropWorker(rs));
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return pos;
    }

    @Override
    public RaindropWorkerPO activateWorker(Long id, String code, int timeUnit, Long version) {
        String sql = "UPDATE `" + tableName + "` SET `code` = ?, `time_unit` = ?, `version` = `version` + 1, `heartbeat_time` = ? WHERE `id` = ? AND `version` = ? ";

        Connection conn = null;
        PreparedStatement statement = null;
        RaindropWorkerPO po = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sql);
            statement.setString(1, code);
            statement.setInt(2, timeUnit);
            statement.setString(3, DateUtils.date2Str(new Date()));
            statement.setLong(4, id);
            statement.setLong(5, version);
            Integer count = statement.executeUpdate();

            if (count != 1) {
                return null;
            }

            return getWorkerById(id);
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return null;
    }

    @Override
    public RaindropWorkerPO heartbeatWorker(RaindropWorkerPO raindropWorkerPO) {
        String sql = "UPDATE `" + tableName + "` SET `version` = `version` + 1, `heartbeat_time` = ? WHERE `id` = ? AND `version` = ? ";

        Connection conn = null;
        PreparedStatement statement = null;
        RaindropWorkerPO po = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sql);
            statement.setString(1, DateUtils.date2Str(new Date()));
            statement.setLong(2, raindropWorkerPO.getId());
            statement.setLong(3, raindropWorkerPO.getVersion());
            Integer count = statement.executeUpdate();
            if (count != 1) {
                log.error("heartbeat worker fail!!! id:"+raindropWorkerPO.getId()+" result: "+ count);
            }
            po = getWorkerById(raindropWorkerPO.getId());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        if (null == po || Objects.equals(po.getId(), 0)){
            raindropWorkerPO.setVersion(raindropWorkerPO.getVersion()+1);
            return raindropWorkerPO;
        }
        return po;
    }

    @Override
    public RaindropWorkerPO getWorkerById(Long id) {
        String sql = sqlPreSelectRow + " AND `id` = ? ";

        Connection conn = null;
        PreparedStatement statement = null;
        RaindropWorkerPO po = null;
        try {
            conn = ds.getConnection();
            statement = conn.prepareStatement(sql);
            statement.setLong(1, id);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                po = buildRaindropWorker(rs);
                break;
            }
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
        }finally {
            close(conn, statement);
        }
        return po;
    }
}
