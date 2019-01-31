package com.zhukai.print.dao;

import com.zhukai.print.model.RequestModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author zhukai
 * @date 2019/1/30
 */
public class CommonDao {

    /**
     * 获取全部配置
     */
    public static Map<String, String> getSysConfig() {
        String sql = "select sys_code, sys_value from sys_config";
        SqliteHelper sh = new SqliteHelper();
        Map<String, String> map = sh.executeQuery(sql, (rs) -> {
            Map<String, String> res = new HashMap<>();
            while (rs.next()) {
                String sysCode = rs.getString("sys_code");
                String sysValue = rs.getString("sys_value");
                res.put(sysCode, sysValue);
            }
            return res;
        });
        return map;
    }

    /**
     * 获取全部配置
     */
    public static RequestModel getLastPrintLog() {
        String sql = "select url, print_type, doc_type, create_time from print_log order by create_time desc limit 1";
        SqliteHelper sh = new SqliteHelper();
        RequestModel reqModel = sh.executeQuery(sql, (rs) -> {
            RequestModel req = null;
            if (rs.next()) {
                req = new RequestModel();
                String url = rs.getString("url");
                String printType = rs.getString("print_type");
                String docType = rs.getString("doc_type");
                req.setUrl(url);
                req.setPrinterType(printType);
                req.setDocType(docType);
            }
            return req;
        });
        return reqModel;
    }

    /**
     * 修改配置, 删除之后在增加
     */
    public static void updateSysConfig(String sysCode, String sysValue) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add("delete from sys_config where sys_code = '" + sysCode + "'");
        sqlList.add("insert into sys_config values ('" + sysCode + "', '" + sysValue + "')");
        new SqliteHelper().executeUpdate(sqlList);
    }

    public static void deleteSysConfig(String sysCode) {
        new SqliteHelper().executeUpdate("delete from sys_config where sys_code = '" + sysCode + "'");
    }

    /**
     * 保留10天的日志记录
     */
    public static void insertPrintLog(RequestModel req) {
        List<String> sqlList = new ArrayList<>();
        sqlList.add("delete from print_log where create_time < date('now', '-10 day')");
        sqlList.add("insert into print_log (url, print_type, doc_type) " +
                "values ('" + req.getUrl() + "', '" + req.getPrinterType() + "', '" + req.getDocType() + "')");
        new SqliteHelper().executeUpdate(sqlList);
    }
}
