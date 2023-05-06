package com.wang.sqltest.core;

import com.wang.sqltest.constant.SQLResultType;
import com.wang.sqltest.constant.SQLType;
import com.wang.sqltest.exception.SqlCompileException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

@Component
public class SqlCompiler {
    private final Logger logger = LoggerFactory.getLogger(SqlCompiler.class);
    private static final String PARAM_REGEX = ":(\\w+)";
    private static final String DYNAMIC_REGEX = "\\[([^\\[\\]]*)]";
    private static final String PARAM_MARKER = ":";
    private static final String NAME_REGEX = "-- :name ([\\w-]+)";
    public static final String KEY_STATEMENT = "statement";
    public static final String KEY_TYPE = "type";
    public static final String KEY_RESULT_TYPE = "resultType";
    public static final String KEY_IS_DYNAMIC = "isDynamic";
    private static volatile SqlCompiler compiler = null;
    private String sqlPath;
    private Map<String, Map<String, String>> namedQueries;

    private SqlCompiler(@Value("${sql.path}") String sqlPath) throws IOException {
        if (compiler != null) {
            throw new RuntimeException("SqlCompiler is singleton");
        }
        this.sqlPath = sqlPath;
        this.namedQueries = new ConcurrentHashMap<>();
        compile(sqlPath);
    }

    @PostConstruct
    public void init() {
        compiler = this;
        compiler.sqlPath = sqlPath;
    }

    public static SqlCompiler getInstance() {
        if (compiler == null) {
            synchronized (SqlCompiler.class) {
                if (compiler == null) {
                    try {
                        compiler = new SqlCompiler(compiler.sqlPath);
                    } catch (IOException e) {
                        throw new SqlCompileException(e);
                    }
                }
            }
        }
        return compiler;
    }

