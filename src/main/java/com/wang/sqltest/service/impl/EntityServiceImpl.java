package com.wang.sqltest.service.impl;

import com.wang.sqltest.constant.SQLResultType;
import com.wang.sqltest.core.SqlCompiler;
import com.wang.sqltest.dao.EntityDao;
import com.wang.sqltest.service.EntityService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class EntityServiceImpl implements EntityService {
    @Autowired
    private EntityDao entityDao;

    @Autowired
    private SqlCompiler compiler;

    @Override
    public Object query(String queryName, Map<String, Object> params) {
        Map<String, String> queryMap = compiler.getQuery(queryName);
        // String type = queryMap.get(SqlCompiler.KEY_TYPE);
        String resultType = queryMap.get(SqlCompiler.KEY_RESULT_TYPE);
        String sql = queryMap.get(SqlCompiler.KEY_STATEMENT);
        if (sql.startsWith("select")) {
            if (resultType.equals(SQLResultType.LIST_RESULT.getResultType())) {
                return entityDao.select(compiler, queryName, params);
            } else if (resultType.equals(SQLResultType.MAP_RESULT.getResultType())) {
                return entityDao.selectOne(compiler, queryName, params);
            } else {
                throw new RuntimeException("Unsupported result type: " + resultType);
            }
        } else if (sql.startsWith("update")) {
            return entityDao.update(compiler, queryName, params);
        } else if (sql.startsWith("delete")) {
            return entityDao.delete(compiler, queryName, params);
        } else {
            throw new RuntimeException("Unsupported SQL type: " + sql);
        }
    }
}
