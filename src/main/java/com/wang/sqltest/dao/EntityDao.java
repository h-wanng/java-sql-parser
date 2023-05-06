package com.wang.sqltest.dao;

import com.wang.sqltest.core.SqlCompiler;
import org.apache.ibatis.annotations.*;
import java.util.List;
import java.util.Map;

@Mapper
public interface EntityDao {
    @SelectProvider(type = SqlProvider.class, method = "getDynamicSql")
    List<Map<String, Object>> select(SqlCompiler compiler, String queryName, @Param("param") Map<String, Object> param);
    @SelectProvider(type = SqlProvider.class, method = "getDynamicSql")
    Map<String, Object> selectOne(SqlCompiler compiler, String queryName, @Param("param") Map<String, Object> param);
    @SelectProvider(type = SqlProvider.class, method = "getDynamicSql")
    int update(SqlCompiler compiler, String queryName, @Param("param") Map<String, Object> param);
    @SelectProvider(type = SqlProvider.class, method = "getDynamicSql")
    int delete(SqlCompiler compiler, String queryName, @Param("param") Map<String, Object> param);

}
