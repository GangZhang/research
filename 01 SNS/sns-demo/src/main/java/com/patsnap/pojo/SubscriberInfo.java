package com.patsnap.pojo;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;

/**
 * Created by IntelliJ IDEA.
 * Author: Gang Zhang
 * Date: 2017/12/29
 */
@JsonDeserialize
public class SubscriberInfo {

    private String endpoint;

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }
}
