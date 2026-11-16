package com.ctg.innovic.cmmpdi.dirsync.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@ConfigurationProperties(prefix = "cmmp.bases")
public class PropertiesCmmpSync {

    // You can use String[] or List<String>
    private List<String> basesOU;

    public List<String> getBasesOU() {
        return basesOU;
    }

    public void setBasesOU(List<String> basesOU) {
        this.basesOU = basesOU;
    }
}