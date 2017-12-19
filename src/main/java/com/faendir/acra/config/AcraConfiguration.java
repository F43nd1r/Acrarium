package com.faendir.acra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Lukas
 * @since 15.12.2017
 */
@ConfigurationProperties(prefix = "acra")
public class AcraConfiguration {

    private User user;

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public static class User {
        private String name;
        private String password;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
