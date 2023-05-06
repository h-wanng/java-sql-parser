import com.wang.sqltest.util.FileListener;
import org.apache.commons.io.filefilter.FileFilterUtils;
import org.apache.commons.io.filefilter.HiddenFileFilter;
import org.apache.commons.io.filefilter.IOFileFilter;
import org.apache.commons.io.monitor.FileAlterationMonitor;
import org.apache.commons.io.monitor.FileAlterationObserver;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class NormalJavaTest {
    private static List<String> extractNestedContent(String input) {
        List<String> result = new ArrayList<>();
        Pattern pattern = Pattern.compile("\\[(?:[^\\[\\]]|(?R))*\\]");
        Matcher matcher = pattern.matcher(input);
        while (matcher.find()) {
            String nestedContent = matcher.group().substring(1, matcher.group().length() - 1);
            result.add(nestedContent);
        }
        return result;
    }


    @Test
    void regexTest() {
        // String input = "where sn.del_flag = '0' [and sn.id = :news_id [and sn.`type` = :type]]";
        // String regex = "\\[([^\\[\\]]*+(?:\\[[^\\[\\]]*+])?+)]";
        //
        // Pattern pattern = Pattern.compile(regex);
        // Matcher matcher = pattern.matcher(input);
        //
        // while (matcher.find()) {
        //     String nested = matcher.group(1);
        //     System.out.println(nested);
        // }

        String statement = "where sn.del_flag = '0' [and sn.id = :news_id [and sn.`type` = :type [and test = 1]]]";
        Pattern pattern = Pattern.compile("\\[([^\\[\\]]*)]");
        Matcher matcher = pattern.matcher(statement);
        while (matcher.find()) {
            String matched = matcher.group(1);
            System.out.println(matched);
            statement = statement.replace("[" + matched + "]", "");
            matcher = pattern.matcher(statement);
        }
    }

    @Test
    void testParamEvaluate() {
        Map<String, Object> params = new HashMap<>();
        params.put("news_id", 1);
        String dynamicExpr = "and sn.id = :news_id";
        Pattern pattern = Pattern.compile(":(\\w+)");
        Matcher matcher = pattern.matcher(dynamicExpr);
        while (matcher.find()) {
            String paramName = matcher.group();
            Object paramValue = params.get(paramName.substring(1));
            // StringBuilder sb = new StringBuilder();
            // int lastIndex = 0;
            // // 将动态参数替换为实际的值，若参数不存在则忽略
            if (paramValue != null) {
                matcher.replaceAll("");
            }
        }
    }

    @Test
    void testWatcher() {
        Watcher watcher = new Watcher();
        Path path = Paths.get("./sql");
        try {
            watcher.watchDir(path);
        } catch (IOException | InterruptedException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testCommonIOWatcher() throws Exception {
        String rootPath = "./sql";
        long interval = TimeUnit.SECONDS.toMillis(1);
        IOFileFilter directories = FileFilterUtils.and(
                FileFilterUtils.directoryFileFilter(),
                HiddenFileFilter.VISIBLE);
        IOFileFilter files = FileFilterUtils.and(
                FileFilterUtils.fileFileFilter(),
                FileFilterUtils.suffixFileFilter(".sql"));
        IOFileFilter filter = FileFilterUtils.or(directories, files);
        FileAlterationObserver observer = new FileAlterationObserver(new File(rootPath), filter);
        observer.addListener(new FileListener());
        FileAlterationMonitor monitor = new FileAlterationMonitor(interval, observer);
        monitor.start();
    }

    class Watcher {
        private WatchService watchService;
        private final Map<WatchKey, Path> directories = new HashMap<>();

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

        public void watchDir(Path startPath) throws IOException, InterruptedException {
            watchService = FileSystems.getDefault().newWatchService();
            registerTree(startPath);
            while (true) {
                System.out.println("watching");
                WatchKey key = watchService.take();
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
                            break;
                        }
                    }
                }
            }
        }
    }
}
