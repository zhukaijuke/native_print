package com.zhukai.dao;

import java.sql.ResultSet;

/**
 * @author zhukai
 * @date 2019/1/25
 */
@FunctionalInterface
public interface ResultSetExtractor<T> {

    T extractData(ResultSet rs);

}
