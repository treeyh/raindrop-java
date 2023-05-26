package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.BaseTest;
import com.treeyh.raindrop.dao.mysql.RaindropWorkerMySqlDAO;
import com.treeyh.raindrop.exception.RaindropException;
import com.treeyh.raindrop.model.RaindropWorkerPO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;

import java.sql.SQLException;
import java.util.Date;
import java.util.concurrent.atomic.AtomicLong;

/**
 * @author: Treeyh
 * @version: 1.0
 * @description:
 * @create: 2023-04-06 17:58
 * @email: tree@ejyi.com
 **/
@Slf4j
public class RaindropWorkerMySqlDAOTest {

    private static RaindropWorkerMySqlDAO raindropWorkerMySqlDAO;

    @BeforeAll
    public static void initTest() {
        try {
            BaseTest.initTestDataSource();
            raindropWorkerMySqlDAO = new RaindropWorkerMySqlDAO();
            raindropWorkerMySqlDAO.initConn(BaseTest.getDefaultRaindropDbConfig());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void dropTable() {
        BaseTest.dropTable();
    }

    @Test
    public void testGetNowTime() {
        Date date = raindropWorkerMySqlDAO.getNowTime();
        log.info(date.toString());
        Assertions.assertNotEquals(date, null);
    }

    @Test
    public void testExistTable() {
        Boolean exist = raindropWorkerMySqlDAO.existTable();
        Assertions.assertFalse(exist);

        Boolean result = raindropWorkerMySqlDAO.initTableWorkers(BaseTest.beginId, BaseTest.endId);
        Assertions.assertTrue(result);

        RaindropWorkerPO po = raindropWorkerMySqlDAO.getWorkerById(BaseTest.beginId);
        Assertions.assertEquals(po.getId(), BaseTest.beginId);
        Assertions.assertEquals(po.getCode(), "");
        Assertions.assertEquals(po.getTimeUnit(), 2);
    }

    @Test
    public void testInitTableInfo() {

        try {
            raindropWorkerMySqlDAO.initTableInfo(1L, 10L);
        } catch (RaindropException e) {
            throw new RuntimeException(e);
        }
    }
}
