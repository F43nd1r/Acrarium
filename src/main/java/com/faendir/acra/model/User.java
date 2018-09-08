/*
 * (C) Copyright 2018 Lukas Morawietz (https://github.com/F43nd1r)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.faendir.acra.model;

import org.springframework.data.annotation.PersistenceConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.Id;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Lukas
 * @since 20.05.2017
 */
@Entity
public class User implements UserDetails {
    @Id private String username;
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Role> roles;
    @Column(nullable = false)
    @ElementCollection(fetch = FetchType.EAGER)
    private Set<Permission> permissions;
    private String password;

    @PersistenceConstructor
    User() {
    }

    public User(@NonNull String username, @NonNull String password, @NonNull Collection<Role> roles) {
        this.username = username;
        this.password = password;
        this.roles = new HashSet<>(roles);
        this.permissions = new HashSet<>();
    }

    @NonNull
    public Set<Permission> getPermissions() {
        return permissions;
    }

    @NonNull
    public Set<Role> getRoles() {
        return roles;
    }

    @NonNull
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return Stream.concat(permissions.stream(), roles.stream()).collect(Collectors.toList());
    }

    @NonNull
    @Override
    public String getPassword() {
        return password;
    }

    public void setPassword(@NonNull String password) {
        this.password = password;
    }

    @NonNull
    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public enum Role implements GrantedAuthority {
        ADMIN,
        USER,
        REPORTER,
        API;

        @Override
        public String getAuthority() {
            return "ROLE_" + name();
        }
    }
}
