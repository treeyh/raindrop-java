package com.treeyh.raindrop.worker;

import com.treeyh.raindrop.config.RaindropConfig;
import com.treeyh.raindrop.consts.Consts;
import com.treeyh.raindrop.consts.ErrorConsts;
import com.treeyh.raindrop.dao.AbstractRaindropWorkerDAO;
import com.treeyh.raindrop.dao.mysql.RaindropWorkerMySqlDAO;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.model.ETimeUnit;
import com.treeyh.raindrop.model.RaindropWorkerPO;
import com.treeyh.raindrop.utils.DateUtils;
import com.treeyh.raindrop.utils.NetworkUtils;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Slf4j
public class RaindropSnowflakeWorker {

    private RaindropWorkerPO worker;
    /**
     * 编号
     */
    private String workerCode;

    private long workerId;
    private int timeUnit;
    private long timeBackInitValue;
    private long endBitsValue;

    // 时间戳位移位数
    private int timeStampShift;
    // 位移位数
    private int workerIdShift;
    // 时间回拨位移位数
    private int timeBackShift;
    // 流水号移位数
    private int seqShift;

    // 最大的id序列值
    private long maxIdSeq;
    // 开始计算时间戳，毫秒
    private long startTime;

    // 当前时间流水，当前时刻毫秒 - startTime,换算时间单位取整
    private AtomicLong nowTimeSeq;

    // 获取新id的锁
    private Lock newIdLock;
    // 上次的获取新id时间序列
    private AtomicLong newIdLastTimeSeq;
    // 时间回拨值
    private AtomicLong timeBackBitValue;
    // 获取新id同一时间的自增序列
    private AtomicLong newIdSeq;

    // 基于code获取新id的生成code锁的锁
    private Lock newCodeLockLock;
    // 获取基于code新id的锁
    private Map<String, Lock> newIdByCodeLockMap;
    // 上次的获取基于code新id时间序列
    private Map<String, AtomicLong> newIdByCodeTimeSeqMap;
    // 基于code 时间回拨值
    private Map<String, AtomicLong> newIdByCodeTimeBackValueMap;
    // 获取基于code新id同一时间的自增序列
    private Map<String, AtomicLong> newIdByCodeSeqMap;


    private AbstractRaindropWorkerDAO raindropWorkerDAO;

    private RaindropSnowflakeWorker() {
        nowTimeSeq = new AtomicLong();
        newIdLock = new ReentrantLock();
        newIdLastTimeSeq = new AtomicLong();
        timeBackBitValue = new AtomicLong();
        newIdSeq = new AtomicLong();

        newCodeLockLock = new ReentrantLock();
        newIdByCodeLockMap = new HashMap<>();
        newIdByCodeTimeSeqMap = new HashMap<>();
        newIdByCodeTimeBackValueMap = new HashMap<>();
        newIdByCodeSeqMap = new HashMap<>();
    }

    private static class Instance {
        private static final RaindropSnowflakeWorker INSTANCE = new RaindropSnowflakeWorker();
    }

    /**
     * 单例
     *
     * @return
     */
    public static RaindropSnowflakeWorker getInstance() {
        return RaindropSnowflakeWorker.Instance.INSTANCE;
    }

    public synchronized void init(RaindropConfig config) throws SQLException, RaindropException, ClassNotFoundException {
        this.initDb(config);

        worker = activateWorker(config);

        if (Objects.equals(worker, null)) {
            throw new RaindropException(ErrorConsts.ACTIVE_WORKER_ERROR, "Failed to get an available Worker");
        }

        initParams(config);

        calcNowTimeSeq();

        startHeartbeat();
        startCalcNowTimeSeq();
    }


    public long newId() {
        return 0;
    }

    public long newIdByCode(){
        return 0;
    }

