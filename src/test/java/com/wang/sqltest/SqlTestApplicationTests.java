package com.wang.sqltest;

import com.wang.sqltest.core.SqlCompiler;
import com.wang.sqltest.dao.EntityDao;
import com.wang.sqltest.service.EntityService;
import org.apache.ibatis.builder.SqlSourceBuilder;
import org.apache.ibatis.builder.StaticSqlSource;
import org.apache.ibatis.mapping.*;
import org.apache.ibatis.scripting.xmltags.DynamicSqlSource;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Thread.sleep;

@SpringBootTest
class SqlTestApplicationTests {

    @Autowired
    private SqlSessionFactory sqlSessionFactory;

    @Autowired
    private EntityDao entityDao;

    @Autowired
    private SqlCompiler compiler1;

    @Autowired
    private EntityService entityService;

    @Test
    void contextLoads() {

    }

    @Test
    void testCompiler() throws Exception {
        // Map<String, Map<String, String>> namedQueries = compiler.listNamedQueries();
        SqlCompiler compiler = SqlCompiler.getInstance();
        Map<String, Map<String, String>> namedQueries = compiler.listNamedQueries();
        System.out.println(namedQueries.size());
        namedQueries.forEach((key, value) -> {
            System.out.println(key);
            System.out.println(value);
            System.out.println("========================");
        });
        Map<String, Object> params = new HashMap<>();
        params.put("news_id", 1);
        params.put("project_id", 1);
        params.put("user_id", 1);
        String query = compiler.getSql("finish-user-project", params);
        System.out.println(query);
    }

    @Test
    void testSqlBuilder() {
        Configuration configuration = sqlSessionFactory.getConfiguration();
        String sql = "select * from sy_news sn where sn.id = #{news_id}";
        Map<String, Object> params = new HashMap<>();
        params.put("news_id", 1);
        // DynamicSqlSource dynamicSqlSource = new DynamicSqlSource(configuration, sql, params.getClass());
        // BoundSql boundSql = dynamicSqlSource.getBoundSql(params);
        // String parsedSql = boundSql.getSql();
        // Object parameterObject = boundSql.getParameterObject();
        // List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        // System.out.println(parsedSql);
        // System.out.println(parameterObject);
        // System.out.println(parameterMappings);
        SqlSourceBuilder sqlSourceBuilder = new SqlSourceBuilder(configuration);
        SqlSource parsed = sqlSourceBuilder.parse(sql, params.getClass(), params);
        BoundSql boundSql = parsed.getBoundSql(params);
        MappedStatement.Builder builder = new MappedStatement.Builder(configuration, "id", parsed, SqlCommandType.SELECT);
        MappedStatement mappedStatement = builder.build();
        try (SqlSession sqlSession = sqlSessionFactory.openSession()) {
            List<Object> objects = sqlSession.selectList(sql, params);
            System.out.println(objects);
        }


        // List<Map<String, Object>> selected = entityDao.select(dynamicSqlSource, parameterObject);
        // System.out.println(selected);
    }

    @Test
    public void testSqlProvider() throws InterruptedException, IOException {
        Map<String, Object> params = new HashMap<>();
        params.put("news_id", 1);
        // params.put("user_id", null);
        // List<Map<String, Object>> news = entityDao.select(params);
        // System.out.println(news);
        // Object query = entityService.query("list-sy_news", params);
        // System.out.println(query);
        Map<String, Map<String, String>> namedQueries = compiler1.listNamedQueries();
        System.out.println(namedQueries.size());
        System.out.println("waiting modify...");
        sleep(10000);
        compiler1.recompile();
        sleep(5000);
        new Thread(() -> {
            try {
                sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            SqlCompiler instance = SqlCompiler.getInstance();
            Map<String, Map<String, String>> stringMapMap = instance.listNamedQueries();
            System.out.println(stringMapMap.size());
        }).start();
    }

}
