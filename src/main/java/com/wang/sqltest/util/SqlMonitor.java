package com.wang.sqltest.util;


import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;

import java.io.File;
import java.util.concurrent.TimeUnit;

public class SqlMonitor {
    /**
     * 获取文件改动监听器
     * @param rootPath 监听根目录
     * @param interval 监听间隔(s)
     * @return 监听器
     */
    public static FileAlterationMonitor getMonitor(String rootPath, long interval) {
        File sqlPath = new File(rootPath);
        if (!sqlPath.exists()) {
            throw new NullPointerException("监听目录不存在");
        }
        long millisInterval = TimeUnit.SECONDS.toMillis(interval);
        IOFileFilter directories = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                HiddenFileFilter.VISIBLE);
        IOFileFilter files = FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".sql"));
        IOFileFilter filter = FileFilterUtils.or(directories, files);
        FileAlterationObserver observer = new FileAlterationObserver(sqlPath, filter);
        observer.addListener(new FileListener());
        return new FileAlterationMonitor(millisInterval, observer);
    }
}
