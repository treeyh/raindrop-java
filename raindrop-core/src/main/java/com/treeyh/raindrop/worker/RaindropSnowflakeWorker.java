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
import com.treeyh.raindrop.utils.Utils;
import lombok.extern.slf4j.Slf4j;

import java.sql.SQLException;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Slf4j
public class RaindropSnowflakeWorker {

    private RaindropWorkerPO worker;
    /**
     * 编号
     */
    private String workerCode;

    private long workerId;
    private int timeUnit;
    private long endBitsValue;

    /**
     * 时间戳位移位数
     */
    private int timeStampShift;
    /**
     * 位移位数
     */
    private int workerIdShift;
    /**
     * 时间回拨位移位数
     */
    private int timeBackShift;
    /**
     * 流水号移位数
     */
    private int seqShift;

    /**
     * 最大的id序列值
     */
    private long maxIdSeq;
    /**
     * 开始计算时间戳，毫秒
     */
    private long startTime;

    /**
     * 当前时间流水，当前时刻毫秒
     */
    private AtomicLong nowTimeSeq;

    /**
     * 获取新id的锁
     */
    private Lock newIdLock;
    /**
     * 上次的获取新id时间序列
     */
    private AtomicLong newIdLastTimeSeq;
    /**
     * 时间回拨初始值
     */
    private long timeBackBitInitValue;
    /**
     * 时间回拨值
     */
    private AtomicLong timeBackBitValue;

    /**
     * 获取新id同一时间的自增序列
     */
    private AtomicLong newIdSeq;


    /**
     * 获取基于code新id的锁
     */
    private Map<String, Lock> newIdByCodeLockMap;
    /**
     * 上次的获取基于code新id时间序列
     */
    private Map<String, AtomicLong> newIdByCodeTimeSeqMap;
    /**
     * 获取基于code新id同一时间的自增序列
     */
    private Map<String, AtomicLong> newIdByCodeSeqMap;
    /**
     * 获取基于时间回溯标识
     */
    private Map<String, AtomicLong> newIdByCodeTimeBackValueMap;


    private AbstractRaindropWorkerDAO raindropWorkerDAO;

    private RaindropSnowflakeWorker() {
        nowTimeSeq = new AtomicLong();
        newIdLock = new ReentrantLock();
        newIdLastTimeSeq = new AtomicLong();
        timeBackBitValue = new AtomicLong();
        newIdSeq = new AtomicLong();

        newIdByCodeLockMap = new ConcurrentHashMap<>();
        newIdByCodeTimeBackValueMap = new ConcurrentHashMap<>();
        newIdByCodeTimeSeqMap = new ConcurrentHashMap<>();
        newIdByCodeSeqMap = new ConcurrentHashMap<>();
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

        if (null == worker) {
            throw new RaindropException(ErrorConsts.ACTIVE_WORKER_ERROR, "Failed to get an available Worker");
        }

        initParams(config);

        calcNowTimeSeq();

        startHeartbeat();
        startCalcNowTimeSeq();
    }


    /**
     * 获取workerId
     * @return
     */
    public long getWorkerId() {
        return null == worker ? 0 : workerId;
    }

