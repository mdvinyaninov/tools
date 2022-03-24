package com.github.mdvinyaninov.tools.services;

public class ServiceException extends RuntimeException {
    public ServiceException(String s) { super(s); }
    public ServiceException(String s, Throwable throwable) { super(s, throwable); }
}
