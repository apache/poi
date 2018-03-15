package com.attackt.logivisual.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 数据库dataSource
 */
public class DataSource {
    private static Config config = ConfigFactory.load();
    //数据库用户名
    private static final String USERNAME = config.getString("mysql.username");
    //数据库密码
    private static final String PASSWORD = config.getString("mysql.password");
    //数据库地址
    private static final String URL = config.getString("mysql.url");
    // 驱动类
    private static final String diverClass = config.getString("mysql.diverClass");
    // 配置初始化大小、最小、最大
    private static final Integer initialSize = config.getInt("mysql.initialSize");
    private static final Integer minIdle = config.getInt("mysql.minIdle");
    private static final Integer maxActive = config.getInt("mysql.maxActive");
    // 配置获取连接等待超时的时间
    private static final Integer maxWait = config.getInt("mysql.maxWait");
    // 配置间隔多久才进行一次检测，检测需要关闭的空闲连接，单位是毫秒
    private static final Integer timeBetweenEvictionRunsMillis = config.getInt("mysql.timeBetweenEvictionRunsMillis");
    // 配置一个连接在池中最小生存的时间，单位是毫秒
    private static final Integer minEvictableIdleTimeMillis = config.getInt("mysql.minEvictableIdleTimeMillis");
    private static final String validationQuery = config.getString("mysql.validationQuery");
    private static final boolean testWhileIdle = config.getBoolean("mysql.testWhileIdle");
    private static final boolean testOnBorrow = config.getBoolean("mysql.testOnBorrow");
    private static final boolean testOnReturn = config.getBoolean("mysql.testOnReturn");
    //打开PSCache，并且指定每个连接上PSCache的大小
    private static final boolean poolPreparedStatements = config.getBoolean("mysql.poolPreparedStatements");
    private static final Integer maxPoolPreparedStatementPerConnectionSize = config.getInt("mysql.maxPoolPreparedStatementPerConnectionSize");
    //配置监控统计拦截的filters
    private static final String filters = config.getString("mysql.filters");



    private static DataSource datasource;
    private DruidDataSource dds;

    private DataSource() throws SQLException {
        dds = new DruidDataSource();
        dds.setDriverClassName(diverClass);
        dds.setUrl(URL);
        dds.setUsername(USERNAME);
        dds.setPassword(PASSWORD);
        dds.setInitialSize(initialSize);
        dds.setMinIdle(minIdle);
        dds.setMaxActive(maxActive);
        dds.setMaxWait(maxWait);
        dds.setTimeBetweenEvictionRunsMillis(timeBetweenEvictionRunsMillis);
        dds.setMinEvictableIdleTimeMillis(minEvictableIdleTimeMillis);
        dds.setValidationQuery(validationQuery);
        dds.setTestWhileIdle(testWhileIdle);
        dds.setTestOnBorrow(testOnBorrow);
        dds.setTestOnReturn(testOnReturn);
        dds.setPoolPreparedStatements(poolPreparedStatements);
        dds.setMaxPoolPreparedStatementPerConnectionSize(maxPoolPreparedStatementPerConnectionSize);
        dds.setFilters(filters);
    }

    public static DataSource getInstance() throws SQLException { //获得单例
        if (datasource == null) {
            datasource = new DataSource();
            return datasource;
        } else {
            return datasource;
        }
    }

    public Connection getConnection() throws SQLException {
        return this.dds.getConnection();
    }

}
