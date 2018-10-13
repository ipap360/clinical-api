package com.team360.hms.admissions.db;

public class DBOperationException extends RuntimeException {

    public DBOperationException() {
        super();
    }

    public DBOperationException(String message) {
        super(message);
    }

}
