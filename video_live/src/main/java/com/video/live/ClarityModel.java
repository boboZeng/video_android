package com.video.live;

import java.io.Serializable;

/**
 * fileDesc
 * <p>
 * Created by ribory on 2019-12-03.
 **/
public class ClarityModel implements Serializable {
    private String name;
    private String url;

    public ClarityModel(String name, String url) {
        this.name = name;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getUrl() {
        return url;
    }
}
