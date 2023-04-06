package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.consts.ErrorConsts;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.model.RaindropWorkerPO;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

@Slf4j
public abstract class AbstractRaindropWorkerDAO {



    /**
     * 校验服务器与数据库时间差异
     * @throws SQLException
     * @throws RaindropException
     */
    public void checkDbTimeInterval() throws SQLException, RaindropException {
        Long now = System.currentTimeMillis();

        Date dbNow = this.getNowTime();

        if ( now > (dbNow.getTime() + Consts.DATABASE_TIME_INTERVAL) || now < (dbNow.getTime() - Consts.DATABASE_TIME_INTERVAL)) {
            log.error("Server and database time gap exceeds threshold. now:"+ now + "; dbNow:"+ dbNow.getTime());
            throw new RaindropException(ErrorConsts.DATABASE_SERVER_TIME_INTERVAL_ERROR, "Server and database time gap exceeds threshold");
        }
    }

    /**
     * 初始化表和workers
     * @param beginId
     * @param endId
     * @throws RaindropException
     */
    public void initTableInfo(Long beginId, Long endId) throws RaindropException {
        if (this.existTable()) {
            return;
        }

        if (this.initTableWorkers(beginId, endId)) {
            throw new RaindropException(ErrorConsts.INIT_TABLE_ERROR, "Init table fail.");
        }
    }

    /**
     * 连接注册
     * @param dbConfig
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public abstract void initConn(RaindropDbConfig dbConfig) throws ClassNotFoundException, SQLException;

    /**
     * 获取数据库当前时间
     * @return 数据库当前时间
     */
    public abstract Date getNowTime() throws SQLException;


    /**
     * 判断表是否存在
     * @return 是否存在
     */
    public abstract Boolean existTable();

    /**
     * 初始化workers
     * @param beginId
     * @param endId
     * @return 是否成功
     */
    public abstract Boolean initTableWorkers(Long beginId, Long endId);

    /**
     * 找到该节点之前的worker
     * @param code
     * @return worker
     */
    public abstract RaindropWorkerPO getBeforeWorker(String code) ;

    /**
     * 查询空闲的workers
     * @param heartbeatTime
     * @return
     */
    public abstract List<RaindropWorkerPO> queryFreeWorkers(Date heartbeatTime);

    /**
     * 激活启用worker
     * @param id
     * @param code
     * @param timeUnit
     * @param version
     * @return
     */
    public abstract RaindropWorkerPO activateWorker(Long id, String code, ETimeUnit timeUnit, Long version);

    /**
     * 心跳
     * @param raindropWorkerPO
     * @return
     */
    public abstract RaindropWorkerPO heartbeatWorker(RaindropWorkerPO raindropWorkerPO);

    /**
     * 根据id获取worker
     * @param id
     * @return
     */
    public abstract RaindropWorkerPO getWorkerById(Long id);
}
