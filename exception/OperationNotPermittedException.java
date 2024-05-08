package com.mchis.exception;

public class OperationNotPermittedException extends Exception{
    public OperationNotPermittedException(String msg) {
        super(msg);
    }
}
