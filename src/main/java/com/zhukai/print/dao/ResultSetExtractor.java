package com.zhukai.print.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhukai
 * @date 2019/1/25
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {

    T extractData(ResultSet rs) throws SQLException;

}
