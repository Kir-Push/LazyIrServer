package com.push.lazyir.modules.screenShare.enity;

import java.util.Objects;

public class AuthInfo {

    private String token;

    public AuthInfo(String token) {
        this.token = token;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthInfo authInfo = (AuthInfo) o;
        return Objects.equals(token, authInfo.token);
    }

    @Override
    public int hashCode() {

        return Objects.hash(token);
    }
}
