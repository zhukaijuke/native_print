package com.zhukai.print.dao;

import lombok.extern.slf4j.Slf4j;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * sqlite帮助类，直接创建该类实例，并调用相应的接口即可对sqlite数据库进行操作
 */
@Slf4j
public class SqliteHelper {

    private Connection connection;
    private Statement statement;
    private ResultSet resultSet;
    private String dbFilePath;

    /**
     * 构造函数
     *
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SqliteHelper() {
        this("db/print.db");
    }

    /**
     * 构造函数
     *
     * @param dbFilePath sqlite db 文件路径
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public SqliteHelper(String dbFilePath) {
        this.dbFilePath = dbFilePath;
        connection = getConnection(dbFilePath);
    }

    /**
     * 获取数据库连接
     *
     * @param dbFilePath db文件路径
     * @return 数据库连接
     * @throws ClassNotFoundException
     * @throws SQLException
     */
    public Connection getConnection(String dbFilePath) {
        Connection conn = null;
        try {
            Class.forName("org.sqlite.JDBC");
            conn = DriverManager.getConnection("jdbc:sqlite:" + dbFilePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

    /**
     * 执行sql查询
     *
     * @param sql sql select 语句
     * @param rse 结果集处理类对象
     * @return 查询结果
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public <T> T executeQuery(String sql, ResultSetExtractor<T> rse) {
        try {
            resultSet = getStatement().executeQuery(sql);
            T rs = rse.extractData(resultSet);
            return rs;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroyed();
        }
    }

    /**
     * 执行select查询，返回结果列表
     *
     * @param sql sql select 语句
     * @param rm  结果集的行数据处理类对象
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public <T> List<T> executeQuery(String sql, RowMapper<T> rm) {
        List<T> rsList = new ArrayList<T>();
        try {
            resultSet = getStatement().executeQuery(sql);
            while (resultSet.next()) {
                rsList.add(rm.mapRow(resultSet, resultSet.getRow()));
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroyed();
        }
        return rsList;
    }

    /**
     * 执行数据库更新sql语句
     *
     * @param sql
     * @return 更新行数
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public int executeUpdate(String sql) {
        try {
            int c = getStatement().executeUpdate(sql);
            return c;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroyed();
        }

    }

    /**
     * 执行多个sql更新语句
     *
     * @param sqls
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void executeUpdate(String... sqls) {
        try {
            if (sqls != null) {
                for (String sql : sqls) {
                    getStatement().executeUpdate(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroyed();
        }
    }

    /**
     * 执行数据库更新 sql List
     *
     * @param sqls sql列表
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public void executeUpdate(List<String> sqls) {
        try {
            if (sqls != null) {
                for (String sql : sqls) {
                    getStatement().executeUpdate(sql);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            destroyed();
        }
    }

    private Connection getConnection() {
        if (null == connection) {
            connection = getConnection(dbFilePath);
        }
        return connection;
    }

    private Statement getStatement() {
        if (null == statement) {
            try {
                statement = getConnection().createStatement();
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
        }
        return statement;
    }

    /**
     * 数据库资源关闭和释放
     */
    public void destroyed() {
        try {
            if (null != connection) {
                connection.close();
                connection = null;
            }

            if (null != statement) {
                statement.close();
                statement = null;
            }

            if (null != resultSet) {
                resultSet.close();
                resultSet = null;
            }
        } catch (SQLException e) {
            log.error("Sqlite数据库关闭时异常", e);
        }
    }
}
