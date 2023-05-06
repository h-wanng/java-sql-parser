package com.wang.sqltest.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SqlWatcher {
    private final Logger logger = LoggerFactory.getLogger(SqlWatcher.class);
    private WatchService watchService;
    private final Map<WatchKey, Path> directories = new HashMap<>();
    private boolean isRunning = true;

    private void registerPath(Path path) throws IOException {
        WatchKey watchKey = path.register(watchService, StandardWatchEventKinds.ENTRY_MODIFY, StandardWatchEventKinds.ENTRY_CREATE);
        directories.put(watchKey, path);
    }

    private void registerTree(Path startPath) throws IOException {
        Files.walkFileTree(startPath, new SimpleFileVisitor<Path>() {
            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
                System.out.println("Registering: " + dir);
                registerPath(dir);
                return FileVisitResult.CONTINUE;
            }
        });
    }

    public void watchDir(Path rootPath, int intervalSeconds) throws IOException, InterruptedException {
        watchService = FileSystems.getDefault().newWatchService();
        registerTree(rootPath);
        while (isRunning) {
            logger.info("watching");
            // WatchKey key = watchService.take();
            WatchKey key = watchService.poll(intervalSeconds, TimeUnit.SECONDS);
            if (key == null) {
                continue;
            }
            for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();
                Path eventPath = (Path) event.context();
                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    Path dirPath = directories.get(key);
                    Path child = dirPath.resolve(eventPath);
                    if (Files.isDirectory(child, LinkOption.NOFOLLOW_LINKS)) {
                        registerTree(child);
                        System.out.println(eventPath.getFileName() + " has been created.");
                    } else {
                        System.out.println(eventPath.getFileName() + " has been created.");
                    }
                } else {
                    System.out.println(eventPath.getFileName() + " has been modified.");
                }
                boolean valid = key.reset();
                if (!valid) {
                    directories.remove(key);
                    if (directories.isEmpty()) {
                        isRunning = false;
                        break;
                    }
                }
            }
        }
    }
}
