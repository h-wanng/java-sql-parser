package com.wang.sqltest.exception;

public class SqlCompileException extends RuntimeException {
    private static final long serialVersionUID = 6539463055669022934L;

    public SqlCompileException(Throwable e)
    {
        super(e.getMessage(), e);
    }

    public SqlCompileException(String message) {
        super(message);
    }

}