    /**
     * 初始化数据库
     *
     * @param config
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void initDb(RaindropConfig config) throws SQLException, ClassNotFoundException, RaindropException {
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

    /**
     * 获取可用的worker
     *
     * @param config
     * @return
     */
    private RaindropWorkerPO activateWorker(RaindropConfig config) {
        String localIp = NetworkUtils.getLocalIp();
        String mac = NetworkUtils.getLocalIpMac();
        workerCode = localIp + "#" + config.getServicePort() + "#" + config.getTimeUnit().getType() + "#" + mac;

        if (config.isPriorityEqualCodeWorkId() && (Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Millisecond) || Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Second))) {
            // 使用已有code
            RaindropWorkerPO po = raindropWorkerDAO.getBeforeWorker(workerCode);

            if (!Objects.equals(po, null)) {
                po = raindropWorkerDAO.activateWorker(po.getId(), po.getCode(), po.getTimeUnit(), po.getVersion());

                if (!Objects.equals(po, null)) {
                    return po;
                }
            }
        }

        Date heartbeatMaxTime = (new Date(System.currentTimeMillis() - (4 * Consts.HEARTBEAT_TIME_INTERVAL)));

        if (Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Minuter)) {
            heartbeatMaxTime = (new Date(heartbeatMaxTime.getTime() - (1 * 60 * 1000L)));
        } else if (Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Hour)) {
            heartbeatMaxTime = (new Date(heartbeatMaxTime.getTime() - (1 * 60 * 60 * 1000L)));
        } else if (Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Day)) {
            heartbeatMaxTime = (new Date(heartbeatMaxTime.getTime() - (1 * 24 * 60 * 60 * 1000L)));
        }

        List<RaindropWorkerPO> pos = raindropWorkerDAO.queryFreeWorkers(heartbeatMaxTime);
        if (Objects.equals(pos, null) || pos.isEmpty()) {
            return null;
        }

        for (RaindropWorkerPO po : pos) {
            RaindropWorkerPO p = raindropWorkerDAO.activateWorker(po.getId(), workerCode, config.getTimeUnit().getType(), po.getVersion());

            if (!Objects.equals(p, null)) {
                return p;
            }
        }
        return null;
    }


    /**
     * 初始化参数
     *
     * @param config
     */
    private void initParams(RaindropConfig config) {
        timeUnit = config.getTimeUnit().getType();
        workerId = worker.getId();

        timeBackInitValue = config.getTimeBackBitValue();
        timeBackBitValue.set(timeBackInitValue);
        endBitsValue = config.getEndBitValue();

        startTime = calcTimestamp(config.getStartTimeStamp().getTime(), timeUnit);

        int seqLength = Consts.ID_BIT_LENGTH - config.getTimeStampLength() - config.getWorkIdLength() -
                Consts.ID_TIME_BACK_BIT_LENGTH - config.getEndBitsLength();

        // 计算同一时刻最大流水号
        maxIdSeq = (1 << seqLength) - 1;

        seqShift = config.getEndBitsLength();
        timeBackShift = seqLength + seqShift;
        workerIdShift = timeBackShift + Consts.ID_TIME_BACK_BIT_LENGTH;
        timeStampShift = workerIdShift + config.getWorkIdLength();

        log.info(String.format("idMode:snowflake, timeBackBitValue:%d, endBitsValue:%d, workerId:%d, seqLength:%d, " +
                "workerLength:%d, timeLength:%d, maxIdSeq:%d, seqShift: %d, timeBackShift: %d, workerIdShift: %d, timeStampShift:%d",
                timeBackBitValue.get(), endBitsValue, workerId, seqLength, config.getWorkIdLength(),
                config.getTimeStampLength(), maxIdSeq, seqShift, timeBackShift, workerIdShift, timeStampShift));
    }

    /**
     * 计算当前时间戳流水
     */
    private void calcNowTimeSeq() {
        long seq = calcTimestamp(System.currentTimeMillis(), timeUnit);
        nowTimeSeq.set(seq);
    }

    private long calcTimestamp(long timestampMilli, int timeUnit) {
        long st = timestampMilli;
        switch (timeUnit) {
            case 2:
                st = st / 1000L;
                break;
            case 3:
                st = st / (60 * 1000L);
                break;
            case 4:
                st = st / (60 * 60 * 1000L);
                break;
            case 5:
                st = st / (24 * 60 * 60 * 1000L);
                break;
        }
        return st;
    }

    /**
     *  启动定时心跳
     */
    private void startHeartbeat() {
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor((runnable) -> {
            Thread thread = new Thread(runnable, "Raindrop Heartbeat");
            thread.setDaemon(true);
            return thread;
        });

        scheduler.scheduleAtFixedRate(() -> {
            log.info("worker heartbeat. workerId:"+ worker.getId());
            worker = this.raindropWorkerDAO.heartbeatWorker(worker);
            if (log.isDebugEnabled()) {
                log.debug("worker heartbeat worker:"+worker.toString());
            }
            Date now = new Date(System.currentTimeMillis());
            if ((worker.getHeartbeatTime().getTime() > worker.getUpdateTime().getTime() + Consts.HEARTBEAT_TIME_INTERVAL) ||
                    (worker.getHeartbeatTime().getTime() < worker.getUpdateTime().getTime() - Consts.HEARTBEAT_TIME_INTERVAL)) {
                log.error("Server and database time gap exceeds threshold!!! heartbeatTime:" +
                        DateUtils.date2Str(worker.getHeartbeatTime()) + "; updateTime:" +
                        DateUtils.date2Str(worker.getUpdateTime()));
            }
        }, Consts.HEARTBEAT_TIME_INTERVAL, Consts.HEARTBEAT_TIME_INTERVAL, TimeUnit.MILLISECONDS);
    }

    /**
     * 启动当前流水号计算
     */
    private void startCalcNowTimeSeq() {
        long interval = 1;
        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor((runnable) -> {
            Thread thread = new Thread(runnable, "Raindrop NowTimeSeq");
            thread.setDaemon(true);
            return thread;
        });

        if (timeUnit != 1) {
            interval = 1000L;
        }

        scheduler.scheduleAtFixedRate(() -> {
            this.calcNowTimeSeq();
        }, interval, interval, TimeUnit.MILLISECONDS);
    }
}