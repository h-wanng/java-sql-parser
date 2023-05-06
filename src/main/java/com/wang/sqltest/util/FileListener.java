package com.wang.sqltest.util;

import com.wang.sqltest.core.SqlCompiler;
import org.apache.commons.io.monitor.FileAlterationListenerAdaptor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.File;

public class FileListener extends FileAlterationListenerAdaptor {
    private final Logger logger = LoggerFactory.getLogger(FileListener.class);
    private final SqlCompiler compiler;

    public FileListener() {
        compiler = SqlCompiler.getInstance();
    }

    @Override
    public void onStart(FileAlterationObserver observer) {
        super.onStart(observer);
    }

    @Override
    public void onDirectoryCreate(File directory) {
        super.onDirectoryDelete(directory);
    }

    @Override
    public void onDirectoryChange(File directory) {
        super.onDirectoryDelete(directory);
    }

    @Override
    public void onDirectoryDelete(File directory) {
        super.onDirectoryDelete(directory);
    }

    @Override
    public void onFileCreate(File file) {
        logger.info("文件创建: {} 重新编译sql查询配置", file.getName());
        recompile();
    }

    @Override
    public void onFileChange(File file) {
        logger.info("文件修改: {} 重新编译sql查询配置", file.getName());
        recompile();
    }

    @Override
    public void onFileDelete(File file) {
        logger.info("文件删除: {} 重新编译sql查询配置", file.getName());
        recompile();
    }

    @Override
    public void onStop(FileAlterationObserver observer) {
        super.onStop(observer);
    }

    private void recompile() {
        // SqlCompiler compiler = SqlCompiler.getInstance();
        compiler.recompile();
    }
}
