package com.timelyworks.clinical.db;

public class DBOperationException extends RuntimeException {

    public DBOperationException() {
        super();
    }

    public DBOperationException(String message) {
        super(message);
    }

}
