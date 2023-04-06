package com.treeyh.raindrop.worker;

import com.treeyh.raindrop.Raindrop;
import com.treeyh.raindrop.config.RaindropConfig;
import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.consts.ErrorConsts;
import com.treeyh.raindrop.dao.AbstractRaindropWorkerDAO;
import com.treeyh.raindrop.dao.mysql.RaindropWorkerMySqlDAO;
import com.treeyh.raindrop.exception.RaindropException;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Date;
import java.util.Objects;

@Slf4j
public class RaindropWorker {

    private AbstractRaindropWorkerDAO raindropWorkerDAO;

    private RaindropWorker(){}

    private static class Instance {
        private static final RaindropWorker INSTANCE = new RaindropWorker();
    }

    /**
     * 单例
     * @return
     */
    public static RaindropWorker getInstance() {
        return RaindropWorker.Instance.INSTANCE;
    }

    /**
     * 初始化数据库
     * @param config
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public synchronized void initDb(RaindropConfig config) throws SQLException, ClassNotFoundException, RaindropException {
        if (!Objects.equals(raindropWorkerDAO, null)) {
            return;
        }

        if (Objects.equals(config.getDbConfig().getDbType(), Consts.DB_TYPE_MYSQL)) {
            raindropWorkerDAO = new RaindropWorkerMySqlDAO();
            raindropWorkerDAO.initConn(config.getDbConfig());
        }

        raindropWorkerDAO.checkDbTimeInterval();

        raindropWorkerDAO.initTableInfo(config.getServiceMinWorkId(), config.getServiceMaxWorkId());
    }
}
