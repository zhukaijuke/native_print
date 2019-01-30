package com.test;

import com.zhukai.print.dao.SqliteHelper;
import org.junit.Test;

import java.util.List;

/**
 * @author zhukai
 * @date 2019/1/25
 */
public class SqliteTest {

    @Test
    public void testHelper() {
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
    }

    public void sample() {
        /*Alert confirmation = AlertUtil.confirmation("确认要关闭吗?");
        Optional<ButtonType> result = confirmation.showAndWait();
        if (result.get() == ButtonType.OK) {
            System.exit(0);
        } else {
            event.consume();
        }*/
    }

}
