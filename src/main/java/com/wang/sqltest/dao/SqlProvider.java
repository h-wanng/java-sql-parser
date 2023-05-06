package com.wang.sqltest.dao;

import com.wang.sqltest.core.SqlCompiler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
public class SqlProvider {

    public String getDynamicSql(SqlCompiler compiler, String queryName, Map<String, Object> param) {
        return compiler.getSql(queryName, param);
    }
}
