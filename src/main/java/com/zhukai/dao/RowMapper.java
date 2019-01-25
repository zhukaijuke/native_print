package com.zhukai.dao;

import java.sql.ResultSet;
import java.sql.SQLException;

/**
 * @author zhukai
 * @date 2019/1/25
 */
@FunctionalInterface
public interface RowMapper<T> {

    T mapRow(ResultSet rs, int index) throws SQLException;

}
