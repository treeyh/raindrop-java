package com.treeyh.raindrop;

import com.treeyh.raindrop.config.RaindropConfig;
import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.consts.ErrorConsts;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.worker.RaindropWorker;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;

@Slf4j
public class Raindrop {

    private Raindrop(){}

    private static class Instance {
        private static final Raindrop INSTANCE = new Raindrop();
    }

    /**
     * 单例
     * @return
     */
    public static Raindrop getInstance() {
        return Instance.INSTANCE;
    }

    public void Init(RaindropConfig config){
        try {
            // TODO 值修改是否返回
            config.checkConfig();

            RaindropWorker.getInstance().initDb(config);
        } catch (RaindropException e) {
            log.error(e.getMessage(), e);
            System.exit(e.getCode());
        } catch (SQLException e) {
            log.error(e.getMessage(), e);
            System.exit(ErrorConsts.INIT_DB_ERROR);
        } catch (ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            System.exit(ErrorConsts.INIT_DB_ERROR);
        }

    }
}