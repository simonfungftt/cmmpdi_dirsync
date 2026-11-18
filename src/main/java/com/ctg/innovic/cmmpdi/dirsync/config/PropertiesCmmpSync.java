package com.ctg.innovic.cmmpdi.dirsync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "cmmp")
public class PropertiesCmmpSync {
    private List<String> bases = new ArrayList<>();

    public List<String> getBases() {
        return bases;
    }
}