    /**
     * 获取新id
     * @return
     * @throws RaindropException 时间回溯，且时间单位不是毫秒和秒，会抛该异常
     */
    public synchronized long newId() throws RaindropException {

        long timeBackValue = timeBackBitValue.get();
        long timestamp = nowTimeSeq.get();
        long lastTimeSeq = newIdLastTimeSeq.get();

        long seq = 0;
        if (Objects.equals(lastTimeSeq, timestamp)) {
            // 同时间戳重复获取id
            seq = newIdSeq.incrementAndGet();

            if (seq > maxIdSeq) {
                // 超过了序列最大值
                // 毫秒，秒还能抢救一下
                if (Objects.equals(timeUnit, ETimeUnit.Millisecond.getType())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("millisecond unit sleep %d, seq: %d, maxIdSeq: %d", timestamp, seq, maxIdSeq));
                    }
                    while (true) {
                        timestamp = nowTimeSeq.get();
                        if (!Objects.equals(timestamp, lastTimeSeq)) {
                            break;
                        }
                    }
                } else if (Objects.equals(timeUnit, ETimeUnit.Second.getType())) {
                    if (log.isDebugEnabled()) {
                        log.debug(String.format("second unit sleep %d, seq: %d, maxIdSeq: %d", timestamp, seq, maxIdSeq));
                    }
                    while (true) {
                        Utils.sleep(10);
                        timestamp = nowTimeSeq.get();
                        if (!Objects.equals(timestamp, lastTimeSeq)) {
                            break;
                        }
                    }
                } else {
                    // 不是毫秒或秒时间单位，不等待返回直接返回错误
                    log.error(String.format("timeUnit: %d, timeSeq: %d, seq: %d, maxIdSeq: %d",
                            timeUnit, timestamp, seq, maxIdSeq));
                    throw new RaindropException(ErrorConsts.NEW_ID_FAIL_SEQUENCE_EXCEEDS_MAX_VALUE, ErrorConsts.SEQUENCE_EXCEEDS_MAX_VALUE);
                }
                seq = 0L;
                newIdSeq.set(0);
            }
        } else {
            seq = 0L;
            newIdSeq.set(0);
        }

        if (lastTimeSeq > timestamp) {

            log.error(String.format("timeUnit:%d, lastTimeSeq: %d, timestamp: %d, Server clock moved backwards.", timeUnit, lastTimeSeq, timestamp));
            // timeBackValue 取反，避免重复
            timeBackValue = timeBackValue ^ 1;
            timeBackBitValue.set(timeBackValue);
        }
        if (!Objects.equals(lastTimeSeq, timestamp)) {
            newIdLastTimeSeq.set(timestamp);
        }
        return ((timestamp - startTime) << timeStampShift) |
                (workerId << workerIdShift) |
                (timeBackValue << timeBackShift) |
                (seq << seqShift) |
                endBitsValue;
    }

    public long newIdByCode(String code) throws RaindropException {
        Lock lock = newIdByCodeLockMap.getOrDefault(code, null);
        if (null == lock) {
            generateCodeLock(code);
            lock = newIdByCodeLockMap.get(code);
        }

        lock.lock();
        try {
            long timeBackValue = newIdByCodeTimeBackValueMap.get(code).get();
            long timestamp = nowTimeSeq.get();

            AtomicLong codeIdSeq = newIdByCodeSeqMap.get(code);
            AtomicLong lastTime = newIdByCodeTimeSeqMap.get(code);
            long lastTimeSeq = lastTime.get();
            long seq = 0;
            if (Objects.equals(lastTimeSeq, timestamp)) {
                // 时间戳未发生变化，需要增加newIdSeq
                seq = codeIdSeq.incrementAndGet();
                if (seq > maxIdSeq) {
                    // 超过了序列最大值
                    // 毫秒，秒还能抢救一下

                    if (Objects.equals(timeUnit, ETimeUnit.Millisecond.getType())) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("code:%s, millisecond unit sleep %d, seq: %d, maxIdSeq: %d", code, timestamp, seq, maxIdSeq));
                        }

                        while (true) {
                            timestamp = nowTimeSeq.get();
                            if (!Objects.equals(timestamp, lastTimeSeq)) {
                                break;
                            }
                        }
                    } else if (Objects.equals(timeUnit, ETimeUnit.Second.getType())) {
                        if (log.isDebugEnabled()) {
                            log.debug(String.format("code:%s, second unit sleep %d, seq: %d, maxIdSeq: %d", code, timestamp, seq, maxIdSeq));
                        }
                        while (true) {
                            Utils.sleep(10);
                            timestamp = nowTimeSeq.get();
                            if (!Objects.equals(timestamp, lastTimeSeq)) {
                                break;
                            }
                        }
                    } else {
                        // 不是毫秒或秒时间单位，不等待返回直接返回错误
                        log.error(String.format("code:%s, timeUnit: %d, timeSeq: %d, seq: %d, maxIdSeq: %d",
                                code, timeUnit, timestamp, seq, maxIdSeq));
                        throw new RaindropException(ErrorConsts.NEW_ID_FAIL_SEQUENCE_EXCEEDS_MAX_VALUE, ErrorConsts.SEQUENCE_EXCEEDS_MAX_VALUE);
                    }
                    seq = 0;
                    codeIdSeq.set(0);
                }
            } else {
                seq = 0;
                codeIdSeq.set(0);
            }

            newIdByCodeSeqMap.put(code, codeIdSeq);

            if (lastTimeSeq > timestamp) {
                log.error(String.format("code:%s, timeUnit:%d, lastTimeSeq: %d, timestamp: %d, Server clock moved backwards.",
                        code, timeUnit, lastTimeSeq, timestamp));
                // timeBackValue 取反，避免重复
                timeBackValue = timeBackValue ^ 1;
                newIdByCodeTimeBackValueMap.get(code).set(timeBackValue);
            }

            if(!Objects.equals(lastTimeSeq, timestamp)) {
                lastTime.set(timestamp);
                newIdByCodeTimeSeqMap.put(code, lastTime);
            }

            return ((timestamp - startTime) << timeStampShift) |
                    (workerId << workerIdShift) |
                    (timeBackValue << timeBackShift) |
                    (seq << seqShift) |
                    endBitsValue;
        } catch (RaindropException e) {
            throw e;
        } finally {
            lock.unlock();
        }
    }

    /**
     * 初始化数据库
     *
     * @param config
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private void initDb(RaindropConfig config) throws SQLException, ClassNotFoundException, RaindropException {
        if (null != raindropWorkerDAO) {
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
    private synchronized RaindropWorkerPO activateWorker(RaindropConfig config) {
        if (null != worker) {
            return worker;
        }

        String localIp = NetworkUtils.getLocalIp();
        String mac = NetworkUtils.getLocalIpMac();
        workerCode = localIp + "#" + config.getServicePort() + "#" + config.getTimeUnit().getType() + "#" + mac;

        if (config.isPriorityEqualCodeWorkId() &&
                (Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Millisecond) ||
                        Objects.equals(config.getTimeUnit().getType(), ETimeUnit.Second))) {
            // 使用已有code
            RaindropWorkerPO po = raindropWorkerDAO.getBeforeWorker(workerCode);

            if (null != po) {
                po = raindropWorkerDAO.activateWorker(po.getId(), po.getCode(), po.getTimeUnit(), po.getVersion());

                if (null != po) {
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
        if (null == pos || pos.isEmpty()) {
            log.error("Failed to get an available Worker. heartbeatMaxTime:"+DateUtils.date2Str(heartbeatMaxTime));
            return null;
        }

        for (RaindropWorkerPO po : pos) {
            RaindropWorkerPO p = raindropWorkerDAO.activateWorker(po.getId(), workerCode, config.getTimeUnit().getType(), po.getVersion());

            if (null != p) {
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
        timeBackBitInitValue = config.getTimeBackBitValue();
        timeBackBitValue.set(config.getTimeBackBitValue());
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
        long now = System.currentTimeMillis();
        long seq = calcTimestamp(now, timeUnit);
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
            default:
                st = st;
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


    private synchronized void generateCodeLock(String code) {
        Lock lock = newIdByCodeLockMap.getOrDefault(code, null);

        if (lock != null) {
            return;
        }
        AtomicLong lastTime = new AtomicLong(0);
        AtomicLong seq = new AtomicLong(0);
        AtomicLong timeBackValue = new AtomicLong(timeBackBitInitValue);
        lock = new ReentrantLock();

        newIdByCodeLockMap.put(code, lock);
        newIdByCodeTimeBackValueMap.put(code,timeBackValue);

        newIdByCodeTimeSeqMap.put(code, lastTime);
        newIdByCodeSeqMap.put(code, seq);
    }
}
