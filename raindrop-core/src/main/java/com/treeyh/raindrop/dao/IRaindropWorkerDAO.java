package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.config.RaindropDbConfig;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.model.RaindropWorkerPO;

import java.sql.SQLException;
import java.util.Date;
import java.util.List;

public interface IRaindropWorkerDAO {


    /**
     * 连接注册
     * @param dbConfig
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    void initConn(RaindropDbConfig dbConfig) throws ClassNotFoundException, SQLException;

    /**
     * 获取数据库当前时间
     * @return 数据库当前时间
     */
    Date getNowTime() throws SQLException;


    /**
     * 判断表是否存在
     * @return 是否存在
     */
    Boolean existTable();

    /**
     * 初始化表
     * @return 是否成功
     */
    Boolean initTable();

    /**
     * 初始化workers
     * @param beginId
     * @param endId
     * @return 是否成功
     */
    Boolean initWorkers(Long beginId, Long endId);

    /**
     * 找到该节点之前的worker
     * @param code
     * @return worker
     */
    RaindropWorkerPO getBeforeWorker(String code) ;

    /**
     * 查询空闲的workers
     * @param heartbeatTime
     * @return
     */
    List<RaindropWorkerPO> queryFreeWorkers(Date heartbeatTime);

    /**
     * 激活启用worker
     * @param id
     * @param code
     * @param timeUnit
     * @param version
     * @return
     */
    RaindropWorkerPO activateWorker(Long id, String code, ETimeUnit timeUnit, Long version);

    /**
     * 心跳
     * @param raindropWorkerPO
     * @return
     */
    RaindropWorkerPO heartbeatWorker(RaindropWorkerPO raindropWorkerPO);

    /**
     * 根据id获取worker
     * @param id
     * @return
     */
    RaindropWorkerPO getWorkerById(Long id);
}
