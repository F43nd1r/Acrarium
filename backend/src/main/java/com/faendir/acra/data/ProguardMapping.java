package com.faendir.acra.data;

import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;

/**
 * @author Lukas
 * @since 19.05.2017
 */
@Document
public class ProguardMapping {
    private String mappings;
    private MetaData id;

    public ProguardMapping(){
    }

    public ProguardMapping(String app, int version, String mappings) {
        this.id = new MetaData(app, version);
        this.mappings = mappings;
    }

    public String getApp() {
        return id.app;
    }

    public int getVersion() {
        return id.version;
    }

    public String getMappings() {
        return mappings;
    }

    static class MetaData implements Serializable {
        private String app;
        private int version;

        MetaData(String app, int version) {
            this.app = app;
            this.version = version;
        }
    }
}
