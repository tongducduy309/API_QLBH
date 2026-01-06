package com.gener.qlbh.dtos.request;

public class IntrospectReq {
    String token;

    public IntrospectReq(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
