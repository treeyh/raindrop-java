package com.treeyh.raindrop.dao;

import com.treeyh.raindrop.BaseTest;
import com.treeyh.raindrop.dao.mysql.RaindropWorkerMySqlDAO;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Date;

@Slf4j
public class RaindropWorkerMySqlDAOTest {

    private static RaindropWorkerMySqlDAO raindropWorkerMySqlDAO;

    @BeforeAll
    public static void initTest() {
        raindropWorkerMySqlDAO = new RaindropWorkerMySqlDAO();
        try {
            raindropWorkerMySqlDAO.initConn(BaseTest.getDefaultRaindropDbConfig());
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
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
        Assertions.assertTrue(exist);
    }

    @Test
    public void testInitTable() {
        Boolean result = raindropWorkerMySqlDAO.initTable();
        Assertions.assertTrue(result);
    }
}
