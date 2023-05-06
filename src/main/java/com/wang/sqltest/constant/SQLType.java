package com.wang.sqltest.constant;

public enum SQLType {
    QUERY("query"),
    WRITE("write");

    private final String type;

    SQLType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    @Override
    public String toString() {
        return type;
    }
}

