package com.tsb.cb.config;

import java.util.HashMap;
import java.util.Map;

public class CommonProperties {

	/**
     * The Optional configured global registry tags if any that can be used with the exported
     * metrics
     */
    Map<String, String> tags = new HashMap<>();

    /**
     * @return the Optional configured registry global tags if any that can be used with the
     * exported metrics
     */
    public Map<String, String> getTags() {
        return tags;
    }

    /**
     * @param tags the optional configured tags values into registry
     */
    public void setTags(Map<String, String> tags) {
        this.tags = tags;
    }
}
