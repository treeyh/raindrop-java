package com.treeyh.raindrop;

import com.treeyh.raindrop.config.RaindropConfig;
import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.consts.ErrorConsts;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.worker.RaindropSnowflakeWorker;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.Objects;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Slf4j
public final class Raindrop {

    private String idMode;

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

    public void Init(RaindropConfig config) {
        try {
            // TODO 值修改是否返回
            config.checkConfig();
            idMode = config.getIdMode().toLowerCase();

            if (Objects.equals(idMode, Consts.ID_MODE_SNOWFLAKE)) {
                RaindropSnowflakeWorker.getInstance().init(config);
            }
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

    /**
     * 获取新id
     * @return
     * @throws RaindropException 时间回溯，且时间单位不是毫秒和秒，会抛该异常
     */
    public long newId() throws RaindropException {
        return RaindropSnowflakeWorker.getInstance().newId();
    }
    /**
     * 根据编号获取新id
     * @return
     * @throws RaindropException 时间回溯，且时间单位不是毫秒和秒，会抛该异常
     */
    public long newIdByCode(String code) throws RaindropException {
        return RaindropSnowflakeWorker.getInstance().newIdByCode(code);
    }
}