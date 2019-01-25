package com.test;

import com.zhukai.dao.RowMapper;
import com.zhukai.dao.SqliteHelper;
import org.junit.Test;

import java.io.File;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

/**
 * @author zhukai
 * @date 2019/1/25
 */
public class SqliteTest {

    @Test
    public void testHelper() {
        try {
            SqliteHelper h = new SqliteHelper();
            //h.executeUpdate("drop table if exists test;");
            //h.executeUpdate("create table test(name varchar(20));");
            h.executeUpdate("insert into test values('sqliteHelper test1');");
            h.executeUpdate("insert into test values('sqliteHelper test2');");
            List<String> sList = h.executeQuery("select name from test", (rs, index) -> {
                return rs.getString("name");
            });
            for (String s : sList) {
                System.out.println(s);
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
