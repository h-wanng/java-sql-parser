package com.wang.sqltest;

import com.wang.sqltest.util.SqlMonitor;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class SqlMonitorInitializer implements ApplicationRunner {
    private final Logger logger = LoggerFactory.getLogger(SqlMonitorInitializer.class);
    @Value("${sql.path}")
    private String sqlPath;
    @Value("${sql.interval:1}")
    private long interval;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        try {
            FileAlterationMonitor monitor = SqlMonitor.getMonitor(sqlPath, interval);
            monitor.start();
            logger.info("SQL监听器启动");
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
