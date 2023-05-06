package com.wang.sqltest.constant;

public enum SQLResultType {
    NUMBER_RESULT("number_result"),
    MAP_RESULT("map_result"),
    LIST_RESULT("list_result");

    private final String resultType;
    SQLResultType(String resultType) {
        this.resultType = resultType;
    }

    public String getResultType() {
        return resultType;
    }

    @Override
    public String toString() {
        return resultType;
    }
}
