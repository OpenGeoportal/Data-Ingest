package org.opengeoportal.dataingest.exception;

public class GeoServerException extends Exception {
    
    private String msg;
    
    public GeoServerException(String msg) {
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }
}
