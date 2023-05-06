package com.wang.sqltest.service;

import java.util.Map;

public interface EntityService {
    Object query(String queryName, Map<String, Object> params);
}
