package com.misfis.x5.runner.model;

import lombok.Data;
import lombok.Getter;
import lombok.ToString;

import java.util.List;
import java.util.Map;

@Getter
@ToString
public class ConfigurationData {
    private int threadsCount;
    private Map<String, ConfigurationProperty> services;

    @Data
    public static class ConfigurationProperty {
        protected int timeout;
        protected List<String> dependsOn;
    }
}
