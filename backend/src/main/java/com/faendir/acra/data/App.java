package com.faendir.acra.data;

import org.springframework.data.mongodb.core.mapping.Document;

/**
 * @author Lukas
 * @since 22.03.2017
 */
@Document
public class App {
    private String id;
    private String name;
    private String password;

    public App() {
    }

    public App(String name, String password) {
        this.name = name;
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public String getPassword() {
        return password;
    }

    public String getId() {
        return id;
    }
}
