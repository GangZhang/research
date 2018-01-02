package com.patsnap.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by IntelliJ IDEA.
 * Author: Gang Zhang
 * Date: 2017/12/29
 */
@JsonDeserialize
public class LogoutInfo {

    private String session;

    public String getSession() {
        return session;
    }

    public void setSession(String session) {
        this.session = session;
    }
}