    /**
     * 扫描读取sql目录下所有sql文件
     * @param sqlFilePath sql文件目录
     * @throws IOException IO异常
     */
    public void compile(String sqlFilePath) throws IOException {
        Path sqlPath = Paths.get(sqlFilePath);
        if (Files.notExists(sqlPath)) {
            throw new FileNotFoundException("Sql path is not exists");
        }
        try(Stream<Path> stream = Files.walk(sqlPath)) {
            stream.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".sql"))
                    .forEach(this::compileSqlFile);
        }
    }

    /**
     * 流式读取、解析、存储sql文件中sql语句
     * @param path sql文件路径
     */
    private void compileSqlFile(Path path) {
        try (BufferedReader br = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            String line;
            String curQueryName = null;
            StringBuilder curQueryBuffer = new StringBuilder();
            SQLType curQueryType = null;
            SQLResultType curQueryResultType = null;
            String isDynamicQuery = null;
            Pattern namePattern = Pattern.compile(NAME_REGEX);
            while ((line = br.readLine()) != null) {
                String trimmedLine = line.trim();
                if (trimmedLine.startsWith("-- :name ")) {
                    if (curQueryName != null) {
                        addQuery(curQueryName, curQueryBuffer.toString().trim(), curQueryType, curQueryResultType, isDynamicQuery);
                        curQueryName = null;
                        curQueryBuffer = new StringBuilder();
                        curQueryType = null;
                        curQueryResultType = null;
                        isDynamicQuery = null;
                    }
                    Matcher matcher = namePattern.matcher(trimmedLine);
                    if (matcher.find()) {
                        curQueryName = matcher.group(1);
                        String replaced = trimmedLine.replace("-- :name ", "");
                        if (replaced.contains(":?")) {
                            curQueryType = SQLType.QUERY;
                        } else if (replaced.contains(":!")) {
                            curQueryType = SQLType.WRITE;
                        } else {
                            throw new SqlCompileException("Missing query type for SQL statement: " + line);
                        }
                        if (replaced.contains(":n")) {
                            curQueryResultType = SQLResultType.NUMBER_RESULT;
                        } else if (replaced.contains(":1")) {
                            curQueryResultType = SQLResultType.MAP_RESULT;
                        } else if (replaced.contains(":*")) {
                            curQueryResultType = SQLResultType.LIST_RESULT;
                        } else {
                            throw new SqlCompileException("Missing query result type for SQL statement: " + line);
                        }
                        if (replaced.contains(":D")) {
                            isDynamicQuery = "1";
                        }
                    }
                } else if (trimmedLine.isEmpty() || (trimmedLine.startsWith("--") && !trimmedLine.startsWith("--~"))
                        || (trimmedLine.startsWith("/*") && !trimmedLine.startsWith("/*~"))) {
                    // ignore empty lines and comments
                } else if (curQueryName != null) {
                    curQueryBuffer.append(line).append("\n");
                } else {
                    throw new SqlCompileException("Missing query name for SQL statement: " + line);
                }
            }
            if (curQueryName != null) {
                addQuery(curQueryName, curQueryBuffer.toString().trim(), curQueryType, curQueryResultType, isDynamicQuery);
                logger.info("高级查询配置文件 " + path.getFileName() + " 分析完成");
            }
        } catch (IOException e) {
            throw new SqlCompileException(e);
        }
    }

    /**
     * 增加解析完成的sql语句
     * @param queryName 语句名称 :name
     * @param queryStatement 语句体
     * @param queryType 语句类型 :? 查 :! 写
     * @param queryResultType 语句结果类型 :n number :1 map :* list
     * @param isDynamic 是否为动态语句 :D
     */
    private void addQuery(String queryName, String queryStatement, SQLType queryType, SQLResultType queryResultType, String isDynamic) {
        Map<String, String> queryMap = new HashMap<>();
        queryMap.put(KEY_STATEMENT, queryStatement);
        queryMap.put(KEY_TYPE, queryType.getType());
        queryMap.put(KEY_RESULT_TYPE, queryResultType.getResultType());
        queryMap.put(KEY_IS_DYNAMIC, isDynamic);
        namedQueries.put(queryName, queryMap);
    }

    /**
     * 获取解析存储的sql语句
     * @param queryName 语句名称 :name
     * @return 语句Map
     */
    public Map<String, String> getQuery(String queryName) {
        Map<String, String> template = namedQueries.get(queryName);
        if (template == null) {
            throw new SqlCompileException("No query found for name: " + queryName);
        }
        return template;
    }

    /**
     * 获取处理后的sql语句
     * @param queryName 语句名称
     * @param params 语句参数
     * @return 解析处理后的sql语句
     */
    public String getSql(String queryName, Map<String, Object> params) {
        Map<String, String> queryMap = getQuery(queryName);
        String statement = queryMap.get(KEY_STATEMENT);
        String type = queryMap.get(KEY_TYPE);
        String resultType = queryMap.get(KEY_RESULT_TYPE);
        String isDynamic = queryMap.get(KEY_IS_DYNAMIC);
        if (isDynamic != null) {
            statement = removeCommentSymbols(statement);
            Pattern dynamicPattern = Pattern.compile(DYNAMIC_REGEX);
            Matcher dynamicMatcher = dynamicPattern.matcher(statement);
            while (dynamicMatcher.find()) {
                String dynamicExpr = dynamicMatcher.group(1);
                String replacedExpr = evaluateDynamicExpr(dynamicExpr, params);
                statement = statement.replace("[" + dynamicExpr + "]", replacedExpr);
            }
        }
        Pattern paramPattern = Pattern.compile(PARAM_REGEX);
        Matcher paramMatcher = paramPattern.matcher(statement);
        while (paramMatcher.find()) {
            String paramName = paramMatcher.group(1);
            Object paramValue = params.get(paramName);
            if (paramValue == null) {
                throw new SqlCompileException("Missing value for parameter: " + paramName);
            }
            statement = statement.replace(PARAM_MARKER + paramName, "#{param." + paramName + "}");
        }

        return removeBlankLines(statement);
    }

    /**
     * 根据参数解析动态语句部分
     * @param dynamicExpr 正则表达式匹配的动态语句块 []
     * @param params 语句参数
     * @return 解析处理后的语句
     */
    private String evaluateDynamicExpr(String dynamicExpr, Map<String, Object> params) {
        Pattern pattern = Pattern.compile(PARAM_REGEX);
        Matcher matcher = pattern.matcher(dynamicExpr);
        StringBuilder sb = new StringBuilder();
        int lastIndex = 0;
        while (matcher.find()) {
            String paramName = matcher.group();
            Object paramValue = params.get(paramName.substring(1));

            // 根据动态参数的存在与否，处理语句中动态部分
            if (paramValue != null) {
                sb.append(dynamicExpr, lastIndex, matcher.start());
                // sb.append("#{").append(paramName.substring(1)).append("}");
                sb.append(paramName);
                lastIndex = matcher.end();
            } else {
                lastIndex = dynamicExpr.length();
            }
        }
        sb.append(dynamicExpr.substring(lastIndex));
        return sb.toString().trim();
    }

    /**
     * 去除动态sql中的注释标记
     * @param statement 需处理的语句
     * @return 去除标记的语句
     */
    private String removeCommentSymbols(String statement) {
        return statement.replace("/*~", "")
                .replace("~*/", "")
                .replace("--~", "").trim();
    }

    /**
     * 去除解析操作后留下的空白行
     * @param statement 需处理的语句
     * @return 去除空白行的语句
     */
    private String removeBlankLines(String statement) {
        String[] lines = statement.split("\\r?\\n");
        List<String> nonEmptyLines = new ArrayList<>();
        for (String line : lines) {
            if (!line.trim().isEmpty()) {
                nonEmptyLines.add(line);
            }
        }
        return String.join(System.lineSeparator(), nonEmptyLines);
    }

    /**
     * 获取所有原始sql语句信息
     * @return 所有原始sql语句信息
     */
    public Map<String, Map<String, String>> listNamedQueries() {
        return namedQueries;
    }

    public void recompile() {
        Map<String, Map<String, String>> oldNamedQueries = new HashMap<>(namedQueries);
        namedQueries.clear();
        try {
            compile(sqlPath);
            logger.info("已重新编译sql查询配置");
        } catch (IOException | SqlCompileException e) {
            namedQueries = oldNamedQueries;
            logger.error("重新编译sql查询配置失败, 取消更新 {}", e.getMessage());
        }
    }

}
