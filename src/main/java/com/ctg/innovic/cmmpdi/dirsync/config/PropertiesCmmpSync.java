package com.ctg.innovic.cmmpdi.dirsync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cmmp.sync")
public class PropertiesCmmpSync {

    // You can use String[] or List<String>
    private List<String> sourceBases;

    public List<String> getSourceBases() {
        return sourceBases;
    }

    public void setSourceBases(List<String> sourceBases) {
        this.sourceBases = sourceBases;
    }

    // Convenience helper if you strictly need an array:
    public String[] getSourceBasesAsArray() {
        return sourceBases != null ? sourceBases.toArray(new String[0]) : new String[0];
    }
}